package org.rr.jeborker.db;

import java.util.Iterator;
import java.util.List;

import com.orientechnologies.orient.core.db.object.ODatabaseObjectTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * NICHT PRODUKTIV!!! Das hier ist ein Versuch die Ergebnisliste zu kürzen.
 */
public class OSQLSynchQueryLimitResultWrapper<T> implements Iterator<T>, Iterable<T> {
	
	private OSQLSynchQuery<IDBObject> oSQLSynchQuery;
	
	private ODatabaseObjectTx db;
	
	private Class<IDBObject> type;
	
	private int pointer = 0;
	
	List<IDBObject> resultset;
	
	private int size = -1;
	
	private int limit;
	
	public OSQLSynchQueryLimitResultWrapper(Class<IDBObject> type, String sql, ODatabaseObjectTx db, int limit) {
		this.oSQLSynchQuery = new OSQLSynchQuery<IDBObject>(sql.toString() + " limit " + limit);
		this.type = type;
		this.db = db;
		this.limit = limit;
		
		this.resultset = db.query(this.oSQLSynchQuery);
	}
	
	private void swapNext() {
		if(!resultset.isEmpty()) {
			ORID last = resultset.get(resultset.size() - 1).getIdentity();
			this.resultset = db.query(new OSQLSynchQuery<IDBObject>("select from " + type.getSimpleName() + " limit " + getLimit() + " range " + last.next()));
		}
	}
	
	private int getLimit() {
		return limit;
	}

	public int size() {
		if(this.size < 0) {
			this.size = (int) db.countClass(type.getSimpleName());
		}
		return this.size;
	}

	@Override
	public boolean hasNext() {
		if(!resultset.isEmpty() && pointer % getLimit() == 0) {
			swapNext();
		}
		
		if(resultset.isEmpty()) {
			return false;
		}
		
		return true;
	}

	@Override
	public T next() {
		try {
//	System.out.println(pointer % getLimit());		
			Object object = this.resultset.get(pointer % getLimit());
			if(object instanceof ORecordInternal) {
				return (T) db.getUserObjectByRecord((ORecordInternal) object, null);
			} else {
				return (T) object;
			}
		} finally {
			pointer++;
		}
		
	}

	@Override
	public void remove() {
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}
}
