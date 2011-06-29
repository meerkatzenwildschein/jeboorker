package org.rr.jeborker.metadata;

import static org.rr.jeborker.JeboorkerConstants.MIME_EPUB;
import static org.rr.jeborker.JeboorkerConstants.MIME_PDF;

import org.rr.commons.mufs.IResourceHandler;

public class MetadataHandlerFactory {
	
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
				return new EPubMetadataReader(resource);
			} else if(resource.getMimeType().equals(MIME_PDF)) {
				return new PDFMetadataReader(resource);
			}
		}
		return null;
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
				return new EPubMetadataWriter(resource);
			} else if(resource.getMimeType().equals(MIME_PDF)) {
				return new PDFMetadataWriter(resource);
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
