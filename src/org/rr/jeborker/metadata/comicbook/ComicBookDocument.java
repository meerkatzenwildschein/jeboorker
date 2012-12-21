package org.rr.jeborker.metadata.comicbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ComicBookDocument {
	
	private final HashMap<String, Object> info = new HashMap<String, Object>();

	private final List<ComicBookPageInfo> pages = new ArrayList<ComicBookPageInfo>();
	
	final byte[] comicInfoXml;
	
	byte[] cover = null;

	ComicBookDocument(byte[] xml) {
		this.comicInfoXml = xml;
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

	public int getCount() {
		return (Integer) info.get("Count");
	}

	public void setCount(int count) {
		info.put("Count", count);
	}

	public int getVolume() {
		return (Integer) info.get("Volume");
	}

	public void setVolume(int volume) {
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

	public int getAlternateCount() {
		return (Integer) info.get("AlternateCount");
	}

	public void setAlternateCount(int alternateCount) {
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

	public int getYear() {
		return (Integer) info.get("Year");
	}

	public void setYear(int year) {
		info.put("Year", year);
	}

	public int getMonth() {
		return (Integer) info.get("Month");
	}

	public void setMonth(int month) {
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

	public int getPageCount() {
		return (Integer) info.get("PageCount");
	}

	public void setPageCount(int pageCount) {
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
		return (YeyNoType) info.get("BlackAndWhite");
	}

	public void setBlackAndWhite(YeyNoType blackAndWhite) {
		info.put("BlackAndWhite", blackAndWhite);
	}

	public YeyNoType getManga() {
		return (YeyNoType) info.get("Manga");
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
		return cover;
	}

	void setCover(byte[] cover) {
		this.cover = cover;
	}

	public byte[] getComicInfoXml() {
		return comicInfoXml;
	}
}
