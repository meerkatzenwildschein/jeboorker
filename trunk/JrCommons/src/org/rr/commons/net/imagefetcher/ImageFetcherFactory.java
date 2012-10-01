package org.rr.commons.net.imagefetcher;

public class ImageFetcherFactory {

	public static enum FETCHER_TYPES {
		GOOGLE_IMAGES, BING_IMAGES
	}
	
	/**
	 * Get the image fetcher of the desired type.
	 * @param type The image fetcher type.
	 * @return The fetcher type.
	 * @see FETCHER_TYPES
	 */
	public static IImageFetcher getImageFetcher(FETCHER_TYPES type) {
		switch(type) {
			case GOOGLE_IMAGES:
				return new GoogleImageFetcher();
			case BING_IMAGES:
				return new BingImageFetcher();				
		}
		
		return null;
	}
	
}
