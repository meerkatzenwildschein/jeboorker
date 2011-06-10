package org.rr.jeborker.metadata;

import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.db.item.EbookPropertyItem;


public interface IMetadataReader {
	
	/**
	 * gets the ebook {@link IResourceHandler} for this instance.
	 * @return The desired ebook {@link IResourceHandler}
	 */
	public IResourceHandler getEbookResource();

	/**
	 * Read the metadata from the given {@link IResourceHandler}.
	 * @return The {@link MetaData} for the given {@link IResourceHandler}. 
	 */
	public List<MetadataProperty> readMetaData(); 
	
	/**
	 * Gets a list of supported metadata entries.
	 * @return All supported metadata entries.
	 */
	public List<MetadataProperty> getSupportedMetaData();
	
	/**
	 * Sets the {@link EbookPropertyItem} properties from the given {@link MetadataProperty}.
	 * @param metadataProperties metadata which values should be transfered to the given {@link EbookPropertyItem}.
	 * @param item The item to be filled.
	 */
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item);
	
	/**
	 * Get the book cover image bytes in a jpeg format.
	 * @return The cover image data.
	 */
	public byte[] getCover();
	
	/**
	 * Get the plain and editable metadata.
	 * @return The desired metadata or <code>null</code> if the metadata couldn't be fetched.
	 */
	public String getPlainMetaData();
	
	/**
	 * The mime type of the metadata returned by the {@link #getPlainMetaData()} method.
	 * @return The mimetype of the metadata.
	 */
	public String getPlainMetaDataMime();
	
	/**
	 * Some clean up code. Should be invoked at the end of the readers usage
	 * but it's not a must.
	 */
	public void dispose();

}
