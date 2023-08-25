package org.rr.pm.image;

import java.io.IOException;

import org.rr.commons.mufs.IResourceHandler;


public class ImageProviderFactory {

	/**
	 * Gets an {@link IImageProvider} for the desired image file
	 * 
	 * @param resourceHandler The file for which the {@link IImageProvider} should be fetched for.
	 * @return The desired {@link IImageProvider} for the given file.
	 * @throws IOException If the desired file could not be opened or read.
	 */
	public static IImageProvider getImageProvider(IResourceHandler resourceHandler) {
		String mimeType = resourceHandler.getMimeType(true);
		if(mimeType != null && (mimeType.endsWith("/jpg") || mimeType.endsWith("/jpeg"))) {
			return new DefaultImageProvider(resourceHandler, "image/jpeg");
		} else {
			return new DefaultImageProvider(resourceHandler, mimeType);
		}
	}
}
