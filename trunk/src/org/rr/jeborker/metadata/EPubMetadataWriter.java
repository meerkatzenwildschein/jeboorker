package org.rr.jeborker.metadata;

import java.io.IOException;
import java.util.Iterator;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.UtilConstants;
import org.rr.commons.utils.ZipUtils;
import org.rr.commons.utils.ZipUtils.ZipDataEntry;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class EPubMetadataWriter extends AEpubMetadataHandler implements IMetadataWriter {

	public EPubMetadataWriter(IResourceHandler ebookResourceHandler) {
		super(ebookResourceHandler);
	}

	@Override
	public void writeMetadata(Iterator<MetadataProperty> props) {
		final IResourceHandler ebookResourceHandler = getEbookResource();
		try {
			final byte[] zipData = this.getContent(ebookResourceHandler);
			final String opfFile = this.getOpfFile(zipData);
			final ZipDataEntry containerXml = ZipUtils.extract(zipData, opfFile);
			if (containerXml != null) {
				final Document document = getDocument(containerXml.data, ebookResourceHandler);
				final Element metadataNode = this.getMetadataElement(document);

				// delete metadata node
				Node packageNode = metadataNode.getParentNode();
				packageNode.removeChild(metadataNode);

				Element newMetadata = document.createElement("metadata");
				newMetadata.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
				newMetadata.setAttribute("xmlns:opf", "http://www.idpf.org/2007/opf");
				packageNode.insertBefore(newMetadata, packageNode.getFirstChild());

				// do the modifications to the doc ....
				this.addMetadataElements(ebookResourceHandler, document, newMetadata, props);

				// write the doc.
				final byte[] docData = getDocumentBytes(document);
				this.writeZipData(docData, opfFile);
			} else {
				LoggerFactory.logWarning(this.getClass(), "Container not found " + opfFile + " in " + ebookResourceHandler, new RuntimeException("dumpstack"));
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "could not write metadata to file " + ebookResourceHandler, e);
		}
	}

	/**
	 * Attaches the data from the given {@link EbookPropertyItem} as nodes to the given {@link Document}. Existing data will be refreshed.
	 * 
	 * @param item
	 *            The {@link EbookPropertyItem} containing the data to be written
	 * @param document
	 *            The {@link Document} which is needed to create new elements.
	 * @param metadataNode
	 *            The meta data node where the data should be attached.
	 */
	private void addMetadataElements(IResourceHandler item, final Document document, final Element metadataNode, Iterator<MetadataProperty> props) {
		while (props.hasNext()) {
			MetadataProperty metadataProperty = props.next();
			if (metadataProperty instanceof EpubMetadataProperty) {
				final Element element = ((EpubMetadataProperty) metadataProperty).createElement(document);
				metadataNode.appendChild(element);
			}
		}
	}

	@Override
	public void setCover(final byte[] cover) {
		final IResourceHandler ebookResourceHandler = getEbookResource();
		try {
			final byte[] zipData = this.getContent(ebookResourceHandler);
			final byte[] containerXmlData = getContainerOPF(zipData);

			final Document document = getDocument(containerXmlData, ebookResourceHandler);
			if (document != null) {
				final Element metadataElement = this.getMetadataElement(document);
				final Element manifestElement = this.getManifestElement(document);
				if (metadataElement != null) {
					final String coverNameReference = findMetadataCoverNameReference(metadataElement);
					final String coverName = findManifestCoverName(manifestElement, coverNameReference);
					if(coverName!=null) {
						//continue with existing cover
						final String decodedCoverName = coverName != null ? getOpfFilePath(zipData) + StringUtils.decodeURL(coverName) : "";
						final String targetConversionMime = getTargetConversionMime(decodedCoverName, cover);
						if(targetConversionMime!=null) {
							IImageProvider coverImageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getVirtualResourceLoader(null, cover));
							byte[] imageBytes = ImageUtils.getImageBytes(coverImageProvider.getImage(), targetConversionMime);
							if(imageBytes!=null) {
								this.writeZipData(imageBytes, decodedCoverName);								
							} else {
								LoggerFactory.logWarning(this, "could not encode cover to " + targetConversionMime, new RuntimeException("dumpstack"));
							}
						} else {
							this.writeZipData(cover, decodedCoverName);
						}
					} else {
						//continue without existing cover
						final String createCoverEntry = createCoverEntry(document, metadataElement, manifestElement, cover);
						final String decodedCoverName = getOpfFilePath(zipData) + createCoverEntry;
						this.writeZipData(cover, decodedCoverName);
						
						// write the doc.
						final byte[] docData = getDocumentBytes(document);
						final String opfFile = this.getOpfFile(zipData);
						this.writeZipData(docData, opfFile);						
					}
				}
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(this.getClass(), "could not write cover for " + ebookResourceHandler, e);
		}
	}
	
	/**
	 * Determines the mime of the target image and tests if the mime matches to these one
	 * which is given with the cover data. If the mimes macthes to each other, <code>null</code>
	 * is returned. If not, the target mime is returned. 
	 * @param targetCoverName The target file name of the cover.  
	 * @param cover The cover data to be written to the ebook
	 * @return The mime type to which the image must be converted.
	 */
	private String getTargetConversionMime(final String targetCoverName, final byte[] cover) {
		String targetImageMime = null;
		if(targetCoverName.indexOf('.')!=-1) {
			//extract mime from file name
			String extension = StringUtils.substringAfter(targetCoverName, ".", false, UtilConstants.COMPARE_BINARY);
			if(extension.length()==3 || extension.length()==4) {
				targetImageMime = "image/" + StringUtils.substringAfter(targetCoverName, ".", false, UtilConstants.COMPARE_BINARY);	
			}
		} 
		
		if(targetImageMime == null){
			//need to load the existing cover image to determine it's image type
			IMetadataReader reader = MetadataHandlerFactory.getReader(getEbookResource());
			byte[] oldCover = reader.getCover();
			if(oldCover!=null && oldCover.length > 0) {
				IResourceHandler virtualResourceLoader = ResourceHandlerFactory.getVirtualResourceLoader(null, oldCover);
				targetImageMime = virtualResourceLoader.getMimeType();
			}
		}
		
		//test if the source mime matches already to the target one
		if(targetImageMime!=null) {
			IResourceHandler virtualResourceLoader = ResourceHandlerFactory.getVirtualResourceLoader(null, cover);
			String sourceMimeType = virtualResourceLoader.getMimeType();
			if(targetImageMime.endsWith("/jpg") || targetImageMime.endsWith("/jpeg")) {
				if(sourceMimeType.endsWith("/jpg") || sourceMimeType.endsWith("/jpeg")) {
					//no conversion needed
					return null;
				}
				return "image/jpeg";
			} else if(targetImageMime.equals(sourceMimeType)) {
				return null;
			}
		}
		
		return targetImageMime;
	}
	
	/**
	 * Creates new xml entries for the cover. 
	 * @param document The document where the new entries should be attached to.
	 * @param metadataElement The metadata element where the cover reference entry should be attached to.
	 * @param manifestElement The manifest element to point from the cover reference to the jpeg file name in the zip.
	 * @return The file name where the cover data should be stored to.
	 */
	private String createCoverEntry(final Document document, final Element metadataElement, final Element manifestElement, final byte[] cover) {
		final Element metaElement = document.createElement("meta");
		metaElement.setAttribute("name", "cover");
		metaElement.setAttribute("content", "cover");
		metadataElement.appendChild(metaElement);
		
		final IResourceHandler virtualImageResourceLoader = ResourceHandlerFactory.getVirtualResourceLoader("", cover);
		final String coverMimeType = virtualImageResourceLoader.getMimeType();
		if(coverMimeType==null || coverMimeType.indexOf('/')==-1) {
			throw new RuntimeException("could not detect file format");
		}
		final String fileExtension = StringUtils.substringAfter(coverMimeType, "/", true, UtilConstants.COMPARE_BINARY);
		
		final String coverFileName = "cover." + fileExtension;
		final Element itemElement = document.createElement("item");
		itemElement.setAttribute("id", "cover");
		itemElement.setAttribute("media-type", coverMimeType);
		itemElement.setAttribute("href", coverFileName);
		manifestElement.appendChild(itemElement);
		return coverFileName;
	}

	/**
	 * Write the given content to the file in the underlying zip.
	 * @param content The content to be added to the zip archive.
	 * @param file The file name for the content in the zip archive.
	 * @throws IOException
	 */
	private void writeZipData(byte[] content, final String file) throws IOException {
		final IResourceHandler ebookResourceHandler = getEbookResource();
		final byte[] zipData = this.getContent(ebookResourceHandler);
		final byte[] newZipData = ZipUtils.add(zipData, new ZipUtils.ZipDataEntry(file, content));
		if (newZipData != null && newZipData.length > 0) {
			final IResourceHandler tmpEbookResourceLoader = ResourceHandlerFactory.getTemporaryResourceLoader(ebookResourceHandler, "tmp");
			tmpEbookResourceLoader.setContent(newZipData);
			tmpEbookResourceLoader.moveTo(ebookResourceHandler, true);
			
			super.zipContent = newZipData;
		}
	}

	@Override
	public void storePlainMetadata(byte[] plainMetadata) {
		try {
			final byte[] zipData = this.getContent(getEbookResource());
			this.writeZipData(plainMetadata, getOpfFile(zipData));
		} catch(Exception e) {
			LoggerFactory.logWarning(this, "Could not write metadata to " + getEbookResource(), e);
		}
	}
}
