package org.rr.pm.image;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.rr.commons.mufs.IResourceHandler;


class DefaultImageProvider extends AImageProvider implements IImageProvider {
	
	private final String mime;
	
	/**
	 * Creates a JpgImage from a specified file name
	 *
	 * @param  resourceHandler    the name of a JPEG file
	 * @exception  IOException    if the file cannot be opened or read
	 */
	public DefaultImageProvider (IResourceHandler resourceHandler, String mime) {
		super(resourceHandler);
		this.mime = mime;
	}
	
	/**
	 * Returns the height (in pixels) of the current JpgImage object
	 * 
	 * @return  the height of the current image
	 */
	public int getHeight () {
		int height = super.getHeight();
		if(height > 0) {
			return height;
		}
		if(this.getImage()!=null) {
			return this.getImage().getHeight();
		}
		return height;
	}
	
	/**
	 * Returns the width (in pixels) of the current JpgImage object
	 * 
	 * @return  the width of the current image
	 */
	public int getWidth () {
		int width = super.getWidth();
		if(width > 0) {
			return width;
		}
		if(this.getImage()!=null) {
			return this.getImage().getWidth();
		} 
		return width;
	}
	
	/**
	 * Returns the current JpgImage object as a BufferedImage. Invoking 
	 * {@link #getImage()} the first time, the image is loaded and decoded.
	 *
	 * @return a BufferedImage representing the current JpgImage
	 */
	public BufferedImage getImage() {
		return ImageUtils.decodeImage(this.resourceLoader, mime);	
	}
}
