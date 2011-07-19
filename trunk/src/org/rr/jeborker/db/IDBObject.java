package org.rr.jeborker.db;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;

/**
 * All pojos should implements this interface.
 */
public interface IDBObject {

	public ORID getIdentity();
	
}
