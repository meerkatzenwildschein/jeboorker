package org.rr.jeborker.metadata;

import static org.rr.commons.utils.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.rr.commons.collection.TransformValueList;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class PDFMetadataProperty extends MetadataProperty {
	
	private Hashtable<String, String> attributes;
	
	private Class<?> propertyClass = null;
	
	/**
	 * The list Element if the value is not located at the textContent of the tag.
	 * For example rdf:Seq or rdf:Alt
	 */
	private String listElementName;
	
	/**
	 * If there is no simple value but a list of them, it is represented by a list
	 * of {@link PDFMetadataProperty}.
	 */
	private List<PDFMetadataProperty> childs;
	
	private Object value;
	
	/**
	 * The namespace like xap pdf or dc 
	 */
	private String namespace = EMPTY;
	
	PDFMetadataProperty(final String tagName, final Object value, final String listElementName) {
		super(tagName, value);
		this.listElementName = listElementName;
		if(value instanceof String) {
			this.value = StringUtils.trim((String)value); 
		} else {
			this.value = value;			
		}
		
		final int namespaceSeparatorIndex = tagName.indexOf(':');
		if(namespaceSeparatorIndex!=-1) {
			this.namespace = tagName.substring(0, namespaceSeparatorIndex);
		}
	}
	
	/**
	 * Gets the name of the property. The name did not contain the namespace.
	 */
	@Override
	public String getName() {
		final String name = super.getName();
		try {
			return name.substring(namespace.length()+1);
		} catch (Exception e) {
			return name;
		}
	}	

	@Override
	public List<Object> getValues() {
		boolean isDate = ReflectionUtils.equals(getPropertyClass(), Date.class);
		if (childs != null && childs.size() > 0) {
			final ArrayList<Object> result = new ArrayList<>(childs.size()); 
			for (PDFMetadataProperty child : childs) {
				Object childValue = child.getValues().get(0);
				if(isDate && !(childValue instanceof Date)) {
					Date date = DateConversionUtils.toDate(StringUtils.toString(childValue));
					result.add(date);
				} else {
					result.add(childValue);
				}
			}
			return result;
		} else {
			List<Object> values = super.getValues();
			if(isDate) {
				return new TransformValueList<Object, Object>(values) {

					@Override
					public Object transform(Object source) {
						Date d = DateConversionUtils.toDate(StringUtils.toString(source));
						if(d != null) {
							return d;
						}
						return source;
					}
					
				};
			}
			return values;
		}
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
			if(this.name.endsWith("Date")) {
				propertyClass = Date.class;
			} else if(values.get(0) instanceof Date) {
				propertyClass = Date.class;
			}
		} else {
			return propertyClass;
		}
		return String.class;
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
	 * Adds a child property. This property could be one of several
	 * ones and holds the value of the parent here.
	 * @param property The property to be added.
	 */
	public void addChild(PDFMetadataProperty property) {
		if(this.childs==null) {
			//mostly there is only one entry.
			this.childs = new ArrayList<>(1);
		}
		this.childs.add(property);
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
		if(listElementName==null && super.getValues().size()==1) {
			Object value = super.getValues().get(0);
			if(value instanceof Date) {
				final String dateString = DateConversionUtils.toString((Date)value, DateConversionUtils.DATE_FORMATS.W3C_SECOND);
				resultElement.setTextContent(dateString);		
			} else {
				resultElement.setTextContent(StringUtils.toString(value));				
			}
		} else if(listElementName!=null && childs!=null && childs.size() > 0) {
			final Element listElement = document.createElement(listElementName);
			for (PDFMetadataProperty child : childs) {
				Element childElement = child.createElement(document);
				listElement.appendChild(childElement);
			}
			resultElement.appendChild(listElement);
		}
		
		return resultElement;
	}

	@Override
	public void setValue(Object value, int idx) {
		final boolean isDate = ReflectionUtils.equals(getPropertyClass(), Date.class);
		
		if(isDate) {
			final String dateValue = DateConversionUtils.toString(DateConversionUtils.toDate(value != null ? String.valueOf(value) : null), DateConversionUtils.DATE_FORMATS.W3C_SECOND);
			setValueToList(dateValue, idx);
		} else {
			setValueToList(value, idx);
		}
	}
	
	private void setValueToList(final Object value, final int idx) {
		if(childs==null && idx==0) {
			super.setValue(value, idx);
		} else {
			try {
				PDFMetadataProperty pdfMetadataProperty = childs.get(idx);
				pdfMetadataProperty.setValue(value, idx);
			} catch(ArrayIndexOutOfBoundsException e) {
				LoggerFactory.logWarning(this, "could not set value " + value + " to " + idx, e);
			}
		}
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	/**
	 * Creates a new {@link MultiMetadataProperty} instance with the data of this {@link MultiMetadataProperty}.
	 */	
	@Override
	public MetadataProperty clone() {
		PDFMetadataProperty newMetadataProperty = new PDFMetadataProperty(this.name, this.value, this.listElementName);
		newMetadataProperty.attributes = this.attributes;
		newMetadataProperty.propertyClass = this.propertyClass;
		newMetadataProperty.childs = this.childs;
		newMetadataProperty.namespace = this.namespace;
		newMetadataProperty.hints = this.hints;
		return newMetadataProperty;
	}		

}
