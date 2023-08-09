package org.rr.jeborker.metadata.download;

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
import org.rr.commons.utils.StringUtil;

/**
 * {@link MetadataDownloader} implementation that loads metadata from the "Katalog der deutschen Nationalbibliothek".
 */
public class GoogleBooksDeMetadataDownloader implements MetadataDownloader {

	private static final String MAIN_URL = "http://www.google.de";

	private static final String QUERY_URL_PART = MAIN_URL + "/search?tbm=bks&q=";

	private static final int ENTRIES_TO_FETCH = 50;

	private static final int PAGES_TO_LOAD = ENTRIES_TO_FETCH / 10;
	
	private static final String PAGE_CHARSET = Charsets.ISO_8859_1.name();

	@Override
	public List<MetadataDownloadEntry> search(String phrase) {
		try {
			List<URL> searchUrl = getSearchPageUrls(phrase);
			List<byte[]> pageHtmlContent = MetadataDownloadUtils.loadPages(searchUrl, PAGES_TO_LOAD);
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
		List<MetadataDownloadEntry> result = new ArrayList<>(metadataHtmlContent.size());
		for (byte[] html : metadataHtmlContent) {
			if (html != null) {
				Document htmlDoc = Jsoup.parse(new ByteArrayInputStream(html), PAGE_CHARSET, MAIN_URL);
				GoogleBooksDeDownloadMetadataEntry entry = new GoogleBooksDeDownloadMetadataEntry(htmlDoc);
				if(StringUtil.isNotEmpty(entry.getTitle())) {
					result.add(entry);
				}
			}
		}
		return result;
	}

	private List<byte[]> loadLinkContent(Set<String> allLinks) throws IOException {
		List<byte[]> loadPages = MetadataDownloadUtils.loadPages(new TransformValueSet<String, URL>(allLinks) {

			@Override
			public URL transform(String link) {
				try {
					return new URL(link);
				} catch (MalformedURLException e) {
					LoggerFactory.getLogger(this).log(Level.SEVERE, "Failed to create url for " + link, e);
				}
				return null;
			}
		}, 10);
		return loadPages;
	}

	private Set<String> getSearchResultLinks(List<Document> htmlDocs) {
		Set<String> allLinks = new LinkedHashSet<>();
		for (Document document : htmlDocs) {
			allLinks.addAll(getSearchResultLinks(document));
		}
		return allLinks;
	}

	private List<String> getSearchResultLinks(Document doc) {
		List<String> links = new ArrayList<>(ENTRIES_TO_FETCH);
		Elements headlines = doc.getElementsByTag("h3");
		for (Element headline : headlines) {
			Element link = headline;
			while ((link = link.parent()) != null) {
				if (link.tagName().equalsIgnoreCase("a")) {
					String href = link.attr("href");
					if (href != null && href.contains("books.google.") && !href.contains("printsec="))
					{
						href = href.replaceAll("https://", "http://");
						links.add(href);
					}
					break;
				}
			}
		}
		return links;
	}

	private List<Document> getDocuments(List<byte[]> content) throws IOException {
		List<Document> documents = new ArrayList<>(content.size());
		for (byte[] bs : content) {
			documents.add(Jsoup.parse(new ByteArrayInputStream(bs), PAGE_CHARSET, MAIN_URL));
		}
		return documents;
	}

	private List<URL> getSearchPageUrls(String searchTerm) throws UnsupportedEncodingException, MalformedURLException {
		String encodesSearchPhrase = URLEncoder.encode(searchTerm, StringUtil.UTF_8);
		List<URL> urls = new ArrayList<>(PAGES_TO_LOAD);
		for (int i = 0; i < PAGES_TO_LOAD; i++) {
			String position = "&start=" + (i * 10);
			urls.add(new URL(QUERY_URL_PART + encodesSearchPhrase + position));
		}
		return urls;
	}

}
