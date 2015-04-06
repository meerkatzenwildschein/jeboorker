package org.rr.jeborker.metadata;


import java.util.List;

import org.rr.jeborker.db.item.EbookPropertyItem;

public interface IMetadataWriter {

	/**
	 * Write the metadata from the given {@link EbookPropertyItem}
	 */
	public void writeMetadata(List<MetadataProperty> props); 
	
	/**
	 * Stores the given metadata. This can be for example some xml previously
	 * read with the {@link IMetadataReader#getPlainMetadata()} method.
	 * @param plainMetadata The data to be stored to the ebook.
	 */
	public void storePlainMetadata(byte[] plainMetadata);
}
