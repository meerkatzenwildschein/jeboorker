package org.rr.jeborker.metadata;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jempbox.xmp.Thumbnail;
import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchema;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.Base64;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.HTMLEntityConverter;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.metadata.pdf.PDFDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


class PDFCommonMetadataReader extends APDFCommonMetadataHandler implements IMetadataReader {

	private IResourceHandler ebookResource;

	private PDFDocument pdfDoc;

	PDFCommonMetadataReader(final IResourceHandler ebookResource) {
		this.ebookResource = ebookResource;
		this.pdfDoc = PDFDocument.getPDFCommonDocumentInstance(PDFDocument.ITEXT, ebookResource);
	}

	@Override
	public List<IResourceHandler> getEbookResource() {
		return Collections.singletonList(this.ebookResource);
	}

	@Override
	public List<MetadataProperty> readMetadata() {
		try {
			final ArrayList<MetadataProperty> result = new ArrayList<>();
			final byte[] xmpMetadataBytes = getXmpMetadata();
			if(xmpMetadataBytes != null) {
				final Document document = getDocument(xmpMetadataBytes, ebookResource);
				final XMPMetadata metadata = document != null ? new XMPMetadata(document) : new XMPMetadata();

				List<XMPSchema> schemas = metadata.getSchemas();
				for (XMPSchema xmpSchema : schemas) {
	                this.addSchemaProperties(result, xmpSchema);
				}
			}

			final Map<String, String> pdfInfo = getInfo();
			if(pdfInfo != null) {
				for (Entry<String, String> entry : pdfInfo.entrySet()) {
					final String key = entry.getKey();
					final String value = entry.getValue();
					try {
						if(value != null && !value.trim().isEmpty()) { //no sense having empty entries.
							if(key.endsWith("Date") || key.endsWith("SourceModified")) {
								final Date dateValue = DateConversionUtils.toDate(value);
								if(dateValue != null) {
									result.add(new MetadataProperty(key, dateValue, Date.class));
								} else {
									result.add(new MetadataProperty(key, value));
								}
							} else {
								result.add(new MetadataProperty(key, value));
							}
						}
					} catch(Exception e) {
						LoggerFactory.logWarning(this, "could not handle property " + key + " and value " + value, e);
					}
				}
			} else {
				LoggerFactory.logWarning(this, "Could not get metadata from " + ebookResource, new RuntimeException("dumpstack"));
			}

			try {
				byte[] fetchThumbnail = fetchXMPThumbnail(ebookResource);
				if(fetchThumbnail == null) {
					fetchThumbnail = pdfDoc.fetchCoverFromPDFContent();
				}
				if(fetchThumbnail != null) {
					result.add(new MetadataProperty(IMetadataReader.COMMON_METADATA_TYPES.COVER.getName(), fetchThumbnail));
				}
			} catch (Exception e) {
				LoggerFactory.logWarning(getClass(), "Could not read cover for pdf " + ebookResource, e);
			}

			return result;
		} catch (Throwable e) {
			LoggerFactory.logWarning(getClass(), "Could not read metadata for pdf " + ebookResource, e);
		}
		return new ArrayList<MetadataProperty>(0);
	}

	private byte[] getXmpMetadata() {
		try {
			return pdfDoc.getXMPMetadata();
		} catch (IOException e) {
			LoggerFactory.logWarning(getClass(), "Could not read xmp metadata for pdf " + ebookResource, e);
		}
		return null;
	}

	private Map<String, String> getInfo() {
		try {
			return pdfDoc.getInfo();
		} catch (IOException e) {
			LoggerFactory.logWarning(getClass(), "Could not read info metadata for pdf " + ebookResource, e);
		}
		return null;
	}

