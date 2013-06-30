package org.rr.jeborker.metadata;

import java.util.Collections;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.db.item.EbookPropertyItem;

public class EmptyMetadataReader implements IMetadataReader {
	
	private IResourceHandler ebookResourceHandler;
	
	EmptyMetadataReader(IResourceHandler resource) {
		this.ebookResourceHandler = resource;
	}
	
	@Override
	public List<IResourceHandler> getEbookResource() {
		return Collections.singletonList(this.ebookResourceHandler);
	}

	@Override
	public List<MetadataProperty> readMetaData() {
		return Collections.emptyList();
	}

	@Override
	public List<MetadataProperty> getSupportedMetaData() {
		return Collections.emptyList();
	}

	@Override
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item) {
	}

	@Override
	public String getPlainMetaData() {
		return "";
	}

	@Override
	public String getPlainMetaDataMime() {
		return null;
	}

	@Override
	public List<MetadataProperty> getMetadataByType(boolean create, List<MetadataProperty> props, METADATA_TYPES type) {
		return Collections.emptyList();
	}

}
