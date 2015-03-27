package org.rr.jeborker.db.item;

import java.io.Serializable;

import org.rr.jeborker.db.IDBObject;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "PreferenceItem")
public class PreferenceItem implements IDBObject, Serializable {

	@DatabaseField(id = true, index = true)
	private String name;
	
	@DatabaseField(width = Integer.MAX_VALUE)
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

	public String toString() {
		return getClass().getName() + "[" + name + "=" + value + "]";
	}
}
