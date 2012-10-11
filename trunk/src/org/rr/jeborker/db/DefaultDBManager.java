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
import org.rr.commons.utils.ReflectionFailureException;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.JeboorkerUtils;
import org.rr.jeborker.db.item.EbookBlobItem;
import org.rr.jeborker.db.item.EbookKeywordItem;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.db.item.Index;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.query.nativ.ONativeSynchQuery;
import com.orientechnologies.orient.core.query.nativ.OQueryContextNative;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.record.impl.ORecordBytes;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.sun.org.apache.xml.internal.security.signature.ObjectContainer;

/**
 * The {@link DefaultDBManager} provides methods for handle database connections and it's 
 * content.
 * @param <T>
 */
public class DefaultDBManager {
	
	private static final String BINARY_STORE_BINARY = "binary";

	private static final String BINARY_STORE_FIELD_NAME = "fieldName";

	private static final String BINARY_STORE_ID = "id";

	private static final String BINARY_STORE_BLOB = "EbookBlobItem";

	private static DefaultDBManager manager;
	
	private static OObjectDatabaseTx db;
	
	/**
	 * Gets a shared {@link ConfigManager} instance.
	 * @return The desired {@link ConfigManager} instance.
	 */
	public synchronized static DefaultDBManager getInstance() {
		if(manager==null) {
			manager = new DefaultDBManager();
		}
		
		return manager;
	}
	
	private DefaultDBManager() {
	}
	
	/**
	 * Opens the database with the given <code>dbName</code> and returns the db {@link ObjectContainer}.
	 * @param The database file name. The database is stored in the user app config folder.
	 * @return The ready to use container
	 */
	public synchronized OObjectDatabaseTx getDB() {
		if(db!=null) {
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
			
			db.getEntityManager().registerEntityClass(EbookPropertyItem.class);
			db.getEntityManager().registerEntityClass(EbookKeywordItem.class);
			
			this.createIndices(db, EbookPropertyItem.class);
			this.createIndices(db, EbookKeywordItem.class);
			db.close();
		} else {
			
		}
		
		db = OObjectDatabasePool.global().acquire("local:" + dbFile, "admin", "admin");
		db.getEntityManager().registerEntityClass(EbookPropertyItem.class);
		db.getEntityManager().registerEntityClass(EbookKeywordItem.class);
		db.getEntityManager().registerEntityClass(ORecordBytes.class);
		db.getEntityManager().registerEntityClass(EbookBlobItem.class);
		
		ODatabaseRecordThreadLocal.INSTANCE.set((ODatabaseRecord) db.getUnderlying().getUnderlying());
		
		return db;
	}
	
	public <T> void deleteIndices(final OObjectDatabaseTx db, final Class<?> itemClass) {
		List<ODocument> indicies = db.query(new OSQLSynchQuery<EbookPropertyItem>("select flatten(indexes) from #0:1"));
		for (ODocument index : indicies) {
			if(!"DICTIONARY".equals(index.field("type"))) {
				//delete all indices except of the DICTIONARY one.
				String name = index.field("name");
				String field =((ODocument) index.field("indexDefinition")).field("field");
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
			try {
				final String indexType = ((Index)field.getAnnotation(Index.class)).type();
				final StringBuilder sql = new StringBuilder();

				//CREATE PROPERTY User.id STRING
				try {
					sql.append("CREATE PROPERTY " + itemClass.getSimpleName() + "." + field.getName() + " STRING");
					db.command(new OCommandSQL(sql.toString())).execute();
				} catch (Exception e) {
					LoggerFactory.log(Level.SEVERE, this, "could not create index property " + itemClass.getSimpleName() + "." + field.getName(), e);
				} finally {
					sql.setLength(0);
				}

				//CREATE INDEX <name> [ON <class-name> (prop-names)] <type> [<key-type>]
				sql.append("CREATE INDEX indexFor").append(itemClass.getSimpleName()).append(field.getName())
					.append(" ON ")
					.append(itemClass.getSimpleName())
					.append("(")
					.append(field.getName())
					.append(") ")
					.append(indexType);
				db.command(  new OCommandSQL(sql.toString()) ).execute(); 
			} catch (Exception e) {
				LoggerFactory.log(Level.SEVERE, this, "could not clear EbookPropertyItem field " + field.getName(), e);
			}
		}		
	}	
	
	/**
	 * Closes and shutdown all database connections previously opened.
	 */
	public synchronized void shutdown() {
        getDB().close();
	}

	public void storeObject(final IDBObject item) {
		IDBObject saveLocal;
		try {
			saveLocal = getDB().save(item);
		} catch(Exception e) {
			//If storage fails try to delete and store the record.
			deleteObject(item);
			saveLocal = getDB().save(item);
		}
		final IDBObject save = saveLocal;
		
		new ByteStorageFieldRunner(item) {
			
			@Override
			public void run(final Field field) {
				try {
					final String fieldName = field.getName();
					final byte[] bytes = (byte[]) ReflectionUtils.getFieldValue(item, fieldName, true);
					if(bytes != null) {
						ReflectionUtils.setFieldValue(save, fieldName, bytes);
					}
				} catch (ReflectionFailureException e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "could not transfer image data." ,e);
				}
			}
		}.start();
		
		storeAllTransientBinaryData(save);
	}
	
