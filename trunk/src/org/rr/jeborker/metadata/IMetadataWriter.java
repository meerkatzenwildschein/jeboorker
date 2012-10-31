package org.rr.jeborker.metadata;


import java.util.List;

import org.rr.jeborker.db.item.EbookPropertyItem;

public interface IMetadataWriter {

	/**
	 * Write the metadata from the given {@link EbookPropertyItem}
	 */
	public void writeMetadata(List<MetadataProperty> props); 
	
	/**
	 * (Over)writes the cover to the ebook handled by this {@link IMetadataWriter}
	 * implementation.
	 * @param cover The cover data to be written.
	 */
	public void setCover(byte[] cover);
	
	/**
	 * Stores the given metadata. This can be for example some xml previously
	 * read with the {@link IMetadataReader#getPlainMetaData()} method.
	 * @param plainMetadata The data to be stored to the ebook.
	 */
	public void storePlainMetadata(byte[] plainMetadata);
	
	/**
	 * Some clean up code. Should be invoked at the end of the readers usage
	 * but it's not a must.
	 */
	public void dispose();	

}
