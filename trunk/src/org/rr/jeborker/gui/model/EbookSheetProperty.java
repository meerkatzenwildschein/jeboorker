package org.rr.jeborker.gui.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataProperty;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;

public class EbookSheetProperty extends DefaultProperty {

	private static final long serialVersionUID = 1L;
	
	protected MetadataProperty metadataProperty;
	
	protected int propertyIndex;
	
	/**
	 * Flags that tells that the value for this property has changed 
	 */
	private boolean changed = false;
	
	public EbookSheetProperty(final MetadataProperty metadataProperty, final int propertyIndex) {
		this.metadataProperty = metadataProperty;
		this.propertyIndex = propertyIndex;
		this.setEditable(metadataProperty.isEditable());
	}

	
	/**
	 * Creates all Property elements supported by the ebook format given
	 * with the {@link EbookPropertyItem} parameter.
	 * @param item The item where the {@link Property} array should be created for.
	 * @return A couple of properties for the given {@link EbookPropertyItem} never returns <code>null</code>.
	 */
	public static Property[] createProperties(final IResourceHandler resourceLoader, final IMetadataReader reader) {
		if(resourceLoader==null) {
			return new Property[0];
		}
		final ArrayList<Property> result = new ArrayList<Property>(20);
		
		final DefaultProperty fileNameProperty = new DefaultProperty();
		fileNameProperty.setDisplayName(Bundle.getString("EbookPropertySheetProperty.property.name.file"));
		fileNameProperty.setName("file");
		fileNameProperty.setEditable(false);
		fileNameProperty.setValue(resourceLoader);
		fileNameProperty.setShortDescription(resourceLoader!=null ? resourceLoader.toString() : "");
		result.add(fileNameProperty);
		
		setupMetadata(result, resourceLoader, reader);
		
		return result.toArray(new Property[result.size()]);
	}

	/**
	 * attaches a property for each metadata item of the resource given with the
	 * resourceLoader parameter.
	 * @param result The list where the metadata properties should be attched to.
	 * @param resourceLoader The {@link IResourceHandler} providing the ebook data.
	 */
	private static void setupMetadata(final ArrayList<Property> result, final IResourceHandler resourceLoader, final IMetadataReader reader) {
		final List<MetadataProperty> allMetaData = reader.readMetaData();
		for (int i=0; i < allMetaData.size(); i++) {
			final MetadataProperty metadataProperty = allMetaData.get(i);
			final List<Object> values = metadataProperty.getValues();
			if(values.size()==1) {
				//find properties with the same name and value to merge them in the sheet view.
				boolean found = false;
				for (int j=i+1; j < allMetaData.size(); j++) {
					MetadataProperty metadataProperty2 = allMetaData.get(j);
					if(comparePropertiesForMerge(metadataProperty, metadataProperty2)) {
						EbookSheetProperty property = new MultipleEbookSheetProperty(new MetadataProperty[] {metadataProperty, metadataProperty2});
						result.add(property);
						allMetaData.remove(j);
						found = true;
						break;
					}
				}
				
				if(!found) {
					Property property = createProperty(metadataProperty, 0);
					result.add(property);
				}
			} else {
				for (int j=0; j < values.size(); j++) {
					Property property = createProperty(metadataProperty, j);
					result.add(property);				
				}
			}
		}
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
		// test for value
		boolean result = metadataProperty2.getValues().size() == 1 && metadataProperty1.getValues().size() == 1
				&& metadataProperty2.getValues().get(0).equals(metadataProperty1.getValues().get(0));
		
		final String metadataProperty1Name = metadataProperty1.getName().toLowerCase();
		final String metadataProperty2Name = metadataProperty2.getName().toLowerCase();		

		if (result && metadataProperty1Name.equals(metadataProperty2Name)) {
			// name is the same
			return true;
		} else if(StringUtils.compareTwice(metadataProperty1Name, metadataProperty2Name, "createdate", "creationdate")) {
			//merge createdate and creationdate because they have the same sense
			return true;
		} else if(StringUtils.compareTwice(metadataProperty1Name, metadataProperty2Name, "calibrerating", "rating")) {
			return true;
		} else {
			return false;
		}
	}
	
