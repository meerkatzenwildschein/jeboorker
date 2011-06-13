package org.rr.pm.image;

import java.awt.image.BufferedImage;

import org.rr.commons.mufs.IResourceHandler;

public interface IImageProvider {
	
	/**
	 * Gets the {@link BufferedImage} for the image provided by this {@link IImageProvider} instance.
	 * @return The desired {@link BufferedImage}.
	 */
	public BufferedImage getImage();

	/**
	 * Gets the height of the image provided by this {@link IImageProvider} instance.
	 * @return The image height.
	 */
	public int getHeight();
	
	/**
	 * Gets the width of the image provided by this {@link IImageProvider} instance.
	 * @return The image width.
	 */
	public int getWidth();
	
	/**
	 * Gets the {@link IResourceHandler} which allows to access the image resource. 
	 * @return The source {@link IResourceHandler}.
	 */
	public IResourceHandler getResourceHandler();
	
	/**
	 * Gets the {@link ImageInfo} for the image provided by this {@link IImageProvider} instance.
	 * @return The desired {@link ImageInfo}.
	 */
	public ImageInfo getImageInfo();
	
}
