package org.rr.jeborker.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bouncycastle.asn1.cms.MetaData;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;


public interface IMetadataReader {
	
	public static interface MetadataEntryType {
		public String getName();
		
		public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item);
		
		public List<String> getValue(EbookPropertyItem item);
	}	
	
	static enum METADATA_TYPES implements MetadataEntryType {
		AUTHOR {
			public String getName() {
				return "author";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				final List<Object> values = metadataProperty.getValues();
				if(values != null) {
					final List<String> authors = item.getAuthors();
					for(Object author : values) {
						String a = StringUtils.toString(author);
						if(!StringUtils.isEmpty(a)) {
							authors.add(a);
						}
					}
					item.setAuthors(authors);
				}
			}

			@Override
			public List<String> getValue(EbookPropertyItem item) {
				final List<String> authors = item.getAuthors();
				return new ArrayList<String>(authors);
			}
		},
		TITLE {
			public String getName() {
				return "title";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setTitle(metadataProperty.getValueAsString());
			}
			
			@Override
			public List<String> getValue(EbookPropertyItem item) {
				return new ArrayList<String>(Collections.singleton(item.getTitle()));
			}			
		},
		SERIES_NAME {
			public String getName() {
				return "seriesname";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setSeriesName(metadataProperty.getValueAsString());
			}
			
			@Override
			public List<String> getValue(EbookPropertyItem item) {
				return new ArrayList<String>(Collections.singleton(item.getSeriesName()));
			}						
		},
		GENRE {
			public String getName() {
				return "genre";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setGenre(metadataProperty.getValueAsString());
			}
			
			@Override
			public List<String> getValue(EbookPropertyItem item) {
				return new ArrayList<String>(Collections.singleton(item.getGenre()));
			}				
		}		
	}	
	
	/**
	 * gets the ebook {@link IResourceHandler} for this instance.
	 * @return The desired ebook {@link IResourceHandler}
	 */
	public List<IResourceHandler> getEbookResource();

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
	 * Gets a new, empty metadata property for the rating value.
	 * @return The desired metadata property.
	 */
	public MetadataProperty createRatingMetaData();	
	
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
	 * Get the the metadata entries by it's type.
	 * @param create If no metadata entries with the desired type exists, create a new, empty one. 
	 * @param props Extract the desired metadata type entry from the given metadata properties. If this
	 * 		parameter is <code>null</code>, the properties will be read from the file.
	 * @return A list of genre entries. Never returns <code>null</code>
	 */
	public List<MetadataProperty> getMetadataByType(boolean create, List<MetadataProperty> props, METADATA_TYPES type);
	
	/**
	 * Some clean up code. Should be invoked at the end of the readers usage
	 * but it's not a must.
	 */
	public void dispose();
	
}
