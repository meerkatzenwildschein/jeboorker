package org.rr.commons.net.imagefetcher;

import java.io.IOException;
import java.util.List;

public interface IImageFetcher {
	
	/**
	 * Set the search term for this {@link IImageFetcher} instance.
	 * @param searchTerm The search term to be set.
	 */
	public void setSearchTerm(String searchTerm);

	/**
	 * Get all search entries for the given page. 
	 * @param page The search page starting with 1.
	 * @return The list of images. never returns <code>null</code>.
	 * @throws IOException
	 */
	public List<IImageFetcherEntry> getEntries(int page) throws IOException;
	
}
