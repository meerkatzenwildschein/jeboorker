package org.rr.jeborker.metadata;

import static org.rr.commons.utils.StringUtil.EMPTY;

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
	public List<MetadataProperty> readMetadata() {
		return Collections.emptyList();
	}

	@Override
	public List<MetadataProperty> getSupportedMetadata() {
		return Collections.emptyList();
	}

	@Override
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item) {
	}

	@Override
	public String getPlainMetadata() {
		return EMPTY;
	}

	@Override
	public String getPlainMetadataMime() {
		return null;
	}

	@Override
	public List<MetadataProperty> getMetadataByType(boolean create, List<MetadataProperty> props, COMMON_METADATA_TYPES type) {
		return Collections.emptyList();
	}

}
