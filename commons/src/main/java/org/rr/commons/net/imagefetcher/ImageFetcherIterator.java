package org.rr.commons.net.imagefetcher;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

class ImageFetcherIterator implements Iterator<IImageFetcherEntry>{

	private IImageFetcher imageFetcher;
	
	private List<IImageFetcherEntry> nextEntries;
	
	private int pos = 0;
	
	ImageFetcherIterator(IImageFetcher imageFetcher) {
		this.imageFetcher = imageFetcher;
	}
	
	@Override
	public boolean hasNext() {
		try {
			if(nextEntries == null)
			{
				pos = 0;
				nextEntries = imageFetcher.getNextEntries();
			}
		} catch (IOException e) {
			return false;
		}
		return !nextEntries.isEmpty() && nextEntries.size() > pos;
	}

	@Override
	public synchronized IImageFetcherEntry next() {
		if(hasNext()) {
			IImageFetcherEntry iImageFetcherEntry = nextEntries.get(pos++);
			return iImageFetcherEntry;
		}
		throw new ArrayIndexOutOfBoundsException(pos);
	}

	@Override
	public void remove() {
	}

}
