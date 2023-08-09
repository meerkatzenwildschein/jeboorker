package org.rr.jeborker.metadata.download;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.StringUtil;

/**
 * {@link MetadataDownloader} implementation that loads metadata from the "Katalog der deutschen Nationalbibliothek".
 */
public class DNBMetadataDownloader implements MetadataDownloader {

	private static final String MAIN_URL = "http://portal.dnb.de";

	private static final String QUERY_URL = MAIN_URL + "/opac.htm?query={0}&method=simpleSearch&currentPosition={1}";

	private static final int ENTRIES_TO_FETCH = 30;
	
	private static final int ENTRIES_PER_PAGE = 10;

	private static final int PAGES_TO_LOAD = ENTRIES_TO_FETCH / ENTRIES_PER_PAGE;

	@Override
	public List<MetadataDownloadEntry> search(String phrase) {
		try {
			List<URL> searchUrl = getSearchPageUrls(phrase, PAGES_TO_LOAD, QUERY_URL);
			List<byte[]> pageHtmlContent = MetadataDownloadUtils.loadPages(searchUrl, PAGES_TO_LOAD);
			List<Document> htmlDocs = MetadataDownloadUtils.getDocuments(pageHtmlContent, MAIN_URL);
			List<String> searchResultLinks = findSearchResultLinks(htmlDocs);
			List<byte[]> metadataHtmlContent = MetadataDownloadUtils.loadLinkContent(searchResultLinks, MAIN_URL);
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
				Document htmlDoc = Jsoup.parse(new ByteArrayInputStream(html), StringUtil.UTF_8, MAIN_URL);
				Elements tags = htmlDoc.getElementsByTag("td");
				result.add(new DNBMetadataDownloadEntry(htmlDoc, tags));
			}
		}
		return result;
	}

	private List<String> findSearchResultLinks(List<Document> htmlDocs) {
		List<String> allLinks = new ArrayList<>();
		for (Document document : htmlDocs) {
			allLinks.addAll(findSearchResultLinks(document));
		}
		return allLinks;
	}

	private List<String> findSearchResultLinks(Document doc) {
		String id = "recordLink_";
		List<String> links = new ArrayList<>(ENTRIES_TO_FETCH);
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
	private static List<URL> getSearchPageUrls(String searchTerm, int pagesToLoad, String queryUrl) throws UnsupportedEncodingException,
		MalformedURLException
	{
		String encodesSearchPhrase = URLEncoder.encode(searchTerm, StringUtil.UTF_8);
		List<URL> urls = new ArrayList<>(pagesToLoad);
		for (int i = 0; i < pagesToLoad; i++) {
			String position = String.valueOf(i * 10);
			String url = MessageFormat.format(queryUrl, encodesSearchPhrase, position);
			urls.add(new URL(url));
		}
		return urls;
	}

}
