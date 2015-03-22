package org.rr.jeborker.metadata.download;

import static org.rr.commons.utils.BooleanUtils.not;
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
import org.rr.commons.utils.StringUtils;

public class ComicsOrgDownloadEntry implements MetadataDownloadEntry {
	
	private final Document htmlDoc;
	
	private final String mainUrl;
	
	private byte[] thumbnailImage = null;
	
	private byte[] coverImage = null;
	
	public ComicsOrgDownloadEntry(Document htmlDoc, String mainUrl) {
		this.htmlDoc = htmlDoc;
		this.mainUrl = mainUrl;
	}

	@Override
	public String getTitle() {
		Elements headlines = htmlDoc.getElementsByClass("item_id");
		if(headlines != null && not(headlines.isEmpty())) {
			return headlines.get(0).text();
		}
		return null;
	}

	@Override
	public List<String> getAuthors() {
		Elements singleStories = htmlDoc.getElementsByClass("credits");
		for (Element singleStory : singleStories) {
			Elements authors = singleStory.getElementsByClass("credit_value");
			if(authors != null && not(authors.isEmpty())) {
				List<String> result = new ArrayList<String>(authors.size());
				for (Element author : authors) {
					String authorText = author.text();
					if(not(authorText.startsWith("?")) && not(result.contains(authorText))) {
						result.add(author.text());
					}
				}
				return result;
			}
		}

		return Collections.emptyList();
	}

	@Override
	public String getIsbn10() {
		Element isbn = htmlDoc.getElementById("issue_isbn");
		if(isbn != null && MetadataDownloadUtils.isIsbn10(isbn.text())) {
			return isbn.text();
		}
		return null;
	}

	@Override
	public String getIsbn13() {
		Element isbn = htmlDoc.getElementById("issue_isbn");
		if(isbn != null && MetadataDownloadUtils.isIsbn13(isbn.text())) {
			return isbn.text();
		}
		return null;
	}

	@Override
	public String getLanguage() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getAgeSuggestion() {
		return null;
	}

	@Override
	public byte[] getThumbnailImageBytes() {
		if(thumbnailImage == null) {
			thumbnailImage = getCoverImage(htmlDoc);
		}
		return thumbnailImage;
	}

	@Override
	public byte[] getCoverImage() {
		if(coverImage == null) {
			try {
				Elements coverLinks = htmlDoc.getElementsByClass("issue_cover_links");
				for (Element coverLink : coverLinks) {
					String html = coverLink.html();
					String imagePage = StringUtils.between(html, "\"/issue/", "\"");
					if(imagePage != null) {
						imagePage = StringUtils.replace(imagePage, "\"", EMPTY);
						byte[] loadPage = MetadataDownloadUtils.loadPage(new URL(mainUrl + imagePage));
						Document document = MetadataDownloadUtils.getDocument(loadPage, mainUrl);
						coverImage = getCoverImage(document);
					}
				}
			} catch(Exception e) {
				LoggerFactory.getLogger().log(Level.WARNING, "Failed to load cover", e);
			}
		}
		return coverImage;
	}
	
	public byte[] getCoverImage(Document document) {
		byte[] imageData = null;
		Elements images = document.getElementsByClass("cover_img");
		for (Element image : images) {
			if (image.tagName().equals("img")) {
				String imageUrl = image.attr("src");
				imageData = MetadataDownloadUtils.loadImage(imageUrl);
			}
		}
		return imageData;
	}

}
