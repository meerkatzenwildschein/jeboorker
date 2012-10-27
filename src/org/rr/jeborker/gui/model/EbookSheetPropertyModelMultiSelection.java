package org.rr.jeborker.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

import com.l2fprod.common.propertysheet.Property;

public class EbookSheetPropertyModelMultiSelection extends EbookSheetPropertyModel {

	
	public void loadProperties(List<EbookPropertyItem> items) {
		final List<IResourceHandler> ebookResourceHandlers = new ArrayList<IResourceHandler>(items.size());
		for (int i = 0; i < items.size(); i++) {
			IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceLoader(items.get(i).getFile());
			ebookResourceHandlers.add(resourceHandler);
		}
		
		final IMetadataReader reader = MetadataHandlerFactory.getReader(ebookResourceHandlers);		
		final List<MetadataProperty> allMetaData = reader.readMetaData();
		final ArrayList<Property> result = new ArrayList<Property>(allMetaData.size());
		for(MetadataProperty metadataProperty : allMetaData) {
			Property property = createProperty(metadataProperty, items, -1);
			result.add(property);
		}
		
		Collections.sort(result, PROPERTY_COMPARATOR);
		
		setProperties(result.toArray(new Property[result.size()]));
	}
	
}
