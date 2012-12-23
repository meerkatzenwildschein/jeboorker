package org.rr.jeborker.metadata.comicbook;

import java.util.HashMap;

public class ComicBookPageInfo {
	
	private HashMap<String, Object> info = new HashMap<String, Object>();

	public Integer getImage() {
		Object value = info.get("Image");
		return ComicBookUtils.getAsInteger(value);
	}
	
	public void setImage(Integer image) {
		info.put("Image", image);
	}
	
	public ComicPageType getType() {
		Object value = info.get("Type");
		return ComicBookUtils.getAsComicPageType(value);
	}
	
	public void setType(ComicPageType story) {
		info.put("Type", story);
	}
	
	public Boolean getDoublePage() {
		Object value = info.get("DoublePage");
		return ComicBookUtils.getAsBoolean(value);
	}
	
	public void setDoublePage(Boolean doublePage) {
		info.put("DoublePage", doublePage);
	}
	
	public Long getImageSize() {
		Object value = info.get("ImageSize");
		return ComicBookUtils.getAsLong(value);
	}
	
	public void setImageSize(Long imageSize) {
		info.put("ImageSize", imageSize);
	}
	
	public String getKey() {
		return (String) info.get("Key");
	}
	
	public void setKey(String key) {
		info.put("Key", key);
	}
	
	public Integer getImageWidth() {
		Object value = info.get("ImageWidth");
		return ComicBookUtils.getAsInteger(value);
	}
	
	public void setImageWidth(Integer imageWidth) {
		info.put("ImageWidth", imageWidth);
	}
	
	public Integer getImageHeight() {
		Object value = info.get("ImageHeight");
		return ComicBookUtils.getAsInteger(value);
	}
	
	public void setImageHeight(Integer imageHeight) {
		info.put("ImageHeight", imageHeight);
	}
	
	public HashMap<String, Object> getInfo() {
		return info;
	}

}
