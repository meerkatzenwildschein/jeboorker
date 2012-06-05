package org.rr.jeborker.db;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.persistence.Transient;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ReflectionFailureException;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.JeboorkerUtils;
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

	private static final String BINARY_STORE_BLOB = "Blob";

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
			db.close();
		} 
		
		db = OObjectDatabasePool.global().acquire("local:" + dbFile, "admin", "admin");
		db.getEntityManager().registerEntityClass(EbookPropertyItem.class);
		db.getEntityManager().registerEntityClass(ODocument.class);
		db.getEntityManager().registerEntityClass(ORecordBytes.class);
		
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
		Object save = getDB().save(item);
		ORID identity = getDB().getIdentity(save);
		storeTransientBinaryData(item, identity);
	}

	/** 
	 * Stores the binary fields of the given {@link IDBObject} separately in a Blob document.
	 */
	private void storeTransientBinaryData(final IDBObject item, final ORID identity) {
		List<Field> fields = ReflectionUtils.getFields(item.getClass(), ReflectionUtils.VISIBILITY_VISIBLE_ALL);
		for (Field field : fields) {
			if(field.getType().getName().equals(byte[].class.getName())) {
				if(field.getAnnotation(Transient.class)!=null) {
					//store bytes separately
					try {
						final byte[] bytes = (byte[]) ReflectionUtils.getFieldValue(item, field.getName());
						if(bytes != null && bytes.length > 0) {
							ORecordBytes record = new ORecordBytes(getDB().getUnderlying(), bytes);

							ODocument doc = new ODocument(getDB().getUnderlying(), BINARY_STORE_BLOB);
							doc.field(BINARY_STORE_ID, identity.toString());
							doc.field(BINARY_STORE_FIELD_NAME, field.getName());
							doc.field(BINARY_STORE_BINARY, record);
							doc.save();							
						}
					} catch (ReflectionFailureException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Restores the binary fields of the given {@link IDBObject} instance.
	 * @param item {@link IDBObject} where the binary fields should be restored for.
	 * @param identity The identity of the given {@link IDBObject}.
	 */
	void restoreTransientBinaryData(final IDBObject item, final ORID identity) {
		List<Field> fields = ReflectionUtils.getFields(item.getClass(), ReflectionUtils.VISIBILITY_VISIBLE_ALL);
		for (final Field field : fields) {
			if(field.getType().getName().equals(byte[].class.getName())) {
				if(field.getAnnotation(Transient.class)!=null) {
					try {
						List<?> result = (List<?>) new ONativeSynchQuery<OQueryContextNative>(getDB().getUnderlying(), BINARY_STORE_BLOB, new OQueryContextNative()) {

							@Override
							public boolean filter(OQueryContextNative iRecord) {
								return iRecord.field(BINARY_STORE_ID).eq(identity.toString()).and().field(BINARY_STORE_FIELD_NAME).eq(field.getName()).go();
							}
									
						}.execute((Object[]) null);
						
						if(!result.isEmpty()) {
							ODocument object = (ODocument) result.get(0);
							ORecordBytes bytes = object.field(BINARY_STORE_BINARY);
							ReflectionUtils.setFieldValue(item, field.getName(), bytes.toStream());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
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
long time = System.currentTimeMillis();
			List<T> listResult = getDB().query(new OSQLSynchQuery<T>(sql.toString()));
System.out.println(System.currentTimeMillis() - time);
			return new ODocumentMapper<T>(listResult, db);
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
				localSql.append(" where");
			}
			
			if(connect!=null && connect.length() > 0 && sql.charAt(sql.length()-1)!='(') {
				localSql.append(" ")
				.append(connect).append(" ");
			}
			if(condition.getFieldName() != null && StringUtils.toString(condition.getValue()).length() > 0) {
				localSql.append(" ");
				localSql.append(condition.getFieldName()+".toLowerCase()");
				localSql.append(" ");
				localSql.append(condition.getOperator());
				localSql.append(" ");
				localSql.append("'"+condition.getValue()+"'");
				result = true;
			}
				
			if(condition.getOrChildren() != null) {
				localSql.append("(");
				List<QueryCondition> orChildren = condition.getOrChildren();
				for (int i=0; i < orChildren.size(); i++) {
					boolean orResult = appendQueryCondition(localSql, orChildren.get(i), i==0 ? "" : "or", deepness+1);
					if(orResult) {
						result = true;
					}
				}
				localSql.append(")");
			}
			
			if(condition.getAndChildren() != null) {
				localSql.append("(");
				List<QueryCondition> andChildren = condition.getAndChildren();
				for (int i=0; i < andChildren.size(); i++) { 
					boolean andResult = appendQueryCondition(localSql, andChildren.get(i), i==0 ? "" : "and", deepness+1);
					if(andResult) {
						result = true;
					}
				}
				localSql.append(")");
			}
		}
		
		if(result) {
			sql.append(localSql);
		}
		
		return result;
	}

	/**
	 * Updates the given item.
	 * @param item
	 */
	public void updateObject(IDBObject item) {
		//getDB().save(item);
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

	public int deleteObject(IDBObject toRemove) {
		getDB().delete(toRemove);
		return 1;
	}
}
