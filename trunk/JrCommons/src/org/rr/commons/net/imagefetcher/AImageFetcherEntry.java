package org.rr.commons.net.imagefetcher;

import java.io.IOException;
import java.net.URL;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;

abstract class AImageFetcherEntry implements IImageFetcherEntry {
	
	public byte[] getThumbnailImageBytes() throws IOException {
		final URL thumbnailURL = getThumbnailURL();
		if(thumbnailURL == null) {
			throw new IOException("Empty URL");
		}
		final IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceLoader(thumbnailURL);
		try {
			byte[] thumbnailBytes = resourceHandler.getContent();
			return thumbnailBytes;
		} finally {
			resourceHandler.dispose();
		}
	}	
	
	public byte[] getImageBytes() throws IOException {
		IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceLoader(getImageURL());
		try {
			byte[] imageBytes = resourceHandler.getContent();
			return imageBytes;
		} finally {
			resourceHandler.dispose();
		}
	}
}
