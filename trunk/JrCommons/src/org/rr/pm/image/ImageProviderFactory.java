package org.rr.pm.image;

import java.io.IOException;

import org.rr.commons.mufs.IResourceHandler;

import com.sun.image.codec.jpeg.ImageFormatException;

public class ImageProviderFactory {

	/**
	 * Gets an {@link IImageProvider} for the desired image file
	 * 
	 * @param resourceHandler The file for which the {@link IImageProvider} should be fetched for.
	 * @return The desired {@link IImageProvider} for the given file.
	 * @throws ImageFormatException Always if the image file format is unknown
	 * @throws IOException If the desired file could not be opened or read.
	 */
	public static IImageProvider getImageProvider(IResourceHandler resourceHandler) {
		if(resourceHandler.getMimeType().endsWith("/jpg") || resourceHandler.getMimeType().endsWith("/jpeg")) {
			return new JpgImageProvider(resourceHandler);
		} else {
			return new DefaultImageProvider(resourceHandler);
		}
	}
}
