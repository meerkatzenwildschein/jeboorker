package org.rr.commons.net.imagefetcher;

import java.util.ArrayList;
import java.util.List;

public class ImageFetcherFactory {

	private static interface FetcherType {

		String getName();
	}

	public static enum FETCHER_TYPES implements FetcherType {
		GOOGLE_IMAGES {

			public String getName() {
				return "Google";
			}

		},
		BING_IMAGES {

			public String getName() {
				return "Bing";
			}
		}
	}

	/**
	 * Get the image fetcher of the desired type.
	 * 
	 * @param type
	 *            The image fetcher type.
	 * @return The fetcher type.
	 * @see FETCHER_TYPES
	 */
	public static IImageFetcher getImageFetcher(FETCHER_TYPES type) {
		switch (type) {
		case GOOGLE_IMAGES:
			return new GoogleImageFetcher();
		case BING_IMAGES:
			return new BingImageFetcher();
		}

		return null;
	}
	
	public static IImageFetcher getImageFetcher(String fetcherName) {
		return getImageFetcher(fetcherName, null);
	}
	
	public static IImageFetcher getImageFetcher(String fetcherName, String searchTerm) {
		for (FETCHER_TYPES type : FETCHER_TYPES.values()) {
			if(fetcherName.equals(type.getName())) {
				IImageFetcher imageFetcher = getImageFetcher(type);
				if(searchTerm != null) {
					imageFetcher.setSearchTerm(searchTerm);
				}
				return imageFetcher;
			}
		}		
		return null;
	}

	/**
	 * Get the names of all available fetchers.
	 * @return All available fetcher names.
	 */
	public static List<String> getFetcherNames() {
		ArrayList<String> result = new ArrayList<String>();
		for (FETCHER_TYPES type : FETCHER_TYPES.values()) {
			result.add(type.getName());
		}
		return result;
	}
	
	
}
