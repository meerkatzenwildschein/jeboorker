package org.rr.jeborker.metadata.comicbook;

public class ComicPageType {
	private static final String FRONTCOVER = "FrontCover";
	private static final String INNERCOVER = "InnerCover";
	private static final String ROUNDUP = "Roundup";
	private static final String STORY = "Story";
	private static final String ADVERTISMENT = "Advertisment";
	private static final String EDITORIAL = "Editorial";
	private static final String LETTERS = "Letters";
	private static final String PREVIEW = "Preview";
	private static final String BACKCOVER = "BackCover";
	private static final String OTHER = "Other";
	private static final String DELETED = "Deleted";
	
	public static final ComicPageType TYPE_FRONTCOVER = new ComicPageType(FRONTCOVER);
	public static final ComicPageType TYPE_INNERCOVER = new ComicPageType(INNERCOVER);
	public static final ComicPageType TYPE_ROUNDUP = new ComicPageType(ROUNDUP);
	public static final ComicPageType TYPE_STORY = new ComicPageType(STORY);
	public static final ComicPageType TYPE_ADVERTISMENT = new ComicPageType(ADVERTISMENT);
	public static final ComicPageType TYPE_EDITORIAL = new ComicPageType(EDITORIAL);
	public static final ComicPageType TYPE_LETTERS = new ComicPageType(LETTERS);
	public static final ComicPageType TYPE_PREVIEW = new ComicPageType(PREVIEW);
	public static final ComicPageType TYPE_BACKCOVER = new ComicPageType(BACKCOVER);
	public static final ComicPageType TYPE_OTHER = new ComicPageType(OTHER);
	public static final ComicPageType TYPE_DELETED = new ComicPageType(DELETED);
	
	private String type;
	
	private ComicPageType(String type) {
		this.type = type;
	}

	public String toString() {
		return type;
	}
	
	public static ComicPageType getInstance(String type) {
		if(type != null) {
			type = type.trim();
			if(type.equalsIgnoreCase(TYPE_FRONTCOVER.toString())) {
				return TYPE_FRONTCOVER;
			} else if(type.equalsIgnoreCase(TYPE_INNERCOVER.toString())) {
				return TYPE_INNERCOVER;
			} else if(type.equalsIgnoreCase(TYPE_STORY.toString())) {
				return TYPE_STORY;
			} else if(type.equalsIgnoreCase(TYPE_ROUNDUP.toString())) {
				return TYPE_ROUNDUP;
			} else if(type.equalsIgnoreCase(TYPE_ADVERTISMENT.toString())) {
				return TYPE_ADVERTISMENT;
			} else if(type.equalsIgnoreCase(TYPE_EDITORIAL.toString())) {
				return TYPE_EDITORIAL;
			} else if(type.equalsIgnoreCase(TYPE_LETTERS.toString())) {
				return TYPE_LETTERS;
			} else if(type.equalsIgnoreCase(TYPE_PREVIEW.toString())) {
				return TYPE_PREVIEW;
			} else if(type.equalsIgnoreCase(TYPE_BACKCOVER.toString())) {
				return TYPE_BACKCOVER;
			} else if(type.equalsIgnoreCase(TYPE_OTHER.toString())) {
				return TYPE_OTHER;
			} else if(type.equalsIgnoreCase(TYPE_DELETED.toString())) {
				return TYPE_DELETED;
			}
		}
		return TYPE_STORY;		
	}
}
