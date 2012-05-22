package org.rr.jeborker.db;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.JeboorkerUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.object.ODatabaseObjectTx;
import com.orientechnologies.orient.core.query.nativ.ONativeSynchQuery;
import com.orientechnologies.orient.core.query.nativ.OQueryContextNativeSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.sun.org.apache.xml.internal.security.signature.ObjectContainer;

/**
 * The {@link DefaultDBManager} provides methods for handle database connections and it's 
 * content.
 * @param <T>
 */
public class DefaultDBManager {
	
	private static DefaultDBManager manager;
	
	private static ODatabaseObjectTx db;
	
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
	 * Gets all {@link IDBObject} classes handled by this {@link DefaultDBManager} instance.
	 * @return All handled {@link IDBObject} classes.
	 */
	@SuppressWarnings("unchecked")
	protected Class<IDBObject>[] getIDBObjectForSetup() {
		return new Class[] {EbookPropertyItem.class};
	}
	
	/**
	 * Gets the database name for the database handled by this {@link DefaultDBManager} instance.
	 * @return The database file name.
	 */
	protected String getDBName() {
		return "jeborkerDB";
	}
	
	/**
	 * Opens the database with the given <code>dbName</code> and returns the db {@link ObjectContainer}.
	 * @param The database file name. The database is stored in the user app config folder.
	 * @return The ready to use container
	 */
	public synchronized ODatabaseObjectTx getDB() {
		if(db!=null) {
			return db;
		}
		
		final String dbName = getDBName();
		final String configPath = JeboorkerUtils.getConfigDirectory();
		final String dbFile = configPath + dbName;
		final IResourceHandler dbResourceHandler = ResourceHandlerFactory.getResourceLoader(dbFile);
		
		// OPEN / CREATE THE DATABASE
		if(!dbResourceHandler.exists()) {
			db = new ODatabaseObjectTx ("local:" + dbFile).create();
			db.getMetadata().getSchema().createClass(EbookPropertyItem.class);
		} else {
			db = new ODatabaseObjectTx ("local:" + dbFile).open("admin", "admin");
			db.getEntityManager().registerEntityClass(EbookPropertyItem.class);
		}
		
		return db;
	}
	
	/**
	 * Closes and shutdown all database connections previously opened.
	 */
	public synchronized void shutdown() {
        getDB().close();
	}

	public void storeObject(Object item) {
		getDB().save(item);
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
		if(orderFields==null || orderFields.isEmpty()) {
			if(queryConditions == null || queryConditions.isEmpty()) {
				return getItems(class1);
			}
		}

		final StringBuilder sql = new StringBuilder()
			.append("select * from ")
			.append(class1.getSimpleName());

		appendQueryCondition(sql, queryConditions, null, 0);
		appendOrderBy(sql, orderFields, orderDirection);
//System.out.println("query: " + sql.toString());		
		try {
			List<T> listResult = getDB().query(new OSQLSynchQuery<T>(sql.toString()));
			return listResult;
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
	 * @return <code>true</code> if the append process has been successfull and <code>false</code> if nothing has been appended.
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
				localSql.append(condition.getFieldName() + ".toLowerCase()");
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
		getDB().save(item);
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
		List<?> result = getDB().command(
				  new ONativeSynchQuery<ODocument, OQueryContextNativeSchema<ODocument>>(getDB().getUnderlying(), class1.getSimpleName(), new OQueryContextNativeSchema<ODocument>()) {
					private static final long serialVersionUID = 1L;

					@Override
				    public boolean filter(OQueryContextNativeSchema<ODocument> iRecord) {
				      //return iRecord.field("city").field("name").eq("Rome").and().field("name").like("G%").go();
				    	return iRecord.field(field).eq(value).go();
				    };
				  }).execute();
		return new ODocumentMapper<T>(result, db);
	}

	public int deleteObject(Object toRemove) {
		getDB().delete(toRemove);
		return 1;
	}
	

	
}