	public static Property createProperty(final MetadataProperty metadataProperty, int valueIndex) {
		return new EbookSheetProperty(metadataProperty, valueIndex);
	}
	
	/**
	 * Tells if the property value has changed.
	 * @return <code>true</code> if the property value has changed and <code>false</code> otherwise.
	 */
	public boolean isChanged() {
		return changed;
	}

	void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public void setValue(Object value) {
		if(value!=null && !value.equals(getValue())) {
			metadataProperty.setValue(value, this.propertyIndex);
			this.setChanged(true);	
		}
		super.setValue(value);
	}

	@Override
	public String getName() {
		String name = metadataProperty.getName();
		String localizedName = MainController.getController().getLocalizedString(name);
		
		if(metadataProperty.getValues().size() > 1) {
			localizedName = (this.propertyIndex+1) + ")" + localizedName; 
		}
		return localizedName;
	}
	
	@Override
	public String getShortDescription() {
		final String details = metadataProperty != null ? metadataProperty.getDetails() : "";
		final StringBuilder value = new StringBuilder(details != null ? details : "");
		final boolean isDate = ReflectionUtils.equals(metadataProperty.getPropertyClass(), Date.class);
		
		if(isDate) {
			if(value.length()>0) {
				value.append("<br/>");
			}
			
			Object propertyValue = metadataProperty.getValues().get(propertyIndex);
			if(propertyValue instanceof Date) {
				value.append(DateFormat.getDateInstance(SimpleDateFormat.LONG).format((Date)propertyValue));
			} else {
				Date date = DateConversionUtils.toDate(StringUtils.toString(metadataProperty.getValues().get(propertyIndex)));
				if(date!=null) {
					value.append(DateFormat.getDateInstance(SimpleDateFormat.LONG).format(date));
				}
			}
		} else {
			if (value.length() > 0) {
				value.append("<br/>");
			}			
			value.append(String.valueOf(metadataProperty.getValues().get(propertyIndex)));
		}
		
		if(!MainController.getController().getLocalizedString(metadataProperty.getName()).equals(metadataProperty.getName())) {
			value.append(" (").append(metadataProperty.getName()).append(")");
		}
		
		return value.toString();
	}

	@Override
	public String getDisplayName() {
		return this.getName();
	}


	@Override
	public Object getValue() {
		return metadataProperty.getValues().get(propertyIndex);
	}


	@Override
	public Class<?> getType() {
		return metadataProperty.getPropertyClass();
	}

	/**
	 * Gets the encapsulated {@link MetadataProperty} which holds the current value state.
	 * @return The encapsulated {@link MetadataProperty} or <code>null</code> if no one existing.
	 */
	public MetadataProperty[] getMetadataProperties() {
		return new MetadataProperty[] {this.metadataProperty};
	}
	
	/**
	 * This {@link EbookSheetProperty} derivat can handle multiple {@link MetadataProperty} instances.
	 * The first instance in the given {@link MetadataProperty} array is this one, where the name
	 * and the description are taken from. The value will be set to each of the given {@link MetadataProperty}
	 * instances. This allows to merge different properties to be shown as one.
	 */
	private static class MultipleEbookSheetProperty extends EbookSheetProperty {

		private static final long serialVersionUID = 3047729348480097722L;
		
		final MetadataProperty[] metadataProperty;
		                                                                     
		public MultipleEbookSheetProperty(final MetadataProperty[] metadataProperty) {
			super(metadataProperty[0], 0);
			this.metadataProperty = metadataProperty;
			for (int i = 0; i < metadataProperty.length; i++) {
				if(!metadataProperty[i].isEditable()) {
					this.setEditable(false);
				}
			}
		}

		@Override
		public void setValue(Object value) {
			if(value!=null && !value.equals(getValue())) {
				for (int i = 0; i < metadataProperty.length; i++) {
					metadataProperty[i].setValue(value, 0);
				}
				this.setChanged(true);	
			}
			super.setValue(value);
		}
		
		/**
		 * Gets the encapsulated {@link MetadataProperty} which holds the current value state.
		 * @return The encapsulated {@link MetadataProperty} or <code>null</code> if no one existing.
		 */
		public MetadataProperty[] getMetadataProperties() {
			return metadataProperty;
		}		
	}
}
