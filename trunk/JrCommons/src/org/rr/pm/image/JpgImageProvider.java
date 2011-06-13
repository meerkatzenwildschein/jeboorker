package org.rr.pm.image;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.rr.commons.mufs.IResourceHandler;

import com.sun.image.codec.jpeg.ImageFormatException;

class JpgImageProvider extends AImageProvider implements IImageProvider {
	
	/**
	 * Creates a JpgImage from a specified file name
	 *
	 * @param  resourceLoader    the name of a JPEG file
	 * @param gallery the gallery if the image to be displayed is associated with one. can be <code>null</code>.
	 * @exception  IOException    if the file cannot be opened or read
	 * @exception  ImageFormatException    if the JPEG file is invalid
	 */
	public JpgImageProvider (IResourceHandler resourceLoader) {
		super(resourceLoader);
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
		return this.getImage().getHeight();
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
		return this.getImage().getWidth();
	}
	
	/**
	 * Returns the current JpgImage object as a BufferedImage. Invoking 
	 * {@link #getImage()} the first time, the image is loaded and decoded.
	 *
	 * @return a BufferedImage representing the current JpgImage
	 */
	public BufferedImage getImage() {
		return ImageUtils.loadJpegImage(this.resourceLoader);
	}
}
