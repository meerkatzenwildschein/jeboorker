package org.rr.jeborker.metadata;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.jempbox.xmp.Thumbnail;
import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchema;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.bouncycastle.util.encoders.Base64;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageInfo;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class PDFCommonMetadataWriter extends APDFCommonMetadataHandler implements IMetadataWriter {

	private IResourceHandler ebookResource;
	
	private PDFCommonDocument pdfDoc;

	PDFCommonMetadataWriter(final IResourceHandler ebookResource) {
		this.ebookResource = ebookResource;
		this.pdfDoc = PDFCommonDocument.getInstance(PDFCommonDocument.ITEXT, ebookResource);
	}

	@Override
	public void writeMetadata(List<MetadataProperty> props) {
		try {
			final byte[] fetchXMPThumbnail = fetchXMPThumbnail();
			HashMap<String, String> info = new HashMap<String, String>();
			XMPMetadata blankXMP = new XMPMetadata();

			// flag wich tells if xmp meta data where really be created.
			// so no empty xmp doc will be inserted.
			boolean xmpMetadataSet = false;
			for (MetadataProperty metadataProperty : props) {
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
			
			pdfDoc.setInfo(info);
			pdfDoc.setXMPMetadata(xmpMetadataSet ? blankXMP.asByteArray() : null);
			pdfDoc.write();
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "could not write pdf meta data for " + ebookResource, e);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		this.ebookResource = null;
		if(pdfDoc != null) {
			pdfDoc.dispose();
			pdfDoc = null;
		}
	}

	@Override
	public void setCover(byte[] coverData) {
		try {
			final byte[] xmpMetadataBytes = fetchXMPMetadata();
			final XMPMetadata xmp = attachCoverToXmp(coverData, getDocument(xmpMetadataBytes, ebookResource));
			pdfDoc.setXMPMetadata(xmp.asByteArray());
			pdfDoc.write();
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
			thumbnail.setImage(new String(Base64.encode(coverData)));
			ImageInfo imageInfo = new ImageInfo(ResourceHandlerFactory.getVirtualResourceLoader("setCover", coverData));
			thumbnail.setHeight(imageInfo.getHeight());
			thumbnail.setWidth(imageInfo.getWidth());
		} else {
			IImageProvider imageProvider = ImageProviderFactory.getImageProvider(coverResourceLoader);
			BufferedImage image = imageProvider.getImage();
			byte[] jpegCover = ImageUtils.getImageBytes(image, "image/jpeg");
			thumbnail.setImage(new String(Base64.encode(jpegCover)));
			thumbnail.setHeight(image.getHeight());
			thumbnail.setWidth(image.getWidth());
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
	
	private byte[] fetchXMPMetadata() {
		final PDFCommonMetadataReader reader = (PDFCommonMetadataReader) MetadataHandlerFactory.getReader(ebookResource);
		try {
			String fetchXMPThumbnail = reader.getPlainMetaData();
			return fetchXMPThumbnail.getBytes();
		} finally {
			reader.dispose();
		}		
	}
	
	private byte[] fetchXMPThumbnail() throws Exception {
		final PDFCommonMetadataReader reader = (PDFCommonMetadataReader) MetadataHandlerFactory.getReader(ebookResource);
		try {
			byte[] fetchXMPThumbnail = reader.fetchXMPThumbnail(ebookResource);
			return fetchXMPThumbnail;
		} finally {
			reader.dispose();
		}
	}
}
