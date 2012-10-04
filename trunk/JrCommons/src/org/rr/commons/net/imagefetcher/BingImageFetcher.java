package org.rr.commons.net.imagefetcher;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.HTMLEntityConverter;

class BingImageFetcher extends AImageFetcher {
	
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
	private static List<IImageFetcherEntry> searchImages(String searchTerm, int page) throws IOException {
		final String encodesSearchPhrase = URLEncoder.encode(searchTerm, "UTF-8");
		final int pageParameter = ((page -1) * 30) + 1; //always in 30 steps.
		final IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceLoader("http://www.bing.com/images/async?q="+encodesSearchPhrase+"&format=htmlraw&first=" + pageParameter);
		try {
			final byte[] content = resourceLoader.getContent();
			final String contentString = new String(content);
			if(contentString.indexOf(" m=\"") != -1) {
				final String[] splited = contentString.split(" m=\"");
				final ArrayList<IImageFetcherEntry> result = new ArrayList<IImageFetcherEntry>(splited.length-1);
				for (int i = 1; i < splited.length; i++) {
					final String entry = splited[i].substring(0, splited[i].indexOf('"'));
					HTMLEntityConverter converter = new HTMLEntityConverter(entry, HTMLEntityConverter.ENCODE_EIGHT_BIT_ASCII);
					final String decodedEntry = converter.decodeEntities();
					try {
						JSONObject jobj = new JSONObject(decodedEntry);
						IImageFetcherEntry image = new BingImageFetcherEntry(jobj);
						
						result.add(image);
					} catch(JSONException e) {
						throw new IOException(e);
					}
					
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
		
		private JSONObject jobj;
		
		BingImageFetcherEntry(JSONObject jobj) {
			this.jobj = jobj;
		}

		@Override
		public URL getThumbnailURL() {
			try {
				return new URL(jobj.getString("turl"));
			} catch (Exception e) {
				return null;
			} 
		}

		@Override
		public URL getImageURL() {
			try {
				return new URL(jobj.getString("imgurl"));
			} catch (Exception e) {
				return null;
			} 
		}

		@Override
		public int getImageWidth() {
			try {
				return jobj.getInt("width");
			} catch (Exception e) {
				return 0;
			}
		}

		@Override
		public int getImageHeight() {
			try {
				return jobj.getInt("height");
			} catch (Exception e) {
				return 0;
			}
		}

		@Override
		public String getTitle() {
			try {
				return jobj.getString("t");
			} catch (JSONException e) {
				return "";
			}
		}
	}

}
