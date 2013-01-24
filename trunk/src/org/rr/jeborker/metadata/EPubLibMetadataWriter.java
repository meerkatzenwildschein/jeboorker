package org.rr.jeborker.metadata;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Meta;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.zip.ZipUtils;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

class EPubLibMetadataWriter extends AEpubMetadataHandler implements IMetadataWriter {

	public EPubLibMetadataWriter(IResourceHandler ebookResourceHandler) {
		super(ebookResourceHandler);
	}

	@Override
	public void writeMetadata(List<MetadataProperty> props) {
		final IResourceHandler ebookResourceHandler = getEbookResource().get(0);
		final EpubReader reader = new EpubReader();
		
		try {
			final Book epub = reader.readEpub(ebookResourceHandler.getContentInputStream(), ebookResourceHandler.getName());
			setMetadata(epub, props);
			
			writeBook(epub, ebookResourceHandler);
			LoggerFactory.logInfo(this, "Metadata successfully written to " + ebookResourceHandler, null);
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "could not write metadata to file " + ebookResourceHandler, e);
		}
	}
	
	private void setMetadata(final Book epub, final List<MetadataProperty> props) {
		final Metadata metadata = epub.getMetadata();
		
		metadata.clearAll();
		Iterator<MetadataProperty> propsIterator = props.iterator();
		while(propsIterator.hasNext()) {
			final EpubLibMetadataProperty<?> meta = (EpubLibMetadataProperty<?>) propsIterator.next();
			
			if(EPUB_METADATA_TYPES.AUTHOR.getName().equals(meta.getName())) {
				Author author = new Author(meta.getValueAsString());
				author.setRelator(((Author) meta.getType()).getRelator());				
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
			} else if(EPUB_METADATA_TYPES.CONTRIBUTOR.getName().equals(meta.getName()) || meta.getType() instanceof Author) {
				Author author = new Author(meta.getValueAsString());
				author.setRelator(((Author) meta.getType()).getRelator());
				metadata.addContributor(author);
			} else if(EPUB_METADATA_TYPES.DATE.getName().equals(meta.getName())) {
				Date date = meta.getValueAsDate();
				if(date != null) {
					metadata.addDate(new nl.siegmann.epublib.domain.Date(date));						
				} else {
					throw new RuntimeException("Invalid date '" + meta.getValueAsString() + "'");
				}
			} else if(EPUB_METADATA_TYPES.PUBLICATION_DATE.getName().equals(meta.getName()) || EPUB_METADATA_TYPES.CREATION_DATE.getName().equals(meta.getName()) || EPUB_METADATA_TYPES.MODIFICATION_DATE.getName().equals(meta.getName())) {
				Date date = meta.getValueAsDate();
				if(date != null) {
					metadata.addDate(new nl.siegmann.epublib.domain.Date(date, ((nl.siegmann.epublib.domain.Date)meta.getType()).getEvent()));
				} else {
					metadata.addDate(new nl.siegmann.epublib.domain.Date(meta.getValueAsString(), ((nl.siegmann.epublib.domain.Date)meta.getType()).getEvent()));
				}
			} else if(EPUB_METADATA_TYPES.IDENTIFIER.getName().equals(meta.getName()) || EPUB_METADATA_TYPES.UUID.getName().equals(meta.getName()) || EPUB_METADATA_TYPES.ISBN.getName().equals(meta.getName())) {
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
			} else if(EPUB_METADATA_TYPES.FORMAT.getName().equals(meta.getName())) {
				metadata.setFormat(meta.getValueAsString());
			} else if(EPUB_METADATA_TYPES.COVER.getName().equals(meta.getName())) {
				boolean isCover = false;
				if(meta.getValues() != null && !meta.getValues().isEmpty()) {
					byte[] cover = (byte[]) meta.getValues().get(0);
					if(cover != null) {
						this.setCover(epub, cover);
						isCover = true;
					}
				}
				if(!isCover) {
					epub.setCoverImage(null);
				}
			} else {
				Meta m = new Meta( meta.getName(), meta.getValueAsString() );
				metadata.addOtherMeta(m);
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
	
	private void setCover(final Book epub, final byte[] cover) {
		try {
			final Resource oldCoverImage = epub.getCoverImage();
			
			if(oldCoverImage != null) {
				//replace old cover
				final String targetConversionMime = oldCoverImage.getMediaType().getName();
				final String oldCoverFileName = new File(oldCoverImage.getHref()).getName();
				final IImageProvider coverImageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getVirtualResourceLoader(oldCoverFileName, cover));
				final byte[] imageBytes = ImageUtils.getImageBytes(coverImageProvider.getImage(), targetConversionMime);
				oldCoverImage.setData(imageBytes);
				epub.setCoverImage(oldCoverImage);
			} else {
				//create new cover
				final IResourceHandler imageResourceLoader = ResourceHandlerFactory.getVirtualResourceLoader("DummyImageCoverName", cover);
				final String mimeType = imageResourceLoader.getMimeType();
				final String fileExtension = mimeType.substring(mimeType.indexOf('/') + 1);
				final Resource newCoverImage = new Resource(cover, new MediaType(mimeType, "." + mimeType.substring(mimeType.indexOf('/') + 1) ));
				
				String coverFilePath = getCoverFile(epub, "cover", fileExtension);
				newCoverImage.setHref(coverFilePath);
				newCoverImage.setId("cover");
				epub.setCoverImage(newCoverImage);
			}
		} catch (Exception e) {
			final IResourceHandler ebookResourceHandler = getEbookResource().get(0);
			LoggerFactory.logWarning(this.getClass(), "could not write cover for " + ebookResourceHandler.getName(), e);
		}			
	}
	
	/**
	 * Gets a cover file name with path. The path is this one where other image files also be located at if there exists any.
	 * The cover file name is tested for uniqueness in the folder.
	 */
	private static String getCoverFile(final Book epub, String desiredFileName, String fileExtension) throws IOException {
		final Collection<Resource> resources = epub.getResources().getAll();
		String path = "";
		for(Resource resource : resources) {
			if(resource.getMediaType().getName().startsWith("image")) {
				String hrefParent = new File(resource.getHref()).getParent();
				if(hrefParent != null) {
					path = hrefParent + "/";
				}
				break;
			}
		}
		
		final Object additional = new Object() {
			int count = 0;
			
			public String toString() {
				if(count == 0) {
					count ++;
					return "";
				} 
				return String.valueOf(count++);
			}
		};

		String newCoverFileName;
		while(epub.getResources().containsByHref(newCoverFileName = path + desiredFileName  + additional.toString() + "." + fileExtension )) {
		}
		return newCoverFileName;
	}

	/**
	 * Write the given content to the file in the underlying zip.
	 * @param content The content to be added to the zip archive.
	 * @param file The file name for the content in the zip archive.
	 * @throws IOException
	 */
	private void writeZipData(byte[] content, final String file) throws IOException {
		final IResourceHandler ebookResourceHandler = getEbookResource().get(0);
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
			final byte[] zipData = this.getContent(getEbookResource().get(0));
			this.writeZipData(plainMetadata, getOpfFile(zipData));
		} catch(Exception e) {
			LoggerFactory.logWarning(this, "Could not write metadata to " + getEbookResource(), e);
		}
	}
}
