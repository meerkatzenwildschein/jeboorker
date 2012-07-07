package org.rr.jeborker.db;

import java.util.AbstractList;
import java.util.List;

import org.rr.commons.collection.CompoundList;
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
		this.query = new OSQLSynchQuery<T>(sql.toString());	
	}	
	
	private synchronized List<?> getDocuments() {
		if(this.documents == null) {
			this.documents = getNextDocuments();
		}
		return this.documents;
	}
	
	private List<?> getNextDocuments() {
		long time = System.currentTimeMillis();
		List<?> result = db.query(query);
		System.out.println("getNextDocuments: " + (System.currentTimeMillis() - time) + "ms");
		return result;
	}
	
	/**
	 * Tells if the query have a limit statement or not.
	 * @return <code>true</code> if there is  a limit statement at the query and <code>false</code> otherwise.
	 */
	private boolean isQueryLimit() {
		return sql!=null && sql.indexOf(" " + LIMIT + " ")!=-1;
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
//			identity = ((ORecordInternal<T>) object).getIdentity();
			} catch (Exception e) {
				return (T) new EbookPropertyItem();
			}
		} else {
			result = (T)object;
//			identity = db.getIdentity(object);
		}
		
		//fill binary data to the object instance. 
		DefaultDBManager.getInstance().loadAllTransientBinaryData((IDBObject) result);
		
		this.fetchNextRecords(index);
		
		return result;
	}

	@Override
	public boolean isEmpty() {
		return getDocuments().isEmpty();
	}

	@Override
	public int size() {
//		System.out.println(getDocuments().size());
		return getDocuments().size();
	}
}
