package org.rr.jeborker.metadata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Date.Event;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Meta;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Relator;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.epub.EpubReader;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.zip.ZipUtils;
import org.rr.commons.utils.zip.ZipUtils.ZipDataEntry;
import org.rr.jeborker.db.item.EbookPropertyItem;

class EPubLibMetadataReader extends AEpubMetadataHandler implements IMetadataReader {
	
	EPubLibMetadataReader(IResourceHandler ebookResourceHandler) {
		super(ebookResourceHandler);
	}

	@Override
	public List<MetadataProperty> readMetaData() {
		final IResourceHandler ebookResourceHandler = getEbookResource().get(0);
		
		try {
			final byte[] zipData = this.getContent(ebookResourceHandler);
			final Book epub = readBook(zipData, ebookResourceHandler);
			final Metadata metadata = epub.getMetadata();
			
			final List<MetadataProperty> metadataList = this.createMetadataList(epub, metadata);
			return metadataList;
		} catch (Throwable e) {
			LoggerFactory.logWarning(this.getClass(), "Could not read metadata for epub " + ebookResourceHandler, e);
		} finally {
			ebookResourceHandler.dispose();
		}
		return new ArrayList<MetadataProperty>(0);
	}
	
	/**
	 * Read all entries from, the given zip data and creates a {@link Book} instance from them. 
	 * @throws IOException
	 */
	private Book readBook(final byte[] zipData, final IResourceHandler ebookResourceHandler) throws IOException {
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

	/**
	 * Read all metadata entries from the given {@link Metadata} instance into {@link EpubLibMetadataProperty}.
	 * @param metadata The metadata instance where the entries read from.
	 * @return All available metadata from teh given {@link Metadata} instance.
	 * @throws IOException 
	 */
	private List<MetadataProperty> createMetadataList(final Book epub, final Metadata metadata) throws IOException {
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>() {

			@Override
			public boolean add(MetadataProperty e) {
				List<Object> values = e.getValues();
				if(!isListEmpty(values)) {
					return super.add(e);
				}
				return false;
			}
			
			private boolean isListEmpty(List<Object> values) {
				if(values != null && !values.isEmpty()) {
					for(Object value : values) {
						if(!StringUtils.isEmpty(value != null ? String.valueOf(value) : null)) {
							return false;
						}
					}
				}	
				return true;
			}
			
		};
		
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
			Event event = date.getEvent();
			if(event != null && Event.PUBLICATION.equals(event)) {
				result.add(new EpubLibMetadataProperty<nl.siegmann.epublib.domain.Date>(EPUB_METADATA_TYPES.PUBLICATION_DATE.getName(), date.getValue(), date));	
			} else if(event != null && Event.CREATION.equals(event)) {
				result.add(new EpubLibMetadataProperty<nl.siegmann.epublib.domain.Date>(EPUB_METADATA_TYPES.CREATION_DATE.getName(), date.getValue(), date));	
			} else if(event != null && Event.MODIFICATION.equals(event)) {
				result.add(new EpubLibMetadataProperty<nl.siegmann.epublib.domain.Date>(EPUB_METADATA_TYPES.MODIFICATION_DATE.getName(), date.getValue(), date));	
			} else {
				result.add(new EpubLibMetadataProperty<nl.siegmann.epublib.domain.Date>(EPUB_METADATA_TYPES.DATE.getName(), date.getValue(), date));
			}
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
			if("cover".equals(meta.getName().toLowerCase())) {
				continue;
			}
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
		
		final Resource coverImage = epub.getCoverImage();
		final byte[] data;
		if(coverImage != null) {
			data = coverImage.getData();
		} else {
			data = searchCoverImage(epub);
		}
		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.COVER.getName(), data, null));
		
		return new ArrayList<MetadataProperty>(result);
	}

	/**
	 * Searches the given {@link Book} for an image which seems to be the cover image.
	 * @return The desired cover image bytes or <code>null</code> if nbo cover could be found.
	 */
	public byte[] searchCoverImage(Book epub) {
		Resources resources = epub.getResources();
		Collection<String> allHrefs = resources.getAllHrefs();
		for(String href : allHrefs) {
			if(new File(href).getName().toLowerCase().indexOf("cover") != -1 && (href.endsWith(".jpg") || href.endsWith(".jpeg"))) {
				Resource resourcerByHref = resources.getByHref(href);
				if(resourcerByHref != null) {
					try {
						byte[] data = resourcerByHref.getData();
						if(data != null && data.length > 0) {
							return resourcerByHref.getData();
						}
					} catch (IOException e) {
						break;
					}
				}
			}
		}
		
		return null;
	}	

	@Override
	public void fillEbookPropertyItem(final List<MetadataProperty> metadataProperties, final EbookPropertyItem item) {
		item.clearMetadata();
		for(MetadataProperty metadataProperty : metadataProperties) {
			for(EPUB_METADATA_TYPES type : EPUB_METADATA_TYPES.values()) {
				if(type.getName().equals(metadataProperty.getName())) {
					type.fillItem(metadataProperty, item);
					break;
				}
			}
		}
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
			Author author = new Author("");
			author.setRelator(Relator.AUTHOR);			
			newProperty = new EpubLibMetadataProperty<Author>(EPUB_METADATA_TYPES.AUTHOR.getName(), "", author);
			break;
		case TITLE:
			newProperty = new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.TITLE.getName(), "");
			break;
		case GENRE:
			newProperty = new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.SUBJECT.getName(), "");
			break;
		case SERIES_NAME:
			Meta seriesName = new Meta(EPUB_METADATA_TYPES.CALIBRE_SERIES.getName(), "");
			newProperty = new EpubLibMetadataProperty<Meta>(seriesName.getName(), "", seriesName);
			break;
		case RATING:
			Meta ratingName = new Meta(EPUB_METADATA_TYPES.CALIBRE_RATING.getName(), "");
			newProperty = new EpubLibMetadataProperty<Meta>(ratingName.getName(), "", ratingName);
			break;	
		case COVER:
			newProperty = new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.COVER.getName(), null, null);
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
