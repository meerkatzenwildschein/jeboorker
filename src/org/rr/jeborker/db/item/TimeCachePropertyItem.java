package org.rr.jeborker.db.item;

import java.io.Serializable;

import org.rr.jeborker.db.IDBObject;

public class TimeCachePropertyItem implements IDBObject, Serializable {

	@Index(type= "FULLTEXT")
	private String name;
	
	private long time;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}
