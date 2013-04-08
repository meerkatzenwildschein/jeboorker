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

	public byte[] getImage();
	
}
