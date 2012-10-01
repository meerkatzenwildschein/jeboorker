package org.rr.commons.net.imagefetcher;

import java.io.IOException;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;

abstract class AImageFetcherEntry implements IImageFetcherEntry {

	public byte[] getThumbnailImageBytes() throws IOException {
		IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceLoader(getThumbnailURL());
		try {
			return resourceHandler.getContent();
		} finally {
			resourceHandler.dispose();
		}
	}	
	
	public byte[] getImageBytes() throws IOException {
		IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceLoader(getImageURL());
		try {
			return resourceHandler.getContent();
		} finally {
			resourceHandler.dispose();
		}
	}
}
