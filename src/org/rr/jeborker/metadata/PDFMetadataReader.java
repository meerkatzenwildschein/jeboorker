package org.rr.jeborker.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jempbox.xmp.Thumbnail;
import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchema;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPUtils;
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

import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStream;
import com.itextpdf.text.pdf.codec.Base64;


class PDFMetadataReader extends APDFMetadataHandler implements IMetadataReader {

	private IResourceHandler ebookResource;
	
	private PdfReader pdfReader;
	
	private HashMap<String, String> pdfInfo;
	
	PDFMetadataReader(IResourceHandler ebookResource) {
		this.ebookResource = ebookResource;
	}
	
	@Override
	public IResourceHandler getEbookResource() {
		return this.ebookResource;
	}	
	
	@Override
	public List<MetadataProperty> readMetaData() {
		try {
			final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>();
			final PdfReader pdfReader = getReader();
			
			//XMP example: http://itextpdf.com/examples/iia.php?id=217
			final byte[] xmpMetadataBytes = pdfReader.getMetadata();
			if(XMPUtils.isValidXMP(xmpMetadataBytes)) {
				final Document document = getDocument(xmpMetadataBytes, ebookResource);
				final XMPMetadata metadata = document != null ? new XMPMetadata(document) : new XMPMetadata();
                
				List<XMPSchema> schemas = metadata.getSchemas();
				for (XMPSchema xmpSchema : schemas) {
	                this.addSchemaProperties(result, xmpSchema);
				}
			}
			
			final HashMap<String, String> pdfInfo = getPdfInfo(getReader());
			if(pdfInfo!=null) {
				for (Entry<String, String> entry : pdfInfo.entrySet()) {
					final String key = entry.getKey();
					final Object value = entry.getValue();
					try {
						if(key.endsWith("Date") || key.endsWith("SourceModified")) {
							final Date dateValue = DateConversionUtils.toDate((String)value);
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
		} catch (Exception e) {
			LoggerFactory.logWarning(this.getClass(), "Could not read metadata for pdf " + ebookResource, e);
		}
		return new ArrayList<MetadataProperty>(0);
	}

	private void addSchemaProperties(final ArrayList<MetadataProperty> result, final XMPSchema schema) throws IOException {
		if(schema==null) {
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
					Date dateValue = DateConversionUtils.toDate(StringUtils.toString(value));
					if(value != null && value.toString().isEmpty()) {
						//2004-12-11T00:00:+0Z
						value = dateValue;
					}
				}
				final PDFMetadataProperty pdfMetadataProperty = new PDFMetadataProperty(tagName, value, null);
				result.add(pdfMetadataProperty);
			}
		}
	}
	
	/**
	 * Gets the PDF reader. The reader will be created and cached for
	 * the next time. 
	 * @return The desired reader.
	 * @throws IOException
	 */
	private PdfReader getReader() throws IOException {
		if(pdfReader==null) {
			//bytes will also be completely loaded by the PdfReader. 
			System.gc();
			byte[] pdfBytes = ebookResource.getContent();
			pdfReader = new PdfReader(pdfBytes);
		}
		return pdfReader;
	}	
	
	private HashMap<String, String> getPdfInfo(PdfReader reader) {
		if(pdfInfo==null) {
			pdfInfo = reader.getInfo();
		}
		return pdfInfo; 
	}	

	@Override
	public void dispose() {
		this.pdfReader = null;
		this.pdfInfo = null;
	}
	
	protected void finalize() throws Throwable {
		dispose();
	}

	@Override
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item) {
		item.clearMetadata();
		boolean authorSet = false;
		for (MetadataProperty metadataProperty : metadataProperties) {
			final String name = metadataProperty.getName().toLowerCase();
			if(name.equals("title")) {
				item.setTitle(metadataProperty.getValueAsString());
			} else if(name.equals("author")) {
				item.setAuthor(metadataProperty.getValueAsString());
				authorSet = true;
			} else if(!authorSet && name.equals("creator")) {
				item.setAuthor(metadataProperty.getValueAsString());
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
	}

	@Override
	public byte[] getCover() {
		byte[] tumbnailData = null;
		try {
			byte[] fetchThumbnail = fetchXMPThumbnail(pdfReader, ebookResource);
			if(pdfReader!=null && fetchThumbnail!=null) {
				return fetchThumbnail;
			} else if(pdfReader != null) {
				return fetchCoverFromPDFContent(pdfReader);
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(this.getClass(), "Could not read cover for pdf " + ebookResource, e);
		}
		return tumbnailData;
	}	
	
	/**
	 * Tries to extract the cover by looking for the embedded images of the pdf. The first
	 * image which seems to be a cover will be returned.
	 *  
	 * @param pdfReader The reader for accessing the pdf content.
	 * @return The desired image or <code>null</code> if the image couldn't be read.
	 * @throws Exception
	 */
	private byte[] fetchCoverFromPDFContent(final PdfReader pdfReader) throws Exception {
		for (int i = 0; i < pdfReader.getXrefSize(); i++) {
			PdfObject pdfobj = pdfReader.getPdfObject(i);
			if(pdfobj!=null) {
				if (pdfobj.isStream()) {
					PdfStream stream = (PdfStream) pdfobj;
					
					PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
					if (pdfsubtype == null) {
						//throw new Exception("Not an image stream");
						continue;
					}
					if (!pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
						//throw new Exception("Not an image stream");
						continue;
					}
	
					// now you have a PDF stream object with an image
					byte[] img = PdfReader.getStreamBytesRaw((PRStream) stream);		
					if(img.length > 1000) {
						int width = 0;
						int height = 0;
						try {
							width = Integer.parseInt(stream.get(PdfName.WIDTH).toString());
							height = Integer.parseInt(stream.get(PdfName.HEIGHT).toString());
							
							if(width<=0 || height<=0) {
								continue;
							}
							
							PdfObject bitspercomponent = stream.get(PdfName.BITSPERCOMPONENT);
							if(bitspercomponent!=null) {
								Number bitspercomponentNum = CommonUtils.toNumber(bitspercomponent.toString());
								if(bitspercomponentNum!=null && bitspercomponentNum.intValue()==1) {
									//no b/w images
									continue;
								}
							}							
						} catch(Exception e) {}
						
						double aspectRatio = ((double)height) / ((double)width);
						if(width > 150 && aspectRatio > 1.5d && aspectRatio < 1.7d) {
							return img;
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public String getPlainMetaData() {
		try {
			final byte[] xmpMetadataBytes = getReader().getMetadata();	
			if(xmpMetadataBytes!=null && xmpMetadataBytes.length > 0) {
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
	public MetadataProperty createRatingMetaData() {
		return new MetadataProperty("Rating", "");
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
		MetadataProperty creatorProperty = null;
		for (MetadataProperty property : metadataProperties) {
			if(property.getName().equalsIgnoreCase("Author")) {
				result.add(property);
				authorProperty = property;
			} 
			/*else if(property.getName().equalsIgnoreCase("creator")) {
				result.add(property);
				creatorProperty = property;
			}*/
		}
		
		//if author and creator exists and don't have the same content, remove the creator prop.
		/*if(authorProperty != null && creatorProperty != null && !authorProperty.getValueAsString().equals(creatorProperty.getValueAsString())) {
			result.remove(creatorProperty);
		}*/
		
		//if the list is empty and a new property should be created, add a new, empty author property to the result.
		if(create && result.isEmpty()) {
			authorProperty = new MetadataProperty("Author", "");
			result.add(authorProperty);
		} 
		return Collections.unmodifiableList(result);
	}	
	
	@Override
	public List<MetadataProperty> getMetaDataByType(boolean create, List<MetadataProperty> props, METADATA_TYPES type) {
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