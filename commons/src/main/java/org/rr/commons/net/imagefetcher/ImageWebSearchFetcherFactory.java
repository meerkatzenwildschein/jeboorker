package org.rr.commons.net.imagefetcher;

import java.util.ArrayList;
import java.util.List;

public class ImageWebSearchFetcherFactory implements IImageFetcherFactory {
	
	private static ImageWebSearchFetcherFactory factorySingleton;
	
	private static interface FetcherType {

		String getName();
	}

	enum FETCHER_TYPES implements FetcherType {
		BIG_BOOK_IMAGES {

			public String getName() {
				return "Big Book Search";
			}
		}
	}
	
	/**
	 * get the singleton instance for the ImageFetcherFactory here.
	 * @return
	 */
	public static ImageWebSearchFetcherFactory getInstance() {
		if(factorySingleton == null) {
			factorySingleton = new ImageWebSearchFetcherFactory();
		}
		return factorySingleton;
	}

	/**
	 * Get the image fetcher of the desired type.
	 * 
	 * @param type
	 *            The image fetcher type.
	 * @return The fetcher type.
	 * @see FETCHER_TYPES
	 */
	private IImageFetcher getImageFetcher(FETCHER_TYPES type) {
		switch (type) {
		case BIG_BOOK_IMAGES:
			return new BigBookSearchImageFetcher();
		}

		return null;
	}
	
	public IImageFetcher getImageFetcher(String fetcherName) {
		return getImageFetcher(fetcherName, null);
	}
	
	public IImageFetcher getImageFetcher(String fetcherName, String searchTerm) {
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
	 * Get the names of all available fetcher.
	 * @return All available fetcher names.
	 */
	public List<String> getFetcherNames() {
		ArrayList<String> result = new ArrayList<>();
		for (FETCHER_TYPES type : FETCHER_TYPES.values()) {
			result.add(type.getName());
		}
		return result;
	}

	@Override
	public boolean searchTermSupport() {
		return true;
	}
	
	
}
