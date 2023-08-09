package org.rr.jeborker.metadata.download;

import static org.rr.commons.utils.BooleanUtils.not;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.rr.commons.collection.TransformValueList;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtil;
import org.rr.commons.utils.ThreadUtils;

class MetadataDownloadUtils {

	static byte[] loadPage(URL url) throws IOException {
		try {
			LoggerFactory.getLogger().log(Level.INFO, "Downloading " + url);
			IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceHandler(url);
			if(resourceLoader != null) {
				return resourceLoader.getContent();
			}
			LoggerFactory.getLogger(MetadataDownloadUtils.class).log(Level.INFO, "No resource loader for " + url);
		} catch (IOException e) {
			LoggerFactory.getLogger(MetadataDownloadUtils.class).log(Level.INFO, "Failed load " + url, e);
		}
		return null;
	}
	
	static List<byte[]> loadPages(Iterable<URL> url, int threads) throws IOException {
		return ThreadUtils.loopAndWait(url, new ThreadUtils.RunnableImpl<URL, byte[]>() {

			@Override
			public byte[] run(URL url) {
				try {
					return loadPage(url);
				} catch (IOException e) {
					LoggerFactory.getLogger(this).log(Level.INFO, "Failed load " + url, e);
				}
				return null;
			}
		}, threads);
	}
	
	static Document getDocument(byte[] content, String url) throws IOException {
		return Jsoup.parse(new ByteArrayInputStream(content), StringUtil.UTF_8, url);
	}
	
	static List<Document> getDocuments(List<byte[]> content, String url) throws IOException {
		List<Document> documents = new ArrayList<>(content.size());
		for (byte[] bs : content) {
			if(content != null) {
				documents.add(getDocument(bs, url));
			}
		}
		return documents;
	}
	
	/**
	 * Load the content of given links. All given links has to be relative links.
	 * 
	 * @param allLinks Links to be loaded.
	 * @param url The main url for the links.
	 * @return A list with the content of all links. Can contain <code>null</code> entries if a links could not be loaded.
	 * @throws IOException
	 */
	static List<byte[]> loadLinkContent(List<String> allLinks, final String url) throws IOException {
		List<byte[]> loadPages = MetadataDownloadUtils.loadPages(new TransformValueList<String, URL>(allLinks) {

			@Override
			public URL transform(String link) {
				try {
					String absoluteUrl = url + link;
					if(not(url.endsWith("/")) || not(link.startsWith("/"))) {
						absoluteUrl = url + "/" + link;
					}
					return new URL(absoluteUrl);
				} catch (MalformedURLException e) {
					LoggerFactory.getLogger(this).log(Level.SEVERE, "Failed to create url for " + link, e);
				}
				return null;
			}
		}, 10);
		return loadPages;
	}
	
	static byte[] loadImage(String imageUrl) {
		if(imageUrl != null) {
			LoggerFactory.getLogger(MetadataDownloadUtils.class).log(Level.INFO, "Downloading image from " + imageUrl);
			try {
				IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(new URL(imageUrl));
				return resourceHandler.getContent();
			} catch (Exception e) {
				LoggerFactory.getLogger(MetadataDownloadUtils.class).log(Level.WARNING, "Failed to get image " + imageUrl);
			}
		}		
		return null;
	}
	
	static boolean isIsbn13(String isbn) {
		return Pattern.matches("97(?:8|9)([ -])\\d{1,5}\\1\\d{1,7}\\1\\d{1,6}\\1\\d", isbn);
	}
	
	static boolean isIsbn10(String isbn) {
		return Pattern.matches("^\\d{1,5}([- ])\\d{1,7}\\1\\d{1,6}\\1(\\d|X)$", isbn);
	}
}
