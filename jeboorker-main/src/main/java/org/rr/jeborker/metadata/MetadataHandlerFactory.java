package org.rr.jeborker.metadata;

import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_CBR;
import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_CBZ;
import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_EPUB;
import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_HTML;
import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_PDF;
import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_MOBI;
import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_AZW;

import java.util.Collections;
import java.util.List;

import org.rr.commons.collection.TransformValueList;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;

public class MetadataHandlerFactory {
	
	private static IMetadataReader latestReader = null;
	
	/**
	 * Gets a reader which supports multiple ebook resources.
	 * @param items The items to be handled by the result {@link IMetadataReader}.
	 * @return The desired {@link IMetadataReader} instance.
	 */
	public static IMetadataReader getReaderForEbookPropertyItems(final List<EbookPropertyItem> items) {
		return getReaderForIResourceHandlers(new TransformValueList<EbookPropertyItem, IResourceHandler>(items) {

			@Override
			public IResourceHandler transform(EbookPropertyItem source) {
				return source.getResourceHandler();
			}
		});
	}
	
	/**
	 * Gets a reader which supports multiple ebook resources.
	 * @param resources The resources to be handled by the result {@link IMetadataReader}.
	 * @return The desired {@link IMetadataReader} instance.
	 */
	public static IMetadataReader getReaderForIResourceHandlers(final List<IResourceHandler> resources) {
		IMetadataReader cachedReader = null;
		if((cachedReader = getCachedReader(resources)) != null) {
			return cachedReader;
		}
		
		if(resources.size() == 1) {
			return latestReader = getReader(resources.get(0));
		} else {
			return latestReader = new MultiMetadataHandler(resources);
		}
	}
	
	/**
	 * Get a meta data reader for the given {@link IResourceHandler}.
	 * @param resource The resource for which a meta data reader should be fetched for.
	 * @return The desired {@link IMetadataWriter} instance or <code>null</code> if no reader is available
	 * 	for the given {@link IResourceHandler}.
	 */
	public static IMetadataReader getReader(final IResourceHandler resource) {
		IMetadataReader cachedReader = null;
		if((cachedReader = getCachedReader(Collections.singletonList(resource))) != null) {
			return cachedReader;
		}
		
		final String mimeType = resource.getMimeType(true);
		if (mimeType != null) {
			latestReader = null;
			if(resource.getMimeType(true).equals(MIME_EPUB.getMime())) {
				return latestReader = new EPubLibMetadataReader(resource);
			} else if(resource.getMimeType(true).equals(MIME_PDF.getMime())) {
				return latestReader = new PDFCommonMetadataReader(resource);
			} else if(resource.getMimeType(true).equals(MIME_CBZ.getMime()) || resource.getMimeType(true).equals(MIME_CBR.getMime())) {
				return latestReader = new ComicBookMetadataReader(resource);
			} else if(resource.getMimeType(true).equals(MIME_HTML.getMime())) {
				return latestReader = new HTMLMetadataReader(resource);
			} else if(resource.getMimeType(true).equals(MIME_MOBI.getMime()) || resource.getMimeType(true).equals(MIME_AZW.getMime())) {
				return latestReader = new MobiMetadataReader(resource);
			}  
		}
		return latestReader = new EmptyMetadataReader(resource);
	}
	
	/**
	 * Get a meta data writer for the given {@link IResourceHandler}.
	 * @param resources The resources for which a meta data writer should be fetched for.
	 * @return The desired {@link IMetadataWriter} instance or <code>null</code> if no writer is available
	 * 	for the given {@link IResourceHandler}.
	 */
	public static IMetadataWriter getWriter(final List<IResourceHandler> resources) {
		if(resources.size() == 1) {
			return getWriter(resources.get(0));
		} else {
			return wrap(new MultiMetadataHandler(resources));
		}
	}	
	
	/**
	 * Get a meta data writer for the given {@link IResourceHandler}.
	 * @param resource The resource for which a meta data writer should be fetched for.
	 * @return The desired {@link IMetadataWriter} instance or <code>null</code> if no writer is available
	 * 	for the given {@link IResourceHandler}.
	 */
	public static IMetadataWriter getWriter(final IResourceHandler resource) {
		final String mimeType = resource.getMimeType(true);
		if(mimeType!=null) {
			if(resource.getMimeType(true).equals(MIME_EPUB.getMime())) {
				return wrap(new EPubLibMetadataWriter(resource));
			} else if(resource.getMimeType(true).equals(MIME_PDF.getMime())) {
				return wrap(new PDFCommonMetadataWriter(resource));
			} else if(resource.getMimeType(true).equals(MIME_CBZ.getMime()) || resource.getMimeType(true).equals(MIME_CBR.getMime())) {
				return wrap(new ComicBookMetadataWriter(resource));
			} else if(resource.getMimeType(true).equals(MIME_MOBI.getMime()) || resource.getMimeType(true).equals(MIME_AZW.getMime())) {
				return wrap(new MobiMetadataWriter(resource));
			}
		}
		return null;
	}
	
