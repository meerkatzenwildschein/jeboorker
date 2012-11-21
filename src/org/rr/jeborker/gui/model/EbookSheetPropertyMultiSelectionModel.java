package org.rr.jeborker.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;
import org.rr.jeborker.metadata.IMetadataReader.METADATA_TYPES;

import com.l2fprod.common.propertysheet.Property;

public class EbookSheetPropertyMultiSelectionModel extends EbookSheetPropertyModel {

	
	public void loadProperties(List<EbookPropertyItem> items) {
		final List<IResourceHandler> ebookResourceHandlers = new ArrayList<IResourceHandler>(items.size());
		for (int i = 0; i < items.size(); i++) {
			IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceLoader(items.get(i).getFile());
			ebookResourceHandlers.add(resourceHandler);
		}
		
		final IMetadataReader reader = MetadataHandlerFactory.getReader(ebookResourceHandlers);
		final ArrayList<MetadataProperty> allMetadata = new ArrayList<MetadataProperty>(METADATA_TYPES.values().length);
		final ArrayList<Property> result = new ArrayList<Property>(allMetadata.size());
		
		for(METADATA_TYPES type : METADATA_TYPES.values()) {
			List<MetadataProperty> l = Collections.emptyList();
			MetadataProperty metadataProperty = reader.getMetadataByType(true, l, type).get(0);
			allMetadata.add(metadataProperty);
			
			Property property = createProperty(metadataProperty, items, type);
			result.add(property);
		}
		
		Collections.sort(result, PROPERTY_COMPARATOR);
		
		setProperties(result.toArray(new Property[result.size()]));
	}
	
	private static Property createProperty(final MetadataProperty metadataProperty, final List<EbookPropertyItem> items, final METADATA_TYPES type) {
		return new EbookSheetProperty(metadataProperty, items, -1) {
			final ArrayList<String> values = new ArrayList<String>(items.size()) {
				{
					add(null); //selection value
					for(EbookPropertyItem item : items) {
						addAll(type.getValue(item));
					}	
				}
			};

			
			@Override
			public Object getValue() {
				Object sValue = super.getValue();
				if(sValue instanceof List) {
					Object o = ((List<?>) sValue).get(0);
					values.set(0, StringUtils.toString(o, true));
				} else {
					values.set(0, StringUtils.toString(sValue, true));
				}
				return values;
			}
			
		};
	}		
	
}
