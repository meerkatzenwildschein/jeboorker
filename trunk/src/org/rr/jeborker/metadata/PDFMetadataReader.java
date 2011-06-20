package org.rr.jeborker.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jempbox.xmp.Thumbnail;
import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchema;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.HTMLEntityConverter;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
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
			if(xmpMetadataBytes!=null) {
				final Document document = getDocument(xmpMetadataBytes, ebookResource);
				final XMPMetadata metadata = new XMPMetadata(document);
                
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
					value = DateConversionUtils.toDate(StringUtils.toString(value));
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
			final byte[] pdfData = ebookResource.getContent();
			pdfReader = new PdfReader(pdfData);
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

	@Override
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item) {
		for (MetadataProperty metadataProperty : metadataProperties) {
			final String name = metadataProperty.getName().toLowerCase();
			if(item.getTitle()==null && name.equals("title")) {
				item.setTitle(metadataProperty.getValueAsString());
			} else if(name.equals("author")) {
				item.setAuthor(metadataProperty.getValueAsString());
			} else if(item.getAuthor()==null && name.equals("creator")) {
				item.setAuthor(metadataProperty.getValueAsString());
			} else if(name.equals("keywords")) {
				item.setKeywords(metadataProperty.getValueAsString());
			} else if(name.equals("description")) {
				item.setDescription(metadataProperty.getValueAsString());
			} else if(name.equals("creationdate")) {
				item.setCreationDate(DateConversionUtils.toDate(metadataProperty.getValueAsString()));
			} else if(name.equals("subject")) {
				item.setGenre(metadataProperty.getValueAsString());
			} else if(name.equals("agesuggestion")) {
				item.setAgeSuggestion(metadataProperty.getValueAsString());
			} else if(name.equals("rating")) {
				item.setRating(metadataProperty.getValueAsString());
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
			if(fetchThumbnail!=null) {
				return fetchThumbnail;
			} else {
				return fetchCoverFromPDFContent(pdfReader);
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
	static byte[] fetchXMPThumbnail(final PdfReader pdfReader, final IResourceHandler ebookResource) throws Exception {
		final byte[] xmpMetadataBytes = pdfReader.getMetadata();
		if(xmpMetadataBytes!=null) {
			final Document document = getDocument(xmpMetadataBytes, ebookResource);
			final XMPMetadata metadata = new XMPMetadata(document);
			XMPSchemaBasic basicSchema = metadata.getBasicSchema();
			if(basicSchema!=null) {
				Thumbnail thumbnail = basicSchema.getThumbnail();
				if(thumbnail!=null) {
					String image = thumbnail.getImage();					
					byte[] decodeBase64 = Base64.decode(image);
					if(decodeBase64!=null && decodeBase64.length > 5) {
						return decodeBase64;
					}
				}
			}
		}
		return null;
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
						
//						HashMap<Object, Object> fieldValue = (HashMap) ReflectionUtils.getFieldValue(stream, "hashMap");
//						System.out.println(ebookResource);
//						for (Entry<Object, Object> entry : fieldValue.entrySet()) {
//							Object key = entry.getKey();
//							Object value = entry.getValue();
//							System.out.println(key + ":" + value);
//						}
//						System.out.println();	
						double aspectRatio = ((double)height) / ((double)width);
						if(width > 150 && aspectRatio > 1.5d && aspectRatio < 1.7d) {
//							int bitsPerPixel = new ImageInfo(ResourceHandlerFactory.getVirtualResourceLoader(null, img)).getBitsPerPixel();
//						System.out.println("bitsPerPixel:" + bitsPerPixel + " " + ebookResource.getName());						
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
				LoggerFactory.logWarning(this, "Could not get plain metadata for " + ebookResource, new Exception("dumpstack"));
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
		result.add(new MetadataProperty("agesuggestion", ""));
		result.add(new MetadataProperty("rating", ""));
		result.add(new MetadataProperty("seriesindex", ""));
		result.add(new MetadataProperty("seriesname", ""));
		result.add(new MetadataProperty("ModDate", "", Date.class));
		result.add(new MetadataProperty("CreationDate", "", Date.class));
		result.add(new MetadataProperty("SourceModified", "", Date.class));		
		return result;
	}
	
	@Override
	public String getPlainMetaDataMime() {
		return "text/xml";
	}
}