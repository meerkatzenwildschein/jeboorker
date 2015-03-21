package org.rr.jeborker.remote.metadata;

import static org.rr.commons.utils.StringUtils.EMPTY;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.Base64;
import org.rr.commons.utils.ListUtils;

public class GoogleBooksDeDownloadMetadataEntry implements MetadataDownloadEntry {

	private Document htmlDoc;
	
	private String title;
	
	private String isbn10;
	
	private String isbn13;
	
	private String description;
	
	private List<String> authors;
	
	private byte[] thumbnailImage;
	
	GoogleBooksDeDownloadMetadataEntry(Document htmlDoc) {
		this.htmlDoc = htmlDoc;
	}
	
	private Element getValueElement(String label) {
		Elements labels = htmlDoc.getElementsByClass("metadata_label");
		for (Element l : labels) {
			if(l.tagName().equals("td") && l.text().equals(label)) {
				return l.nextElementSibling();
			}
		}
		return null;
	}
	
	@Override
	public String getTitle() {
		if(title == null) {
			title = htmlDoc.getElementsByClass("booktitle").text();
			if(title == null) {
				Element titleElement = getValueElement("Titel");
				if(titleElement != null) {
					title = titleElement.text();
				}
			}
		}
		return title;
	}

	@Override
	public List<String> getAuthors() {
		if(authors == null) {
			authors = new ArrayList<>();
			Element authorElement = getValueElement("Autoren");
			if(authorElement != null) {
				Elements links = authorElement.getElementsByTag("a");
				for (Element link : links) {
					authors.add(link.text());
				}
			}
		}
		return authors != null ? authors : Collections.<String>emptyList();
	}

	@Override
	public String getIsbn10() {
		Element isbnElement = getValueElement("ISBN");
		if(isbnElement != null) {
			List<String> isbns = ListUtils.split(isbnElement.text(), ",");
			for (String isbn : isbns) {
				if(isbn.replaceAll("-", EMPTY).length() == 10) {
					isbn10 = isbn;
				}
			}
		}
		return isbn10;
	}

	@Override
	public String getIsbn13() {
		Element isbnElement = getValueElement("ISBN");
		if(isbnElement != null) {
			List<String> isbns = ListUtils.split(isbnElement.text(), ",");
			for (String isbn : isbns) {
				if(isbn.replaceAll("-", EMPTY).length() == 13) {
					isbn13 = isbn;
				}
			}
		}
		return isbn13;
	}

	@Override
	public String getLanguage() {
		return null;
	}

	@Override
	public String getDescription() {
		Element descriptionElement = htmlDoc.getElementById("synopsistext");
		if(descriptionElement != null) {
			description = descriptionElement.text();
		}
		return description;
	}

	@Override
	public String getAgeSuggestion() {
		return null;
	}

	@Override
	public byte[] getThumbnailImageBytes() {
		if(thumbnailImage == null) {
			Element imageElement = htmlDoc.getElementById("summary-frontcover");
			if(imageElement != null) {
				String imageUrl = imageElement.attr("src");
				try {
					if(imageUrl.startsWith("http")) {
						LoggerFactory.getLogger(this).log(Level.INFO, "Downloading " + imageUrl);
						IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(new URL(imageUrl));
						thumbnailImage = resourceHandler.getContent();
					}
				} catch(Exception e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to create URL " + imageUrl, e);
				}
			}
		}
		return thumbnailImage;
	}

	@Override
	public String getBase64EncodedThumbnailImage() {
		return Base64.encode(getThumbnailImageBytes());
	}

	@Override
	public byte[] getCoverImage() {
		return getThumbnailImageBytes();
	}

}
