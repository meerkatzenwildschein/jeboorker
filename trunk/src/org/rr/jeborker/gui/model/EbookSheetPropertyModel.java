package org.rr.jeborker.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;

public class EbookSheetPropertyModel extends PropertySheetTableModel {

	private static final long serialVersionUID = -4633492433120559387L;
	
	protected static final Comparator<Property> PROPERTY_COMPARATOR = new MetadataPropertyComparator();

	private boolean changed = false;
	
	protected IMetadataReader reader;

	private IResourceHandler resourceHandler;
	
	protected List<MetadataProperty> allMetaData;

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
		if(!isCoverProperty(property)) { 
			super.addProperty(index, property);
		}
		
		if(property instanceof EbookSheetProperty) {
			MetadataProperty metadataProperty = ((EbookSheetProperty)property).metadataProperty;
			allMetaData.add(metadataProperty);
		}
		changed = true;
	}

	@Override
	public void addProperty(Property property) {
		if(!isCoverProperty(property)) { 
			super.addProperty(property);
		}
		
		if(property instanceof EbookSheetProperty) {
			MetadataProperty metadataProperty = ((EbookSheetProperty)property).metadataProperty;
			allMetaData.add(metadataProperty);
		}		
		changed = true;
	}

	@Override
	public void removeProperty(Property property) {
		property.setValue("");
		super.removeProperty(property);
		if(property instanceof EbookSheetProperty) {
			MetadataProperty metadataProperty = ((EbookSheetProperty)property).metadataProperty;
			allMetaData.remove(metadataProperty);
		}		
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
	
	/**
	 * The rating property.
	 * @return The desired rating property or <code>null</code> if no rating property is exists.
	 */
	public int getRatingIndex() {
		List<Property> properties = getProperties();
		for (int i = 0; i < properties.size(); i++) {
			if(properties.get(i).getName().toLowerCase().indexOf("rating") != -1) {
				return i;
			}
		}
		return -1;
	}	
	
	public void loadProperties(EbookPropertyItem item) {
		this.resourceHandler = ResourceHandlerFactory.getResourceLoader(item.getFile());
		this.reader = MetadataHandlerFactory.getReader(resourceHandler);
		
		Property[] newProperties = createProperties(resourceHandler, item, reader);
		setProperties(newProperties);
	}
	
	public IMetadataReader getMetadataReader() {
		return reader;
	}

	public IResourceHandler getResourceHandler() {
		return resourceHandler;
	}
	
	/**
	 * Get the display name for the given property.
	 * @param property The property where the display name should be gotten for.
	 * @return The desired display name. Never returns <code>null</code>.  
	 */
	public String getDisplayName(Property property) {
		if(property instanceof DefaultProperty) {
			String displayName = ((DefaultProperty)property).getDisplayName();
			if(displayName != null) {
				return displayName;
			}
		}
		return "";
	}
	
	/**
	 * Get the {@link EbookPropertyItem} assigned to the given {@link Property} instance.
	 * @param property The {@link Property} instance where none, one or more {@link EbookPropertyItem} assigned to.
	 * @return The list with all assigned {@link EbookPropertyItem}s. Never returns <code>null</code>.
	 */
	public List<EbookPropertyItem> getEbookPropertyItems(Property property) {
		if(property instanceof EbookSheetProperty) {
			List<EbookPropertyItem> ebookPropertyItems = ((EbookSheetProperty)property).getEbookPropertyItems();
			return ebookPropertyItems;
		}
		return Collections.emptyList();
	}
	
	/**
	 * Get the {@link MetadataProperty} assigned to the given {@link Property} instance.
	 * @param property The {@link Property} instance where none, one or more {@link MetadataProperty} assigned to.
	 * @return The list with all assigned {@link MetadataProperty}. Never returns <code>null</code>.
	 */	
	public List<MetadataProperty> getMetadataProperties(Property property) {
		if(property instanceof EbookSheetProperty) {
			List<MetadataProperty> metadataProperties = ((EbookSheetProperty)property).getMetadataProperties();
			return metadataProperties;
		}
		return Collections.emptyList();		
	}
	
	public List<MetadataProperty> getAllMetaData() {
		return allMetaData;
	}
	
	/**
	 * Get the cover bytes from the metadata.
	 * @return The desired cover bytes or <code>null</code> if no cover bytes are present.
	 */
	public byte[] getCover() {
		IMetadataReader metadataReader = getMetadataReader();
		if(metadataReader != null) {
			List<MetadataProperty> metadataByType = metadataReader.getMetadataByType(false, allMetaData, IMetadataReader.METADATA_TYPES.COVER);
			if(metadataByType != null && !metadataByType.isEmpty()) {
				MetadataProperty metadataProperty = metadataByType.get(0);
				if(metadataProperty.getValues() != null && !metadataProperty.getValues().isEmpty()) {
					Object value = metadataProperty.getValues().get(0);
					if(value instanceof byte[])  {
						return (byte[]) value;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Creates all Property elements supported by the ebook format given
	 * with the {@link EbookPropertyItem} parameter.
	 * @param item The item where the {@link Property} array should be created for.
	 * @return A couple of properties for the given {@link EbookPropertyItem} never returns <code>null</code>.
	 */
	private Property[] createProperties(final IResourceHandler resourceHandler, final EbookPropertyItem item, final IMetadataReader reader) {
		if(resourceHandler == null) {
			return new Property[0];
		}
		
		final List<Property> properties = setupMetadata(Collections.singletonList(item), reader);
		
		final DefaultProperty fileNameProperty = new DefaultProperty();
		fileNameProperty.setDisplayName(Bundle.getString("EbookPropertySheetProperty.property.name.file"));
		fileNameProperty.setName("file");
		fileNameProperty.setEditable(false);
		fileNameProperty.setDeletable(false);
		fileNameProperty.setValue(resourceHandler);
		fileNameProperty.setShortDescription(resourceHandler != null ? resourceHandler.toString() : "");		
		properties.add(fileNameProperty);
		
		Collections.sort(properties, PROPERTY_COMPARATOR);
		
		return properties.toArray(new Property[properties.size()]);
	}	
	
	
	/**
	 * attaches a property for each metadata item of the resource given with the
	 * resourceLoader parameter.
	 * @param result The list where the metadata properties should be attched to.
	 * @param resourceLoader The {@link IResourceHandler} providing the ebook data.
	 */
	protected List<Property> setupMetadata(final List<EbookPropertyItem> items, final IMetadataReader reader) {
		final ArrayList<Property> result = new ArrayList<Property>(items.size() + 1);
		allMetaData = reader.readMetaData();
		for (int i = 0; i < allMetaData.size(); i++) {
			final MetadataProperty metadataProperty = allMetaData.get(i);
			final List<Object> values = metadataProperty.getValues();
			if(values.size() == 1) {
				//find properties with the same name and value to merge them in the sheet view.
				List<MetadataProperty> mergedProperties = getMergedProperties(metadataProperty, allMetaData);
				if(mergedProperties.size() > 1) {
					EbookSheetProperty property = new MultipleEbookSheetProperty(mergedProperties, items);
					result.add(property);					
					allMetaData.removeAll(mergedProperties);
					i--;
				} else {
					Property property = createProperty(metadataProperty, items, 0);
					result.add(property);
				}
			} else {
				for (int j=0; j < values.size(); j++) {
					Property property = createProperty(metadataProperty, items, j);
					result.add(property);				
				}
			}
		}
		
		this.removeCoverProperty(result);
		
		return result;
	}
	
	protected void removeCoverProperty(List<Property> result) {
		for(Property property : new ArrayList<Property>(result)) {
			if(isCoverProperty(property)) {
				result.remove(property);
			}
		}		
	}
	
	/**
	 * tests if the given {@link Property} instance is a cover property instance.
	 * @param property The {@link Property} instance to be tested.
	 * @return <code>true</code> if the given {@link Property} is a cover and <code>false</code> otherwise.
	 */
	private boolean isCoverProperty(Property property) {
		if(IMetadataReader.METADATA_TYPES.COVER.getName().equalsIgnoreCase(property.getName())) {
			return true;
		}		
		return false;
	}
	
	/**
	 * searches for all metadata properties matching to the given one and returns them. 
	 * @param ref The ref to be searched. This one is in the result list in any case.
	 * @param allMetaData The metadata list to be searched.
	 * @return The list with all metadata instances matching with the given one. Never returns <code>null</code>.
	 */
	protected List<MetadataProperty> getMergedProperties(MetadataProperty ref, List<MetadataProperty> allMetaData) {
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
	protected boolean comparePropertiesForMerge(final MetadataProperty metadataProperty1, MetadataProperty metadataProperty2) {
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
	
	public static Property createProperty(final MetadataProperty metadataProperty, final List<EbookPropertyItem> items, int valueIndex) {
		return new EbookSheetProperty(metadataProperty, items, valueIndex);
	}	
	
	protected static class MetadataPropertyComparator implements Comparator<Property> {

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
			} else if(name1.equals("title")) {
				return "03";
			} else {
				return "50" + name1;
			}
		}
	}	
	
	/**
	 * This {@link EbookSheetProperty} derivat can handle multiple {@link MetadataProperty} instances.
	 * The first instance in the given {@link MetadataProperty} array is this one, where the name
	 * and the description are taken from. The value will be set to each of the given {@link MetadataProperty}
	 * instances. This allows to merge different properties to be shown as one.
	 */
	protected static class MultipleEbookSheetProperty extends EbookSheetProperty {

		private static final long serialVersionUID = 3047729348480097722L;
		
		final List<MetadataProperty> metadataProperties;
		                                                                     
		public MultipleEbookSheetProperty(final List<MetadataProperty> metadataProperties, final List<EbookPropertyItem> items) {
			super(metadataProperties.get(0), items, 0);
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
	
	public void dispose() {
		if(this.reader != null) {
			this.reader = null;
		}
	}
	
	/**
	 * search for the property which has the ebook file as value
	 * @param properties The properties to be searched.
	 * @return The desired {@link IResourceHandler} or <code>null</code> if no {@link IResourceHandler} could be found.
	 */
	public List<IResourceHandler> getPropertyResourceHandler() {
		final List<Property> properties = getProperties();
		for (Property property : properties) {
			if(property.getValue() instanceof IResourceHandler) {
				return Collections.singletonList((IResourceHandler) property.getValue());
			}
		}
		
		final List<EbookPropertyItem> selectedEbookPropertyItems = MainController.getController().getSelectedEbookPropertyItems();
		final List<IResourceHandler> result = new ArrayList<IResourceHandler>(selectedEbookPropertyItems.size());
		for (EbookPropertyItem ebookPropertyItem : selectedEbookPropertyItems) {
			result.add(ebookPropertyItem.getResourceHandler());
		}
		return result;
	}	

}
