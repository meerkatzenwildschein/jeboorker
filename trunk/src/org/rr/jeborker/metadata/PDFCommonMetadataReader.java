package org.rr.jeborker.metadata;

import java.io.IOException;
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
import org.bouncycastle.util.encoders.Base64;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.HTMLEntityConverter;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.item.EbookKeywordItem;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


class PDFCommonMetadataReader extends APDFCommonMetadataHandler implements IMetadataReader {

	private IResourceHandler ebookResource;
	
	private PDFCommonDocument pdfDoc;

	PDFCommonMetadataReader(final IResourceHandler ebookResource) {
		this.ebookResource = ebookResource;
		this.pdfDoc = PDFCommonDocument.getInstance(PDFCommonDocument.ITEXT, ebookResource);
	}
	
	@Override
	public List<IResourceHandler> getEbookResource() {
		return Collections.singletonList(this.ebookResource);
	}	
	
	@Override
	public List<MetadataProperty> readMetaData() {
		try {
			final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>();
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
					} catch(Exception e) {
						LoggerFactory.logWarning(this, "could not handle property " + key + " and value " + value, e);
					}
				}
			} else {
				LoggerFactory.logWarning(this, "Could not get metadata from " + ebookResource, new RuntimeException("dumpstack"));
			}					
			
			return result;
		} catch (Throwable e) {
			LoggerFactory.logWarning(this.getClass(), "Could not read metadata for pdf " + ebookResource, e);
		}
		return new ArrayList<MetadataProperty>(0);
	}
	
	private byte[] getXmpMetadata() {
		try {
			return pdfDoc.getXMPMetadata();
		} catch (IOException e) {
			LoggerFactory.logWarning(this.getClass(), "Could not read xmp metadata for pdf " + ebookResource, e);
		}
		return null;
	}
	
	private Map<String, String> getInfo() {
		try {
			return pdfDoc.getInfo();
		} catch (IOException e) {
			LoggerFactory.logWarning(this.getClass(), "Could not read info metadata for pdf " + ebookResource, e);
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
					final String stringValue = StringUtils.toString(value);
					if(stringValue.isEmpty()) {
						continue; //no sense to add an empty Date.
					} else {
						Date dateValue = DateConversionUtils.toDate(stringValue);
						if(value != null && value.toString().isEmpty()) {
							//2004-12-11T00:00:+0Z
							value = dateValue;
						}
					}
				}
				final PDFMetadataProperty pdfMetadataProperty = new PDFMetadataProperty(tagName, value, null);
				result.add(pdfMetadataProperty);
			}
		}
	}

	@Override
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item) {
		item.clearMetadata();
		MetadataProperty authorMetadataProperty = null;
		for (MetadataProperty metadataProperty : metadataProperties) {
			final String name = metadataProperty.getName().toLowerCase();
			if(name.equals("title")) {
				item.setTitle(metadataProperty.getValueAsString());
			} else if(name.equals("author")) {
				authorMetadataProperty = metadataProperty;
			} else if(authorMetadataProperty == null && name.equals("creator")) {
				authorMetadataProperty = metadataProperty;
			} else if(name.equals("keywords")) {
				List<String> keywords = ListUtils.split(metadataProperty.getValueAsString(), ",");
				List<EbookKeywordItem> asEbookKeywordItem = EbookPropertyItemUtils.getAsEbookKeywordItem(keywords);
				item.setKeywords(asEbookKeywordItem);
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
			}
		}
		if(authorMetadataProperty != null) {
			METADATA_TYPES.AUTHOR.fillItem(authorMetadataProperty, item);
		}
	}

	@Override
	public byte[] getCover() {
		byte[] tumbnailData = null;
		try {
			byte[] fetchThumbnail = fetchXMPThumbnail(ebookResource);
			if(fetchThumbnail != null) {
				return fetchThumbnail;
			} else {
				return pdfDoc.fetchCoverFromPDFContent();
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(this.getClass(), "Could not read cover for pdf " + ebookResource, e);
		}
		return tumbnailData;
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
				//Thumbnails could have xap: or xmp: namespace in the BasicSchema.
				Thumbnail thumbnail = xmpBasicSchema.getThumbnail(null, "xap");
				if(thumbnail == null) {
					thumbnail = xmpBasicSchema.getThumbnail(null, "xmp");
				}
				if (thumbnail != null) {
					String image = thumbnail.getImage();					
					byte[] decodeBase64 = Base64.decode(image);
					if(decodeBase64!=null && decodeBase64.length > 5) {
						result = decodeBase64;
					}
				}
			}
		}
		return result;
	}		
	
	@Override
	public String getPlainMetaData() {
		try {
			final byte[] xmpMetadataBytes = pdfDoc.getXMPMetadata();
			if(xmpMetadataBytes != null && xmpMetadataBytes.length > 0) {
				String xml = new String(xmpMetadataBytes, "UTF-8");
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
	public List<MetadataProperty> getSupportedMetaData() {
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>();
		result.add(new MetadataProperty("Author", ""));
		result.add(new MetadataProperty("Title", ""));
		result.add(new MetadataProperty("Creator", ""));
		result.add(new MetadataProperty("Subject", ""));
		result.add(new MetadataProperty("Producer", ""));
		result.add(new MetadataProperty("AgeSuggestion", ""));
		result.add(new MetadataProperty("Rating", ""));
		result.add(new MetadataProperty("SeriesIndex", ""));
		result.add(new MetadataProperty("SeriesName", ""));
		result.add(new MetadataProperty("ModDate", "", Date.class));
		result.add(new MetadataProperty("CreationDate", "", Date.class));
		result.add(new MetadataProperty("SourceModified", "", Date.class));		
		return result;
	}
	
	@Override
	public String getPlainMetaDataMime() {
		return "text/xml";
	}
	
	private List<MetadataProperty> getAuthorMetaData(boolean create, List<MetadataProperty> props) {
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>(2);
		final List<MetadataProperty> metadataProperties;
		if(props != null) {
			metadataProperties = props;
		} else {
			metadataProperties = readMetaData();
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
			authorProperty = new MetadataProperty("Author", "");
			result.add(authorProperty);
		} 
		return Collections.unmodifiableList(result);
	}	
	
	@Override
	public List<MetadataProperty> getMetadataByType(boolean create, List<MetadataProperty> props, METADATA_TYPES type) {
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
				return this.getAuthorMetaData(create, props);
			default:
				return null;
		}
		
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>(2);
		final List<MetadataProperty> metadataProperties;
		if(props != null) {
			metadataProperties = props;
		} else {
			metadataProperties = readMetaData();
		}
		
		for (MetadataProperty property : metadataProperties) {
			if(property.getName().equalsIgnoreCase(search)) {
				result.add(property);
			}
		}
		
		//if the list is empty and a new property should be created, add a new, empty author property to the result.
		if(create && result.isEmpty()) {
			result.add(new MetadataProperty(name, ""));
		} 
		return Collections.unmodifiableList(result);
	}
	
}