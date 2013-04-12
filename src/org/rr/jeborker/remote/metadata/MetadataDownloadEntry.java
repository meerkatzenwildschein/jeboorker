package org.rr.jeborker.remote.metadata;

import java.util.List;

public interface MetadataDownloadEntry {

	public String getTitle() ;

	public List<String> getAuthors();

	public String getIsbn10();
	
	public String getIsbn13();
	
	public String getLanguage();
	
	public String getDescription();
	
	public String getAgeSuggestion();

	/**
	 * Get a thumbnail image which can be used for the preview.
	 */
	public byte[] getThumbnailImageBytes();

	public String getBase64EncodedThumbnailImage();
	
	/**
	 * Get the cover image in the best possible image size.
	 */
	public byte[] getCoverImage();
	
}