	/** 
	 * Stores the binary fields of the given {@link IDBObject} separately in a Blob document.
	 */
	private void storeAllTransientBinaryData(final IDBObject item) {
		new ByteStorageFieldRunner(item) {
			
			@Override
			public void run(final Field field) {
				try {
					final String fieldName = field.getName();
					final byte[] bytes = (byte[]) ReflectionUtils.getFieldValue(item, fieldName, true);
					if(bytes != null && bytes.length > 0) {
						ORID itemIdentity = getDB().getIdentity(item);
						if(itemIdentity.getClusterId() != -1) {
							ORecordBytes record = new ORecordBytes(getDB().getUnderlying(), bytes);
	
							ODocument doc = new ODocument(getDB().getUnderlying());
							doc.setClassNameIfExists(BINARY_STORE_BLOB);
							doc.field(BINARY_STORE_ID, itemIdentity.toString());
							doc.field(BINARY_STORE_FIELD_NAME, fieldName);
							doc.field(BINARY_STORE_BINARY, record);
							doc.save();	
						}
					}
				} catch (ReflectionFailureException e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "could not store binary data for " + item ,e);
				}
			}
		}.start();		
	}
	
	/**
	 * Restores the binary fields of the given {@link IDBObject} instance.
	 * @param item {@link IDBObject} where the binary fields should be restored for.
	 * @param identity The identity of the given {@link IDBObject}.
	 */
	void loadAllTransientBinaryData(final IDBObject item) {
		new ByteStorageFieldRunner(item) {
			
			@Override
			public void run(final Field field) {
				try {
					final ORecordBytes bytes = getTransientBinaryData(item, field);
					if(bytes != null) {
						ReflectionUtils.setFieldValue(item, field.getName(), bytes.toStream());
					}
				} catch (ReflectionFailureException e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "could not store binary data for " + item ,e);
				}
			}
		}.start();	
	}
	
	/**
	 * Delete the binary data objects for the given {@link IDBObject} instance from the database.
	 * The binary data will also be removed from  the given {@link IDBObject} instance.
	 * @param item The {@link IDBObject} instance which binary data should be deleted.
	 */
	private void deleteAllTransientBinaryData(final IDBObject item) {
		new ByteStorageFieldRunner(item) {
			
			@Override
			public void run(final Field field) {
				try {
					final ODocument doc = getTransientBinaryDataDocument(item, field);
					
					if(doc != null) {
						getDB().delete(doc);
						field.set(item, null);
					}
				} catch (Exception e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "could not delete binary for " + item ,e);
				}
			}
		}.start();			
	}
	
	/**
	 * Gets the binary data object for the field of the given {@link IDBObject} instance.
	 * @param item The item to get the binary for.
	 * @param field The field belongs to the binary data.
	 * @return The desired binary data object or <code>null</code> if no belonging data object was found.
	 */
	private ORecordBytes getTransientBinaryData(final IDBObject item, final Field field) {
		final ODocument doc = getTransientBinaryDataDocument(item, field);
		if(doc != null) {
			ORecordBytes bytes = doc.field(BINARY_STORE_BINARY);
			return bytes;
		}
		return null;
	}
	
	/**
	 * Gets the binary data document for the field of the given {@link IDBObject} instance.
	 * 
	 * The following document fields are used.
	 * 	doc.field(BINARY_STORE_ID, itemIdentity.toString()); //id of the given {@link IDBObject} instance.
	 *	doc.field(BINARY_STORE_FIELD_NAME, field.getName()); //name of the field belonging to the {@link IDBObject} object.
	 *	doc.field(BINARY_STORE_BINARY, record); //the ORecordBytes containing the bytes to be stored.
	 * 
	 * @param item The item to get the binary for.
	 * @param field The field belongs to the binary data.
	 * @return The desired binary data document or <code>null</code> if no belonging data document was found.
	 */
	private ODocument getTransientBinaryDataDocument(final IDBObject item, final Field field) {
		final ORID identity = getDB().getIdentity(item);
		final List<?> result = (List<?>) new ONativeSynchQuery<OQueryContextNative>(getDB().getUnderlying(), BINARY_STORE_BLOB, new OQueryContextNative()) {

			@Override
			public boolean filter(OQueryContextNative iRecord) {
				boolean f = iRecord.field(BINARY_STORE_ID).eq(identity.toString()).and().field(BINARY_STORE_FIELD_NAME).eq(field.getName()).go();
				return f;
			}
					
		}.execute((Object[]) null);
		
		if(!result.isEmpty()) {
			ODocument doc = (ODocument) result.get(0);
			return doc;
		}
		return null;
	}
	
	public long count(Class<?> cls) {
		return getDB().countClass(cls);
	}
	
	/**
	 * Gets simply all items from the database which matches to the 
	 * given class type.
	 * @param cls The class type of the pojos to be fetched
	 * @return A Iterable which provides the data. Never returns <code>null</code>.
	 */
	public <T> Iterable<T> getItems(Class<T> cls) {
		try {
			return getDB().browseClass(cls);
		} catch(NullPointerException e) {
			return Collections.emptyList();
		}
	}
	
	/**
	 * Gets simply all items from the database which matches to the 
	 * given class type.
	 * @param class1 The class type of the pojos to be fetched
	 * @return A Iterable which provides the data. Never returns <code>null</code>.
	 */
	public <T> Iterable<T> getItems(final Class<T> class1, final QueryCondition queryConditions, final List<Field> orderFields, final OrderDirection orderDirection) {
		final StringBuilder sql = new StringBuilder()
			.append("select * from ")
			.append(class1.getSimpleName());
		
		appendQueryCondition(sql, queryConditions, null, 0);
		appendOrderBy(sql, orderFields, orderDirection);
		try {
//long time = System.currentTimeMillis();
//			List<T> listResult = getDB().query(new OSQLSynchQuery<T>(sql.toString()));
//System.out.println(System.currentTimeMillis() - time);
//			return new ODocumentMapper<T>(listResult, db);
			return new ODocumentMapper<T>(sql, getDB());
		} catch(NullPointerException e) {
			return Collections.emptyList();
		} catch(OException e) {
			if(e.getMessage().indexOf("Error on parsing query")== -1 && e.getMessage().indexOf("Query: EbookPropertyItem")== -1) {
				LoggerFactory.logWarning(this, "Reading database entries has failed", e);
			}
			return Collections.emptyList();
		} catch(Exception e) {
			LoggerFactory.logWarning(this, "Reading database entries has failed", e);
			return Collections.emptyList();
		}
	}	
	
	/**
	 * Appends the order by statement to the given sql {@link StringBuilder}.
	 * @param sql The already created sql statement where the order should be attached to.
	 * @param orderByFields The order by fields.
	 * @param orderDirection The order direction.
	 */
	private static void appendOrderBy(StringBuilder sql, List<Field> orderByFields, OrderDirection orderDirection) {
		Iterator<Field> orderByFieldIter = orderByFields.iterator();
		for (int i = 0; orderByFieldIter.hasNext(); i++) {
			Field nextField = orderByFieldIter.next();
			if(i==0) {
				sql.append(" order by ");
			} else {
				sql.append(",");
			}
			
			sql.append(" ")
				.append(nextField.getName())
				.append(" ")
				.append(orderDirection != null ? orderDirection.toString() : "");
		}
	}
	
	/**
	 * Adds the query condition statement for the given {@link QueryCondition}.
	 * @param sql The sql where the condition should be appended.
	 * @param condition The condition
	 * @param connect "and" or "or" for the creation process. Initially can be <code>null</code>
	 * @param deepness The recursion deepness is needed for detecting the root process.
	 * @return <code>true</code> if the append process has been successful and <code>false</code> if nothing has been appended.
	 * @see http://code.google.com/p/orient/wiki/SQLQuery
	 */
	private static boolean appendQueryCondition(StringBuilder sql, QueryCondition condition, String connect, int deepness) {
		StringBuilder localSql = new StringBuilder();
		boolean result = false;
		if(condition!=null) {
			if(deepness == 0) {
				localSql.append(" WHERE ");
			}
			
			if(connect!=null && connect.length() > 0 && sql.charAt(sql.length()-1)!='(') {
				localSql.append(" ")
				.append(connect).append(" ");
			}
			if(condition.getFieldName() != null && StringUtils.toString(condition.getValue()).length() > 0) {
				localSql.append(condition.toString());
				result = true;
			}
				
			if(condition.getOrChildren() != null) {
				List<QueryCondition> orChildren = condition.getOrChildren();
				if(deepness > 0 && orChildren.size() > 1) {
					localSql.append("( ");
				}				
				for (int i=0; i < orChildren.size(); i++) {
					boolean orResult = appendQueryCondition(localSql, orChildren.get(i), i==0 ? "" : "OR", deepness+1);
					if(orResult) {
						result = true;
					}
				}
				if(deepness > 0 && orChildren.size() > 1) {
					localSql.append(" )");
				}	
			}
			
			if(condition.getAndChildren() != null) {
				List<QueryCondition> andChildren = condition.getAndChildren();
				if(deepness > 0 && andChildren.size() > 1) {
					localSql.append("( ");
				}
				for (int i=0; i < andChildren.size(); i++) { 
					boolean andResult = appendQueryCondition(localSql, andChildren.get(i), i==0 ? "" : "AND", deepness+1);
					if(andResult) {
						result = true;
					}
				}
				if(deepness > 0 && andChildren.size() > 1) {
					localSql.append(" )");
				}
			}
		}
		
		if(result) {
			if(sql.length() > 0 && Character.isWhitespace(sql.charAt(sql.length()-1))) {
				//no duplicate whitespaces.
				sql.append(StringUtils.ltrim(localSql.toString()));
			} else {
				sql.append(localSql);
			}
		}
		
		return result;
	}

	/**
	 * Updates the given item.
	 * @param item {@link IDBObject} instance to be updated.
	 */
	public void updateObject(final IDBObject item) {		
		//store the bytes before deleting
		final HashMap<Field, byte[]> data = new HashMap<Field, byte[]>();
		new ByteStorageFieldRunner(item) {
			
			@Override
			public void run(final Field field) {
				try {
					data.put(field, (byte[]) field.get(item));
				} catch (Exception e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "could not delete binary for " + item, e);
				}
			}
		}.start();		
		
		deleteAllTransientBinaryData(item);
		
		//delete the item and all the attached binary data 
