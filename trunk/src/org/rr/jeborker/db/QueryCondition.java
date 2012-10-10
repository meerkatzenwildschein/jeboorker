package org.rr.jeborker.db;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple helper class for building query conditions.
 * @see http://code.google.com/p/orient/wiki/SQLQuery
 */
public class QueryCondition {
	
	private String identifier;

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
	
	public QueryCondition(String field, String value, String operator, String identifier) {
		this.fieldName = field;
		this.value = value;
		this.operator = operator;
		this.identifier = identifier;
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
		String resultValue = value != null ? value.toLowerCase() : value;
		resultValue = DBUtils.escape(resultValue);
		return resultValue;
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
	
	/**
	 * Tests if this {@link QueryCondition} instance contains any conditions.
	 * @return <code>true</code> if no conditions are in here and <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		final boolean hasChilds = (this.andChildren!=null && !this.andChildren.isEmpty()) || (this.orChildren != null && !this.orChildren.isEmpty());
		if(hasChilds) {
			if(this.andChildren!=null) {
				for(QueryCondition condition : this.andChildren) {
					if(!condition.isEmpty()) {
						return false;
					}
				}
			}
			if(this.orChildren != null) {
				for(QueryCondition condition : this.orChildren) {
					if(!condition.isEmpty()) {
						return false;
					}
				}				
			}
		}
		
		if(this.fieldName != null && !this.fieldName.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the identifier for the QueryCondition instance. This is only an
	 * identifier for the instance. The query is not affected by the identifier.
	 * @return The desired identifier.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Sets the identifier for the QueryCondition instance. This is only an
	 * identifier for the instance. The query is not affected by the identifier.
	 * @param identifier The identifier to set.
	 */
	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * Removes all conditions with the given identifier.
	 * @param identifier The identifier for the conditions to be removed.
	 */
	public void removeConditionByIdentifier(final String identifier) {
		this.remove(this.andChildren, identifier);
		this.remove(this.orChildren, identifier);
	}
	
	/**
	 * Does a recursive remove of the conditions with the given identifier.
	 * @param conditions for example a list like <code>this.andChildren</cdeo> or <code>this.orChildren</code>.
	 * @param identifier The identifier of the {@link QueryCondition} to be removed.
	 */
	private void remove(final List<QueryCondition> conditions, final String identifier) {
		if(conditions == null || identifier == null) {
			return;
		}
		
		final ArrayList<QueryCondition> toRemove = new ArrayList<QueryCondition>();
		for(QueryCondition condition : conditions) {
			if(identifier.equals(condition.getIdentifier())) {
				toRemove.add(condition);
			}
			condition.removeConditionByIdentifier(identifier);
		}
		conditions.removeAll(toRemove);
	}
}
