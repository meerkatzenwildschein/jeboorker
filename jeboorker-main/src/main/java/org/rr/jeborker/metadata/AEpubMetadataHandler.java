package org.rr.jeborker.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.commons.utils.compression.CompressedDataEntry;
import org.rr.commons.utils.compression.FileEntryFilter;
import org.rr.commons.utils.compression.zip.LazyZipEntryStream;
import org.rr.commons.utils.compression.zip.ZipUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.metadata.IMetadataReader.COMMON_METADATA_TYPES;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.epub.EpubReader;

abstract class AEpubMetadataHandler extends AMetadataHandler {

	private IResourceHandler ebookResourceHandler;

	private Date ebookResourceHandlerTimestamp;

	private byte[] containerOpfData = null;

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
				item.setKeywords(keywords);
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
				COMMON_METADATA_TYPES.SERIES_NAME.fillItem(metadataProperty, item);
			}
		},SUBJECT {
			public String getName() {
				return "subject";
			}

			@Override
			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				COMMON_METADATA_TYPES.GENRE.fillItem(metadataProperty, item);
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
				return COMMON_METADATA_TYPES.TITLE.getName();
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				COMMON_METADATA_TYPES.TITLE.fillItem(metadataProperty, item);
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
				COMMON_METADATA_TYPES.AUTHOR.fillItem(metadataProperty, item);
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
				COMMON_METADATA_TYPES.COVER.fillItem(metadataProperty, item);
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
	 * @throws IOException
	 */
	protected String getOpfFile(final IResourceHandler ebookResource) throws IOException {
		if(this.opfFileName == null) {
			final String fullPathString = "full-path=";
			InputStream contentInputStream = null;
			try {
				final CompressedDataEntry containerXml = ZipUtils.extract(ebookResource, "META-INF/container.xml");
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
			} finally {
				if(contentInputStream != null) {
					IOUtils.closeQuietly(contentInputStream);
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
			final String opfFile = this.getOpfFile(ebookResource);
			if (opfFile != null) {
				InputStream contentInputStream = null;
				try {
					final CompressedDataEntry containerXml = ZipUtils.extract(ebookResource, opfFile);
					if (containerXml != null) {
						this.containerOpfData = containerXml.getBytes();
					} else {
						LoggerFactory.logWarning(this, "Could not get file" + opfFile, new RuntimeException("dumpstack"));
					}
				} finally {
					if(contentInputStream != null) {
						IOUtils.closeQuietly(contentInputStream);
					}
				}
			}
		}
		return this.containerOpfData;
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
	protected Book readBook(final InputStream zipData, final IResourceHandler ebookResourceHandler, final boolean lazy) throws IOException {
		try {
			final EpubReader reader = new EpubReader();
			final Resources resources = new Resources();
			final EpubZipFileFilter epubZipFileFilter = new EpubZipFileFilter(lazy);
			final List<CompressedDataEntry> extracted = ZipUtils.extract(ebookResourceHandler, epubZipFileFilter);
			final List<String> lazyEntries = epubZipFileFilter.getLazyEntries();
			final List<byte[]> lazyRawEntries = epubZipFileFilter.getLazyRawEntries();

			for(CompressedDataEntry entry : extracted) {
				Resource resource = new Resource(entry.getBytes(), entry.rawPath);
				resources.add(resource);
			}

			if(lazyEntries.size() == lazyRawEntries.size()) {
				for(int i = 0; i < lazyEntries.size(); i++) {
					String entry = lazyEntries.get(i);
					byte[] rawEntry = lazyRawEntries.get(i);
					Resource resource = new Resource(new LazyZipEntryStream(ebookResourceHandler, entry), rawEntry);
					resources.add(resource);
				}
			} else {
				throw new IOException("Zip entries not even");
			}

			final Book epub = reader.readEpub(resources, StringUtil.UTF_8, ebookResourceHandler.getName());
			return epub;
		} finally {
			IOUtils.closeQuietly(zipData);
		}
	}

	/**
	 * Zip file filter that collects all zip file entries and support lazy handling for
	 * having not all files to be extracted. Only these files will be extracted which
	 * are commonly used by the {@link EpubReader}.
	 */
	private static class EpubZipFileFilter implements FileEntryFilter {

		boolean lazy = false;

		List<String> lazyEntries = new ArrayList<>();

		List<byte[]> lazyRawEntries = new ArrayList<byte[]>();

		EpubZipFileFilter(boolean lazy) {
			this.lazy = lazy;
		}

		@Override
		public boolean accept(String entry, byte[] rawEntry) {
			boolean accept = true;
			if(lazy) {
				String lowerCaseEntry = entry.toLowerCase();
				if(lowerCaseEntry.endsWith("/container.xml")) {
					accept = true;
				} else if(lowerCaseEntry.endsWith(".opf")) {
					accept = true;
				} else if(lowerCaseEntry.endsWith(".ncx")) {
					accept = true;
				} else if(lowerCaseEntry.endsWith("cover.jpg") || lowerCaseEntry.endsWith("cover.jpeg")) {
					accept = true;
				} else {
					accept = false;
				}
			}

			if(!accept) {
				lazyEntries.add(entry);
				lazyRawEntries.add(rawEntry);
			}
			return accept;
		}

		public List<String> getLazyEntries() {
			return this.lazyEntries;
		}

		public List<byte[]> getLazyRawEntries() {
			return this.lazyRawEntries;
		}
	}
}
