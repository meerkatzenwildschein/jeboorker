package org.rr.jeborker.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;

/**
 * Class for storing metadata in a key/value kind with value class support.
 */
public class MetadataProperty implements Cloneable {

	protected final String name;
	
	protected List<Object> values;
	
	private Class<?> propertyClass = null;
	
	private Class<?> propertyEditorClass = null;
	
	private Class<?> propertyRendererClass = null;
	
	private List<String> validValues = null;
	
	protected HashMap<MetadataProperty.HINTS, Object> hints;
	
	static interface ActionType {
		String getName();
	}
	
	public static enum HINTS implements ActionType {
		COVER_FROM_EBOOK_FILE_NAME {

			@Override
			public String getName() {
				return "CoverFromEbookFileName";
			}
		}
	}	
	
	MetadataProperty(String name, List<Object> values) {
		this.name = name;
		this.values = values;
	}
	
	MetadataProperty(String name, Object value) {
		this.name = name;
		this.values = new ArrayList<Object>(1);
		if(value instanceof String) {
			value = StringUtils.trim((String)value); 
		}
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
		if(values.size() > 0) {
			Object object = values.get(0);
			return StringUtils.toString(object);
		}
		return "";
	}
	
	/**
	 * Sets the value to the desired index.
	 * @param idx The index of the value
	 */
	public void setValue(final Object value, final int idx) {
		ListUtils.set(this.values, value, idx >= 0 ? idx : 0);
	}

	/**
	 * Drop all existing values and set this ones from the given List.
	 * @param newValues The new values for this {@link MetadataProperty} instance.
	 */
	public void setValues(final List<Object> newValues) {
		if(this.values != null) {
			this.values.clear();
			this.values.addAll(newValues);
		} else {
			this.values = new ArrayList<Object>(newValues);
		}
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
	
	/**
	 * Get a list of values which are valid for this {@link MetadataProperty}.
	 */
	public List<String> getValidValues() {
		if(validValues == null) {
			return Collections.emptyList();
		}
		return validValues;
	}
	
	/**
	 * Add values to the list of valid values.
	 * @param values The list of valid values.
	 */
	void addValidValues(List<String> values) {
		if(values != null) {
			if(validValues == null) {
				validValues = new ArrayList<String>(values);
			} else {
				validValues.addAll(values);
			}
		}
	}
	
	/**
	 * Creates a new {@link MetadataProperty} instance with the data of this {@link MetadataProperty}.
	 */
	public MetadataProperty clone() {
		MetadataProperty newMetadataProperty = new MetadataProperty(this.name, new ArrayList<Object>(this.values));
		newMetadataProperty.propertyClass = this.propertyClass;
		newMetadataProperty.propertyEditorClass = this.propertyEditorClass;
		newMetadataProperty.propertyRendererClass = this.propertyRendererClass;
		newMetadataProperty.validValues = this.validValues;
		newMetadataProperty.hints = this.hints;
		return newMetadataProperty;
	}

	public Class<?> getPropertyEditorClass() {
		return propertyEditorClass;
	}

	public void setPropertyEditorClass(Class<?> propertyEditorClass) {
		this.propertyEditorClass = propertyEditorClass;
	}

	public Class<?> getPropertyRendererClass() {
		return propertyRendererClass;
	}

	public void setPropertyRendererClass(Class<?> propertyRendererClass) {
		this.propertyRendererClass = propertyRendererClass;
	}
	
	/**
	 * Adds a hint to this {@link MetadataProperty} instance.
	 */
	public void addHint(MetadataProperty.HINTS key, Object value) {
		if(this.hints == null) {
			this.hints = new HashMap<MetadataProperty.HINTS, Object>();
		}
		this.hints.put(key, value);
	}
	
	/**
	 * Gets a previously added hint for this {@link MetadataProperty} instance.
	 * @return The desired hint or <code>null</code> if the desired hint is not available. 
	 */
	public Object getHint(MetadataProperty.HINTS key) {
		if(this.hints != null) {
			return this.hints.get(key);
		}
		return null;
	}
}