	/**
	 * Tells if there is writer support for at least one of the given {@link IResourceHandler}.
	 */
	public static boolean hasWriterSupport(final List<IResourceHandler> resources) {
		for(int i = 0; i < resources.size(); i++) {
			final IResourceHandler resource = resources.get(i);
			if(getWriter(resource) != null) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tells if there is cover writer support for the given resource.
	 * @param resource The resource to be tested for support.
	 * @return <code>true</code> if writer support is available and <code>false</code> otherwise.
	 */
	public static boolean hasCoverWriterSupport(final IResourceHandler resource) {
		final String mimeType = resource.getMimeType(true);
		if(mimeType != null) {
			if(resource.getMimeType(true).equals(MIME_EPUB.getMime())) {
				return true;
			} else if(resource.getMimeType(true).equals(MIME_PDF.getMime())) {
				return true;
			} else if(resource.getMimeType(true).equals(MIME_MOBI.getMime()) || resource.getMimeType(true).equals(MIME_AZW.getMime())) {
				return true;
			}
		}	
		return false;
	}
	
	/**
	 * Tells if there is cover writer support for the given resource.
	 * @param resourceHandler The resource to be tested for support.
	 * @return <code>true</code> if writer support is available and <code>false</code> otherwise.
	 */
	public static boolean hasPlainMetadataSupport(final IResourceHandler resourceHandler) {
		final String mimeType = resourceHandler.getMimeType(true);
		if(mimeType!=null) {
			if(resourceHandler.getMimeType(true).equals(MIME_EPUB.getMime())) {
				return true;
			} else if(resourceHandler.getMimeType(true).equals(MIME_PDF.getMime())) {
				return true;
			} else if(resourceHandler.getMimeType(true).equals(MIME_CBZ.getMime()) || resourceHandler.getMimeType(true).equals(MIME_CBR.getMime())) {
				return true;
			} else if(resourceHandler.getMimeType(true).equals(MIME_HTML.getMime())) {
				return true;
			}
		}	
		return false;
	}	
	
	/**
	 * The latest {@link IMetadataReader} instance is cached and always this one is delivered if it's already usable.
	 * @param resourceHandler The {@link IResourceHandler} for the requested {@link IMetadataReader} instance.
	 * @return The cached {@link IMetadataReader} or <code>null</code> if the latest {@link IMetadataReader} is no longer usable.
	 */
	private static IMetadataReader getCachedReader(final List<IResourceHandler> resourceHandler) {
		if(latestReader != null && latestReader.getEbookResource() != null) {
			if(resourceHandler.size() > 1 && resourceHandler.size() == latestReader.getEbookResource().size()) {
				List<IResourceHandler> ebookResource = latestReader.getEbookResource();
				List<IResourceHandler> difference = ListUtils.difference(ebookResource, resourceHandler);
				if(difference.isEmpty()) {
					return latestReader;
				}
			} else if(latestReader.getEbookResource().size() == 1 && resourceHandler.size() == 1 && resourceHandler.get(0).equals(latestReader.getEbookResource().get(0))) {
				return latestReader;
			}
		}
		return null;
	}
	
	/**
	 * Wrap the given {@link IMetadataWriter} with the {@link MetadataWriterWrapper}.
	 * @param writer The writer instance to be wrapped.
	 * @return The MetadataWriterWrapper wrapping the given {@link IMetadataWriter} instance.
	 */
	private static IMetadataWriter wrap(final IMetadataWriter writer) {
		return new MetadataWriterWrapper(writer);
	}
	
	/**
	 * Wrapper for all {@link IMetadataWriter}. It's needed for resetting the cached {@link IMetadataReader} instance
	 * because the reader data are out of date after a writer does it's write.
	 */
	private static class MetadataWriterWrapper implements IMetadataWriter {

		private IMetadataWriter writer;
		
		MetadataWriterWrapper(IMetadataWriter writer) {
			this.writer = writer;
		}
		
		@Override
		public void writeMetadata(List<MetadataProperty> props) {
			writer.writeMetadata(props);
			latestReader = null;
		}

		@Override
		public void storePlainMetadata(byte[] plainMetadata) {
			writer.storePlainMetadata(plainMetadata);
			latestReader = null;
		}
	}

}
