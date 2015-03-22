package org.rr.jeborker.metadata.download;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.apache.commons.io.Charsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.Jeboorker;

class AmazonDeMetadataDownloader implements MetadataDownloader {

	private static final int FETCH_PAGES = 2;

	private static final String AMAZON_CONTENT_ENTRY_DIV = "result_"; //_15, _16...

	private ExecutorService pool = Jeboorker.APPLICATION_THREAD_POOL;

	private String amazonURL = "http://www.amazon.de";

	private String ageSuggestionMarker = "Vom Hersteller empfohlenes Alter:";

	private String languageMarker = "Sprache:";

	private String authorMarker = "(Autor)";

	@Override
	public List<MetadataDownloadEntry> search(String searchTerm) {
		final ArrayList<MetadataDownloadEntry> result = new ArrayList<>();
		try {
			List<Future<Elements>> fetchAmazonSearchPageDivElements = fetchAmazonSearchPageDivElements(searchTerm);
			Iterator<Future<Elements>> fetchAmazonSearchPageDivElementsIterator = fetchAmazonSearchPageDivElements.iterator();
			while(fetchAmazonSearchPageDivElementsIterator.hasNext()) {
				Future<Elements> futureDivElements = fetchAmazonSearchPageDivElementsIterator.next();
				Elements searchPageDivElements = futureDivElements.get();
				for(Element divElement : searchPageDivElements) {
					String id = divElement.id();
					String title = getTitle(divElement);
					URL targetPageUrl = getTargetPageUrl(divElement);
					URL thumbnailImageUrl = getThumbnailImageUrl(divElement);
					AmazonMetadataDownloadEntry entry = new AmazonMetadataDownloadEntry(title, id, targetPageUrl, thumbnailImageUrl);
					if(!result.contains(entry)) {
						result.add(entry);
					}
				}
			}
		} catch(Throwable e) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to fetch metadata for search '" + searchTerm + "'", e);
		}
		return result;
	}

	/**
	 * Fetch the amazon search page entries for the given search term.
	 * @param searchTerm Value to be searched.
	 */
	private List<Future<Elements>> fetchAmazonSearchPageDivElements(final String searchTerm) throws IOException, InterruptedException {
		final String encodesSearchPhrase = URLEncoder.encode(searchTerm, StringUtils.UTF_8);
		final List<Callable<Elements>> callables = new ArrayList<Callable<Elements>>(FETCH_PAGES);

		for(int i = 0; i < FETCH_PAGES; i++) {
			final int page = i;
			callables.add(new Callable<Elements>() {

				@Override
				public Elements call() throws Exception {
					byte[] fetch = loadAmazonSearchPage(encodesSearchPhrase, page);
					String html = new String(fetch, Charsets.UTF_8);
					Document htmlDoc = Jsoup.parse(html);
					Elements contentDivs = htmlDoc.getElementsByAttributeValueStarting("id", AMAZON_CONTENT_ENTRY_DIV);
					return contentDivs;
				}

		    });
		}

		List<Future<Elements>> elements = pool.invokeAll(callables);
		return elements;
	}

	/**
	 * Get the cover thumbnail url from the given div element.
	 */
	private URL getThumbnailImageUrl(Element contentDiv) throws MalformedURLException {
		Elements elementsByClass = contentDiv.getElementsByClass("productImage");
		for(Element e : elementsByClass) {
			if(e.tagName().equals("img")) {
				return new URL(e.attr("src"));
			}
		}
		return null;
	}

	/**
	 * Get the URL for the target page which contains all the details to this div.
	 */
	private URL getTargetPageUrl(Element contentDiv) throws MalformedURLException {
		Elements elementsByClass = contentDiv.getElementsByClass("newaps");
		for(Element e : elementsByClass) {
			if(e.tagName().startsWith("h")) {
				Elements links = e.getElementsByTag("a");
				for(Element link : links) {
					return new URL(link.attr("href"));
				}
			}
		}
		return null;
	}

	/**
	 * Get the headline / title provided by the given div element.
	 */
	private String getTitle(Element contentDiv) {
		Elements elementsByClass = contentDiv.getElementsByClass("lrg");
		for(Element e : elementsByClass) {
			if(e.classNames().contains("bold") && e.tagName().equals("span")) {
				return e.text();
			}
		}
		return null;
	}

	/**
	 * Loads the amazon search page html bytes for the given page number.
	 */
	private byte[] loadAmazonSearchPage(final String encodesSearchPhrase, final int page) throws IOException {
		//http://www.amazon.de /s/ref=nb_sb_noss_1?field-keywords=die+orks&rh=n%3A186606
		//http://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=Terry+Pratchett
		final String urlString = getAmazonURL() + "/s/ref=nb_sb_noss?url=search-alias%3Daps&ie=UTF8&field-keywords=" + encodesSearchPhrase + "&page=" + page;
		LoggerFactory.getLogger(this).log(Level.INFO, "Loading... " + urlString);
		final IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceHandler(urlString);
		final byte[] content = resourceLoader.getContent();
		return content;
	}

	private class AmazonMetadataDownloadEntry implements MetadataDownloadEntry {

		private final Future<Document> amazonDetailPageDocument;

		private final Future<byte[]> thumbnailBytes;

		private final String title;

		private final String id;

		private Document document;

		AmazonMetadataDownloadEntry(final String title, final String id, final URL targetPageURL, final URL thumbnailImageURL) {
			this.title = title;
			this.id = id;
			thumbnailBytes = pool.submit(new Callable<byte[]>() {

				@Override
				public byte[] call() throws Exception {
					if(thumbnailImageURL != null) {
						try {
							LoggerFactory.getLogger(this).log(Level.INFO, "Loading... " + thumbnailImageURL);
							IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(thumbnailImageURL);
							return resourceHandler.getContent();
						} catch(Exception e) {
							LoggerFactory.getLogger().log(Level.WARNING, "Failed to load image " + thumbnailImageURL, e);
						}
					}
					return null;
				}
			});
			amazonDetailPageDocument = pool.submit(new Callable<Document>() {

				@Override
				public Document call() throws Exception {
					if(targetPageURL != null) {
						try {
							IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(targetPageURL);
							byte[] content = resourceHandler.getContent();
							String html = new String(content, "ISO-8859-15");
							return Jsoup.parse(html);
						} catch(Exception e) {
							LoggerFactory.getLogger().log(Level.WARNING, "Failed to load url " + thumbnailImageURL, e);
						}
					}
					return null;
				}
			});
		}

		@Override
		public byte[] getThumbnailImageBytes() {
			try {
				return thumbnailBytes.get();
			} catch (Exception e) {
				LoggerFactory.getLogger().log(Level.WARNING, "Failed to fetch thumbnail image for '" + getTitle() + "'", e);
			}
			return null;
		}

		@Override
		public String getTitle() {
			return this.title;
		}

		@Override
		public List<String> getAuthors() {
			ArrayList<String> result = new ArrayList<>(1);
			try {
				if(getDocument() != null) {
					Elements h1Authors = getDocument().getElementsByClass("parseasinTitle");
					if(!h1Authors.isEmpty()) {
						Elements siblingElements = h1Authors.first().siblingElements();
						for(Element sibling : siblingElements) {
							if("span".equals(sibling.tagName())) {
								Elements aElements = sibling.getElementsByTag("a");
								for(Element aElement : aElements) {
									String href = aElement.attr("href"); ///s/ref=ntt_athr_dp_sr_1?_encoding=UTF8&field-author=Olivier%20Peru&search-alias=books-de&sort=relevancerank
									int authorIdx = href.indexOf("field-author");
									if(authorIdx != -1) {
										Element nextElementSibling = aElement.nextElementSibling();
										if(nextElementSibling != null && nextElementSibling.text().contains(getAuthorMarker())) {
											String author = href.substring(authorIdx + 13, href.indexOf("&", authorIdx));
											result.add(URLDecoder.decode(author, StringUtils.UTF_8));
										}
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				LoggerFactory.getLogger().log(Level.WARNING, "Failed to fetch author for '" + getTitle() + "'", e);
			}
			return result;
		}

		@Override
		public String getDescription() {
			try {
				if(getDocument() != null) {
					Element details = getDocument().getElementById("postBodyPS");
					if(details != null) {
						return details.text();
					}
				}
			} catch (Exception e) {
				LoggerFactory.getLogger().log(Level.WARNING, "Failed to fetch author for '" + getTitle() + "'", e);
			}

			return null;
		}

		@Override
		public String getIsbn10() {
			return getProductInformationValue("ISBN-10:");
		}

		@Override
		public String getIsbn13() {
			return getProductInformationValue("ISBN-13:");
		}

		@Override
		public String getAgeSuggestion() {
			return getProductInformationValue(getAgeSuggestionMarker());
		}

		@Override
		public String getLanguage() {
			return getProductInformationValue(getLanguageMarker());
		}

		@Override
		public byte[] getCoverImage() {
			try {
				if(getDocument() != null) {
					Element img = getDocument().getElementById("main-image");
					if(img == null) {
						img = getDocument().getElementById("original-main-image");
					}
					if(img != null) {
						String src = img.attr("src");
						String coverURL;
						if(src.indexOf(',') != -1 && src.indexOf('.') != -1) {
							coverURL = src.substring(0, src.indexOf(',')) + src.substring(src.lastIndexOf('.'));
						} else {
							coverURL = src;
						}
						IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(coverURL);
						return resourceHandler.getContent();
					} else {
						LoggerFactory.getLogger().log(Level.WARNING, "Could not find cover image for '" + getTitle() + "'");
					}
				}
			} catch (Exception e) {
				LoggerFactory.getLogger().log(Level.WARNING, "Failed to fetch cover for '" + getTitle() + "'", e);
			}
			return null;
		}

		/**
		 * Get a value from the product info box at the amazon page.
		 * @param label The label for the value.
		 * @return The desired value or <code>null</code> if the value did not exists.
		 */
		private String getProductInformationValue(String label) {
			try {
				if(getDocument() != null) {
					Elements bucketElements = getDocument().getElementsByClass("bucket");
					for(Element bucketElement : bucketElements) {
						if(bucketElement.tagName().equals("td") && bucketElement.firstElementSibling() == null) {
							Elements b;
							if(!(b = bucketElement.getElementsContainingOwnText(label)).isEmpty()) {
								return b.first().parent().ownText();
							}
						}
					}
				}
			} catch (Exception e) {
				LoggerFactory.getLogger().log(Level.WARNING, "Failed to fetch " + label + " for '" + getTitle() + "'", e);
			}
			return null;
		}

		private Document getDocument() throws InterruptedException, ExecutionException {
			if(document == null) {
				document = amazonDetailPageDocument.get();
			}
			return document;
		}

		@Override
		public boolean equals(Object obj) {
			if(super.equals(obj)) {
				return true;
			} else if(obj == null) {
				return false;
			} else if(!(obj instanceof AmazonMetadataDownloadEntry)) {
				return false;
			} else if(this.id != null) {
				return this.id.equals(((AmazonMetadataDownloadEntry)obj).id);
			}
			return false;
		}

	}

	public String getAmazonURL() {
		return amazonURL;
	}

	public String getLanguageMarker() {
		return languageMarker;
	}

	public String getAuthorMarker() {
		return authorMarker;
	}

	public String getAgeSuggestionMarker() {
		return ageSuggestionMarker;
	}

}
