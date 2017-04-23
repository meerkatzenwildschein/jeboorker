package org.rr.jeborker.metadata.comicbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.rr.commons.mufs.MimeUtils;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.ResourceHandlerUtils;

public class ComicBookDocument {

	private final HashMap<String, Object> info = new HashMap<String, Object>();

	private final List<ComicBookPageInfo> pages = new ArrayList<>();

	private List<String> archiveEntries;

	final byte[] comicInfoXml;

	private byte[] cover = null;

	private String xmlFilePath;

	private IArchiveHandler archiveHandler;

	ComicBookDocument(IArchiveHandler archiveHandler) {
		this.archiveHandler = archiveHandler;
		this.comicInfoXml = archiveHandler.getComicXmlData();
		this.xmlFilePath = archiveHandler.getComicXmlFilename();
		this.archiveEntries = archiveHandler.getArchiveEntries();
	}

	public String getTitle() {
		return (String) info.get("Title");
	}

	public void setTitle(String title) {
		info.put("Title", title);
	}

	public String getRating() {
		return (String) info.get("Rating");
	}

	public void setRating(String rating) {
		info.put("Rating", rating);
	}

	public String getSeries() {
		return (String) info.get("Series");
	}

	public void setSeries(String series) {
		info.put("Series", series);
	}

	public String getNumber() {
		return (String) info.get("Number");
	}

	public void setNumber(String number) {
		info.put("Number", number);
	}

	public Integer getCount() {
		Object value = info.get("Count");
		return ComicBookUtils.getAsInteger(value);
	}

	public void setCount(Integer count) {
		info.put("Count", count);
	}

	public Integer getVolume() {
		Object value = info.get("Volume");
		return ComicBookUtils.getAsInteger(value);
	}

	public void setVolume(Integer volume) {
		info.put("Volume", volume);
	}

	public String getAlternateSeries() {
		return (String) info.get("AlternateSeries");
	}

	public void setAlternateSeries(String alternateSeries) {
		info.put("AlternateSeries", alternateSeries);
	}

	public String getAlternateNumber() {
		return (String) info.get("AlternateNumber");
	}

	public void setAlternateNumber(String alternateNumber) {
		info.put("AlternateNumber", alternateNumber);
	}

	public Integer getAlternateCount() {
		Object value = info.get("AlternateCount");
		return ComicBookUtils.getAsInteger(value);
	}

	public void setAlternateCount(Integer alternateCount) {
		info.put("AlternateCount", alternateCount);
	}

	public String getSummary() {
		return (String) info.get("Summary");
	}

	public void setSummary(String summary) {
		info.put("Summary", summary);
	}

	public String getNotes() {
		return (String) info.get("Notes");
	}

	public void setNotes(String notes) {
		info.put("Notes", notes);
	}

	public Integer getYear() {
		Object value = info.get("Year");
		return ComicBookUtils.getAsInteger(value);
	}

	public void setYear(Integer year) {
		info.put("Year", year);
	}

	public Integer getMonth() {
		Object value = info.get("Month");
		return ComicBookUtils.getAsInteger(value);
	}

	public void setMonth(Integer month) {
		info.put("Month", month);
	}

	public String getWriter() {
		return (String) info.get("Writer");
	}

	public void setWriter(String writer) {
		info.put("Writer", writer);
	}

	public String getPenciller() {
		return (String) info.get("Penciller");
	}

	public void setPenciller(String penciller) {
		info.put("Penciller", penciller);
	}

	public String getInker() {
		return (String) info.get("Inker");
	}

	public void setInker(String inker) {
		info.put("Inker", inker);
	}

	public String getColorist() {
		return (String) info.get("Colorist");
	}

	public void setColorist(String colorist) {
		info.put("Colorist", colorist);
	}

	public String getLetterer() {
		return (String) info.get("Letterer");
	}

	public void setLetterer(String letterer) {
		info.put("Letterer", letterer);
	}

	public String getCoverArtist() {
		return (String) info.get("CoverArtist");
	}

	public void setCoverArtist(String coverArtist) {
		info.put("CoverArtist", coverArtist);
	}

	public String getEditor() {
		return (String) info.get("Editor");
	}

	public void setEditor(String editor) {
		info.put("Editor", editor);
	}

	public String getPublisher() {
		return (String) info.get("Publisher");
	}

	public void setPublisher(String publisher) {
		info.put("Publisher", publisher);
	}

	public String getImprint() {
		return (String) info.get("Imprint");
	}

	public void setImprint(String imprint) {
		info.put("Imprint", imprint);
	}

	public String getGenre() {
		return (String) info.get("Genre");
	}

	public void setGenre(String genre) {
		info.put("Genre", genre);
	}

	public String getWeb() {
		return (String) info.get("Web");
	}

	public void setWeb(String web) {
		info.put("Web", web);
	}

	public Integer getPageCount() {
		Object value = info.get("PageCount");
		return ComicBookUtils.getAsInteger(value);
	}

	public void setPageCount(Integer pageCount) {
		info.put("PageCount", pageCount);
	}

	public String getLanguageISO() {
		return (String) info.get("LanguageISO");
	}

	public void setLanguageISO(String languageISO) {
		info.put("LanguageISO", languageISO);
	}

	public String getFormat() {
		return (String) info.get("Format");
	}

	public void setFormat(String format) {
		info.put("Format", format);
	}

	public YeyNoType getBlackAndWhite() {
		Object value = info.get("BlackAndWhite");
		return ComicBookUtils.getAsYesNoType(value);
	}

	public void setBlackAndWhite(YeyNoType blackAndWhite) {
		info.put("BlackAndWhite", blackAndWhite);
	}

	public YeyNoType getManga() {
		Object value = info.get("Manga");
		return ComicBookUtils.getAsYesNoType(value);
	}

	public void setManga(YeyNoType manga) {
		info.put("Manga", manga);
	}

	public HashMap<String, Object> getInfo() {
		return info;
	}

	public List<ComicBookPageInfo> getPages() {
		return pages;
	}

	public byte[] getCover() {
		if(this.cover == null) {
			//search for the page which specifies the cover image in the archive
			for(ComicBookPageInfo page : pages) {
				if(page.getType() != null && page.getType().equals(ComicPageType.TYPE_FRONTCOVER)) {
					int index = page.getImage();
					String archiveEntry = archiveEntries.get(index);
						try {
							this.cover = archiveHandler.getArchiveEntry(archiveEntry);
						} catch (IOException e) {
						}
					break;
				}
			}

			//simply get the first image as cover image
			if(this.cover == null && !archiveEntries.isEmpty()) {
				try {
					for(int i = 0; i < archiveEntries.size(); i++) {
						byte[] archiveEntry = archiveHandler.getArchiveEntry(archiveEntries.get(i));
						String mime = ResourceHandlerUtils.guessFormat(ResourceHandlerFactory.getResourceHandler(archiveEntry));
						if(MimeUtils.isImageMime(mime)) {
							this.cover = archiveEntry;
							break;
						}
					}
				} catch (IOException e) {
				}
			}
		}
		return this.cover;
	}

	public byte[] getComicInfoXml() {
		return comicInfoXml;
	}

	String getComicInfoFilePath() {
		return this.xmlFilePath;
	}

	public List<String> getImageNames() {
		return this.archiveEntries;
	}

}
