package org.rr.commons.net.imagefetcher;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtil;

public class AbabeenImageFetcher extends AImageFetcher {

	private boolean fetched = false;

	@Override
	public List<IImageFetcherEntry> getNextEntries() throws IOException {
		if (fetched || StringUtils.isBlank(getSearchTerm())) {
			return Collections.emptyList();
		}

		return searchImages(searchTerm, 25);
	}

	private List<IImageFetcherEntry> searchImages(String searchTerm, int count) throws IOException {
		String encodesSearchPhrase = URLEncoder.encode(searchTerm, StringUtil.UTF_8);
		String urlString = "http://api.ababeen.com/api/images.php?q=" + encodesSearchPhrase + "&count=" + count;
		List<IImageFetcherEntry> result = new ArrayList<>(count);
		
		final IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceHandler(urlString);
		try {
			final byte[] content = resourceLoader.getContent();
			try {
				JSONArray json = new JSONArray(new String(content));
				for (int i = 0; i < json.length(); i++) {
					JSONObject jobj = (JSONObject) json.get(i);
					result.add(new AbabeenImageFetcherEntry(jobj));
				}
			} catch (JSONException e) {
				throw new IOException(e);
			}
		} finally {
			resourceLoader.dispose();
		}
		return result;
	}
	
	private static class AbabeenImageFetcherEntry extends AImageFetcherEntry {
		
		private JSONObject jobj;
		
		AbabeenImageFetcherEntry(JSONObject jobj) {
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
				return jobj.getString("title");
			} catch (JSONException e) {
				return EMPTY;
			}
		}
	}

}
