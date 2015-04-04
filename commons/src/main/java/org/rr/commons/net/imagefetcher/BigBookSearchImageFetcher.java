package org.rr.commons.net.imagefetcher;

import static org.rr.commons.utils.StringUtils.EMPTY;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.HTMLEntityConverter;

class BigBookSearchImageFetcher extends AImageFetcher {
	
	private int page = 0;

	/**
	 * Perform a google image search and returns the result.
	 * @param searchTerm The search phrase for the search.
	 * @param page The page number starting with 1.
	 *
	 * @return All images found by the search.
	 * @throws IOException
	 *
	 * @see https://developers.google.com/image-search/v1/jsondevguide#json_snippets_java
	 */
	private  List<IImageFetcherEntry> searchImages(String searchTerm, int page) throws IOException {
		final String encodesSearchPhrase = URLEncoder.encode(searchTerm, "UTF-8");
		
		//http://bigbooksearch.com/query.php?SearchIndex=books&Keywords=katze+tod&ItemPage=1
		String urlString = "http://bigbooksearch.com/query.php?SearchIndex=books&Keywords=" + encodesSearchPhrase + "&ItemPage=" + page;
		LoggerFactory.getLogger(this).log(Level.INFO, "Loading... " + urlString);
		final IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceHandler(urlString);
		try {
			final byte[] content = resourceLoader.getContent();
			final String contentString = new String(content);
			if(contentString.indexOf("<img id='item-") != -1) {
				final String[] splited = contentString.split("<img id='item-");
				final ArrayList<IImageFetcherEntry> result = new ArrayList<>(splited.length-1);
				for (int i = 1; i < splited.length; i++) {
					IImageFetcherEntry image = new BingImageFetcherEntry(splited[i]);
					result.add(image);
				}
				return result;
			}
		} finally {
			resourceLoader.dispose();
		}
		
		return Collections.emptyList();
	}

	public List<IImageFetcherEntry> getNextEntries() throws IOException {
		if(getSearchTerm() == null || getSearchTerm().isEmpty()) {
			return Collections.emptyList();
		}
		return searchImages(getSearchTerm(), ++page);
	}
	
	/**
	 * The implementation for one image search result entry.
	 */
	private static class BingImageFetcherEntry extends AImageFetcherEntry {
		
		private String htmlSearchEntry;
		
		BingImageFetcherEntry(String htmlSearchEntry) {
			this.htmlSearchEntry = htmlSearchEntry;
		}

		@Override
		public URL getThumbnailURL() {
			try {
				return getImageURL();
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public URL getImageURL() {
			try {
				String imageSourceURL = getParameterValue(htmlSearchEntry, "src");
				return new URL(imageSourceURL);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public int getImageWidth() {
			try {
				String imageWidth = getParameterValue(htmlSearchEntry, "width");
				return CommonUtils.toNumber(imageWidth).intValue();
			} catch (Exception e) {
				return 0;
			}
		}

		@Override
		public int getImageHeight() {
			try {
				String imageHeight = getParameterValue(htmlSearchEntry, "height");
				return CommonUtils.toNumber(imageHeight).intValue();
			} catch (Exception e) {
				return 0;
			}
		}

		@Override
		public String getTitle() {
			try {
				String imageTitle = getParameterValue(htmlSearchEntry, "alt");
				HTMLEntityConverter converter = new HTMLEntityConverter(imageTitle, HTMLEntityConverter.ENCODE_EIGHT_BIT_ASCII);
				imageTitle = converter.decodeEntities();
				return imageTitle;
			} catch (Exception e) {
				return EMPTY;
			}
		}
		
		/**
		 * get the first parameter from the given html.
		 * @param html HTML string to be searched.
		 * @param parameter Parameter where the value should be returned for.
		 * @return The value or <code>null</code> if no parameter could be found.
		 */
		private String getParameterValue(String html, String parameter) {
			int parameterStartIndex = html.indexOf(parameter + "='") + parameter.length() + 2;
			int parameterEndIndex = html.indexOf("'", parameterStartIndex);
			String value = html.substring(parameterStartIndex, parameterEndIndex);
			return value;
		}
	}

}
