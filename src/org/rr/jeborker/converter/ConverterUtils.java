package org.rr.jeborker.converter;

import java.util.ArrayList;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.IMetadataWriter;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

class ConverterUtils {

	/**
	 * Transfers the main metadata between the given source and the target resource.
	 */
	static void transferMetadata(final IResourceHandler sourceResource, final IResourceHandler targetResource) {
		IMetadataReader sourceReader = MetadataHandlerFactory.getReader(sourceResource);
		IMetadataReader targetReader = MetadataHandlerFactory.getReader(targetResource);
		List<MetadataProperty> sourceMetaData = sourceReader.readMetaData();
		List<MetadataProperty> targetMetaData = new ArrayList<MetadataProperty>(sourceMetaData.size());
		
		for(IMetadataReader.METADATA_TYPES type : IMetadataReader.METADATA_TYPES.values()) {
			List<MetadataProperty> sourceMetadataByType = sourceReader.getMetadataByType(false, sourceMetaData, type);
			List<MetadataProperty> targetMetadataByType = targetReader.getMetadataByType(true, targetMetaData, type);
			
			if(!sourceMetadataByType.isEmpty()) {
				MetadataProperty sourceMetadataProperty = sourceMetadataByType.get(0);
				MetadataProperty targetMetadataProperty = targetMetadataByType.get(0);
				targetMetadataProperty.setPropertyClass(sourceMetadataProperty.getPropertyClass());
				targetMetadataProperty.setValues(sourceMetadataProperty.getValues());
				
				targetMetaData.add(targetMetadataProperty);
			}
		}
		
		IMetadataWriter writer = MetadataHandlerFactory.getWriter(targetResource);
		writer.writeMetadata(targetMetaData);
	}	
}
