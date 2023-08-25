package org.rr.jeborker.metadata;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.cell.DatePropertyCellEditor;
import org.rr.jeborker.gui.cell.DatePropertyCellRenderer;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Date;
import nl.siegmann.epublib.domain.Date.Event;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Meta;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Relator;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;

class EPubLibMetadataReader extends AEpubMetadataHandler implements IMetadataReader {

	EPubLibMetadataReader(IResourceHandler ebookResourceHandler) {
		super(ebookResourceHandler);
	}

	@Override
	public List<MetadataProperty> readMetadata() {
		final IResourceHandler ebookResourceHandler = getEbookResource().get(0);

		try {
			final Book epub = readBook(ebookResourceHandler.getContentInputStream(), ebookResourceHandler, true);
			final Metadata metadata = epub.getMetadata();

			final List<MetadataProperty> metadataList = this.createMetadataList(epub, metadata);
			return metadataList;
		} catch (Throwable e) {
			LoggerFactory.logWarning(getClass(), "Could not read metadata for epub " + ebookResourceHandler, e);
		} finally {
			ebookResourceHandler.dispose();
		}
		return new ArrayList<MetadataProperty>(0);
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
						if(!StringUtil.isEmpty(value != null ? String.valueOf(value) : null)) {
							return false;
						}
					}
				}
				return true;
			}

		};

		List<Author> authors = metadata.getAuthors();
		for (Author author : authors) {
			EpubLibMetadataProperty<Author> epubLibMetadataProperty = new EpubLibMetadataProperty<>(EPUB_METADATA_TYPES.AUTHOR.getName() , (author.getFirstname() + " " + author.getLastname()).trim(), author);
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
			EpubLibMetadataProperty<Date> property;
			if(event != null && Event.PUBLICATION.equals(event)) {
				property = new EpubLibMetadataProperty<nl.siegmann.epublib.domain.Date>(EPUB_METADATA_TYPES.PUBLICATION_DATE.getName(), date.getValue(), date);
			} else if(event != null && Event.CREATION.equals(event)) {
				property = new EpubLibMetadataProperty<nl.siegmann.epublib.domain.Date>(EPUB_METADATA_TYPES.CREATION_DATE.getName(), date.getValue(), date);
			} else if(event != null && Event.MODIFICATION.equals(event)) {
				property = new EpubLibMetadataProperty<nl.siegmann.epublib.domain.Date>(EPUB_METADATA_TYPES.MODIFICATION_DATE.getName(), date.getValue(), date);
			} else {
				property = new EpubLibMetadataProperty<nl.siegmann.epublib.domain.Date>(EPUB_METADATA_TYPES.DATE.getName(), date.getValue(), date);
			}
			property.setPropertyRendererClass(DatePropertyCellRenderer.class);
			property.setPropertyEditorClass(DatePropertyCellEditor.class);
			result.add(property);
		}

		List<Identifier> identifiers = metadata.getIdentifiers();
		for (Identifier identifier : identifiers) {
			EpubLibMetadataProperty<Identifier> epubLibMetadataProperty;
			if("uuid".equalsIgnoreCase(identifier.getScheme())) {
				epubLibMetadataProperty = new EpubLibMetadataProperty<>(EPUB_METADATA_TYPES.UUID.getName(), identifier.getValue(), identifier);
			} else if("isbn".equalsIgnoreCase(identifier.getScheme())) {
				epubLibMetadataProperty = new EpubLibMetadataProperty<>(EPUB_METADATA_TYPES.ISBN.getName(), identifier.getValue(), identifier);
			} else {
				epubLibMetadataProperty = new EpubLibMetadataProperty<>(EPUB_METADATA_TYPES.IDENTIFIER.getName(), identifier.getValue(), identifier);
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
			//no need to read the cover meta entry. It's already provided by the Reader and added later.
			if(IMetadataReader.COMMON_METADATA_TYPES.COVER.getName().equalsIgnoreCase(meta.getName())) {
				continue;
			}
			EpubLibMetadataProperty<Meta> property = new EpubLibMetadataProperty<Meta>(meta.getName(), meta.getContent(), meta);
			if(StringUtils.equals(property.getName(), "calibre:timestamp")) {
				property.setPropertyRendererClass(DatePropertyCellRenderer.class);
				property.setPropertyEditorClass(DatePropertyCellEditor.class);
			}
			result.add(property);
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
	 * @return The desired cover image bytes or <code>null</code> if no cover could be found.
	 */
	private byte[] searchCoverImage(Book epub) {
		Resources resources = epub.getResources();
		Collection<String> allHrefs = resources.getAllHrefs();
		for(String href : allHrefs) {
			if(new File(href).getName().toLowerCase().contains("cover") && (href.endsWith(".jpg") || href.endsWith(".jpeg"))) {
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
	public List<MetadataProperty> getSupportedMetadata() {
		final ArrayList<MetadataProperty> result = new ArrayList<>();
		Author author = new Author(EMPTY);
		author.setRelator(Relator.AUTHOR);
		result.add(new EpubLibMetadataProperty<Author>(EPUB_METADATA_TYPES.AUTHOR.getName(), EMPTY, author));

		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.TITLE.getName(), EMPTY));

		Meta ageSuggestion = new Meta(EPUB_METADATA_TYPES.JB_AGE_SUGGESTION.getName(), EMPTY);
		result.add(new EpubLibMetadataProperty<Meta>(ageSuggestion.getName(), EMPTY, ageSuggestion));

		Meta keywords = new Meta(EPUB_METADATA_TYPES.JB_KEYWORDS.getName(), EMPTY);
		result.add(new EpubLibMetadataProperty<Meta>(keywords.getName(), EMPTY, keywords));

		Meta rating = new Meta(EPUB_METADATA_TYPES.CALIBRE_RATING.getName(), EMPTY);
		result.add(new EpubLibMetadataProperty<Meta>(rating.getName(), EMPTY, rating));

		Meta seriesIndex = new Meta(EPUB_METADATA_TYPES.CALIBRE_SERIES_INDEX.getName(), EMPTY);
		result.add(new EpubLibMetadataProperty<Meta>(seriesIndex.getName(), EMPTY, seriesIndex));

		Meta seriesName = new Meta(EPUB_METADATA_TYPES.CALIBRE_SERIES.getName(), EMPTY);
		result.add(new EpubLibMetadataProperty<Meta>(seriesName.getName(), EMPTY, seriesName));

		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.SUBJECT.getName(), EMPTY));

		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.PUBLISHER.getName(), EMPTY));

		Identifier identifier = new Identifier("uuid", UUID.randomUUID().toString());
		result.add(new EpubLibMetadataProperty<Identifier>(EPUB_METADATA_TYPES.UUID.getName(), identifier.getValue(), identifier));

		Identifier isbn = new Identifier("isbn", EMPTY);
		result.add(new EpubLibMetadataProperty<Identifier>(EPUB_METADATA_TYPES.ISBN.getName(), EMPTY, isbn));

		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.RIGHTS.getName(), EMPTY));

		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.LANGUAGE.getName(), EMPTY));

		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.DESCRIPTION.getName(), EMPTY));

		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.DATE.getName(), EMPTY));

		Author creator = new Author(EMPTY);
		creator.setRelator(Relator.CREATOR);
		result.add(new EpubLibMetadataProperty<Author>(EPUB_METADATA_TYPES.CREATOR.getName(), EMPTY, creator));

		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.TYPE.getName(), EMPTY));

		Author contributor = new Author(EMPTY);
		contributor.setRelator(Relator.CONTRIBUTOR);
		result.add(new EpubLibMetadataProperty<Author>(EPUB_METADATA_TYPES.CONTRIBUTOR.getName(), EMPTY, contributor));

		result.add(new EpubLibMetadataProperty<Void>(EPUB_METADATA_TYPES.FORMAT.getName(), EMPTY));
		return result;
	}

	@Override
	public String getPlainMetadata() {
		try {
			final byte[] containerXmlData = getContainerOPF(getEbookResource().get(0));
			return new String(containerXmlData, StandardCharsets.UTF_8);
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "Could not get plain metadata for " + getEbookResource(), e);
		}
		return null;
	}

	@Override
	public String getPlainMetadataMime() {
		return "text/xml";
	}

	@Override
	public List<MetadataProperty> getMetadataByType(boolean create, List<MetadataProperty> props, IMetadataReader.COMMON_METADATA_TYPES type) {
		MetadataProperty newProperty;
		switch(type) {
		case AUTHOR:
			Author author = new Author(EMPTY);
			author.setRelator(Relator.AUTHOR);
			newProperty = new EpubLibMetadataProperty<>(EPUB_METADATA_TYPES.AUTHOR.getName(), EMPTY, author);
			break;
		case TITLE:
			newProperty = new EpubLibMetadataProperty<>(EPUB_METADATA_TYPES.TITLE.getName(), EMPTY);
			break;
		case GENRE:
			newProperty = new EpubLibMetadataProperty<>(EPUB_METADATA_TYPES.SUBJECT.getName(), EMPTY);
			break;
		case SERIES_NAME:
			Meta seriesName = new Meta(EPUB_METADATA_TYPES.CALIBRE_SERIES.getName(), EMPTY);
			newProperty = new EpubLibMetadataProperty<>(seriesName.getName(), EMPTY, seriesName);
			break;
		case RATING:
			Meta ratingName = new Meta(EPUB_METADATA_TYPES.CALIBRE_RATING.getName(), EMPTY);
			newProperty = new EpubLibMetadataProperty<>(ratingName.getName(), EMPTY, ratingName);
			break;
		case AGE_SUGGESTION:
			Meta ageSuggestionName = new Meta(EPUB_METADATA_TYPES.JB_AGE_SUGGESTION.getName(), EMPTY);
			newProperty = new EpubLibMetadataProperty<>(ageSuggestionName.getName(), EMPTY, ageSuggestionName);
			break;
		case DESCRIPTION:
			newProperty = new EpubLibMetadataProperty<>(EPUB_METADATA_TYPES.DESCRIPTION.getName(), EMPTY);
			break;
		case ISBN:
			Identifier isbn = new Identifier("isbn", EMPTY);
			newProperty = new EpubLibMetadataProperty<>(EPUB_METADATA_TYPES.ISBN.getName(), EMPTY, isbn);
			break;
		case LANGUAGE:
			newProperty = new EpubLibMetadataProperty<>(EPUB_METADATA_TYPES.LANGUAGE.getName(), EMPTY);
			break;
		case COVER:
			newProperty = new EpubLibMetadataProperty<>(EPUB_METADATA_TYPES.COVER.getName(), null, null);
			break;
		default: newProperty = null;
		}

		final ArrayList<MetadataProperty> result = new ArrayList<>();
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
