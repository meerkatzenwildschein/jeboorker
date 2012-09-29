package org.rr.jeborker.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.ZipUtils.ZipDataEntry;
import org.rr.jeborker.db.item.EbookKeywordItem;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class EPubMetadataReader extends AEpubMetadataHandler implements IMetadataReader {

	private static final String COVER = "cover";
	private static final String JB_AGE_SUGGESTION = "jeboorker:age_suggestion";
	private static final String JB_KEYWORDS = "jeboorker:keywords";
	private static final String CALIBRE_RATING = "calibre:rating";
	private static final String CALIBRE_SERIES_INDEX = "calibre:series_index";
	private static final String CALIBRE_SERIES = "calibre:series";
	private static final String SUBJECT = "subject";
	private static final String PUBLISHER = "publisher";
	private static final String IDENTIFIER = "identifier";
	private static final String RIGHTS = "rights";
	private static final String LANGUAGE = "language";
	private static final String DESCRIPTION = "description";
	private static final String TITLE = "title";
	private static final String DATE = "date";
	private static final String CREATOR = "creator";
	private static final String CREATOR_AUT = "creator / Aut";
	private static final String CREATOR_AUTHOR = "creator / Author";

	EPubMetadataReader(IResourceHandler ebookResourceHandler) {
		super(ebookResourceHandler);
	}

	@Override
	public List<MetadataProperty> readMetaData() {
		final IResourceHandler ebookResourceHandler = getEbookResource();
		try {
			final byte[] containerXmlData = getContainerOPF(ebookResourceHandler);

			// http://www.virtualuniversity.ch/software/java-xml/3-6.html
			final Document document = getDocument(containerXmlData, ebookResourceHandler);
			if (document != null) {
				final Element metadataNode = this.getMetadataElement(document);
				if (metadataNode != null) {
					List<MetadataProperty> result1 = this.createMetadataList(metadataNode);
					return result1;
				}
			} else {
				LoggerFactory.logWarning(this, "Could not read metadata for epub " + ebookResourceHandler, null);
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(this.getClass(), "Could not read metadata for epub " + ebookResourceHandler, e);
		}
		return new ArrayList<MetadataProperty>(0);
	}

	/**
	 * Creates a list of {@link MetadataProperty} from the given metadata element.
	 * 
	 * @param metadataElement
	 *            The {@link Element} containing the metadata to be read.
	 * @return A list with the metadata. Never returns <code>null</code>.
	 */
	private List<MetadataProperty> createMetadataList(final Element metadataElement) {
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>();
		final NodeList childNodes = ((Element) metadataElement).getElementsByTagName("*");
		final int length = childNodes.getLength();

		for (int i = 0; i < length; i++) {
			Element item = (Element) childNodes.item(i);
			if (item.getParentNode() == metadataElement) {
				EpubMetadataProperty epubMetadataProperty = new EpubMetadataProperty(item.getTagName(), item.getTextContent());

				final NamedNodeMap attributes = item.getAttributes();
				if (attributes != null) {
					for (int j = 0; j < attributes.getLength(); j++) {
						Node attribute = attributes.item(j);
						String nodeName = attribute.getNodeName();
						String nodeValue = attribute.getNodeValue();
						epubMetadataProperty.addAttribute(nodeName, nodeValue);
					}
				}
				this.setEditableFlag(epubMetadataProperty);
				result.add(epubMetadataProperty);
			}
		}

		return result;
	}

	private void setEditableFlag(EpubMetadataProperty epubMetadataProperty) {
		if (epubMetadataProperty.getName() == COVER) {
			epubMetadataProperty.setEditable(false);
		}
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
		final IResourceHandler ebookResourceHandler = getEbookResource();
		try {
			final byte[] zipData = this.getContent(ebookResourceHandler);
			final byte[] containerXmlData = getContainerOPF(zipData);
			final Document document = getDocument(containerXmlData, ebookResourceHandler);
			if(document!=null) {
				final Element metadataNode = this.getMetadataElement(document);
				final Element manifestNode = this.getManifestElement(document);
	
				final String coverNameReference = findMetadataCoverNameReference(metadataNode);
				final String coverName = findManifestCoverName(manifestNode, coverNameReference);
				final ZipDataEntry extract = extractCoverFromZip(zipData, coverName != null ? coverName : coverNameReference);
				if (extract != null && extract.data != null && extract.data.length > 0) {
					return extract.data;
				}
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "Could not get cover for " + ebookResourceHandler, e);
		}
		return null;
	}

	@Override
	public void fillEbookPropertyItem(final List<MetadataProperty> metadataProperties, final EbookPropertyItem item) {
		item.clearMetadata();
		for (MetadataProperty metadataProperty : metadataProperties) {
			final String name = metadataProperty.getName(); // The name is guaranteed to be interned.
			final Object value = metadataProperty.getValues().get(0);

			if (name == CREATOR || name == CREATOR_AUT || name == CREATOR_AUTHOR) {
				String opfRole = ((EpubMetadataProperty) metadataProperty).getAttributeValueByName("opf:role");
				if (opfRole == null || opfRole.startsWith("aut")) {
					// dc:creator opf:role=â€�autâ€� opf:file-as="#authorSort#" author
					item.setAuthor(value != null ? String.valueOf(value) : null);
					String authorSort = ((EpubMetadataProperty) metadataProperty).getAttributeValueByName("opf:file-as");
					item.setAuthorSort(authorSort);
				}
			} else if (name == DATE) {
				String opfEvent = ((EpubMetadataProperty) metadataProperty).getAttributeValueByName("opf:event");
				if (opfEvent == null || opfEvent.equals("publication")) {
					// <dc:date opf:event="publication">1891</dc:date>
					if (value instanceof Date) {
						item.setPublishingDate((Date) value);
					} else {
						item.setPublishingDate(value != null ? DateConversionUtils.toDate(String.valueOf(value)) : null);
					}
				} else if (opfEvent == null || opfEvent.equals("creation")) {
					// <dc:date opf:event="creation">2009-08-24</dc:date>
					if (value instanceof Date) {
						item.setPublishingDate((Date) value);
					} else {
						item.setCreationDate(value != null ? DateConversionUtils.toDate(String.valueOf(value)) : null);
					}
				}
			} else if (name == TITLE) {
				item.setTitle(value != null ? String.valueOf(value) : null);
			} else if (name == DESCRIPTION) {
				item.setDescription(value != null ? String.valueOf(value) : null);
			} else if (name == LANGUAGE) {
				item.setLanguage(value != null ? String.valueOf(value) : null);
			} else if (name == RIGHTS) {
				item.setRights(value != null ? String.valueOf(value) : null);
			} else if (name == IDENTIFIER) {
				String opfRole = ((EpubMetadataProperty) metadataProperty).getAttributeValueByName("opf:scheme");
				if (opfRole != null && opfRole.equalsIgnoreCase("isbn")) {
					// //<dc:identifier opf:scheme="ISBN">xxxxxxxxxxxx</dc:identifier>
					// //<dc:identifier id="ISBN" opf:scheme="ISBN">urn:isbn:xxx-x-xxxx-xxxx-x</dc:identifier>
					item.setIsbn(value != null ? String.valueOf(value) : null);
				} else if (opfRole != null && opfRole.equalsIgnoreCase("uuid")) {
					// //<dc:identifier id="uuid_id" opf:scheme="uuid">xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx</dc:identifier>
					// //<dc:identifier id="BookID" opf:scheme="UUID">xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx</dc:identifier>
					item.setUuid(value != null ? String.valueOf(value) : null);
				}
			} else if (name == PUBLISHER) {
				item.setPublisher(value != null ? String.valueOf(value) : null);
			} else if (name == SUBJECT) {
				item.setGenre(value != null ? String.valueOf(value) : null);
			} else if (name.equalsIgnoreCase(CALIBRE_SERIES)) {
				// <meta name="calibre:series" content="Lord of the Rings Trilogy"/>
				item.setSeriesName(value != null ? String.valueOf(value) : null);
			} else if (name.equalsIgnoreCase(CALIBRE_SERIES_INDEX)) {
				// <meta name="calibre:series_index" content="1"/>
				item.setSeriesIndex(value != null ? String.valueOf(value) : null);
			} else if (name.equalsIgnoreCase(CALIBRE_RATING)) {
				// <opf:meta name="calibre:rating" content="8"/>
				item.setRating(CommonUtils.toNumber(metadataProperty.getValueAsString()).intValue());
			} else if (name.equalsIgnoreCase(JB_KEYWORDS)) {
				// <meta name="jb:keywords" content="foo, bar"/>
				if(value != null) {
					List<String> keywords = ListUtils.split(String.valueOf(value), ",");
					List<EbookKeywordItem> asEbookKeywordItem = EbookPropertyItemUtils.getAsEbookKeywordItem(keywords);
					item.setKeywords(asEbookKeywordItem);
				} else {
					item.setKeywords(null);
				}
			} else if (name.equalsIgnoreCase(JB_AGE_SUGGESTION)) {
				// <meta name="jb:age_suggestion" content="13-15"/>
				item.setAgeSuggestion(value != null ? String.valueOf(value) : null);
			}
		}
	}

	public void dispose() {
		super.dispose();
	}

	@Override
	public List<MetadataProperty> getSupportedMetaData() {
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>();
		result.add(createSupportedMetadataProperty("dc:title"));
		result.add(createSupportedMetadataProperty("dc:subject"));
		result.add(createSupportedMetadataProperty("dc:creator", new String[] { "opf:role" }, new String[] { "aut" }));
		result.add(createSupportedMetadataProperty("dc:rights"));
		result.add(createSupportedMetadataProperty("dc:publisher"));
		result.add(createSupportedMetadataProperty("dc:description"));
		result.add(createSupportedMetadataProperty("dc:date", new String[] { "opf:event" }, new String[] { "publication" }));
		result.add(createSupportedMetadataProperty("dc:language"));
		result.add(createSupportedMetadataProperty("dc:identifier", new String[] { "id", "opf:scheme" }, new String[] { "uuid_id", "uuid" }));
		result.add(createSupportedMetadataProperty("dc:identifier", new String[] { "opf:scheme" }, new String[] { "ISBN" }));
		result.add(createSupportedMetadataProperty("meta", new String[] { "name", "content" }, new String[] { CALIBRE_SERIES, "" }));
		result.add(createSupportedMetadataProperty("meta", new String[] { "name", "content" }, new String[] { CALIBRE_SERIES_INDEX, "" }));
		result.add(createSupportedMetadataProperty("meta", new String[] { "name", "content" }, new String[] { CALIBRE_RATING, "" }));
		result.add(createSupportedMetadataProperty("meta", new String[] { "name", "content" }, new String[] { JB_KEYWORDS, "" }));
		result.add(createSupportedMetadataProperty("meta", new String[] { "name", "content" }, new String[] { JB_AGE_SUGGESTION, "" }));

		return result;
	}

	@Override
	public MetadataProperty createRatingMetaData() {
		return createSupportedMetadataProperty("meta", new String[] { "name", "content" }, new String[] { CALIBRE_RATING, "" });
	}

	private MetadataProperty createSupportedMetadataProperty(String tagName) {
		return createSupportedMetadataProperty(tagName, null, null);
	}

	private MetadataProperty createSupportedMetadataProperty(String tagName, String[] attributeNames, String[] attributeValues) {
		EpubMetadataProperty epubMetadataProperty = new EpubMetadataProperty(tagName, "");
		if (attributeNames != null && attributeValues != null) {
			for (int i = 0; i < attributeValues.length; i++) {
				epubMetadataProperty.addAttribute(attributeNames[i], attributeValues[i]);
			}
		}
		return epubMetadataProperty;
	}

	@Override
	public String getPlainMetaData() {
		try {
			final byte[] containerXmlData = getContainerOPF(getEbookResource());
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

	private List<MetadataProperty> getAuthorMetaData(boolean create, List<MetadataProperty> props) {
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>(2);
		final List<MetadataProperty> metadataProperties;
		if (props != null) {
			metadataProperties = props;
		} else {
			metadataProperties = readMetaData();
		}

		for (MetadataProperty property : metadataProperties) {
			if (property.getName().startsWith("creator")) {
				if (((EpubMetadataProperty) property).getAttributeValueByName("opf:role") != null) {
					if (((EpubMetadataProperty) property).getAttributeValueByName("opf:role").startsWith("aut")) {
						// opf:role = auth is the author.
						result.add(property);
					}
				} else {
					// no attribute. Good chance it's the author
					result.add(property);
				}
			}
		}

		// if the list is empty and a new property should be created, add a new, empty author property to the result.
		if (create && result.isEmpty()) {
			MetadataProperty authorProperty = createSupportedMetadataProperty("dc:creator", new String[] { "opf:role" }, new String[] { "aut" });
			result.add(authorProperty);
		}
		return Collections.unmodifiableList(result);
	}
	
	@Override
	public List<MetadataProperty> getMetaDataByType(boolean create, List<MetadataProperty> props, METADATA_TYPES type) {
		final String tag;
		final String name;
		
		switch(type) {
			case GENRE:
				tag = "dc:subject";
				name = SUBJECT;
				break;
			case TITLE:
				tag = "dc:title";
				name = TITLE;
				break;	
			case SERIES_NAME:
				tag = CALIBRE_SERIES;
				name = CALIBRE_SERIES;
				break;					
			case AUTHOR:
				return getAuthorMetaData(create, props);
			default:
				return null;
		}
		
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>(2);
		final List<MetadataProperty> metadataProperties;
		if (props != null) {
			metadataProperties = props;
		} else {
			metadataProperties = readMetaData();
		}

		for (MetadataProperty property : metadataProperties) {
			if (property.getName() == name) {
				result.add(property);
			}
		}

		// if the list is empty and a new property should be created, add a new, empty subject property to the result.
		if (create && result.isEmpty()) {
			MetadataProperty subjectProperty = createSupportedMetadataProperty(tag);
			result.add(subjectProperty);
		}
		return Collections.unmodifiableList(result);
	}

}
