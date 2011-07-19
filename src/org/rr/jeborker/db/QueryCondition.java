package org.rr.jeborker.db;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple helper class for building query conditions.
 * @see http://code.google.com/p/orient/wiki/SQLQuery
 */
public class QueryCondition {

	private String operator;
	
	private String fieldName;
	
	private String value;
	
	public List<QueryCondition> andChildren = null;
	
	public List<QueryCondition> orChildren = null;
	
	public QueryCondition(String field, String value, String operator) {
		this.fieldName = field;
		this.value = value;
		this.operator = operator;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setField(String field) {
		this.fieldName = field;
	}

	public String getValue() {
		return value != null ? value.toLowerCase() : value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public void addOrChild(QueryCondition child) {
		if(orChildren==null) {
			orChildren = new ArrayList<QueryCondition>();
		}
		orChildren.add(child);
	}
	
	public void addAndChild(QueryCondition child) {
		if(andChildren==null) {
			andChildren = new ArrayList<QueryCondition>();
		}
		andChildren.add(child);
	}

	public List<QueryCondition> getAndChildren() {
		return andChildren;
	}

	public void setAndChildren(List<QueryCondition> andChildren) {
		this.andChildren = andChildren;
	}

	public List<QueryCondition> getOrChildren() {
		return orChildren;
	}

	public void setOrChildren(List<QueryCondition> orChildren) {
		this.orChildren = orChildren;
	}	
	
	public boolean isEmpty() {
		return this.fieldName == null || this.fieldName.length() == 0;
	}
}
