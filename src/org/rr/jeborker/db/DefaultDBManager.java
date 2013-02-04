package org.rr.jeborker.db;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.JeboorkerUtils;
import org.rr.jeborker.db.item.EbookKeywordItem;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.db.item.Index;
import org.rr.jeborker.db.item.PreferenceItem;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.query.nativ.ONativeSynchQuery;
import com.orientechnologies.orient.core.query.nativ.OQueryContextNative;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.sun.org.apache.xml.internal.security.signature.ObjectContainer;

/**
 * The {@link DefaultDBManager} provides methods for handle database connections and it's content.
 * 
 * @param <T>
 */
public class DefaultDBManager {

	private static DefaultDBManager manager;

	private static OObjectDatabaseTx db;

	/**
	 * Gets a shared {@link ConfigManager} instance.
	 * 
	 * @return The desired {@link ConfigManager} instance.
	 */
	public synchronized static DefaultDBManager getInstance() {
		if (manager == null) {
			manager = new DefaultDBManager();
		}
		
		/**
		 * Just a workaround if there appears a ODatabaseRecordThreadLocal exception. Could happens with
		 * threading. The database instance is not known to all threads.
		 */
		ODatabaseRecordThreadLocal.INSTANCE.set(manager.getDB().getUnderlying());
		
		return manager;
	}
	
	/**
	 * Just a workaround if there appears a ODatabaseRecordThreadLocal exception. Could happens with
	 * threading. The database instance is not known to all threads.
	 */	
	public static void setDefaultDBThreadInstance() {
		ODatabaseRecordThreadLocal.INSTANCE.set(manager.getDB().getUnderlying());
	}
	
	private DefaultDBManager() {
	}

	/**
	 * Opens the database with the given <code>dbName</code> and returns the db {@link ObjectContainer}.
	 * 
	 * @param The
	 *            database file name. The database is stored in the user app config folder.
	 * @return The ready to use container
	 */
	public synchronized OObjectDatabaseTx getDB() {
		if (db != null) {
			ODatabaseRecordThreadLocal.INSTANCE.set((ODatabaseRecord) db.getUnderlying().getUnderlying());
			return db;
		}

		final String dbName = "jeborkerDB";
		final String configPath = JeboorkerUtils.getConfigDirectory();
		final String dbFile = configPath + dbName;
		final IResourceHandler dbResourceHandler = ResourceHandlerFactory.getResourceLoader(dbFile);

		// OPEN / CREATE THE DATABASE
		if (!dbResourceHandler.exists()) {
			db = new OObjectDatabaseTx("local:" + dbFile).create();
			this.registerEntityClasses(new Class[] {EbookPropertyItem.class, EbookKeywordItem.class, PreferenceItem.class}, db);
			
			this.createIndices(db, EbookPropertyItem.class);
			this.createIndices(db, EbookKeywordItem.class);
			this.createIndices(db, PreferenceItem.class);
			
			db.close();
		}
		
		db = OObjectDatabasePool.global().acquire("local:" + dbFile, "admin", "admin");
		this.registerEntityClasses(new Class<?>[] {EbookPropertyItem.class, EbookKeywordItem.class, PreferenceItem.class}, db);

		ODatabaseRecordThreadLocal.INSTANCE.set((ODatabaseRecord) db.getUnderlying().getUnderlying());

		return db;
	}
	
	/**
	 * Register the given classes at the given database instance.
	 * @param classes The classes to be registered.
	 * @param db The database where the given classes should be registered to.
	 */
	private void registerEntityClasses(Class<?>[] classes, OObjectDatabaseTx db) {
		for (Class<?> c : classes) {
			db.getEntityManager().registerEntityClass(c);
		}
	}

