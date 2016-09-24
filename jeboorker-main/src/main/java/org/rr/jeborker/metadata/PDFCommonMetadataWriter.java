package org.rr.jeborker.metadata;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.apache.jempbox.xmp.Thumbnail;
import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchema;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.MimeUtils;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.Base64;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.app.FileRefreshBackground;
import org.rr.jeborker.metadata.pdf.PDFDocument;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageInfo;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class PDFCommonMetadataWriter extends APDFCommonMetadataHandler implements IMetadataWriter {

	private IResourceHandler ebookResource;
	
	private PDFDocument pdfDoc;

	PDFCommonMetadataWriter(final IResourceHandler ebookResource) {
		this.ebookResource = ebookResource;
		this.pdfDoc = PDFDocument.getPDFCommonDocumentInstance(PDFDocument.ITEXT, ebookResource);
	}

	@Override
	public void writeMetadata(List<MetadataProperty> props) {
		try {
			final PDFCommonMetadataReader reader = (PDFCommonMetadataReader) MetadataHandlerFactory.getReader(ebookResource);

			byte[] fetchXMPThumbnail = reader.fetchXMPThumbnail(ebookResource);
			HashMap<String, String> info = new HashMap<String, String>();
			XMPMetadata blankXMP = new XMPMetadata();

			for (MetadataProperty metadataProperty : props) {
				final String name = metadataProperty.getName();

				if (metadataProperty instanceof PDFMetadataProperty) {
					PDFMetadataProperty pdfMetadataProperty = (PDFMetadataProperty) metadataProperty;
					String namespace = pdfMetadataProperty.getNamespace();
					XMPSchema xmpSchema = getXMPSchema(namespace, blankXMP);

					if (xmpSchema != null) {
						Element xmpSchemaElement = xmpSchema.getElement();
						Element propertyElement = pdfMetadataProperty.createElement(xmpSchemaElement.getOwnerDocument());
						xmpSchemaElement.appendChild(propertyElement);
					} else {
						LoggerFactory.logWarning(this, "No schema for " + pdfMetadataProperty.getName() + " witdh namespace " + namespace + " in "
								+ ebookResource, null);
					}
				} else {
					Object firstValue = ListUtils.get(metadataProperty.getValues(), 0);
					if (ReflectionUtils.equals(metadataProperty.getPropertyClass(), Date.class)) {
						// date must be formatted to something like D:20061204092842
						String dateValue;
						if(firstValue instanceof Date) {
							dateValue = DateConversionUtils.toString((Date) firstValue, DateConversionUtils.DATE_FORMATS.PDF);
						} else if(firstValue instanceof String) {
							dateValue = DateConversionUtils.toString(DateConversionUtils.toDate((String) firstValue), DateConversionUtils.DATE_FORMATS.PDF);
						} else {
							throw new IllegalArgumentException("The value '" + firstValue + "' is no member of the expected class type.");
						}
						info.put(name, dateValue);
					} else {
						if(IMetadataReader.COMMON_METADATA_TYPES.COVER.getName().equalsIgnoreCase(name) && firstValue instanceof byte[]) {
							fetchXMPThumbnail = (byte[]) firstValue;
						} else if(IMetadataReader.COMMON_METADATA_TYPES.COVER.getName().equalsIgnoreCase(name) && firstValue instanceof String) {
							fetchXMPThumbnail = Base64.decode((String) firstValue);
						} else {
							if(info.containsKey(name)) {
								String oldValue = info.get(name);
								firstValue = oldValue + ", " + StringUtil.toString(firstValue);
							}
							String insValue = StringUtil.toString(firstValue).trim();
							if(!insValue.isEmpty()) {
								info.put(name, insValue);
							}
						}
					}
				}
			}
			
			if(fetchXMPThumbnail != null) {
				blankXMP = attachCoverToXmp(fetchXMPThumbnail, blankXMP.getXMPDocument());
			}
			
			pdfDoc.setInfo(info);
			pdfDoc.setXMPMetadata(blankXMP.asByteArray());
			FileRefreshBackground.setDisabled(true);
			pdfDoc.write();
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "could not write pdf meta data for " + ebookResource, e);
		} finally {
			FileRefreshBackground.setDisabled(false);
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
		final IResourceHandler coverResourceLoader = ResourceHandlerFactory.getVirtualResourceHandler("PDFMetadataWriterSetCover", coverData);
		final String coverMimeType = coverResourceLoader.getMimeType(true);

		Thumbnail thumbnail = xmpSchema.getThumbnail();
		if(thumbnail == null) {
			thumbnail = new Thumbnail(xmp);
		}
		
		if(MimeUtils.isJpegMime(coverMimeType)) {
			thumbnail.setImage(new String(Base64.encode(coverData)));
			ImageInfo imageInfo = new ImageInfo(ResourceHandlerFactory.getVirtualResourceHandler("setCover", coverData));
			thumbnail.setHeight(imageInfo.getHeight());
			thumbnail.setWidth(imageInfo.getWidth());
		} else {
			IImageProvider imageProvider = ImageProviderFactory.getImageProvider(coverResourceLoader);
			BufferedImage image = imageProvider.getImage();
			if(image != null) {
				byte[] jpegCover = ImageUtils.getImageBytes(image, MimeUtils.MIME_JPEG);
				thumbnail.setImage(new String(Base64.encode(jpegCover)));
				thumbnail.setHeight(image.getHeight());
				thumbnail.setWidth(image.getWidth());
			} else {
				LoggerFactory.log(Level.INFO, this, "Unknown image format");
			}
		}
		thumbnail.setFormat(Thumbnail.FORMAT_JPEG);
		xmpSchema.setThumbnail(thumbnail, "xap");
		return xmp;
	}

	@Override
	public void storePlainMetadata(byte[] plainMetadata) {
		try {
			pdfDoc.setXMPMetadata(plainMetadata);
			pdfDoc.write();
		} catch(Exception e) {
			LoggerFactory.logWarning(this, "Could not write metadata to " + ebookResource, e);
		}
	}
}
