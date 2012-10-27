package org.rr.jeborker.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rr.commons.collection.CompoundList;

/**
 * Class for storing metadata in a key/value kind with value class support.
 */
public class MultiMetadataProperty extends MetadataProperty {

	private static final String noChangeText = Bundle.getString("MultiMetadataProperty.noChangeText");
	
	MultiMetadataProperty(String name, List<Object> values) {
		super(name, new CompoundList<Object>(new ArrayList<Object>(Collections.singletonList((Object) noChangeText)), values));
	}
	
	public Class<?> getPropertyClass() {
		return java.util.List.class;
	}
	
	public String toString() {
		return new StringBuilder(MultiMetadataProperty.class.getSimpleName())
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
		return false;
	}
	
	/**
	 * Tells if the property could inserted more than one in a document.
	 * @return <code>true</code> if it could not be inserted more than once and <code>false</code> otherwise.
	 */
	public boolean isSingle() {
		return true;
	}

}