	private void addSchemaProperties(final ArrayList<MetadataProperty> result, final XMPSchema schema) throws IOException {
		if(schema == null) {
			return;
		}

		final Element schemaElement = schema.getElement();
		final List<Element> schemaChildren = getChildren(schemaElement);
		for (Element schemaChild : schemaChildren) {
			final String tagName = schemaChild.getTagName();
			final List<Element> rdfChildren = getChildren(schemaChild); //Need to handle <rdf:Alt> or <rdf:Seq>
			if(!rdfChildren.isEmpty()) {
				if(rdfChildren.size()==1) {
					final Element rdfChild = rdfChildren.get(0);
					final String rdfChildName = rdfChild.getTagName();
					final PDFMetadataProperty pdfMetadataProperty = new PDFMetadataProperty(tagName, null, rdfChildName);
					final List<Element> valueChilds = getChildren(rdfChild);
					for (Element valueChild : valueChilds) {
						//<rdf:li> childs
						final PDFMetadataProperty valueChildProperty = new PDFMetadataProperty(valueChild.getTagName(), valueChild.getTextContent(), null);
						final NamedNodeMap attributes = valueChild.getAttributes();
						for (int i = 0; i < attributes.getLength(); i++) {
							final Node item = attributes.item(i);
							valueChildProperty.addAttribute(item.getNodeName(), item.getNodeValue());
						}
						pdfMetadataProperty.addChild(valueChildProperty);
					}
				}
			} else {
				Object value = schemaChild.getTextContent();
				if(tagName.endsWith("Date") || tagName.endsWith("SourceModified")) {
					final String stringValue = StringUtil.toString(value);
					if(stringValue.trim().isEmpty()) {
						continue; //no sense to add an empty Date.
					} else {
						try {
							Date dateValue = DateConversionUtils.toDate(stringValue);
							if(value != null && value.toString().isEmpty()) {
								//2004-12-11T00:00:+0Z
								value = dateValue;
							}
						} catch(java.lang.NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
				if(!StringUtil.toString(value).trim().isEmpty()) {
					final PDFMetadataProperty pdfMetadataProperty = new PDFMetadataProperty(tagName, value, null);
					result.add(pdfMetadataProperty);
				}
			}
		}
	}

	@Override
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item) {
		item.clearMetadata();
		List<MetadataProperty> authorMetadataProperty = new ArrayList<>(2);
		List<MetadataProperty> creatorMetadataProperty = new ArrayList<>(2);
		for (MetadataProperty metadataProperty : metadataProperties) {
			final String name = metadataProperty.getName().toLowerCase();
			if(name.equals("title")) {
				item.setTitle(metadataProperty.getValueAsString());
			} else if(name.equals("author")) {
				authorMetadataProperty.add(metadataProperty);
			} else if(authorMetadataProperty.isEmpty() && name.equals("creator")) {
				creatorMetadataProperty.add(metadataProperty);
			} else if(name.equals("keywords")) {
				List<String> keywords = ListUtils.split(metadataProperty.getValueAsString(), ",");
				item.setKeywords(keywords);
			} else if(name.equals("description")) {
				item.setDescription(metadataProperty.getValueAsString());
			} else if(name.equals("creationdate")) {
				item.setCreationDate(DateConversionUtils.toDate(metadataProperty.getValueAsString()));
			} else if(name.equals("subject")) {
				item.setGenre(metadataProperty.getValueAsString());
			} else if(name.equals("agesuggestion")) {
				item.setAgeSuggestion(metadataProperty.getValueAsString());
			} else if(name.equals("rating")) {
				Number number = CommonUtils.toNumber(metadataProperty.getValueAsString());
				item.setRating(number != null ? number.intValue() : null);
			} else if(name.equals("seriesindex")) {
				item.setSeriesIndex(metadataProperty.getValueAsString());
			} else if(name.equals("seriesname")) {
				item.setSeriesName(metadataProperty.getValueAsString());
			} else if(name.equals(IMetadataReader.COMMON_METADATA_TYPES.COVER.getName())) {
				IMetadataReader.COMMON_METADATA_TYPES.COVER.fillItem(metadataProperty, item);
			}
		}
		if(!authorMetadataProperty.isEmpty()) {
			for(MetadataProperty property : authorMetadataProperty) {
				COMMON_METADATA_TYPES.AUTHOR.fillItem(property, item);
			}
		} else {
			for(MetadataProperty property : creatorMetadataProperty) {
				COMMON_METADATA_TYPES.AUTHOR.fillItem(property, item);
			}
		}
	}

	/**
	 * Fetches the thumbnail from the xmp metadata.
	 * @param pdfReader The reader instance to be used to read the XMP data
	 * @return The thumbnail or <code>null</code> if not thumbnail is embedded.
	 * @throws Exception
	 */
	byte[] fetchXMPThumbnail(final IResourceHandler ebookResource) throws Exception {
		if(ebookResource == null) {
			return null;
		}
		final byte[] xmpMetadataBytes = pdfDoc.getXMPMetadata();
		byte[] result = null;

		if(XMPUtils.isValidXMP(xmpMetadataBytes)) {
			final Document document = getDocument(xmpMetadataBytes, ebookResource);
			final XMPMetadata xmp = new XMPMetadata(document);
			final XMPSchemaBasic xmpBasicSchema = xmp.getBasicSchema(); //same as getXMPSchema("xap", xmp);

			if(xmpBasicSchema != null) {
				// Thumbnails could have xap: or xmp: namespace in the BasicSchema.
				Thumbnail thumbnail = xmpBasicSchema.getThumbnail(null, "xap");
				if(thumbnail == null) {
					thumbnail = xmpBasicSchema.getThumbnail(null, "xmp");
				}
				if (thumbnail != null) {
					String image = thumbnail.getImage();
					if(image != null) {
						byte[] decodeBase64 = Base64.decode(image);
						if(decodeBase64 != null && decodeBase64.length > 5) {
							result = decodeBase64;
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public String getPlainMetadata() {
		try {
			final byte[] xmpMetadataBytes = pdfDoc.getXMPMetadata();
			if(xmpMetadataBytes != null && xmpMetadataBytes.length > 0) {
				String xml = new String(xmpMetadataBytes, StandardCharsets.UTF_8);
				xml = new HTMLEntityConverter(xml, -1).decodeEntities();

				return xml;
			} else {
				LoggerFactory.logInfo(this, "Could not get plain metadata for " + ebookResource, null);
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "Could not get plain metadata for " + ebookResource, e);
		}

		return null;
	}

	@Override
	public List<MetadataProperty> getSupportedMetadata() {
		final ArrayList<MetadataProperty> result = new ArrayList<>();
		result.add(new MetadataProperty("Author", EMPTY));
		result.add(new MetadataProperty("Title", EMPTY));
		result.add(new MetadataProperty("Creator", EMPTY));
		result.add(new MetadataProperty("Subject", EMPTY));
		result.add(new MetadataProperty("Producer", EMPTY));
		result.add(new MetadataProperty("AgeSuggestion", EMPTY));
		result.add(new MetadataProperty("Rating", EMPTY));
		result.add(new MetadataProperty("SeriesIndex", EMPTY));
		result.add(new MetadataProperty("SeriesName", EMPTY));
		result.add(new MetadataProperty("Description", EMPTY));
		result.add(new MetadataProperty("ModDate", EMPTY, Date.class));
		result.add(new MetadataProperty("CreationDate", EMPTY, Date.class));
		result.add(new MetadataProperty("SourceModified", EMPTY, Date.class));
		return result;
	}

	@Override
	public String getPlainMetadataMime() {
		return "text/xml";
	}

	private List<MetadataProperty> getAuthorMetadata(boolean create, List<MetadataProperty> props) {
		final ArrayList<MetadataProperty> result = new ArrayList<>(2);
		final List<MetadataProperty> metadataProperties;
		if(props != null) {
			metadataProperties = props;
		} else {
			metadataProperties = readMetadata();
		}

		MetadataProperty authorProperty = null;
		for (MetadataProperty property : metadataProperties) {
			if(property.getName().equalsIgnoreCase("Author")) {
				result.add(property);
				authorProperty = property;
			}
		}

		//if the list is empty and a new property should be created, add a new, empty author property to the result.
		if(create && result.isEmpty()) {
			authorProperty = new MetadataProperty("Author", EMPTY);
			result.add(authorProperty);
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public List<MetadataProperty> getMetadataByType(boolean create, List<MetadataProperty> props, COMMON_METADATA_TYPES type) {
		final String search;
		final String name;

		switch(type) {
			case GENRE:
				search = "subject";
				name = "Subject";
				break;
			case TITLE:
				search = "title";
				name = "Title";
				break;
			case SERIES_NAME:
				search = "seriesname";
				name = "seriesname";
				break;
			case RATING:
				search = "rating";
				name = "Rating";
				break;
			case AUTHOR:
				return this.getAuthorMetadata(create, props);
			case AGE_SUGGESTION:
				search = "agesuggestion";
				name = "AgeSuggestion";
				break;
			case DESCRIPTION:
				search = "description";
				name = "Description";
				break;
			case ISBN:
				search = "isbn";
				name = "Isbn";
				break;
			case LANGUAGE:
				search = "language";
				name = "Language";
				break;
			case COVER:
				search = "cover";
				name = "Cover";
				break;
			default:
				return null;
		}

		final ArrayList<MetadataProperty> result = new ArrayList<>(2);
		final List<MetadataProperty> metadataProperties;
		if(props != null) {
			metadataProperties = props;
		} else {
			metadataProperties = readMetadata();
		}

		for (MetadataProperty property : metadataProperties) {
			if(property.getName().equalsIgnoreCase(search)) {
				result.add(property);
			}
		}

		//if the list is empty and a new property should be created, add a new, empty author property to the result.
		if(create && result.isEmpty()) {
			result.add(new MetadataProperty(name, EMPTY));
		}
		return Collections.unmodifiableList(result);
	}

}