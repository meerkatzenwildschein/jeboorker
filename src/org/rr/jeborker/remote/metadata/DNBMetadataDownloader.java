package org.rr.jeborker.remote.metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
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
import org.rr.commons.utils.Base64;
import org.rr.commons.utils.ThreadUtils;

public class DNBMetadataDownloader implements MetadataDownloader {
	
	private static final String MAIN_URL = "http://portal.dnb.de";
	
	private static final String QUERY_URL_PART = MAIN_URL + "/opac.htm?query=";
	
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
			final Document htmlDoc = Jsoup.parse(new ByteArrayInputStream(html), "UTF-8", MAIN_URL);
			final Elements tags = htmlDoc.getElementsByTag("td");
			result.add(new MetadataDownloadEntry() {
				
				byte[] imageData = null;
				
				String isbn = null;
				
				List<String> authors = null;
				
				private Element getTag(String headline) {
					for (Element tag : tags) {
						if(tag.text().equals(headline)) {
							return tag.nextElementSibling();
						}
					}
					return null;
				}
				
				private String getValue(String headline) {
					Element tag = getTag(headline);
					if(tag != null) {
						return tag.text();
					}
					return null;
				}
				
				@Override
				public String getTitle() {
					return getValue("Titel/Bezeichnung");
				}
				
				@Override
				public byte[] getThumbnailImageBytes() {
					return getCoverImage();
				}
				
				@Override
				public String getLanguage() {
					return getValue("Sprache(n)");
				}
				
				
				public String getIsbn() {
					if(isbn == null) {
						String htmlString = htmlDoc.html();
						String search = "loadCover(\"fullRecordTable\", \"";
						int start = htmlString.indexOf(search);
						if(start != -1) {
							start += search.length();
							int end = htmlString.indexOf("\"", start);
							if(end != -1) {
								isbn = htmlString.substring(start, end);
							}
						}
					}
					return isbn;
				}
				
				@Override
				public String getIsbn13() {
					if(getIsbn().replaceAll("-", "").length() == 13) {
						return getIsbn();
					}
					
					return null;
				}
				
				@Override
				public String getIsbn10() {
					if(getIsbn().replaceAll("-", "").length() == 10) {
						return getIsbn();
					}
					
					return null;
				}
				
				@Override
				public String getDescription() {
					return null;
				}
				
				@Override
				public byte[] getCoverImage() {
					if(imageData == null) {
						String isbn = getIsbn13();
						if(isbn != null) {
							String imageUrl = "http://vlb.de/GetBlob.aspx?strIsbn=" + isbn + "&size=M";
							try {
								IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(new URL(imageUrl));
								return imageData = resourceHandler.getContent();
							} catch (MalformedURLException e) {
								LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to create URL " + imageUrl, e);
							} catch (IOException e) {
								LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to load URL " + imageUrl, e);
							}
						}
					}
					return imageData;
				}
				
				@Override
				public String getBase64EncodedThumbnailImage() {
					return Base64.encode(getCoverImage());
				}
				
				@Override
				public List<String> getAuthors() {
					if(authors == null) {
						List<String> evaluatedAuthors = new ArrayList<String>();
						Element e = getTag("Person(en)");
						if(e != null) {
							Elements authorLinks = e.getElementsByTag("a");
							if(authorLinks != null) {
								for (Element authorLink : authorLinks) {
									evaluatedAuthors.add(authorLink.text());
								}
							}
						}
						authors = evaluatedAuthors;
					}
					return authors;
				}
				
				@Override
				public String getAgeSuggestion() {
					return null;
				}
			});
		}
		return result;
	}

	private List<byte[]> loadLinkContent(Set<String> allLinks) throws IOException {
		List<byte[]> loadPages = loadPages(new TransformValueSet<String, URL>(allLinks) {

			@Override
			public URL transform(String link) {
				try {
					return new URL(MAIN_URL  + link);
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
		List<String> links = new ArrayList<String>();
		for(int i = 0; i < 50; i++) {
			Element recordLink = doc.getElementById(id + i);
			if(recordLink != null) {
				String href = recordLink.attr("href");
				if(href != null) {
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
		List<URL> urls = new ArrayList<URL>(5);
		for(int i = 0; i < 5; i++) {
			String position = "&currentPosition=" + (i * 10);
			urls.add(new URL(QUERY_URL_PART + encodesSearchPhrase + "&method=simpleSearch" + position));
		}
		return urls;
	}

	private List<byte[]> loadPages(Iterable<URL> url) throws IOException {
		final List<byte[]> results = Collections.synchronizedList(new ArrayList<byte[]>());
		ThreadUtils.RunnableImpl<URL> each = new ThreadUtils.RunnableImpl<URL>() {
			
			@Override
			public void run(URL url) {
				try {
					IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceHandler(url);
					results.add(resourceLoader.getContent());
				} catch (IOException e) {
					LoggerFactory.getLogger(this).log(Level.INFO, "Failed load " + url, e);
				}
			}
		};
		
		ThreadUtils.loop(url, each, 5);
		return results;
	}

}
