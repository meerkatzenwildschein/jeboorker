package org.rr.commons.net;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.rr.commons.net.imagefetcher.IImageFetcher;
import org.rr.commons.net.imagefetcher.IImageFetcherEntry;
import org.rr.commons.net.imagefetcher.ImageWebSearchFetcherFactory;

public class ImageFetcherTest extends TestCase {
	
	public void xtestFetcherGoogle() {
		try {
			IImageFetcher imageFetcher = ImageWebSearchFetcherFactory.getInstance().getImageFetcher("Google");
			imageFetcher.setSearchTerm("Der Fänger");
			List<IImageFetcherEntry> searchImages = imageFetcher.getNextEntries();
			for(IImageFetcherEntry m : searchImages) {
				m.getImageURL();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void xtestFetcherBigBooks() {
		try {
			IImageFetcher imageFetcher = ImageWebSearchFetcherFactory.getInstance().getImageFetcher("Big Book Search");
			imageFetcher.setSearchTerm("Der Fänger");
			List<IImageFetcherEntry> searchImages = imageFetcher.getNextEntries();
			for(IImageFetcherEntry m : searchImages) {
				m.getImageURL();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	public void testFetcherBigBooksIterator() {
		try {
			IImageFetcher imageFetcher = ImageWebSearchFetcherFactory.getInstance().getImageFetcher("Big Book Search");
			imageFetcher.setSearchTerm("Der Fänger");
			Iterator<IImageFetcherEntry> searchImages = imageFetcher.getEntriesIterator();
			for(int i=0; searchImages.hasNext(); i++) {
				IImageFetcherEntry next = searchImages.next();
				System.out.println(next.getImageURL());
				if(i == 25) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}		

}
