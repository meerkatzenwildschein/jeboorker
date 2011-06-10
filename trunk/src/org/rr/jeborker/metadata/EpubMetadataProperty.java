package org.rr.jeborker.metadata;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EpubMetadataProperty extends MetadataProperty {
	
	private Hashtable<String, String> attributes;
	
	private Class<?> propertyClass = null;
	
	/**
	 * The name is guaranteed to be interned
	 */
	private String name = null;
	
	EpubMetadataProperty(String tageName, String textContent) {
		super(tageName, textContent);
	}
	
	/**
	 * Internal class for holding a key/value pair for attributes. 
	 */
	static class AttributeSet {
		String name;
		
		String value;
		
		AttributeSet(String name, String value) {
			this.name = name;
			this.value = value;
		}

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
	
	/**
	 * Gets the name of the property. The name did not contain the schema namespace and is used as view name.
	 * The name is guaranteed to be interned.
	 */
	@Override
	public String getName() {
		if(this.name == null) {
			if(this.attributes!=null && (super.name.equalsIgnoreCase("meta") || super.name.equalsIgnoreCase("opf:meta"))) {
				final String nameAttribute = attributes.get("name");
				if(nameAttribute!=null) {
					return this.name = nameAttribute.intern();
				}
			}
			
			this.name = super.getName();
			if(this.name.toLowerCase().startsWith("dc:")) {
				this.name = name.substring(3);
				if(getAttributeValueByName("opf:role")!=null) {
					this.name += " / " + StringUtils.capitalize(getAttributeValueByName("opf:role").toLowerCase());
				} 
				
				if(getAttributeValueByName("opf:scheme")!=null) {
					this.name += " / " + StringUtils.capitalize(getAttributeValueByName("opf:scheme").toLowerCase());
				}
				this.name = this.name.intern();
			}
		}
		return this.name;
	}	
	
	@Override
	public List<Object> getValues() {
		final boolean isDate = ReflectionUtils.equals(getPropertyClass(), Date.class);
		final ArrayList<Object> result = new ArrayList<Object>(1);
		if(this.attributes!=null && super.name.equalsIgnoreCase("meta") || super.name.equalsIgnoreCase("opf:meta")) {
			String content = attributes.get("content");
			if(content!=null) {
				if(isDate) {
					result.add(DateConversionUtils.toDate(content));
					return result;
				} else {
					result.add(content);
					return result;
				}
			}
		}
		
		Object value = super.getValues().get(0);
		if(isDate) {
			if(value instanceof Date) {
				result.add(value);
			} else {
				result.add(DateConversionUtils.toDate(StringUtils.toString(value)));	
			}
			return result;
		}
		
		result.add(value);
		return result;
	}
	
	/**
	 * Gets an attribute by it's name. 
	 * @param name The name of the attribute to be searched.
	 * @return The desired attribute.
	 */
	public String getAttributeValueByName(String name) {
		if(attributes!=null) {
			return attributes.get(name);
		}
		return null;
	}
	
	public Class<?> getPropertyClass() {
		if(propertyClass==null) {
			if(super.name.equalsIgnoreCase("dc:date")) {
				propertyClass = Date.class;
			} else if(this.getName().indexOf("timestamp")!=-1) {
				propertyClass = Date.class;
			} else {
				propertyClass = String.class;
			}
		}
		return propertyClass;
	}		
	
	/**
	 * Adds a key/value pair for attributes.
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public void addAttribute(String name, String value) {
		if(this.attributes==null) {
			this.attributes = new Hashtable<String, String>();
		}
		this.attributes.put(name, value);
	}
	
	/**
	 * Creates an {@link Element} for this {@link EpubMetadataProperty} instance.
	 * @param document Needed for creating the right element for the given document.
	 * @return The desired {@link Element}
	 */
	public Element createElement(final Document document) {
		Element resultElement = document.createElement(super.getName());
		if(attributes!=null) {
			for (Entry<String, String> entry : attributes.entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue();
				resultElement.setAttribute(name, value);
				
			}
		}
		final List<Object> values = super.getValues();
		if(values.isEmpty()) {
			return resultElement;
		} else {
			resultElement.setTextContent(String.valueOf(values.get(0)));	
		}
		
		return resultElement;
	}

	@Override
	public void setValue(Object value, int idx) {
		final boolean isDate = getPropertyClass().getSimpleName().equals("Date");
		if(this.attributes!=null && super.name.equalsIgnoreCase("meta") || super.name.equalsIgnoreCase("opf:meta")) {
			if(isDate) {
				String dateValue = DateConversionUtils.toString(DateConversionUtils.toDate(value!=null?String.valueOf(value):null), DateConversionUtils.DATE_FORMATS.W3C_MILLISECOND);
				attributes.put("content", dateValue);
			} else {
				attributes.put("content", value!=null?String.valueOf(value):null);
			}
			return;
		}
		
		if(isDate) {
			String dateValue = DateConversionUtils.toString(DateConversionUtils.toDate(value!=null?String.valueOf(value):null), DateConversionUtils.DATE_FORMATS.W3C_MILLISECOND);
			super.setValue(dateValue, idx);
		} else {
			super.setValue(value, idx);
		}
	}

	@Override
	public String getDetails() {
		if(attributes==null) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		for (Entry<String, String> entry : attributes.entrySet()) {
			final String key = entry.getKey();
			final String value = entry.getValue();
			
			if(key.equals("name") || key.equals("content")) {
				//no need to handle these ones
				continue;
			}
			
			if(result.length() > 0) {
				result.append("\n");
			}
			
			if(key.startsWith("opf:")) {
				result.append(key.substring(4)).append("=").append(value);
			}
			
		}
		return result.toString();
	}


}