	public <T> void deleteIndices(final OObjectDatabaseTx db, final Class<?> itemClass) {
		List<ODocument> indicies = db.query(new OSQLSynchQuery<EbookPropertyItem>("select flatten(indexes) from #0:1"));
		for (ODocument index : indicies) {
			if (!"DICTIONARY".equals(index.field("type"))) {
				// delete all indices except of the DICTIONARY one.
				String name = index.field("name");
				String field = ((ODocument) index.field("indexDefinition")).field("field");
				try {
					db.command(new OCommandSQL("DROP INDEX " + name)).execute();
				} catch (Exception e) {
					LoggerFactory.log(Level.SEVERE, this, "could not delete index " + name, e);
				}

				try {
					db.command(new OCommandSQL("DROP PROPERTY " + itemClass.getSimpleName() + "." + field)).execute();
				} catch (Exception e) {
					LoggerFactory.log(Level.SEVERE, this, "could not delete property " + itemClass.getSimpleName() + "." + field, e);
				}
			}
		}
	}

	public <T> void createIndices(final OObjectDatabaseTx db, final Class<?> itemClass) {
		List<Field> dbViewFields = EbookPropertyItemUtils.getFieldsByAnnotation(Index.class, itemClass);
		for (Field field : dbViewFields) {
			final String fieldName = field.getName();
			try {
				final String indexType = ((Index) field.getAnnotation(Index.class)).type();
				final StringBuilder sql = new StringBuilder();

				// CREATE PROPERTY User.id STRING
				try {
					sql.append("CREATE PROPERTY " + itemClass.getSimpleName() + "." + fieldName + " STRING");
					db.command(new OCommandSQL(sql.toString())).execute();
				} catch (Exception e) {
					LoggerFactory.log(Level.SEVERE, this, "could not create index property " + itemClass.getSimpleName() + "." + fieldName, e);
				} finally {
					sql.setLength(0);
				}

				// CREATE INDEX <name> [ON <class-name> (prop-names)] <type> [<key-type>]
				String indexName = getIndexName(itemClass, fieldName);

				sql.append("CREATE INDEX ").append(indexName).append(" ON ").append(itemClass.getSimpleName())
						.append("(").append(fieldName).append(") ").append(indexType);
				db.command(new OCommandSQL(sql.toString())).execute();
			} catch (Exception e) {
				LoggerFactory.log(Level.SEVERE, this, "could not clear EbookPropertyItem field " + fieldName, e);
			}
		}
	}
	
	/**
	 * Creates a of an index for the given parameters.
	 */
	private String getIndexName(final Class<?> itemClass, String fieldName) {
		String type = getIndexType(itemClass, fieldName);
		return "indexFor" + itemClass.getSimpleName() + "_" + fieldName + "_" + type;
	}
	
	/**
	 * Get the index type for the given field.
	 * @param itemClass The Class instance where the index type should be fetched from.
	 * @param fieldName The name if the field
	 * @return The index type name.
	 */
	private String getIndexType(final Class<?> itemClass, String fieldName) {
		Field field = ReflectionUtils.getField(itemClass, fieldName);
		final String indexType = ((Index) field.getAnnotation(Index.class)).type();
		return indexType;
	}

	/**
	 * Closes and shutdown all database connections previously opened.
	 */
	public synchronized void shutdown() {
		getDB().close();
	}

	public IDBObject storeObject(final IDBObject item) {
		try {
			return getDB().save(item);
		} catch (Exception e1) {
			try {
			// If storage fails try to delete and store the record.
			getDB().delete(item);
			} catch (Exception e2) {
				getDB().delete(reload(item));
			}
			
			return getDB().save(item);
		}
	}

	public long count(Class<?> cls) {
		return getDB().countClass(cls);
	}

	/**
	 * Gets simply all items from the database which matches to the given class type.
	 * 
	 * @param cls
	 *            The class type of the pojos to be fetched
	 * @return A Iterable which provides the data. Never returns <code>null</code>.
	 */
	public <T> Iterable<T> getItems(Class<T> cls) {
		try {
			return getDB().browseClass(cls);
		} catch (NullPointerException e) {
			return Collections.emptyList();
		}
	}

