package org.rr.jeborker.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bouncycastle.asn1.cms.MetaData;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.IDBObject;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;


public interface IMetadataReader {
	
	public static interface MetadataEntryType {
		public String getName();
		
		public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item);
		
		public List<String> getValue(EbookPropertyItem item);
	}	
	
	public static enum METADATA_TYPES implements MetadataEntryType {
		AUTHOR {
			public String getName() {
				return "author";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				final List<Object> values = metadataProperty.getValues();
				if(values != null) {
					final List<String> authors = item.getAuthor() != null ? ListUtils.split(item.getAuthor(), IDBObject.LIST_SEPARATOR_CHAR) : new ArrayList<String>();
					for(Object author : values) {
						String a = StringUtils.toString(author).trim();
						if(!StringUtils.isEmpty(a) && !authors.contains(a)) {
							authors.add(a);
						}
					}
					item.setAuthor(ListUtils.join(authors, IDBObject.LIST_SEPARATOR_CHAR));
				}
			}

			@Override
			public List<String> getValue(EbookPropertyItem item) {
				if(item != null) {
					final List<String> authors = item.getAuthor() != null ? ListUtils.split(item.getAuthor(), IDBObject.LIST_SEPARATOR_CHAR) : new ArrayList<String>();
					return authors;
				}
				return Collections.emptyList();
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
				if(item != null && item.getTitle() != null) {
					return new ArrayList<String>(Collections.singleton(item.getTitle()));
				}
				return Collections.emptyList();
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
				if(item != null && item.getSeriesName() != null) {
					return new ArrayList<String>(Collections.singleton(item.getSeriesName()));
				} else {
					return Collections.emptyList();
				}
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
				if(item != null && item.getGenre() != null) {
					return new ArrayList<String>(Collections.singleton(item.getGenre()));
				} else {
					return Collections.emptyList();
				}
			}				
		},
		RATING {
			public String getName() {
				return "rating";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				String raintgValue = metadataProperty.getValueAsString();
				Number num = CommonUtils.toNumber(raintgValue);
				if(num != null) {
					item.setRating(num.intValue());
				} else {
					item.setRating(null);
				}
			}
			
			@Override
			public List<String> getValue(EbookPropertyItem item) {
				if(item != null && item.getRating() != null) {
					return new ArrayList<String>(Collections.singleton( Integer.valueOf(item.getRating()).toString()));
				} else {
					return Collections.emptyList();
				}
			}				
		},	
		COVER {
			public String getName() {
				return "cover";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				if(!metadataProperty.getValues().isEmpty() && metadataProperty.getValues().get(0) instanceof byte[]) {
					EbookPropertyItemUtils.setupCoverData(item, (byte[]) metadataProperty.getValues().get(0));
				} else {
					EbookPropertyItemUtils.setupCoverData(item, null);
				}
			}
			
			@Override
			public List<String> getValue(EbookPropertyItem item) {
				return Collections.emptyList(); //no String value for the cover possible.
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
	 * Sets the {@link EbookPropertyItem} properties from the given {@link MetadataProperty}.
	 * @param metadataProperties metadata which values should be transfered to the given {@link EbookPropertyItem}.
	 * @param item The item to be filled.
	 */
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item);
	
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
	
}
