package org.rr.jeborker.metadata;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.rr.commons.utils.StringUtil.EMPTY;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.xml.namespace.QName;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Meta;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.epub.EpubWriter;

import org.apache.commons.lang3.StringUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.Base64;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.compression.truezip.TrueZipUtils;
import org.rr.jeborker.app.FileRefreshBackground;
import org.rr.jeborker.gui.MainController;
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
		
		try {
			boolean lazy = ebookResourceHandler.size() > 10000000; //10MB
			final Book epub = readBook(ebookResourceHandler.getContentInputStream(), ebookResourceHandler, lazy);
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
					LoggerFactory.log(Level.WARNING, this, "Skipping invalid date '" + meta.getValueAsString() + "' at " + getEbookResource().get(0));
				}
			} else if(EPUB_METADATA_TYPES.PUBLICATION_DATE.getName().equals(meta.getName()) || EPUB_METADATA_TYPES.CREATION_DATE.getName().equals(meta.getName()) || EPUB_METADATA_TYPES.MODIFICATION_DATE.getName().equals(meta.getName())) {
				Date date = meta.getValueAsDate();
				if(date != null) {
					metadata.addDate(new nl.siegmann.epublib.domain.Date(date, ((nl.siegmann.epublib.domain.Date)meta.getType()).getEvent()));
				} else {
					metadata.addDate(new nl.siegmann.epublib.domain.Date(meta.getValueAsString(), ((nl.siegmann.epublib.domain.Date)meta.getType()).getEvent()));
				}
			} else if(EPUB_METADATA_TYPES.IDENTIFIER.getName().equals(meta.getName()) || EPUB_METADATA_TYPES.UUID.getName().equals(meta.getName()) || EPUB_METADATA_TYPES.ISBN.getName().equals(meta.getName())) {
				Identifier identifier = new Identifier(((Identifier) meta.getType()).getScheme(), meta.getValueAsString());
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
				if(StringUtils.equals(m.getName(), "calibre:timestamp")) {
					String formattedDate = DateConversionUtils.toString(meta.getValueAsDate(), DateConversionUtils.DATE_FORMATS.W3C_MILLISECOND);
					m.setContent(isNotBlank(formattedDate) ? formattedDate : meta.getValueAsString());
				}
				metadata.addOtherMeta(m);
			} else if(EPUB_METADATA_TYPES.LANGUAGE.getName().equals(meta.getName())) {
				metadata.setLanguage(meta.getValueAsString());
			} else if(EPUB_METADATA_TYPES.FORMAT.getName().equals(meta.getName())) {
				metadata.setFormat(meta.getValueAsString());
			} else if(EPUB_METADATA_TYPES.COVER.getName().equals(meta.getName())) {
				boolean isCover = false;
				if(meta.getValues() != null && !meta.getValues().isEmpty()) {
					Object coverObjectValue = meta.getValues().get(0);
					byte[] cover = null;
					if(coverObjectValue instanceof byte[]) {
						cover = (byte[]) meta.getValues().get(0);
					} else if(coverObjectValue instanceof String) {
						cover = Base64.decode((String) coverObjectValue);
					}
					if(cover != null) {
						this.setCover(epub, meta, cover);
						isCover = true;
					}
				}
				if(!isCover) {
					epub.setCoverImage(null);
				}
			} else {
				Meta m = new Meta(meta.getName(), meta.getValueAsString());
				metadata.addOtherMeta(m);
			}
		}
	}

	private void writeBook(final Book epub, final IResourceHandler ebookResourceHandler) throws IOException {
		FileRefreshBackground.setDisabled(true);
		try {
			final EpubWriter writer = new EpubWriter();
			final IResourceHandler temporaryResourceLoader = ResourceHandlerFactory.getUniqueResourceHandler(ebookResourceHandler, "tmp");
			writer.write(epub, temporaryResourceLoader.getContentOutputStream(false));
			if(temporaryResourceLoader.size() > 0) {
				temporaryResourceLoader.moveTo(ebookResourceHandler, true);
			} else {
				temporaryResourceLoader.delete();
			}
		} finally {
			FileRefreshBackground.setDisabled(false);
		}
	}
	
	private void setCover(final Book epub, final EpubLibMetadataProperty<?> meta, final byte[] cover) {
		try {
			if(meta.getHint(MetadataProperty.HINTS.COVER_FROM_EBOOK_FILE_NAME) != null) {
				this.changeExistingCover(epub, meta);
			} else {
				final Resource oldCoverImage = epub.getCoverImage();
				if(oldCoverImage != null) {
					if(differes(oldCoverImage.getData(), cover)) {
						String message = Bundle.getString("EPubLibMetadataWriter.overwriteCover.message");
						String title = Bundle.getString("EPubLibMetadataWriter.overwriteCover.title");
						int result = MainController.getController().showMessageBox(message, title, JOptionPane.YES_NO_CANCEL_OPTION, "EPubLibMetadataWriter_Cover_Ovwerwrite_Key", -1, true);
						 
						if(result == JOptionPane.YES_OPTION) {
							this.replaceOldCover(epub, cover, oldCoverImage);
						} else if(result == JOptionPane.NO_OPTION) {
							this.createNewCover(epub, cover);
						} else {
							return;
						}						
					} else {
						epub.setCoverImage(oldCoverImage);
					}
				} else {
					this.createNewCover(epub, cover);
				}
			}
		} catch (Exception e) {
			final IResourceHandler ebookResourceHandler = getEbookResource().get(0);
			LoggerFactory.logWarning(getClass(), "could not write cover for " + ebookResourceHandler.getName(), e);
		}			
	}
	
	private boolean differes(byte[] source, byte[] target) {
		if(source.length != target.length) {
			return true;
		}
		for(int i = 0; i < source.length; i++) {
			if(source[i] != target[i]) {
				return true;
			}
		}
		return false;
	}
	
	private void createNewCover(final Book epub, final byte[] cover) throws IOException {
		final IResourceHandler imageResourceLoader = ResourceHandlerFactory.getVirtualResourceHandler("DummyImageCoverName", cover);
		final String mimeType = imageResourceLoader.getMimeType(true);
		final String fileExtension = mimeType.substring(mimeType.indexOf('/') + 1);
		final Resource newCoverImage = new Resource(cover, new MediaType(mimeType, "." + mimeType.substring(mimeType.indexOf('/') + 1) ));
		
		String coverFilePath = getCoverFile(epub, "cover", fileExtension);
		newCoverImage.setHref(coverFilePath);
		newCoverImage.setId("cover");
		epub.setCoverImage(newCoverImage);
	}

	private void replaceOldCover(final Book epub, final byte[] cover, final Resource oldCoverImage) throws IOException {
		final String targetConversionMime = oldCoverImage.getMediaType().getName();
		final String oldCoverFileName = new File(oldCoverImage.getHref()).getName();
		final IImageProvider coverImageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getVirtualResourceHandler(oldCoverFileName, cover));
		final byte[] imageBytes = ImageUtils.getImageBytes(coverImageProvider.getImage(), targetConversionMime);
		oldCoverImage.setData(imageBytes);
		epub.setCoverImage(oldCoverImage);
	}

	private void changeExistingCover(final Book epub, final EpubLibMetadataProperty<?> meta) {
		//use existing cover
		String coverName = (String) meta.getHint(MetadataProperty.HINTS.COVER_FROM_EBOOK_FILE_NAME);
		if(coverName.contains("//")) {
			coverName = coverName.substring(coverName.indexOf("//") + 2);
		}
		Resources resources = epub.getResources();
		Resources unlistedResources = epub.getUnlistedResources();
		Resource coverImage = resources.getByHref(coverName);
		Resource unlistedCoverImage = unlistedResources.getByHref(coverName);
		if(coverImage != null) {
			epub.setCoverImage(coverImage);
		} else if(unlistedCoverImage != null) {
			String href = unlistedCoverImage.getHref();
			unlistedResources.remove(href);
			if(href.indexOf('/') != -1) {
				href = href.substring(href.indexOf('/') + 1);
			}
			unlistedCoverImage.setHref(href);
			epub.setCoverImage(unlistedCoverImage);
		} else {
			while(coverName.indexOf('/') != -1) {
				coverName = coverName.substring(coverName.indexOf('/') + 1);
				coverImage = resources.getByHref(coverName) != null ? resources.getByHref(coverName)  : unlistedResources.getByHref(coverName);
				if(coverImage != null) {
					epub.setCoverImage(coverImage);
					break;
				}
			}
		}
	}
	
	/**
	 * Gets a cover file name with path. The path is this one where other image files also be located at if there exists any.
	 * The cover file name is tested for uniqueness in the folder.
	 */
	private static String getCoverFile(final Book epub, String desiredFileName, String fileExtension) throws IOException {
		final Collection<Resource> resources = epub.getResources().getAll();
		String path = EMPTY;
		for(Resource resource : resources) {
			if(resource.getMediaType() != null && resource.getMediaType().getName() != null && resource.getMediaType().getName().startsWith("image")) {
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
					return EMPTY;
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
		TrueZipUtils.add(ebookResourceHandler, file, new ByteArrayInputStream(content));
	}

	@Override
	public void storePlainMetadata(byte[] plainMetadata) {
		try {
			final IResourceHandler ebookResourceHandler = getEbookResource().get(0);
			this.writeZipData(plainMetadata, getOpfFile(ebookResourceHandler));
		} catch(Exception e) {
			LoggerFactory.logWarning(this, "Could not write metadata to " + getEbookResource(), e);
		}
	}
}
