package org.rr.pm.image;

import org.rr.commons.mufs.IResourceHandler;

abstract class AImageProvider implements IImageProvider {
	
	protected IResourceHandler resourceLoader = null;
	
	/**
	 * The {@link ImageInfo} instance provding some informations from the image header.
	 */
	protected ImageInfo imageInfo;
	
	AImageProvider(IResourceHandler resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
	/**
	 * Gets the {@link IResourceHandler} which provides the data for this
	 * {@link JpgImageProvider} instance.
	 */
	@Override
	public IResourceHandler getResourceHandler() {
		return this.resourceLoader;
	}	
	
	@Override
	public int getHeight() {
		final ImageInfo imageInfo = getImageInfo();
		if(imageInfo!=null) {
			return imageInfo.getHeight();
		}
		return -1;
	}

	@Override
	public int getWidth() {
		final ImageInfo imageInfo = getImageInfo();
		if(imageInfo!=null) {
			return imageInfo.getWidth();
		}
		return -1;
	}

	/**
	 * Gets the {@link ImageInfo} instance provding some informations from the image header.
	 * 
	 * @return the desired {@link ImageInfo} instance.
	 */
	@Override
	public ImageInfo getImageInfo() {
		if(this.imageInfo==null) {
			this.imageInfo = new ImageInfo(this.getResourceHandler());
		}
		return this.imageInfo;
	}	
	
}
