package org.rr.commons.net.imagefetcher;

import java.io.IOException;
import java.net.URL;

public interface IImageFetcherEntry {

	public URL getThumbnailURL() ;

	public URL getImageURL();

	public int getImageWidth();

	public int getImageHeight();

	public String getTitle();
	
	public byte[] getThumbnailImageBytes() throws IOException;
	
	public byte[] getImageBytes() throws IOException;
}
