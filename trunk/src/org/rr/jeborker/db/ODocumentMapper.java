package org.rr.jeborker.db;

import java.util.AbstractList;
import java.util.List;

import org.rr.commons.collection.CompoundList;
import org.rr.jeborker.db.item.EbookPropertyItem;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

class ODocumentMapper<T> extends AbstractList<T> {

	private List<?> documents;
	
	private StringBuilder sql;

	private OObjectDatabaseTx db;
	
	private int limit = 100; 
	
	private ORID last = null;
	
	private boolean allRecordsFetched = false;
	
	public ODocumentMapper(List<?> documents, OObjectDatabaseTx db) {
		this.documents = documents;
		this.db = db;
	}
	
	public ODocumentMapper(StringBuilder sql, OObjectDatabaseTx db) {
		this.sql = sql;
		this.db = db;
		
		if(!isQueryLimit()) {
			last = new ORecordId();
			sql.append(" @rid > ? LIMIT " + limit);
		}		
	}	
	
	private List<?> getDocuments() {
		if(this.documents == null) {
			this.documents = getNextDocuments();
		}
		return this.documents;
	}
	
	@SuppressWarnings("unchecked")
	private List<?> getNextDocuments() {
		List<?> result;
		if(last != null) {
			result = db.query(new OSQLSynchQuery<T>(sql.toString()), last);
			
			//store the last orid for the next query round.
			if(!result.isEmpty() && result.get(0) instanceof ORecordInternal) {
				last = ((ORecordInternal<T>)result.get(result.size() - 1)).getIdentity();
			} else if(!result.isEmpty()){
				last = ((ORecordInternal<T>)db.load(result.get(result.size() - 1))).getIdentity();
			} 
		} else {
			result = db.query(new OSQLSynchQuery<T>(sql.toString()));
			allRecordsFetched = true;
		}
		
		return result;
	}
	
	/**
	 * Tells if the query have a limit statement or not.
	 * @return <code>true</code> if there is  a limit statement at the query and <code>false</code> otherwise.
	 */
	private boolean isQueryLimit() {
		return sql.indexOf(" limit ")!=-1;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void fetchToIndex(int index) {
		if(!allRecordsFetched) {
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
		final ORID identity;
		if(object instanceof ORecordInternal) {
			try {
			result = (T) db.getUserObjectByRecord((ORecordInternal<T>) object, null);
			identity = ((ORecordInternal<T>) object).getIdentity();
			} catch (Exception e) {
				return (T) new EbookPropertyItem();
			}
		} else {
			result = (T)object;
			identity = db.getIdentity(object);
		}
		
		//fill binary data to the object instance. 
		DefaultDBManager.getInstance().restoreTransientBinaryData((IDBObject) result, identity);
		
		this.fetchToIndex(index);
		
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