	/**
	 * Gets simply all items from the database which matches to the given class type.
	 * 
	 * @param class1
	 *            The class type of the pojos to be fetched
	 * @return A Iterable which provides the data. Never returns <code>null</code>.
	 */
	public <T> Iterable<T> getItems(final Class<T> class1, final QueryCondition queryConditions, final List<Field> orderFields,
			final OrderDirection orderDirection) {
		final StringBuilder sql = new StringBuilder().append("select * from ").append(class1.getSimpleName());

		appendQueryCondition(sql, queryConditions, null, 0);
		appendOrderBy(sql, orderFields, orderDirection);
		try {
			// long time = System.currentTimeMillis();
			// List<T> listResult = getDB().query(new OSQLSynchQuery<T>(sql.toString()));
			// System.out.println(System.currentTimeMillis() - time);
			// return new ODocumentMapper<T>(listResult, db);
			return new ODocumentMapper<T>(sql, getDB());
		} catch (NullPointerException e) {
			return Collections.emptyList();
		} catch (OException e) {
			if (e.getMessage().indexOf("Error on parsing query") == -1 && e.getMessage().indexOf("Query: EbookPropertyItem") == -1) {
				LoggerFactory.logWarning(this, "Reading database entries has failed", e);
			}
			return Collections.emptyList();
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "Reading database entries has failed", e);
			return Collections.emptyList();
		}
	}

	/**
	 * Appends the order by statement to the given sql {@link StringBuilder}.
	 * 
	 * @param sql
	 *            The already created sql statement where the order should be attached to.
	 * @param orderByFields
	 *            The order by fields.
	 * @param orderDirection
	 *            The order direction.
	 */
	private static void appendOrderBy(StringBuilder sql, List<Field> orderByFields, OrderDirection orderDirection) {
		Iterator<Field> orderByFieldIter = orderByFields.iterator();
		for (int i = 0; orderByFieldIter.hasNext(); i++) {
			Field nextField = orderByFieldIter.next();
			if (i == 0) {
				sql.append(" order by ");
			} else {
				sql.append(",");
			}

			sql.append(" ").append(nextField.getName()).append(" ").append(orderDirection != null ? orderDirection.toString() : "");
		}
	}

	/**
	 * Adds the query condition statement for the given {@link QueryCondition}.
	 * 
	 * @param sql
	 *            The sql where the condition should be appended.
	 * @param condition
	 *            The condition
	 * @param connect
	 *            "and" or "or" for the creation process. Initially can be <code>null</code>
	 * @param deepness
	 *            The recursion deepness is needed for detecting the root process.
	 * @return <code>true</code> if the append process has been successful and <code>false</code> if nothing has been appended.
	 * @see http://code.google.com/p/orient/wiki/SQLQuery
	 */
	private static boolean appendQueryCondition(StringBuilder sql, QueryCondition condition, String connect, int deepness) {
		StringBuilder localSql = new StringBuilder();
		boolean result = false;
		if (condition != null) {
			if (deepness == 0) {
				localSql.append(" WHERE ");
			}

			if (connect != null && !connect.isEmpty() && !StringUtils.endsWith(sql, '(') && !StringUtils.endsWith(sql, "WHERE ")) {
				localSql.append(" ").append(connect).append(" ");
			}
			if (condition.getFieldName() != null && StringUtils.isNotEmpty(condition.getValue())) {
				if (StringUtils.endsWith(localSql, ' ')) {
					// avoid duplicate whitespaces.
					localSql.append(StringUtils.ltrim(condition.toString()));	
				} else {
					localSql.append(condition.toString());	
				}
				
				result = true;
			}

			if (condition.getOrChildren() != null) {
				List<QueryCondition> orChildren = condition.getOrChildren();
				if (deepness > 0 && orChildren.size() > 1) {
					localSql.append("( ");
				}
				for (int i = 0; i < orChildren.size(); i++) {
					boolean orResult = appendQueryCondition(localSql, orChildren.get(i), i == 0 ? "" : "OR", deepness + 1);
					if (orResult) {
						result = true;
					}
				}
				if (deepness > 0 && orChildren.size() > 1) {
					localSql.append(" )");
				}
			}

			if (condition.getAndChildren() != null) {
				List<QueryCondition> andChildren = condition.getAndChildren();
				if (deepness > 0 && andChildren.size() > 1) {
					localSql.append("( ");
				}
				for (int i = 0; i < andChildren.size(); i++) {
					boolean andResult = appendQueryCondition(localSql, andChildren.get(i), i == 0 ? "" : "AND", deepness + 1);
					if (andResult) {
						result = true;
					}
				}
				if (deepness > 0 && andChildren.size() > 1) {
					localSql.append(" )");
				}
			}
		}

		if (result) {
			if (StringUtils.endsWith(sql, ' ')) {
				// avoid duplicate whitespaces.
				sql.append(StringUtils.ltrim(localSql.toString()));
			} else {
				sql.append(localSql);
			}
		}

		return result;
	}

	/**
	 * Updates the given item.
	 * 
	 * @param item
	 *            {@link IDBObject} instance to be updated.
	 */
	public IDBObject updateObject(final IDBObject item) {
		// store the bytes before deleting
		final HashMap<Field, byte[]> data = new HashMap<Field, byte[]>();

		// restore binary data to IDBObject
		for (Map.Entry<Field, byte[]> entry : data.entrySet()) {
			Field field = entry.getKey();
			byte[] value = entry.getValue();
			try {
				field.set(item, value);
			} catch (Exception e) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "could not restore binary data for " + item, e);
			}
		}

		// store the item and
		return this.storeObject(item);
	}

	/**
	 * Simple getObject method which searches for an entry with the given class type allowing to specify one field with a value as condition.
	 * 
	 * @param <T>
	 *            The class type to be searched
	 * @param class1
	 *            The entry class type.
	 * @param field
	 *            The name of the field for the condition
	 * @param value
	 *            The condition value.
	 * @return A list with all results.
	 */
	public <T> List<T> getObject(Class<T> class1, final String field, final String value) {
		//first try to get the desired object using the index.
		final String indexName = getIndexName(class1, field);
		final OIndex index = getDB().getMetadata().getIndexManager().getIndex(indexName);
		if(index != null && index.getType().equals("DICTIONARY")) {
			Object idxDoc = index.get(value);
			if(idxDoc instanceof ODocument) {
				ORID identity = ((ODocument)idxDoc).getIdentity();
				return Collections.singletonList((T) getDB().load(identity));
			} else if(idxDoc instanceof ORecordId) {
				ORID identity = ((ORecordId)idxDoc).getIdentity();
				return Collections.singletonList((T) getDB().load(identity));
			} else if(idxDoc == null) {
				//index exists but has no entry
//				return Collections.emptyList();
			}
		}

		//search with native query
		List<?> result = (List<?>) new ONativeSynchQuery<OQueryContextNative>(getDB().getUnderlying(), class1.getSimpleName(), new OQueryContextNative()) {

			@Override
			public boolean filter(OQueryContextNative iRecord) {
				return iRecord.field(field).eq(value).go();
			}

			public void end() {
				//since orientdb 1.3
			}

		}.execute((Object[]) null);
		return new ODocumentMapper<T>(result, db);
	}

	/**
	 * Deletes the given item and it's binary entries from the database.
	 * 
	 * @param item
	 *            The item to be deleted.
	 * @return
	 */
	public boolean deleteObject(IDBObject item) {
		try {
			getDB().delete(item);
			if(item instanceof EbookPropertyItem) {
				EbookPropertyItemUtils.deleteCoverThumbnail(((EbookPropertyItem)item).getResourceHandler());
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Gets an instance for the desired {@link IDBObject} class.
	 * 
	 * @param item
	 *            The class for the new {@link IDBObject}.
	 * @return The desired object instance.
	 */
	public <T> T newInstance(Class<T> item) {
		return (T) getDB().newInstance(item);
	}
	
	/**
	 * Rereads the given item from the database.
	 * @return The new item or <code>null</code> if the item is no longer present in the database. 
	 */
	public IDBObject reload(IDBObject item) {
		if(item != null) {
			ORID identity = getDB().getIdentity(item);
			return (IDBObject) getDB().load(identity);
		}
		return null;
	}
}
