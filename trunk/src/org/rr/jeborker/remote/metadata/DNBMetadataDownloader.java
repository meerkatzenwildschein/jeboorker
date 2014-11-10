package org.rr.jeborker.remote.metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.io.Charsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rr.commons.collection.TransformValueSet;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ThreadUtils;

/**
 * {@link MetadataDownloader} implementation that loads metadata from the "Katalog der deutschen Nationalbibliothek".
 */
public class DNBMetadataDownloader implements MetadataDownloader {

	private static final String MAIN_URL = "http://portal.dnb.de";

	private static final String QUERY_URL_PART = MAIN_URL + "/opac.htm?query=";

	private static final int ENTRIES_TO_FETCH = 30;

	private static final int PAGES_TO_LOAD = ENTRIES_TO_FETCH / 10;

	@Override
	public List<MetadataDownloadEntry> search(String phrase) {
		try {
			List<URL> searchUrl = getSearchPageUrls(phrase);
			List<byte[]> pageHtmlContent = loadPages(searchUrl);
			List<Document> htmlDocs = getDocuments(pageHtmlContent);
			Set<String> allLinks = getSearchResultLinks(htmlDocs);
			List<byte[]> metadataHtmlContent = loadLinkContent(allLinks);
			return getMetadataDownloadEntries(metadataHtmlContent);
		} catch (IOException e) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to fetch metadata for search '" + phrase + "'", e);
		}
		return null;
	}

	private List<MetadataDownloadEntry> getMetadataDownloadEntries(List<byte[]> metadataHtmlContent) throws IOException {
		List<MetadataDownloadEntry> result = new ArrayList<MetadataDownloadEntry>(metadataHtmlContent.size());
		for (byte[] html : metadataHtmlContent) {
			if (html != null) {
				Document htmlDoc = Jsoup.parse(new ByteArrayInputStream(html), "UTF-8", MAIN_URL);
				Elements tags = htmlDoc.getElementsByTag("td");
				result.add(new DNBMetadataDownloadEntry(htmlDoc, tags));
			}
		}
		return result;
	}

	private List<byte[]> loadLinkContent(Set<String> allLinks) throws IOException {
		List<byte[]> loadPages = loadPages(new TransformValueSet<String, URL>(allLinks) {

			@Override
			public URL transform(String link) {
				try {
					return new URL(MAIN_URL + link);
				} catch (MalformedURLException e) {
					LoggerFactory.getLogger(this).log(Level.SEVERE, "Failed to create url for " + link, e);
				}
				return null;
			}
		});
		return loadPages;
	}

	private Set<String> getSearchResultLinks(List<Document> htmlDocs) {
		Set<String> allLinks = new LinkedHashSet<String>();
		for (Document document : htmlDocs) {
			allLinks.addAll(getSearchResultLinks(document));
		}
		return allLinks;
	}

	private List<String> getSearchResultLinks(Document doc) {
		String id = "recordLink_";
		List<String> links = new ArrayList<String>(ENTRIES_TO_FETCH);
		for (int i = 0; i < ENTRIES_TO_FETCH; i++) {
			Element recordLink = doc.getElementById(id + i);
			if (recordLink != null) {
				String href = recordLink.attr("href");
				if (href != null) {
					links.add(href);
				}
			}
		}
		return links;
	}

	private List<Document> getDocuments(List<byte[]> content) throws IOException {
		List<Document> documents = new ArrayList<Document>(content.size());
		for (byte[] bs : content) {
			documents.add(Jsoup.parse(new ByteArrayInputStream(bs), "UTF-8", MAIN_URL));
		}
		return documents;
	}

	private List<URL> getSearchPageUrls(String searchTerm) throws UnsupportedEncodingException, MalformedURLException {
		String encodesSearchPhrase = URLEncoder.encode(searchTerm, Charsets.UTF_8.name());
		List<URL> urls = new ArrayList<URL>(PAGES_TO_LOAD);
		for (int i = 0; i < PAGES_TO_LOAD; i++) {
			String position = "&currentPosition=" + (i * 10);
			urls.add(new URL(QUERY_URL_PART + encodesSearchPhrase + "&method=simpleSearch" + position));
		}
		return urls;
	}

	private List<byte[]> loadPages(Iterable<URL> url) throws IOException {
		return ThreadUtils.loopAndWait(url, new ThreadUtils.RunnableImpl<URL, byte[]>() {

			@Override
			public byte[] run(URL url) {
				try {
					IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceHandler(url);
					return resourceLoader.getContent();
				} catch (IOException e) {
					LoggerFactory.getLogger(this).log(Level.INFO, "Failed load " + url, e);
				}
				return null;
			}
		}, PAGES_TO_LOAD);
	}

}
