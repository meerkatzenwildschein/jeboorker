package org.rr.jeborker.metadata;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Meta;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Relator;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.ZipUtils.ZipDataEntry;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class EPubLibMetadataReader extends AEpubMetadataHandler implements IMetadataReader {
	
	EPubLibMetadataReader(IResourceHandler ebookResourceHandler) {
		super(ebookResourceHandler);
	}

	@Override
	public List<MetadataProperty> readMetaData() {
		final IResourceHandler ebookResourceHandler = getEbookResource().get(0);
		final EpubReader reader = new EpubReader();
		
		try {
			final byte[] zipData = this.getContent(ebookResourceHandler);
			final Book epub = reader.readEpub(new ByteArrayInputStream(zipData), ebookResourceHandler.getName());
			final Metadata metadata = epub.getMetadata();
			
			final List<MetadataProperty> metadataList = this.createMetadataList(metadata);
			return metadataList;
		} catch (Exception e) {
			LoggerFactory.logWarning(this.getClass(), "Could not read metadata for epub " + ebookResourceHandler, e);
		} finally {
			ebookResourceHandler.dispose();
		}
		return new ArrayList<MetadataProperty>(0);
	}

	/**
	 * Read all metadata entries from the given {@link Metadata} instance into {@link EpubLibMetadataProperty}.
	 * @param metadata The metadata instance where the entries read from.
	 * @return All available metadata from teh given {@link Metadata} instance.
	 */
	private List<MetadataProperty> createMetadataList(final Metadata metadata) {
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>();
		
		List<Author> authors = metadata.getAuthors();
		for (Author author : authors) {
			EpubLibMetadataProperty<Author> epubLibMetadataProperty = new EpubLibMetadataProperty<Author>(EPUB_METADATA_TYPES.AUTHOR.getName() , (author.getFirstname() + " " + author.getLastname()).trim(), author);
			result.add(epubLibMetadataProperty);
		}
		
		List<String> titles = metadata.getTitles();
		for (String title : titles) {
			result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.TITLE.getName(), title, null));
		}
		
		List<String> descriptions = metadata.getDescriptions();
		for (String description : descriptions) {
			result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.DESCRIPTION.getName(), description, null));
		}		
		
		List<String> publishers = metadata.getPublishers();
		for (String publisher : publishers) {
			result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.PUBLISHER.getName(), publisher, null));
		}
		
		List<String> rights = metadata.getRights();
		for (String right : rights) {
			result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.RIGHTS.getName(), right, null));
		}

		List<String> subjects = metadata.getSubjects();
		for (String subject : subjects) {
			result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.SUBJECT.getName(), subject, null));
		}
		
		List<String> types = metadata.getTypes();
		for (String type : types) {
			result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.TYPE.getName(), type, null));
		}
		
		List<Author> contributors = metadata.getContributors();
		for (Author contributor : contributors) {
			Relator relator = contributor.getRelator();
			if(relator != null && relator.getName().equalsIgnoreCase("CREATOR")) {
				result.add(new EpubLibMetadataProperty<Author>(EPUB_METADATA_TYPES.CREATOR.getName(), contributor.getFirstname() + contributor.getLastname(), contributor));
			} else {
				result.add(new EpubLibMetadataProperty<Author>(EPUB_METADATA_TYPES.CONTRIBUTOR.getName(), contributor.getFirstname() + contributor.getLastname(), contributor));				
			}
		}
		
		List<nl.siegmann.epublib.domain.Date> dates = metadata.getDates();
		for (nl.siegmann.epublib.domain.Date date : dates) {
			result.add(new EpubLibMetadataProperty<nl.siegmann.epublib.domain.Date>(EPUB_METADATA_TYPES.DATE.getName(), date.toString(), date));
		}
		
		List<Identifier> identifiers = metadata.getIdentifiers();
		for (Identifier identifier : identifiers) {
			EpubLibMetadataProperty<Identifier> epubLibMetadataProperty;
			if("uuid".equalsIgnoreCase(identifier.getScheme())) {
				epubLibMetadataProperty = new EpubLibMetadataProperty<Identifier>(EPUB_METADATA_TYPES.UUID.getName(), identifier.getValue(), identifier);
			} else if("isbn".equalsIgnoreCase(identifier.getScheme())) {
				epubLibMetadataProperty = new EpubLibMetadataProperty<Identifier>(EPUB_METADATA_TYPES.ISBN.getName(), identifier.getValue(), identifier);
			} else {
				epubLibMetadataProperty = new EpubLibMetadataProperty<Identifier>(EPUB_METADATA_TYPES.IDENTIFIER.getName(), identifier.getValue(), identifier);	
			}
			result.add(epubLibMetadataProperty);
		}
		
		Map<QName, String> otherProperties = metadata.getOtherProperties();
		for (Map.Entry<QName, String> e : otherProperties.entrySet()){
		    QName key = e.getKey();
		    String value = e.getValue();
		    result.add(new EpubLibMetadataProperty<QName>(key.getPrefix() + ":" + key.getLocalPart(), value, key));
		}		
		
		List<Meta> otherMeta = metadata.getOtherMeta();
		for(Meta meta : otherMeta) {
			result.add(new EpubLibMetadataProperty<Meta>(meta.getName(), meta.getContent(), meta));
		}
		
		String format = metadata.getFormat();
		if(format != null && !format.isEmpty()) {
			result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.FORMAT.getName(), format, null));
		}
		
		String language = metadata.getLanguage();
		if(language != null && !language.isEmpty()) {
			result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.LANGUAGE.getName(), language, null));
		}
		
		return result;
	}


	/**
	 * Searches the cover image in the zip data, extracts it and put it into the given {@link EbookPropertyItem}.
	 * 
	 * @param zipData
	 *            The epub zip data.
	 * @param item
	 *            The item to be setup
	 * @param metadataNode
	 *            The metadata node possibly containing some hints where the cover is.
	 */
	public byte[] getCover() {
		final IResourceHandler ebookResourceHandler = getEbookResource().get(0);
		final EpubReader reader = new EpubReader();
		
		byte[] result = null;
		try {
			final Book epub = reader.readEpub(ebookResourceHandler.getContentInputStream(), ebookResourceHandler.getName());
			final Resource coverImage = epub.getCoverImage();
			if(coverImage != null) {
				final byte[] data = coverImage.getData();
				result = data;
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "Could not get cover for " + ebookResourceHandler, e);
		}
		
		if(result == null) {
			result = getCoverDirty();
		}
		return result;
	}
	
	/**
	 * Searches the cover image in the zip data, extracts it and put it into the given {@link EbookPropertyItem}.
	 * 
	 * @param zipData
	 *            The epub zip data.
	 * @param item
	 *            The item to be setup
	 * @param metadataNode
	 *            The metadata node possibly containing some hints where the cover is.
	 */
	public byte[] getCoverDirty() {
		final IResourceHandler ebookResourceHandler = getEbookResource().get(0);
		try {
			final byte[] zipData = this.getContent(ebookResourceHandler);
			final byte[] containerXmlData = getContainerOPF(zipData);
			final Document document = getDocument(containerXmlData, ebookResourceHandler);
			if(document!=null) {
				final Element metadataNode = this.getMetadataElement(document);
				if(metadataNode != null) {
					final Element manifestNode = this.getManifestElement(document);
		
					final String coverNameReference = findMetadataCoverNameReference(metadataNode, document);
					final String coverName = findManifestCoverName(manifestNode, coverNameReference, document);
					final ZipDataEntry extract = extractCoverFromZip(zipData, coverName != null ? coverName : coverNameReference);
					if (extract != null && extract.data != null && extract.data.length > 0) {
						return extract.data;
					}
				}
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "Could not get cover for " + ebookResourceHandler, e);
		}
		return null;
	}	

	@Override
	public void fillEbookPropertyItem(final List<MetadataProperty> metadataProperties, final EbookPropertyItem item) {
		for(MetadataProperty metadataProperty : metadataProperties) {
			for(EPUB_METADATA_TYPES type : EPUB_METADATA_TYPES.values()) {
				if(type.getName().equals(metadataProperty.getName())) {
					type.fillItem(metadataProperty, item);
					break;
				}
			}
		}
	}

	public void dispose() {
		super.dispose();
	}
	
	protected void finalize() throws Throwable {
		dispose();
	}

	@Override
	public List<MetadataProperty> getSupportedMetaData() {
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>();
		Author author = new Author("");
		author.setRelator(Relator.AUTHOR);
		result.add(new EpubLibMetadataProperty<Author>(EPUB_METADATA_TYPES.AUTHOR.getName(), "", author));
		
		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.TITLE.getName(), ""));
		
		Meta ageSuggestion = new Meta(EPUB_METADATA_TYPES.JB_AGE_SUGGESTION.getName(), "");
		result.add(new EpubLibMetadataProperty<Meta>(ageSuggestion.getName(), "", ageSuggestion));
		
		Meta keywords = new Meta(EPUB_METADATA_TYPES.JB_KEYWORDS.getName(), "");
		result.add(new EpubLibMetadataProperty<Meta>(keywords.getName(), "", keywords));
		
		Meta rating = new Meta(EPUB_METADATA_TYPES.CALIBRE_RATING.getName(), "");
		result.add(new EpubLibMetadataProperty<Meta>(rating.getName(), "", rating));
		
		Meta seriesIndex = new Meta(EPUB_METADATA_TYPES.CALIBRE_SERIES_INDEX.getName(), "");
		result.add(new EpubLibMetadataProperty<Meta>(seriesIndex.getName(), "", seriesIndex));
		
		Meta seriesName = new Meta(EPUB_METADATA_TYPES.CALIBRE_SERIES.getName(), "");
		result.add(new EpubLibMetadataProperty<Meta>(seriesName.getName(), "", seriesName));
		
		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.SUBJECT.getName(), ""));
		
		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.PUBLISHER.getName(), ""));
		
		Identifier identifier = new Identifier("uuid", UUID.randomUUID().toString());
		result.add(new EpubLibMetadataProperty<Identifier>(EPUB_METADATA_TYPES.UUID.getName(), identifier.getValue(), identifier));
		
		Identifier isbn = new Identifier("isbn", "");
		result.add(new EpubLibMetadataProperty<Identifier>(EPUB_METADATA_TYPES.ISBN.getName(), "", isbn));
		
		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.RIGHTS.getName(), ""));
		
		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.LANGUAGE.getName(), ""));
		
		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.DESCRIPTION.getName(), ""));
		
		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.DATE.getName(), ""));
		
		Author creator = new Author("");
		creator.setRelator(Relator.CREATOR);		
		result.add(new EpubLibMetadataProperty<Author>(EPUB_METADATA_TYPES.CREATOR.getName(), "", creator));
		
		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.TYPE.getName(), ""));
		
		Author contributor = new Author("");
		contributor.setRelator(Relator.CONTRIBUTOR);		
		result.add(new EpubLibMetadataProperty<Author>(EPUB_METADATA_TYPES.CONTRIBUTOR.getName(), "", contributor));
		
		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.FORMAT.getName(), ""));
		return result;
	}

	@Override
	public MetadataProperty createRatingMetaData() {
		return createSupportedMetadataProperty("calibre:rating");
	}

	private MetadataProperty createSupportedMetadataProperty(String tagName) {
		return new EpubLibMetadataProperty<Void>(tagName, "", null);
	}

	@Override
	public String getPlainMetaData() {
		try {
			final byte[] containerXmlData = getContainerOPF(getEbookResource().get(0));
			return new String(containerXmlData, "UTF-8");
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "Could not get plain metadata for " + getEbookResource(), e);
		}
		return null;
	}

	@Override
	public String getPlainMetaDataMime() {
		return "text/xml";
	}

	@Override
	public List<MetadataProperty> getMetadataByType(boolean create, List<MetadataProperty> props, IMetadataReader.METADATA_TYPES type) {
		MetadataProperty newProperty;
		switch(type) {
		case AUTHOR:
			newProperty = new MetadataProperty(EPUB_METADATA_TYPES.AUTHOR.getName(), "");
			break;
		case TITLE:
			newProperty = new MetadataProperty(EPUB_METADATA_TYPES.TITLE.getName(), "");
			break;
		case GENRE:
			newProperty = new MetadataProperty(EPUB_METADATA_TYPES.SUBJECT.getName(), "");
			break;
		case SERIES_NAME:
			newProperty = new MetadataProperty(EPUB_METADATA_TYPES.CALIBRE_SERIES.getName(), "");
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

}