//		this.deleteObject(item);
		
		//restore binary data to IDBObject
		for (Map.Entry<Field, byte[]> entry : data.entrySet()) {
            Field field = entry.getKey();
            byte[] value = entry.getValue();
            try {
				field.set(item, value);
			} catch (Exception e) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "could not restore binary data for " + item, e);
			}
        }
		
		//store the item and 
		this.storeObject(item);
	}

	/**
	 * Simple getObject method which searches for an entry with the given class type
	 * allowing to specify one field with a value as condition. 
	 * @param <T> The class type to be searched 
	 * @param class1 The entry class type. 
	 * @param field The name of the field for the condition
	 * @param value The condition value.
	 * @return A list with all results.
	 */
	public <T> List<T> getObject(Class<T> class1, final String field, final String value) {
		List<?> result = (List<?>) new ONativeSynchQuery<OQueryContextNative>(getDB().getUnderlying(), class1.getSimpleName(), new OQueryContextNative()) {

			@Override
			public boolean filter(OQueryContextNative iRecord) {
				return iRecord.field(field).eq(value).go();
			}
					
		}.execute((Object[]) null);
		return new ODocumentMapper<T>(result, db);
	}

	/**
	 * Deletes the given item and it's binary entries from the database.
	 * @param item The item to be deleted.
	 * @return 
	 */
	public boolean deleteObject(IDBObject item) {
		try {
			this.deleteAllTransientBinaryData(item);
			getDB().delete(item);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
