package org.rr.jeborker.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataProperty;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;

public class EbookSheetPropertyModel extends PropertySheetTableModel {

	private static final long serialVersionUID = -4633492433120559387L;
	
	private static final Comparator<Property> PROPERTY_COMPARATOR = new MetadataPropertyComparator();

	private boolean changed = false;

	public boolean isChanged() {
		List<Property> properties = this.getProperties();
		for (Property property : properties) {
			if(property instanceof EbookSheetProperty && ((EbookSheetProperty)property).isChanged()) {
				return true;
			}
		}
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public void addProperty(int index, Property property) {
		super.addProperty(index, property);
		changed = true;
	}

	@Override
	public void addProperty(Property property) {
		super.addProperty(property);
		changed = true;
	}

	@Override
	public void removeProperty(Property property) {
		property.setValue("");
		super.removeProperty(property);
		changed = true;
	}

	@Override
	public void setProperties(Property[] newProperties) {
		super.setProperties(newProperties);
		changed = false;
	}
	
	/**
	 * The rating property.
	 * @return The desired rating property or <code>null</code> if no rating property is exists.
	 */
	public Property getRatingProperty() {
		List<Property> properties = getProperties();
		for (Property property : properties) {
			if(property.getName().toLowerCase().indexOf("rating") != -1) {
				return property;
			}
		}
		return null;
	}
	
	public void reloadProperties(IResourceHandler resourceHandler, EbookPropertyItem item, IMetadataReader reader) {
		Property[] newProperties = createProperties(resourceHandler, item, reader);
		setProperties(newProperties);
	}
	
	/**
	 * Creates all Property elements supported by the ebook format given
	 * with the {@link EbookPropertyItem} parameter.
	 * @param item The item where the {@link Property} array should be created for.
	 * @return A couple of properties for the given {@link EbookPropertyItem} never returns <code>null</code>.
	 */
	private static Property[] createProperties(final IResourceHandler resourceHandler, final EbookPropertyItem item, final IMetadataReader reader) {
		if(resourceHandler==null) {
			return new Property[0];
		}
		final ArrayList<Property> result = new ArrayList<Property>(20);
		
		final DefaultProperty fileNameProperty = new DefaultProperty();
		fileNameProperty.setDisplayName(Bundle.getString("EbookPropertySheetProperty.property.name.file"));
		fileNameProperty.setName("file");
		fileNameProperty.setEditable(false);
		fileNameProperty.setDeletable(false);
		fileNameProperty.setValue(resourceHandler);
		fileNameProperty.setShortDescription(resourceHandler!=null ? resourceHandler.toString() : "");
		result.add(fileNameProperty);
		
		setupMetadata(result, resourceHandler, item, reader);
		
		Collections.sort(result, PROPERTY_COMPARATOR);
		
		return result.toArray(new Property[result.size()]);
	}	
	
	
	/**
	 * attaches a property for each metadata item of the resource given with the
	 * resourceLoader parameter.
	 * @param result The list where the metadata properties should be attched to.
	 * @param resourceLoader The {@link IResourceHandler} providing the ebook data.
	 */
	private static void setupMetadata(final ArrayList<Property> result, final IResourceHandler resourceLoader, final EbookPropertyItem item, final IMetadataReader reader) {
		final List<MetadataProperty> allMetaData = reader.readMetaData();
		for (int i = 0; i < allMetaData.size(); i++) {
			final MetadataProperty metadataProperty = allMetaData.get(i);
			final List<Object> values = metadataProperty.getValues();
			if(values.size() == 1) {
				//find properties with the same name and value to merge them in the sheet view.
				List<MetadataProperty> mergedProperties = getMergedProperties(metadataProperty, allMetaData);
				if(mergedProperties.size() > 1) {
					EbookSheetProperty property = new MultipleEbookSheetProperty(mergedProperties, item);
					result.add(property);					
					allMetaData.removeAll(mergedProperties);
					i--;
				} else {
					Property property = createProperty(metadataProperty, item, 0);
					result.add(property);
				}
			} else {
				for (int j=0; j < values.size(); j++) {
					Property property = createProperty(metadataProperty, item, j);
					result.add(property);				
				}
			}
		}
	}
	
	
	/**
	 * searches for all metadata properties matching to the given one and returns them. 
	 * @param ref The ref to be searched. This one is in the result list in any case.
	 * @param allMetaData The metadata list to be searched.
	 * @return The list with all metadata instances matching with the given one. Never returns <code>null</code>.
	 */
	private static List<MetadataProperty> getMergedProperties(MetadataProperty ref, List<MetadataProperty> allMetaData) {
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>(3);
		for (MetadataProperty metadataProperty : allMetaData) {
			if(comparePropertiesForMerge(metadataProperty, ref)) {
				result.add(metadataProperty);
			}
		}
		return result;
	}
	

	/**
	 * Compares the metadata properties if they could be merged to one property. Properties
	 * can only be merged if they have the same value and size.
	 * 
	 * @param metadataProperty1 The first property to be compared.
	 * @param metadataProperty2 The second property to be compared.
	 * @return <code>true</code> for merge and <code>false</code> otherwise.
	 */
	private static boolean comparePropertiesForMerge(final MetadataProperty metadataProperty1, MetadataProperty metadataProperty2) {
		if(metadataProperty1 == metadataProperty2) {
			return true;
		}
		
		// test for value
		boolean result = metadataProperty2.getValues().size() == 1 && metadataProperty1.getValues().size() == 1
			&& CommonUtils.compareTo(metadataProperty2.getValues().get(0), metadataProperty1.getValues().get(0)) == 0;
		
		final String metadataProperty1Name = metadataProperty1.getName().toLowerCase();
		final String metadataProperty2Name = metadataProperty2.getName().toLowerCase();		

		if (result && metadataProperty1Name.equals(metadataProperty2Name)) {
			// name is the same
			return true;
		} else if(StringUtils.compareTwice(metadataProperty1Name, metadataProperty2Name, "createdate", "creationdate")) {
			//merge createdate and creationdate because they have the same sense
			return true;
		}  else if(StringUtils.compareTwice(metadataProperty1Name, metadataProperty2Name, "modifydate", "moddate")) {
			//merge createdate and creationdate because they have the same sense
			return true;
		} else if(StringUtils.compareTwice(metadataProperty1Name, metadataProperty2Name, "calibrerating", "rating")) {
			return true;
		} else {
			return false;
		}
	}	
	
	public static Property createProperty(final MetadataProperty metadataProperty, final EbookPropertyItem item, int valueIndex) {
		return new EbookSheetProperty(metadataProperty, item, valueIndex);
	}	
	
	private static class MetadataPropertyComparator implements Comparator<Property> {

		@Override
		public int compare(Property p1, Property p2) {
			String name1 = getOrderValue(p1);
			String name2 = getOrderValue(p2);
			return name1.compareTo(name2);
		}
		
		private String getOrderValue(Property p1) {
			String name1 = p1.getName().toLowerCase();
			if(name1.equals("file")) {
				return "01";
			} else if(name1.equals("author") || name1.equals("creator / aut")) {
				return "02";
			}else if(name1.equals("title")) {
				return "03";
			}
			return name1;
		}
	}	
	
	/**
	 * This {@link EbookSheetProperty} derivat can handle multiple {@link MetadataProperty} instances.
	 * The first instance in the given {@link MetadataProperty} array is this one, where the name
	 * and the description are taken from. The value will be set to each of the given {@link MetadataProperty}
	 * instances. This allows to merge different properties to be shown as one.
	 */
	private static class MultipleEbookSheetProperty extends EbookSheetProperty {

		private static final long serialVersionUID = 3047729348480097722L;
		
		final List<MetadataProperty> metadataProperties;
		                                                                     
		public MultipleEbookSheetProperty(final List<MetadataProperty> metadataProperties, final EbookPropertyItem item) {
			super(metadataProperties.get(0), item, 0);
			this.metadataProperties = metadataProperties;
			for (MetadataProperty metadataProperty : metadataProperties) {
				if(!metadataProperty.isEditable()) {
					this.setEditable(false);
				}
			}
		}

		@Override
		public void setValue(Object value) {
			if(value!=null && !value.equals(getValue())) {
				for (MetadataProperty metadataProperty : metadataProperties) {
					metadataProperty.setValue(value, 0);
				}
				
				this.setChanged(true);	
			}
			super.setValue(value);
		}
		
		/**
		 * Gets the encapsulated {@link MetadataProperty} which holds the current value state.
		 * @return The encapsulated {@link MetadataProperty} or <code>null</code> if no one existing.
		 */
		public List<MetadataProperty> getMetadataProperties() {
			return metadataProperties;
		}		
	}	
}
