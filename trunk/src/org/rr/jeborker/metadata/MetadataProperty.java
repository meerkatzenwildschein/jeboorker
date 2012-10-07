package org.rr.jeborker.metadata;

import java.util.ArrayList;
import java.util.List;

import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;

/**
 * Class for storing metadata in a key/value kind with value class support.
 */
public class MetadataProperty {

	protected final String name;
	
	protected ArrayList<Object> values;
	
	private Class<?> propertyClass = null;
	
	MetadataProperty(String name, Object value) {
		this.name = name;
		this.values = new ArrayList<Object>(1);
		this.values.add(value);
	}
	
	MetadataProperty(String name, Object value, Class<?> propertyClass) {
		this.name = name != null ? name.intern() : null;
		this.values = new ArrayList<Object>(1);
		this.values.add(value);
		this.propertyClass = propertyClass;
	}	

	/**
	 * Gets the name of the property. The name is guaranteed to be interned.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * If a {@link MetadataProperty} implementation supports multiple
	 * values per property, a list of values is returned here. These
	 * values are all from the same type or empty/null.
	 * @return All values for this {@link MetadataProperty}
	 */
	public List<Object> getValues() {
		return values;
	}
	
	/**
	 * Convenience method to get the {@link #getValues()} as String. 
	 * @return The desired value as String.
	 */
	public String getValueAsString() {
		final List<Object> values = getValues();
		if(values.size()>0) {
			return StringUtils.toString(values.get(0));
		}
		return "";
	}
	
	/**
	 * Sets the value to the desired index.
	 * @param idx The index of the value
	 */
	public void setValue(final Object value, final int idx) {
		ListUtils.set(this.values, value, idx);
	}
	
	public Class<?> getPropertyClass() {
		if(propertyClass!=null) {
			return propertyClass;
		}
		return String.class;
	}

	public void setPropertyClass(Class<?> propertyClass) {
		this.propertyClass = propertyClass;
	}
	
	public String toString() {
		return new StringBuilder(MetadataProperty.class.getSimpleName())
			.append("[")
			.append("name=")
			.append(name)
			.append(" value=")
			.append(getValueAsString())
			.append("]")
			.toString();
	}

	/**
	 * Tells if the property should be editable or no.
	 * @return <code>true</code> if the property is editable or <code>false</code> if not.
	 */
	public boolean isEditable() {
		return true;
	}
	
	/**
	 * Tells if the property is deletable or not.
	 * @return <code>true</code> if the property is deletable or <code>false</code> if not.
	 */
	public boolean isDeletable() {
		return true;
	}
	
	/**
	 * Tells if the property could inserted more than one in a document.
	 * @return <code>true</code> if it could not be inserted more than once and <code>false</code> otherwise.
	 */
	public boolean isSingle() {
		return true;
	}

}
