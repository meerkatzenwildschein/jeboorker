package org.rr.jeborker.metadata;

import static org.rr.jeborker.JeboorkerConstants.MIME_EPUB;
import static org.rr.jeborker.JeboorkerConstants.MIME_PDF;

import java.util.List;

import org.rr.commons.mufs.IResourceHandler;

public class MetadataHandlerFactory {
	
	/**
	 * Gets a reader which supports multiple ebook resources.
	 * @param resources The resources to be handled by the {@link IMetadataReader}.
	 * @return The desired {@link IMetadataReader} instance.
	 */
	public static IMetadataReader getReader(final List<IResourceHandler> resources) {
		if(resources.size() == 1) {
			return getReader(resources.get(0));
		} else {
			return new MultiMetadataHandler(resources);
		}
	}
	
	/**
	 * Get a meta data reader for the given {@link IResourceHandler}.
	 * @param resource The resource for which a meta data reader should be fetched for.
	 * @return The desired {@link IMetadataWriter} instance or <code>null</code> if no reader is available
	 * 	for the given {@link IResourceHandler}.
	 */
	public static IMetadataReader getReader(final IResourceHandler resource) {
		final String mimeType = resource.getMimeType();
		if(mimeType!=null) {
			if(resource.getMimeType().equals(MIME_EPUB)) {
				return new EPubLibMetadataReader(resource);
			} else if(resource.getMimeType().equals(MIME_PDF)) {
				return new PDFCommonMetadataReader(resource);
			}
		}
		return null;
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
			return new MultiMetadataHandler(resources);
		}
	}	
	
	/**
	 * Get a meta data writer for the given {@link IResourceHandler}.
	 * @param resource The resource for which a meta data writer should be fetched for.
	 * @return The desired {@link IMetadataWriter} instance or <code>null</code> if no writer is available
	 * 	for the given {@link IResourceHandler}.
	 */
	public static IMetadataWriter getWriter(final IResourceHandler resource) {
		final String mimeType = resource.getMimeType();
		if(mimeType!=null) {
			if(resource.getMimeType().equals(MIME_EPUB)) {
				return new EPubLibMetadataWriter(resource);
			} else if(resource.getMimeType().equals(MIME_PDF)) {
				return new PDFCommonMetadataWriter(resource);
			}
		}
		return null;
	}
	
	/**
	 * Tells if there is cover writer support for the given resource.
	 * @param resourceHandler The resource to be tested for support.
	 * @return <code>true</code> if writer support is available and <code>false</code> otherwise.
	 */
	public static boolean hasCoverWriterSupport(final IResourceHandler resourceHandler) {
		final String mimeType = resourceHandler.getMimeType();
		if(mimeType!=null) {
			if(resourceHandler.getMimeType().equals(MIME_EPUB)) {
				return true;
			} else if(resourceHandler.getMimeType().equals(MIME_PDF)) {
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
		final String mimeType = resourceHandler.getMimeType();
		if(mimeType!=null) {
			if(resourceHandler.getMimeType().equals(MIME_EPUB)) {
				return true;
			} else if(resourceHandler.getMimeType().equals(MIME_PDF)) {
				return true;
			}
		}	
		return false;
	}	
}
