package org.rr.commons.net.imagefetcher;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public interface IImageFetcher {
	
	/**
	 * Set the search term for this {@link IImageFetcher} instance.
	 * @param searchTerm The search term to be set.
	 */
	public void setSearchTerm(String searchTerm);

	/**
	 * Get all search entries for the given page. 
	 * @return The list of images. never returns <code>null</code>.
	 * @throws IOException
	 */
	public List<IImageFetcherEntry> getNextEntries() throws IOException;
	
	/**
	 * Gets an iterator which automatically fetches the next entries.
	 * @return Iterator provides all images provided by this {@link IImageFetcher} instance.
	 */
	public Iterator<IImageFetcherEntry> getEntriesIterator();
}
