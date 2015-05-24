package org.rr.jeborker.metadata.download;

import static org.rr.commons.utils.BooleanUtils.not;
import static org.rr.commons.utils.StringUtil.EMPTY;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.StringUtil;

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
		Map<String, String> values = getValues();
		List<String> result = new ArrayList<String>(values.size());
		if(values.containsKey("Pencils")) {
			result.add(values.get("Pencils"));
		}
		if(values.containsKey("Inks")) {
			result.add(values.get("Inks"));
		}
		if(values.containsKey("Colors")) {
			result.add(values.get("Colors"));
		}
		return result;
	}
	
	public Map<String, String> getValues() {
		Map<String, String> result = new HashMap<>();
		Elements credits = htmlDoc.getElementsByClass("single_story");
		for (Element credit : credits) {
			Elements spans = credit.getElementsByTag("span");
			String creditLabel = null;
			for (Element span : spans) {
				if(span.className().equals("credit_label")) {
					creditLabel = span.text().replace(":", "").trim();
				} else if(span.className().equals("credit_value")) {
					String value = span.text().trim();
					if(not(StringUtil.equals(value, "?"))) {
						result.put(creditLabel, span.text().trim());
					}
				}
			}
		}
		
		return result;
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
					String imagePage = StringUtil.between(html, "\"/issue/", "\"");
					if(imagePage != null) {
						imagePage = StringUtil.replace(imagePage, "\"", EMPTY);
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
