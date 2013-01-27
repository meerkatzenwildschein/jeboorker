package org.rr.jeborker.metadata;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.epub.EpubReader;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.zip.ZipUtils;
import org.rr.commons.utils.zip.ZipUtils.ZipDataEntry;
import org.rr.jeborker.db.item.EbookKeywordItem;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.metadata.IMetadataReader.METADATA_TYPES;

abstract class AEpubMetadataHandler extends AMetadataHandler {
	
	private IResourceHandler ebookResourceHandler;
	
	private Date ebookResourceHandlerTimestamp;

	private byte[] containerOpfData = null;
	
	protected byte[] zipContent = null;
	
	private String opfFileName = null;

	protected static interface MetadataEntryType {
		String getName();
		
		void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item);
	}	
	
	static enum EPUB_METADATA_TYPES implements MetadataEntryType {
		JB_AGE_SUGGESTION {
			public String getName() {
				return "jeboorker:age_suggestion";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setAgeSuggestion(metadataProperty.getValueAsString());
			}
		},JB_KEYWORDS {
			public String getName() {
				return "jeboorker:keywords";
			}
			
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				List<String> keywords = ListUtils.split(metadataProperty.getValueAsString(), ",");
				List<EbookKeywordItem> asEbookKeywordItem = EbookPropertyItemUtils.getAsEbookKeywordItem(keywords);
				item.setKeywords(asEbookKeywordItem);
			}			
		},CALIBRE_RATING {
			public String getName() {
				return "calibre:rating";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				Number number = CommonUtils.toNumber(metadataProperty.getValueAsString());
				item.setRating(number != null ? number.intValue() : null);
			}
		},CALIBRE_SERIES_INDEX {
			public String getName() {
				return "calibre:series_index";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setSeriesIndex(metadataProperty.getValueAsString());
			}
		},CALIBRE_SERIES {
			public String getName() {
				return "calibre:series";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				METADATA_TYPES.SERIES_NAME.fillItem(metadataProperty, item);
			}
		},SUBJECT {
			public String getName() {
				return "subject";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				METADATA_TYPES.GENRE.fillItem(metadataProperty, item);
			}
		},PUBLISHER {
			public String getName() {
				return "publisher";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setPublisher(metadataProperty.getValueAsString());
			}
		},IDENTIFIER {
			public String getName() {
				return "identifier";
			}

			@SuppressWarnings("unchecked")
			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				Identifier identifier = ((EpubLibMetadataProperty<Identifier>)metadataProperty).getType();
				if("uuid".equalsIgnoreCase(identifier.getScheme())) {
					item.setUuid(metadataProperty.getValueAsString());
				} else if("idbn".equalsIgnoreCase(identifier.getScheme())) {
					item.setIsbn(metadataProperty.getValueAsString());
				}
			}
		},ISBN {
			public String getName() {
				return "isbn";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setIsbn(metadataProperty.getValueAsString());
			}
		},UUID {
			public String getName() {
				return "uuid";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setUuid(metadataProperty.getValueAsString());
			}
		},RIGHTS {
			public String getName() {
				return "rights";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setRights(metadataProperty.getValueAsString());
			}
		},LANGUAGE {
			public String getName() {
				return "language";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setLanguage(metadataProperty.getValueAsString());
			}
		},DESCRIPTION {
			public String getName() {
				return "description";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setDescription(metadataProperty.getValueAsString());
			}
		},TITLE {
			public String getName() {
				return "title";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				METADATA_TYPES.TITLE.fillItem(metadataProperty, item);
			}
		},DATE {
			public String getName() {
				return "date";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setCreationDate(DateConversionUtils.toDate(metadataProperty.getValueAsString()));
			}
		},PUBLICATION_DATE {
			public String getName() {
				return "pubdate";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setPublishingDate(DateConversionUtils.toDate(metadataProperty.getValueAsString()));
			}
		},CREATION_DATE {
			public String getName() {
				return "createdate";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setCreationDate(DateConversionUtils.toDate(metadataProperty.getValueAsString()));
			}
		},MODIFICATION_DATE {
			public String getName() {
				return "modifydate";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},CREATOR {
			public String getName() {
				return "creator";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},AUTHOR {
			public String getName() {
				return "author";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				METADATA_TYPES.AUTHOR.fillItem(metadataProperty, item);
			}
		},TYPE {
			public String getName() {
				return "type";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},CONTRIBUTOR {
			public String getName() {
				return "contributor";
			}
			
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},FORMAT {
			public String getName() {
				return "format";
			}
			
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}
		},COVER {
			public String getName() {
				return "cover";
			}
			
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				METADATA_TYPES.COVER.fillItem(metadataProperty, item);
			}
		}
	}
	
	AEpubMetadataHandler(IResourceHandler ebookResourceHandler) {
		this.ebookResourceHandler = ebookResourceHandler;
		this.ebookResourceHandlerTimestamp = ebookResourceHandler.getModifiedAt();
	}	
	
	/**
	 * Gets the {@link IResourceHandler} instance for the ebook which is processed
	 * by this {@link AMetadataHandler} instance.
	 * @return The desired {@link IResourceHandler} instance.
	 */
	public List<IResourceHandler> getEbookResource() {
		return Collections.singletonList(this.ebookResourceHandler);
	}
	
	/**
	 * Gets the Opf file where the metadata is stored. The information where the opf
	 * file could be found is stored in the META-INF/container.xml file. This information
	 * will be extracted from there. 
	 * 
	 * @param zipData The zip data bytes.
	 * @return The desired file name or <code>null</code> if no one could be found.
	 */
	protected String getOpfFile(byte[] zipData) {
		if(this.opfFileName == null) {
			final String fullPathString = "full-path=";
			final ZipDataEntry containerXml = ZipUtils.extract(zipData, "META-INF/container.xml");
			if(containerXml!=null) {
				final String containerXmlData = new String(containerXml.getBytes());
				final int fullPathIndex = containerXmlData.indexOf(fullPathString);
				if(fullPathIndex!=-1) {
					final int startIdx = fullPathIndex + fullPathString.length() + 1;
					final int endIdx = containerXmlData.indexOf('"', startIdx);
					final String fullPathValue = containerXmlData.substring(startIdx, endIdx);
					this.opfFileName = fullPathValue;
				}
			}
		}
		return this.opfFileName;
	}	

	/**
	 * gets the container opf file content bytes containing the metdadata informations.
	 */
	protected byte[] getContainerOPF(final IResourceHandler ebookResource) throws IOException {
		if (this.containerOpfData == null || isModified()) {
			final byte[] zipData = this.getContent(ebookResource);
			return getContainerOPF(zipData);
		}
		return this.containerOpfData;
	}	
	
	/**
	 * gets the container opf file content bytes containing the metdadata informations.
	 */
	protected byte[] getContainerOPF(final byte[] zipData) throws IOException {
		if (this.containerOpfData == null) {
			final String opfFile = this.getOpfFile(zipData);
			if (opfFile != null) {
				final ZipDataEntry containerXml = ZipUtils.extract(zipData, opfFile);
				if (containerXml != null) {
					this.containerOpfData = containerXml.getBytes();
				} else {
					LoggerFactory.logWarning(this, "Could not get file" + opfFile, new RuntimeException("dumpstack"));
				}
			}
		}
		return this.containerOpfData;
	}

	/**
	 * Get the zip data content bytes from the epub+zip file. The content
	 * is cached so it's performance safe so invoke this method frequently.
	 */
	protected byte[] getContent(final IResourceHandler ebookResource) throws IOException {
		if (this.zipContent == null || isModified()) {
			this.zipContent = ebookResource.getContent();
		}
		return this.zipContent;
	}	
	
	protected boolean isModified() {
		if(this.ebookResourceHandlerTimestamp != null && this.ebookResourceHandler.getModifiedAt() != null) {
			return !this.ebookResourceHandler.getModifiedAt().equals(ebookResourceHandlerTimestamp);
		}
		return true;
	}
	
	/**
	 * Read all entries from, the given zip data and creates a {@link Book} instance from them. 
	 * @throws IOException
	 */
	protected Book readBook(final byte[] zipData, final IResourceHandler ebookResourceHandler) throws IOException {
		final EpubReader reader = new EpubReader();
		final List<ZipDataEntry> extracted = ZipUtils.extract(zipData, new ZipUtils.EmptyZipFileFilter(), -1);
		final Resources resources = new Resources();
		for(ZipDataEntry entry : extracted) {
			Resource resource = new Resource(entry.data, entry.path);
			resources.add(resource);
		}
		
		final Book epub = reader.readEpub(resources, "UTF-8", ebookResourceHandler.getName());
		return epub;
	}	
	
}
