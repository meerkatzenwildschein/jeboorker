package org.rr.jeborker.db;

import java.util.AbstractList;
import java.util.List;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

class ODocumentMapper<T> extends AbstractList<T> {

	private List<?> documents;
	
	private OObjectDatabaseTx db;
	
	public ODocumentMapper(List<?> documents, OObjectDatabaseTx db) {
		this.documents = documents;
		this.db = db;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(int index) {
		final Object object = documents.get(index);
		final T result;
		final ORID identity;
		if(object instanceof ORecordInternal) {
			result = (T) db.getUserObjectByRecord((ORecordInternal<T>) object, null);
			identity = ((ORecordInternal<T>) object).getIdentity();
		} else {
			result = (T)object;
			identity = db.getIdentity(object);
		}
		
		//restore
		DefaultDBManager.getInstance().restoreTransientBinaryData((IDBObject) result, identity);
		
		return result;
	}

	@Override
	public boolean isEmpty() {
		return documents.isEmpty();
	}

	@Override
	public int size() {
		return documents.size();
	}
}
