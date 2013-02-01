package org.rr.jeborker.db.item;

import java.io.Serializable;

import org.rr.jeborker.db.IDBObject;

public class PreferenceItem implements IDBObject, Serializable {

	@Index(type= "DICTIONARY")
	private String name;
	
	@Index(type= "DICTIONARY")
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
