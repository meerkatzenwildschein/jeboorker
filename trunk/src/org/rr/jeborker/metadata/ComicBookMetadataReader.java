package org.rr.jeborker.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.CommonUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.metadata.comicbook.ComicBookDocument;
import org.rr.jeborker.metadata.comicbook.ComicBookReader;

class ComicBookMetadataReader implements IMetadataReader {

	protected static interface MetadataEntryType {
		String getName();
		
		void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item);
	}	
	
	static enum COMICBOOK_METADATA_TYPES implements MetadataEntryType {
		TITLE {
			public String getName() {
				return "Title";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				METADATA_TYPES.TITLE.fillItem(metadataProperty, item);
			}
		},SERIES {
			public String getName() {
				return "Series";
			}
			
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				METADATA_TYPES.SERIES_NAME.fillItem(metadataProperty, item);
			}			
		},NUMBER {
			public String getName() {
				return "Number";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setSeriesIndex(metadataProperty.getValueAsString());
			}
		},COUNT {
			public String getName() {
				return "Count";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
			}
		},VOLUME {
			public String getName() {
				return "Volume";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},ALTERNATE_SERIES {
			public String getName() {
				return "AlternateSeries";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},ALTERNATE_NUMBER {
			public String getName() {
				return "AlternateNumber";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},ALTERNATE_COUNT {
			public String getName() {
				return "AlternateCount";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},SUMMARY {
			public String getName() {
				return "Summary";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setDescription(metadataProperty.getValueAsString());
			}
		},NOTES {
			public String getName() {
				return "Notes";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},YEAR {
			public String getName() {
				return "Year";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},MONTH {
			public String getName() {
				return "Month";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},WRITER {
			public String getName() {
				return "Writer";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				METADATA_TYPES.AUTHOR.fillItem(metadataProperty, item);
			}
		},PENCILLER {
			public String getName() {
				return "Penciller";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},INKER {
			public String getName() {
				return "Inker";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},COLORIST {
			public String getName() {
				return "Colorist";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},LETTERER {
			public String getName() {
				return "Letterer";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},COVERARTIST {
			public String getName() {
				return "CoverArtist";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},EDITOR {
			public String getName() {
				return "Editor";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},PUBLISHER {
			public String getName() {
				return "Publisher";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setPublisher(metadataProperty.getValueAsString());
			}
		},IMPRINT {
			public String getName() {
				return "Imprint";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},GENRE {
			public String getName() {
				return "Genre";
			}
			
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				METADATA_TYPES.GENRE.fillItem(metadataProperty, item);
			}
		},WEB {
			public String getName() {
				return "Web";
			}
			
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},PAGECOUNT {
			public String getName() {
				return "PageCount";
			}
			
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},LANGUAGE_ISO {
			public String getName() {
				return "LanguageISO";
			}
			
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setLanguage(metadataProperty.getValueAsString());
			}
		},FORMAT {
			public String getName() {
				return "Format";
			}
			
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},BLACK_AND_WHITE {
			public String getName() {
				return "BlackAndWhite";
			}
			
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},MANGA {
			public String getName() {
				return "Manga";
			}
			
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},RATING {
			public String getName() {
				return "Rating";
			}
			
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
				Number number = CommonUtils.toNumber(metadataProperty.getValueAsString());
				item.setRating(number != null ? number.intValue() : null);
			}
		}
	}	
	
	private IResourceHandler ebookResourceHandler;
	
	private ComicBookDocument doc;
	
	ComicBookMetadataReader(IResourceHandler resource) {
		this.ebookResourceHandler = resource;
	}
	
	@Override
	public List<IResourceHandler> getEbookResource() {
		return Collections.singletonList(this.ebookResourceHandler);
	}

	@Override
	public List<MetadataProperty> readMetaData() {
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>();
		try {
			final ComicBookDocument document = getDocument();
			
			HashMap<String, Object> info = document.getInfo();
			for (Entry<String, Object> entry : info.entrySet()) {
				final String key = entry.getKey();
				final Object value = entry.getValue();
				try {
					MetadataProperty metadataProperty = new MetadataProperty(key, value);
					result.add(metadataProperty);
				} catch(Exception e) {
					LoggerFactory.logWarning(this, "could not handle property " + key + " and value " + value, e);
				}
			}			
		} catch (Throwable e) {
			LoggerFactory.logWarning(this.getClass(), "Could not read metadata for comicbook " + ebookResourceHandler, e);
		}
		return result;
	}

	@Override
	public List<MetadataProperty> getSupportedMetaData() {
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>();
		result.add(new MetadataProperty("Title", ""));
		result.add(new MetadataProperty("Writer", ""));
		result.add(new MetadataProperty("Series", ""));
		result.add(new MetadataProperty("Rating", ""));
		result.add(new MetadataProperty("Number", ""));
		result.add(new MetadataProperty("Count", ""));
		result.add(new MetadataProperty("Volume", ""));
		result.add(new MetadataProperty("AlternateSeries", ""));
		result.add(new MetadataProperty("AlternateNumber", ""));
		result.add(new MetadataProperty("AlternateCount", ""));
		result.add(new MetadataProperty("Summary", ""));
		result.add(new MetadataProperty("Notes", ""));
		result.add(new MetadataProperty("Year", ""));
		result.add(new MetadataProperty("Month", ""));		
		result.add(new MetadataProperty("Penciller", ""));
		result.add(new MetadataProperty("Inker", ""));
		result.add(new MetadataProperty("Colorist", ""));
		result.add(new MetadataProperty("Letterer", ""));
		result.add(new MetadataProperty("CoverArtist", ""));
		result.add(new MetadataProperty("Editor", ""));
		result.add(new MetadataProperty("AlternateCount", ""));
		result.add(new MetadataProperty("Publisher", ""));
		result.add(new MetadataProperty("Genre", ""));
		result.add(new MetadataProperty("Web", ""));
		result.add(new MetadataProperty("PageCount", ""));				
		result.add(new MetadataProperty("LanguageISO", ""));
		result.add(new MetadataProperty("Format", ""));
		result.add(new MetadataProperty("BlackAndWhite", ""));
		result.add(new MetadataProperty("Manga", ""));
		return result;
	}

	@Override
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item) {
		item.clearMetadata();
		for(MetadataProperty metadataProperty : metadataProperties) {
			for(COMICBOOK_METADATA_TYPES type : COMICBOOK_METADATA_TYPES.values()) {
				if(type.getName().equals(metadataProperty.getName())) {
					type.fillItem(metadataProperty, item);
					break;
				}
			}
		}
	}

	@Override
	public byte[] getCover() {
		return doc.getCover();
	}

	@Override
	public String getPlainMetaData() {
		return new String(doc.getComicInfoXml());
	}

	@Override
	public String getPlainMetaDataMime() {
		return "text/xml";
	}

	@Override
	public List<MetadataProperty> getMetadataByType(boolean create, List<MetadataProperty> props, METADATA_TYPES type) {
		MetadataProperty newProperty;
		switch(type) {
		case AUTHOR:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.WRITER.getName(), "");
			break;
		case TITLE:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.TITLE.getName(), "");
			break;
		case GENRE:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.GENRE.getName(), "");
			break;
		case SERIES_NAME:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.SERIES.getName(), "");
			break;
		case RATING:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.RATING.getName(), "");
			break;			
		default: newProperty = null;
		}

		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>();
		if(newProperty != null) {
			for(MetadataProperty prop : props) {
				if(prop.getName().equalsIgnoreCase(newProperty.getName())) {
					result.add(prop);
				}
			}
		}
		
		if(create && result.isEmpty() && newProperty != null) {
			result.add(newProperty);
		}
		return result;
	}
	
	private ComicBookDocument getDocument() throws IOException {
		if(this.doc == null) {
			this.doc = new ComicBookReader(ebookResourceHandler).getDocument();
		} 
		return this.doc;
	}	

}
