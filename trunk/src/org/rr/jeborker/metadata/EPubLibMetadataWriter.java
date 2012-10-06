package org.rr.jeborker.metadata;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import javax.xml.namespace.QName;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Meta;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.UtilConstants;
import org.rr.commons.utils.ZipUtils;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class EPubLibMetadataWriter extends AEpubMetadataHandler implements IMetadataWriter {

	public EPubLibMetadataWriter(IResourceHandler ebookResourceHandler) {
		super(ebookResourceHandler);
	}

	@Override
	public void writeMetadata(Iterator<MetadataProperty> props) {
		final IResourceHandler ebookResourceHandler = getEbookResource();
		final EpubReader reader = new EpubReader();
		
		try {
			final Book epub = reader.readEpub(ebookResourceHandler.getContentInputStream());
			setMetadata(epub, props);
			
			writeBook(epub, ebookResourceHandler);
			LoggerFactory.logInfo(this, "Metadata successfully written to " + ebookResourceHandler, null);
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "could not write metadata to file " + ebookResourceHandler, e);
		}
	}
	
	private void setMetadata(final Book epub, final Iterator<MetadataProperty> props) {
		final Metadata metadata = epub.getMetadata();
		
		metadata.clearAll();
		while(props.hasNext()) {
			final EpubLibMetadataProperty<?> meta = (EpubLibMetadataProperty<?>) props.next();
			if(EPUB_METADATA_TYPES.AUTHOR.getName().equals(meta.getName())) {
				Author author = new Author(meta.getValueAsString());
				author.setRelator(((Author)meta.getType()).getRelator());				
				metadata.addAuthor(author);
			} else if(EPUB_METADATA_TYPES.TITLE.getName().equals(meta.getName())) {
				metadata.addTitle(meta.getValueAsString());
			} else if(EPUB_METADATA_TYPES.DESCRIPTION.getName().equals(meta.getName())) {
				metadata.addDescription(meta.getValueAsString());
			} else if(EPUB_METADATA_TYPES.PUBLISHER.getName().equals(meta.getName())) {
				metadata.addPublisher(meta.getValueAsString());
			} else if(EPUB_METADATA_TYPES.RIGHTS.getName().equals(meta.getName())) {
				metadata.addRight(meta.getValueAsString());
			} else if(EPUB_METADATA_TYPES.SUBJECT.getName().equals(meta.getName())) {
				metadata.addSubject(meta.getValueAsString());
			} else if(EPUB_METADATA_TYPES.TYPE.getName().equals(meta.getName())) {
				metadata.addType(meta.getValueAsString());
			} else if(EPUB_METADATA_TYPES.CONTRIBUTOR.getName().equals(meta.getName())) {
				Author author = new Author(meta.getValueAsString());
				author.setRelator(((Author)meta.getType()).getRelator());
				metadata.addContributor(author);
			} else if(EPUB_METADATA_TYPES.DATE.getName().equals(meta.getName())) {
				Date date;
				if(!meta.getValues().isEmpty() && meta.getValues().get(0) instanceof Date) {
					date = (Date) meta.getValues().get(0);
				} else {
					date = DateConversionUtils.toDate(meta.getValueAsString());
				}
				
				if(date != null) {
					metadata.addDate(new nl.siegmann.epublib.domain.Date(date));
				} else {
					throw new RuntimeException("Invalid date '" + meta.getValueAsString() + "'");
				}
			} else if(EPUB_METADATA_TYPES.IDENTIFIER.getName().equals(meta.getName())) {
				Identifier identifier = new Identifier(((Identifier)meta.getType()).getScheme(), meta.getValueAsString());
				metadata.addIdentifier(identifier);
			} else if(meta.getType() instanceof QName) {
				Object type = meta.getType();
				String prefix = ((QName)type).getPrefix();
				String namespaceURI = ((QName)type).getNamespaceURI();
				String localPart = ((QName)type).getLocalPart();
				QName qName = new QName(namespaceURI, localPart, prefix);
				metadata.addOtherProperty(qName, meta.getValueAsString());
			} else if(meta.getType() instanceof Meta) {
				Object type = meta.getType();
				Meta m = new Meta( ((Meta)type).getName(), meta.getValueAsString() );
				metadata.addOtherMeta(m);
			} else if(EPUB_METADATA_TYPES.LANGUAGE.getName().equals(meta.getName())) {
				metadata.setLanguage(meta.getValueAsString());
			}  else if(EPUB_METADATA_TYPES.FORMAT.getName().equals(meta.getName())) {
				metadata.setFormat(meta.getValueAsString());
			} 
		}
	}
	
	private void writeBook(final Book epub, final IResourceHandler ebookResourceHandler) throws IOException {
		final EpubWriter writer = new EpubWriter();
		final IResourceHandler temporaryResourceLoader = ResourceHandlerFactory.getTemporaryResourceLoader(ebookResourceHandler, "tmp");
		writer.write(epub, temporaryResourceLoader.getContentOutputStream(false));
		if(temporaryResourceLoader.size() > 0) {
			temporaryResourceLoader.moveTo(ebookResourceHandler, true);
		} else {
			temporaryResourceLoader.delete();
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
				if (metadataElement != null) {
					final Element manifestElement = this.getManifestElement(document);
					final String coverNameReference = findMetadataCoverNameReference(metadataElement, document);
					final String coverName = findManifestCoverName(manifestElement, coverNameReference, document);
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
			if(tmpEbookResourceLoader.size() > 0) {
				tmpEbookResourceLoader.moveTo(ebookResourceHandler, true);
			} else {
				tmpEbookResourceLoader.delete();
			}
			
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
