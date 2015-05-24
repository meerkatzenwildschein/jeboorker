package org.rr.commons.net.imagefetcher;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;

class GoogleImageFetcher extends AImageFetcher {
	
	private HashSet<URL> alreadyFetchedUrls = new HashSet<>();
	
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
	private List<IImageFetcherEntry> searchImages(String searchTerm, int page) throws IOException {
		final String encodesSearchPhrase = URLEncoder.encode(searchTerm, "UTF-8");
		final String ip = getExternalIP();
		final int pageParameter = ((page -1) * getPageSize()) + 1; //always in page size steps.
		final String urlString = "http://ajax.googleapis.com/ajax/services/search/images?v=1.0&q="+encodesSearchPhrase+"&userip=" + ip + "&rsz=8" + (page <=1 ? EMPTY : "&start=" + pageParameter);
		
		final IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceHandler(urlString);
		try {
			final byte[] content = resourceLoader.getContent();
			try {
				JSONObject json = new JSONObject(new String(content));
				if("200".equals(json.get("responseStatus").toString())) {
					final JSONObject responseData = json.getJSONObject("responseData");
					final JSONArray results = responseData.getJSONArray("results");
					final ArrayList<IImageFetcherEntry> result = new ArrayList<>(results.length());
					
					for(int i=0; i < results.length(); i++) {
						JSONObject entry = results.getJSONObject(i);
						IImageFetcherEntry image = new GoogelImageFetcherEntry(entry);
						if(!alreadyFetchedUrls.contains(image.getImageURL())) {
							result.add(image);
							alreadyFetchedUrls.add(image.getImageURL());
						}
					}
					return result;
				} else {
					throw new IOException("HTTP " + json.get("responseStatus") + " / " +  json.get("responseDetails"));
				}
			} catch (JSONException e) {
				throw new IOException(e);
			}
		} finally {
			resourceLoader.dispose();
		}
	}
	
	public List<IImageFetcherEntry> getNextEntries() throws IOException {
		if(getSearchTerm() == null || getSearchTerm().isEmpty()) {
			return Collections.emptyList();
		}
		return searchImages(getSearchTerm(), ++page);
	}	
	
	/**
	 * The implementation for one image search result entry. 
	 * {"titleNoFormatting":"Filmzitate Datenbank","tbUrl":"http://t1.gstatic.com/images?q=tbn:ANd9GcTTRnYd8MlIsYRRs66HXmpt2sldRTg4vVErab9xKVFQFZRk0bw4lrAKOz4","originalContextUrl":"http://www.filmzitate.info/suche/film-zitate.php?film_id=567","width":"337","unescapedUrl":"http://images-eu.amazon.com/images/P/B00007146P.03.LZZZZZZZ","url":"http://images-eu.amazon.com/images/P/B00007146P.03.LZZZZZZZ","visibleUrl":"www.filmzitate.info","GsearchResultClass":"GimageSearch","tbWidth":"92","content":"Diesen Film bei Amazon","title":"Filmzitate Datenbank","height":"475","imageId":"ANd9GcTTRnYd8MlIsYRRs66HXmpt2sldRTg4vVErab9xKVFQFZRk0bw4lrAKOz4","contentNoFormatting":"Diesen Film bei Amazon","tbHeight":"129"}
	 */
	private static class GoogelImageFetcherEntry extends AImageFetcherEntry {
		
		private JSONObject jobj;
		
		GoogelImageFetcherEntry(JSONObject jobj) {
			this.jobj = jobj;
		}

		@Override
		public URL getThumbnailURL() {
			try {
				return new URL(jobj.getString("tbUrl"));
			} catch (Exception e) {
				return null;
			} 
		}

		@Override
		public URL getImageURL() {
			try {
				return new URL(jobj.getString("url"));
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
				return jobj.getString("titleNoFormatting");
			} catch (JSONException e) {
				return EMPTY;
			}
		}
	}

	public int getPageSize() {
		return 8;
	}	
}
