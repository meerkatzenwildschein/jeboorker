package org.rr.commons.net;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.rr.commons.net.imagefetcher.IImageFetcher;
import org.rr.commons.net.imagefetcher.IImageFetcherEntry;
import org.rr.commons.net.imagefetcher.ImageFetcherFactory;

public class ImageFetcherTest extends TestCase {
	
	public void testFetcherGoogle() {
		try {
			IImageFetcher imageFetcher = ImageFetcherFactory.getInstance().getImageFetcher("Google");
			imageFetcher.setSearchTerm("Der Fänger");
			List<IImageFetcherEntry> searchImages = imageFetcher.getNextEntries();
			for(IImageFetcherEntry m : searchImages) {
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void testFetcherBing() {
		try {
			IImageFetcher imageFetcher = ImageFetcherFactory.getInstance().getImageFetcher("Bing");
			imageFetcher.setSearchTerm("Der Fänger");
			List<IImageFetcherEntry> searchImages = imageFetcher.getNextEntries();
			for(IImageFetcherEntry m : searchImages) {
				System.out.println(m.getImageURL());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

}
