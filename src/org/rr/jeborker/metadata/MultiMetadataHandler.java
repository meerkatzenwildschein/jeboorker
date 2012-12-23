package org.rr.jeborker.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;

class MultiMetadataHandler extends AMetadataHandler implements IMetadataReader, IMetadataWriter {

	private List<IResourceHandler> ebookResourceHandler;
	
	MultiMetadataHandler(List<IResourceHandler> ebookResourceHandler) {
		this.ebookResourceHandler = ebookResourceHandler;
	}
	
	@Override
	public List<IResourceHandler> getEbookResource() {
		return this.ebookResourceHandler;
	}

	@Override
	public List<MetadataProperty> readMetaData() {
		//read and collect metadata from the ebook resources
		final HashMap<METADATA_TYPES, List<MetadataProperty>> metadata = new HashMap<IMetadataReader.METADATA_TYPES, List<MetadataProperty>>();
		for (int i = 0; i < ebookResourceHandler.size(); i++) {
			final IResourceHandler resourceHandler = ebookResourceHandler.get(i);
			final IMetadataReader reader = MetadataHandlerFactory.getReader(resourceHandler);
			final List<MetadataProperty> readMetaData = reader.readMetaData();

			//collect all metadata separated by it's type (author, title, etc.)
			for(METADATA_TYPES type : METADATA_TYPES.values()) {
				List<MetadataProperty> typeMetadata = metadata.containsKey(type) ? metadata.get(type) : new ArrayList<MetadataProperty>();
				typeMetadata.addAll(reader.getMetadataByType(true, readMetaData, type));
				metadata.put(type, typeMetadata);				
			}
		}
		
		//create MultiMetadataProperty
		final List<MetadataProperty> result = createMetadataProperties(metadata);		
		
		return result;
	}

	/**
	 * Create {@link MetadataProperty} list for the properties given with the <i>metadata</i> parameter.
	 * @return List with {@link MetadataProperty}. Never returns <code>null</code>.
	 */
	protected List<MetadataProperty> createMetadataProperties(final HashMap<METADATA_TYPES, List<MetadataProperty>> metadata) {
		final List<MetadataProperty> result = new ArrayList<MetadataProperty>();
        for (Map.Entry<METADATA_TYPES, List<MetadataProperty>> entry : metadata.entrySet()) {
            final METADATA_TYPES metadataType = entry.getKey();
            final List<MetadataProperty> metadataValues = entry.getValue();
            final List<Object> values = new ArrayList<Object>() { 
            	{
            		for(MetadataProperty property : metadataValues) {
            			add(property.getValueAsString());
            		}
            	}
            };
            result.add(new MultiMetadataProperty(metadataType.getName(), values));
        }
		return result;
	}

	@Override
	public List<MetadataProperty> getSupportedMetaData() {
		return Collections.emptyList();
	}

	@Override
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item) {
		for(METADATA_TYPES type : METADATA_TYPES.values()) {
			for(MetadataProperty metadataProperty : metadataProperties) {
				if(metadataProperty.getName().equals(type.getName())) {
					type.fillItem(metadataProperty, item);
				}
			}
		}
	}

	@Override
	public byte[] getCover() {
		return null;
	}

	@Override
	public String getPlainMetaData() {
		return null;
	}

	@Override
	public String getPlainMetaDataMime() {
		return null;
	}

	@Override
	public List<MetadataProperty> getMetadataByType(boolean create, List<MetadataProperty> props, METADATA_TYPES type) {
		List<MetadataProperty> result = new ArrayList<MetadataProperty>();
		for(MetadataProperty prop : props) {
			if(prop.getName().equals(type.getName())) {
				result.add(prop);
			}
		}
		
		if(create && result.isEmpty()) {
			MultiMetadataProperty multiMetadataProperty = new MultiMetadataProperty(type.getName(), new ArrayList<Object>(Collections.singleton(null)));
			result.add(multiMetadataProperty);
		}
		
		return result;
	}

	@Override
	public void writeMetadata(List<MetadataProperty> props) {
		for (int i = 0; i < ebookResourceHandler.size(); i++) {
			final IResourceHandler resourceHandler = ebookResourceHandler.get(i);
			final IMetadataWriter writer = MetadataHandlerFactory.getWriter(resourceHandler);
			final IMetadataReader reader = MetadataHandlerFactory.getReader(resourceHandler);
			final List<MetadataProperty> readMetaData = reader.readMetaData();
			
			boolean change = false;
			for(METADATA_TYPES type : METADATA_TYPES.values()) {
				for(MetadataProperty prop : props) {
					if(type.getName().equals(prop.getName()) && !prop.getValues().isEmpty()) {
						final Object value = prop.getValues().get(0);
						final boolean createNew = !StringUtils.toString(value).isEmpty();
						final List<MetadataProperty> metadataByType = reader.getMetadataByType(createNew, readMetaData, type);
						
						if(!metadataByType.isEmpty()) {
							MetadataProperty metadataProperty = metadataByType.get(0);
							Object oldValue = metadataProperty.getValues().isEmpty() ? null : metadataProperty.getValues().get(0);
							if(value != null && !value.equals(oldValue)) {
								metadataProperty.setValue(value, 0);
								if(!StringUtils.toString(value).isEmpty() && !readMetaData.contains(metadataProperty)) {
									readMetaData.add(metadataProperty);
								} else if(StringUtils.toString(value).isEmpty()) {
									//remove empty metadata entries
									readMetaData.remove(metadataProperty);
								}
								change = true;
							}
						}
					}
				}
			}
			
			if(change) {
				try {
					writer.writeMetadata(readMetaData);
				} catch (Exception e) {
					LoggerFactory.getLogger().log(Level.WARNING, "Writing metadata to " + resourceHandler + " has failed.", e);
				}
			}
		}	
	}

	@Override
	public void setCover(byte[] cover) {
		for (int i = 0; i < ebookResourceHandler.size(); i++) {
			final IResourceHandler resourceHandler = ebookResourceHandler.get(i);
			final IMetadataWriter writer = MetadataHandlerFactory.getWriter(resourceHandler);
			writer.setCover(cover);
		}		
	}

	@Override
	public void storePlainMetadata(byte[] plainMetadata) {
	}

}