package org.rr.commons.net.imagefetcher;

import java.util.List;

public interface IImageFetcherFactory {
	
	public IImageFetcher getImageFetcher(String fetcherName);
	
	public IImageFetcher getImageFetcher(String fetcherName, String searchTerm);
	
	public List<String> getFetcherNames();
	
	public boolean searchTermSupport();
}
