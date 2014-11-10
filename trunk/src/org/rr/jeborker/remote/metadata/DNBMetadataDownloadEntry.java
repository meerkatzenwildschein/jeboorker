package org.rr.jeborker.remote.metadata;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.Base64;

public class DNBMetadataDownloadEntry implements MetadataDownloadEntry {

	private static final String IMAGE_SIZE_S = "S";

	private static final String IMAGE_SIZE_L = "L";

	private Elements tags;

	private Document htmlDoc;

	private byte[] thumbnailImageData = null;

	private byte[] largeImageData = null;

	private String isbn = null;

	private List<String> authors = null;

	DNBMetadataDownloadEntry(Document htmlDoc, Elements tags) {
		this.tags = tags;
		this.htmlDoc = htmlDoc;
	}

	private Element getTag(String headline) {
		for (Element tag : tags) {
			if (tag.text().equals(headline)) {
				return tag.nextElementSibling();
			}
		}
		return null;
	}

	private String getValue(String headline) {
		Element tag = getTag(headline);
		if (tag != null) {
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
		return getCoverImage(IMAGE_SIZE_S);
	}

	@Override
	public String getLanguage() {
		return getValue("Sprache(n)");
	}

	public String getIsbn() {
		if (isbn == null) {
			String htmlString = htmlDoc.html();
			String search = "loadCover(\"fullRecordTable\", \"";
			int start = htmlString.indexOf(search);
			if (start != -1) {
				start += search.length();
				int end = htmlString.indexOf("\"", start);
				if (end != -1) {
					isbn = htmlString.substring(start, end);
				}
			}
		}
		return isbn;
	}

	@Override
	public String getIsbn13() {
		if (getIsbn().replaceAll("-", "").length() == 13) {
			return getIsbn();
		}

		return null;
	}

	@Override
	public String getIsbn10() {
		if (getIsbn().replaceAll("-", "").length() == 10) {
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
		return getCoverImage(IMAGE_SIZE_L);
	}

	public byte[] getCoverImage(String size) {
		String isbn = getIsbn13();
		if (isbn != null) {
			String imageUrl = "http://vlb.de/GetBlob.aspx?strIsbn=" + isbn + "&size=" + size;
			try {
				if (largeImageData == null && size.equals(IMAGE_SIZE_L)) {
					IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(new URL(imageUrl));
					return largeImageData = resourceHandler.getContent();
				} else if (thumbnailImageData == null && size.equals(IMAGE_SIZE_S)) {
					IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(new URL(imageUrl));
					return thumbnailImageData = resourceHandler.getContent();
				}
			} catch (MalformedURLException e) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to create URL " + imageUrl, e);
			} catch (IOException e) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to load URL " + imageUrl, e);
			}
		}
		return size.equals(IMAGE_SIZE_L) ? largeImageData : thumbnailImageData;
	}

	@Override
	public String getBase64EncodedThumbnailImage() {
		return Base64.encode(getCoverImage(IMAGE_SIZE_S));
	}

	@Override
	public List<String> getAuthors() {
		if (authors == null) {
			List<String> evaluatedAuthors = new ArrayList<String>();
			Element e = getTag("Person(en)");
			if (e != null) {
				Elements authorLinks = e.getElementsByTag("a");
				if (authorLinks != null) {
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
}
