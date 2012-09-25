package org.rr.jeborker.metadata;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.jempbox.xmp.Thumbnail;
import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchema;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.xml.XMLUtils;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageInfo;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.codec.Base64;

class PDFMetadataWriter extends APDFMetadataHandler implements IMetadataWriter {

	private IResourceHandler ebookResource;

	private HashMap<String, XMPSchema> schemas = new HashMap<String, XMPSchema>();

	PDFMetadataWriter(IResourceHandler ebookResource) {
		this.ebookResource = ebookResource;
	}

	@Override
	public void writeMetadata(Iterator<MetadataProperty> props) {
		try {
			final byte[] pdfData = ebookResource.getContent();
			final PdfReader pdfReader = new PdfReader(pdfData);
			final byte[] fetchXMPThumbnail = fetchXMPThumbnail(pdfReader);
			final HashMap<String, String> info = new HashMap<String, String>();
			XMPMetadata blankXMP = new XMPMetadata();

			// flag wich tells if xmp meta data where really be created.
			// so no empty xmp doc will be inserted.
			boolean xmpMetadataSet = false;
			while (props.hasNext()) {
				final MetadataProperty metadataProperty = props.next();
				final String name = metadataProperty.getName();
				final List<Object> value = metadataProperty.getValues();

				if (metadataProperty instanceof PDFMetadataProperty) {
					PDFMetadataProperty pdfMetadataProperty = (PDFMetadataProperty) metadataProperty;
					String namespace = pdfMetadataProperty.getNamespace();
					XMPSchema xmpSchema = getXMPSchema(namespace, blankXMP);

					if (xmpSchema != null) {
						Element xmpSchemaElement = xmpSchema.getElement();
						Element propertyElement = pdfMetadataProperty.createElement(xmpSchemaElement.getOwnerDocument());
						xmpSchemaElement.appendChild(propertyElement);
						xmpMetadataSet = true;
					} else {
						LoggerFactory.logWarning(this, "No schema for " + pdfMetadataProperty.getName() + " witdh namespace " + namespace + " in "
								+ ebookResource, null);
					}
				} else {
					Object firstValue = value.get(0);
					if (ReflectionUtils.equals(metadataProperty.getPropertyClass(), Date.class)) {
						// date must be formatted to something like D:20061204092842
						String dateValue = DateConversionUtils.toString((Date) firstValue, DateConversionUtils.DATE_FORMATS.PDF);
						info.put(name, dateValue);
					} else {
						info.put(name, StringUtils.toString(firstValue));
					}
				}
			}
			
			if(fetchXMPThumbnail != null) {
				blankXMP = attachCoverToXmp(fetchXMPThumbnail, blankXMP.getXMPDocument());
			}
			
			writeMetadata(pdfReader, xmpMetadataSet ? blankXMP.asByteArray() : null, info);
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "could not write pdf meta data for " + ebookResource, e);
		}
	}
	
	/**
	 * Get the thumbnail from the pdf.
	 * @param pdfReader The reader for extracting the thumbnail.
	 * @return The thumbnail or <code>null</code> if no thumbnail exists.
	 */
	private byte[] fetchXMPThumbnail(final PdfReader pdfReader) {
		try {
			byte[] fetchXMPThumbnail = PDFMetadataReader.fetchXMPThumbnail(pdfReader, ebookResource);
			return fetchXMPThumbnail;
		} catch (Exception e) {
			return null;
		}
	}

	private XMPSchema getXMPSchema(final String namespace, final XMPMetadata xmpMetadata) throws IOException {
		XMPSchema xmpSchema = schemas.get(namespace);
		if (xmpSchema == null) {
			if (namespace.equals("dc")) {
				xmpSchema = xmpMetadata.getDublinCoreSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addDublinCoreSchema();
				}
			} else if (namespace.equals("xap")) {
				xmpSchema = xmpMetadata.getBasicSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addBasicSchema();
				}
			} else if (namespace.equals("pdf")) {
				xmpSchema = xmpMetadata.getPDFSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addPDFSchema();
				}
			}  else if (namespace.equals("pdfx")) {
				xmpSchema = xmpMetadata.getPDFXSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addPDFXSchema();
				}
			} else if (namespace.equals("xmp")) {
				xmpSchema = xmpMetadata.getXMPSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addXMPSchema();
				}
			} else if (namespace.equals("xapMM") || namespace.equals("xmpMM")) {
				xmpSchema = xmpMetadata.getMediaManagementSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addMediaManagementSchema();
				}
			} else if (namespace.equals("photoshop")) {
				xmpSchema = xmpMetadata.getPhotoshopSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addPhotoshopSchema();
				}
			}
			schemas.put(namespace, xmpSchema);
		}

		return xmpSchema;
	}

	@Override
	public void dispose() {
		this.schemas.clear();
		this.schemas = null;
		this.ebookResource = null;
	}

	@Override
	public void setCover(byte[] coverData) {
		try {
			final byte[] pdfData = ebookResource.getContent();
			final PdfReader pdfReader = new PdfReader(pdfData);
			final byte[] xmpMetadataBytes = pdfReader.getMetadata();
			
			final XMPMetadata xmp = attachCoverToXmp(coverData, getDocument(xmpMetadataBytes, ebookResource));
			
			writeMetadata(pdfReader, xmp.asByteArray(), pdfReader.getInfo());
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "Could not write cover for " + ebookResource, e);
		}
	}

	/**
	 * Attaches the given coverData as jpeg to the given document. The image is
	 * converted into jpeg if needed.
	 * 
	 * @param coverData The image data to be used as cover.
	 * @param document The xmp document where the cover should be attached to. 
	 * @return Metadata created from the given document with the given cover.
	 * @throws IOException
	 */
	private XMPMetadata attachCoverToXmp(byte[] coverData, final Document document) throws IOException {
		final XMPMetadata xmp;
		if(document != null) {
			xmp = new XMPMetadata(document);
		} else {
			xmp = new XMPMetadata();
		}
		
		final XMPSchemaBasic xmpSchema = (XMPSchemaBasic) getXMPSchema("xap", xmp);
		final IResourceHandler coverResourceLoader = ResourceHandlerFactory.getVirtualResourceLoader("PDFMetadataWriterSetCover", coverData);
		final String coverMimeType = coverResourceLoader.getMimeType();
		
		Thumbnail thumbnail = xmpSchema.getThumbnail();
		if(thumbnail == null) {
			thumbnail = new Thumbnail(xmp);
		}
		
		if(coverMimeType.equals("image/jpeg")) {
			thumbnail.setImage(Base64.encodeBytes(coverData));
			ImageInfo imageInfo = new ImageInfo(ResourceHandlerFactory.getVirtualResourceLoader("setCover", coverData));
			thumbnail.setHeight(imageInfo.getHeight());
			thumbnail.setWidth(imageInfo.getWidth());
		} else {
			IImageProvider imageProvider = ImageProviderFactory.getImageProvider(coverResourceLoader);
			BufferedImage image = imageProvider.getImage();
			byte[] jpegCover = ImageUtils.getImageBytes(image, "image/jpeg");
			thumbnail.setImage(Base64.encodeBytes(jpegCover));
			thumbnail.setHeight(image.getHeight());
			thumbnail.setWidth(image.getWidth());
		}
		thumbnail.setFormat(Thumbnail.FORMAT_JPEG);
		xmpSchema.setThumbnail(thumbnail);
		return xmp;
	}

	/**
	 * Writes the given metadata to the pdf file handled by this {@link PDFMetadataWriter} instance. 
	 * 
	 * @param pdfReader The reader for the pdf file.
	 * @param xmp the xmp metadata which should be written to the pdf
	 * @param moreInfo the key/value type metadata which should be written to the pdf.
	 * @throws IOException
	 * @throws DocumentException
	 * @throws Exception
	 */
	private void writeMetadata(final PdfReader pdfReader, final byte[] xmp, HashMap<String, String> moreInfo) throws IOException, DocumentException, Exception {
		final IResourceHandler tmpEbookResourceLoader = ResourceHandlerFactory.getTemporaryResourceLoader(ebookResource, "tmp");
		PdfStamper stamper = null;
		OutputStream ebookResourceOutputStream = null;
		
		try {
			ebookResourceOutputStream = tmpEbookResourceLoader.getContentOutputStream(false);
			stamper = new PdfStamper(pdfReader, ebookResourceOutputStream);
			stamper.setXmpMetadata(xmp);
			if(moreInfo!=null) {
				//to delete old entries, itext need to null them.
				HashMap<String, String> oldInfo = pdfReader.getInfo();
				HashMap<String, String> newInfo = new HashMap<String, String>(oldInfo.size() + moreInfo.size());
				for (Iterator<String> it = oldInfo.keySet().iterator(); it.hasNext();) {
					newInfo.put(it.next(), null); 
				}
				newInfo.putAll(moreInfo);
				
				stamper.setMoreInfo(newInfo);
			}
		} finally {
			if (stamper != null) {
				try {
					stamper.close();
				} catch (DocumentException e) {
					LoggerFactory.logWarning(this, "Could not close pdf stamper for " + ebookResource, e);
				} catch (IOException e) {
					LoggerFactory.logWarning(this, "Could not close pdf stamper for " + ebookResource, e);
				}
			}
			if (ebookResourceOutputStream != null) {
				try {
					ebookResourceOutputStream.flush();
				} catch (IOException e) {
				}
				IOUtils.closeQuietly(ebookResourceOutputStream);
			}
			if(tmpEbookResourceLoader.size() > 0) {
				//new temp pdf looks good. Move the new temp one over the old one. 
				tmpEbookResourceLoader.moveTo(ebookResource, true);
			}
		}
	}

	@Override
	public void storePlainMetadata(byte[] plainMetadata) {
		try {
			if(plainMetadata.length > 9 && new String(plainMetadata, 0, 9).startsWith("<?xpacket")) {
				//XMP 
				if(XMPUtils.isValidXMP(plainMetadata) && XMLUtils.isValidXML(plainMetadata)) {
					final byte[] pdfData = ebookResource.getContent();
					final PdfReader pdfReader = new PdfReader(pdfData);
					
					writeMetadata(pdfReader, plainMetadata, pdfReader.getInfo());
				} else {
					throw new UnsupportedOperationException("XML is not well formed");
				}
			} else {
				//PDF Property
				throw new UnsupportedOperationException("Could not write plain metadata");
			}
		} catch(Exception e) {
			LoggerFactory.logWarning(this, "Could not write metadata to " + ebookResource, e);
		}
		
	}
}
