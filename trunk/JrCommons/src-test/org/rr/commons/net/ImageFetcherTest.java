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
			IImageFetcher imageFetcher = ImageFetcherFactory.getImageFetcher(ImageFetcherFactory.FETCHER_TYPES.GOOGLE_IMAGES);
			imageFetcher.setSearchTerm("Der Fänger");
			List<IImageFetcherEntry> searchImages = imageFetcher.getEntries(1);
			for(IImageFetcherEntry m : searchImages) {
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void testFetcherBing() {
		try {
			IImageFetcher imageFetcher = ImageFetcherFactory.getImageFetcher(ImageFetcherFactory.FETCHER_TYPES.BING_IMAGES);
			imageFetcher.setSearchTerm("Der Fänger");
			List<IImageFetcherEntry> searchImages = imageFetcher.getEntries(1);
			for(IImageFetcherEntry m : searchImages) {
				System.out.println(m.getImageURL());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

}
