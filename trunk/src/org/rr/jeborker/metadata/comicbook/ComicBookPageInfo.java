package org.rr.jeborker.metadata.comicbook;

public class ComicBookPageInfo {

	private int image; //required
	
	private ComicPageType story;
	
	private boolean doublePage;
	
	private long imageSize;
	
	private String key;
	
	private int imageWidth;
	
	private int imageHeight;
	
	public int getImage() {
		return image;
	}
	
	public void setImage(int image) {
		this.image = image;
	}
	
	public ComicPageType getType() {
		return story;
	}
	
	public void setType(ComicPageType story) {
		this.story = story;
	}
	
	public boolean isDoublePage() {
		return doublePage;
	}
	
	public void setDoublePage(boolean doublePage) {
		this.doublePage = doublePage;
	}
	
	public long getImageSize() {
		return imageSize;
	}
	
	public void setImageSize(long imageSize) {
		this.imageSize = imageSize;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public int getImageWidth() {
		return imageWidth;
	}
	
	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}
	
	public int getImageHeight() {
		return imageHeight;
	}
	
	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}
}
