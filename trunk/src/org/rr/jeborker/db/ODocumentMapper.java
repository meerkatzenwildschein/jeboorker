package org.rr.jeborker.db;

import java.util.AbstractList;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.collection.CompoundList;
import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;

import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

class ODocumentMapper<T> extends AbstractList<T> {

	private List<?> documents;
	
	private StringBuilder sql;

	private OObjectDatabaseTx db;
	
	private int limit = 100; 
	
	private boolean allRecordsFetched = false;

	private OSQLSynchQuery<T> query;
	
	private String sqlSring;
	
	private static final String LIMIT = "LIMIT";
	
	public ODocumentMapper(List<?> documents, OObjectDatabaseTx db) {
		this.documents = documents;
		this.db = db;
		
//		this.query = new OSQLSynchQuery<T>(sql.toString());
	}
	
	public ODocumentMapper(StringBuilder sql, OObjectDatabaseTx db) {
		this.sql = sql;
		this.db = db;
		
//		sql.append(" " + LIMIT + " " + limit);
		this.sqlSring = sql.toString();
		this.query = new OSQLSynchQuery<T>(sqlSring);	
	}	
	
	private synchronized List<?> getDocuments() {
		if(this.documents == null) {
			this.documents = getNextDocuments();
		}
		return this.documents;
	}
	
	private List<?> getNextDocuments() {
		try {
			List<?> result = db.query(query);
			return result;
		} catch(com.orientechnologies.orient.core.sql.OCommandSQLParsingException e) {
			//Annoying orientdb sql-parser bug in orientdb <= 1.2. Remove the WHERE condition if it occurs.
			if(sqlSring.indexOf("WHERE") != -1) {
				String orderByStatement = "";
				int orderByIdx = -1;
				if((orderByIdx = sqlSring.indexOf("order by")) != -1) {
					orderByStatement = sqlSring.substring(orderByIdx);
				}				
				String shortenQuery = sqlSring.substring(0, sqlSring.indexOf("WHERE")) + " " + orderByStatement;
				this.query = new OSQLSynchQuery<T>(shortenQuery);		
				List<?> result = db.query(query);
				LoggerFactory.getLogger().log(Level.WARNING, "Failed restoring query " + sqlSring, e);
				return result;
			} else {
				throw e;
			}
		}
	}
	
	/**
	 * Tells if the query have a limit statement or not.
	 * @return <code>true</code> if there is  a limit statement at the query and <code>false</code> otherwise.
	 */
	private boolean isQueryLimit() {
		return sql != null && sql.indexOf(" " + LIMIT + " ") != -1;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void fetchNextRecords(int index) {
		if(!allRecordsFetched && isQueryLimit()) {
			if(Math.round(this.documents.size()*.8) < index) {
				final List<?> nextDocuments = getNextDocuments();
				if(nextDocuments.size() < limit) {
					allRecordsFetched = true; //no more records available
				}
				this.documents = new CompoundList(this.documents, nextDocuments);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(int index) {
		final Object object = getDocuments().get(index);
		final T result;
//		final ORID identity;
		if(object instanceof ORecordInternal) {
			try {
				result = (T) db.getUserObjectByRecord((ORecordInternal<T>) object, null);
//				identity = ((ORecordInternal<T>) object).getIdentity();
			} catch (Exception e) {
				return (T) DefaultDBManager.getInstance().newInstance(EbookPropertyItem.class);
			}
		} else {
			result = (T) object;
//			identity = db.getIdentity(object);
		}
		
		this.fetchNextRecords(index);
		
		return result;
	}

	@Override
	public boolean isEmpty() {
		return getDocuments().isEmpty();
	}

	@Override
	public int size() {
		return getDocuments().size();
	}
}
