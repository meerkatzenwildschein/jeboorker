package org.rr.commons.net.imagefetcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.mobi4java.MobiDocument;
import org.rr.mobi4java.MobiReader;

public class ImageMobiFileFetcherFactory implements IImageFetcherFactory {

	private IResourceHandler mobiFile;
	
	public ImageMobiFileFetcherFactory(final IResourceHandler mobiFile) {
		this.mobiFile = mobiFile;
	}
	
	@Override
	public IImageFetcher getImageFetcher(String fetcherName) {
		return getImageFetcher(fetcherName, null);
	}

	@Override
	public IImageFetcher getImageFetcher(String fetcherName, String searchTerm) {
		return new MobiFileImageFetcher();
	}

	@Override
	public List<String> getFetcherNames() {
		return Collections.singletonList("MOBI");
	}

	@Override
	public boolean searchTermSupport() {
		return false;
	}
	
	private class MobiFileImageFetcher implements IImageFetcher {

		@Override
		public void setSearchTerm(String searchTerm) {
		}

		@Override
		public List<IImageFetcherEntry> getNextEntries() throws IOException {
			return null; //all entries will be delivered with the getEntriesIterator method.
		}

		@Override
		public Iterator<IImageFetcherEntry> getEntriesIterator() {
			return new Iterator<IImageFetcherEntry>() {
				Iterator<byte[]> images;
				{
						try (InputStream in = mobiFile.getContentInputStream()) {
							MobiDocument mobiDoc = new MobiReader().read(in);
							images = mobiDoc.getImages().iterator();
							
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
				}
				
				
				@Override
				public boolean hasNext() {
					return images.hasNext();
				}

				@Override
				public IImageFetcherEntry next() {
					try {
						final byte[] imageBytes = images.next();
						return new IImageFetcherEntry() {
							
							private String title = UUID.randomUUID().toString();
							
							private int width = -1;
							
							private int height = -1;
							
							@Override
							public URL getThumbnailURL() {
								return getImageURL();
							}

							@Override
							public URL getImageURL() {
								try {
									return new URL("file://" + title);
								} catch (Exception e) {
									LoggerFactory.getLogger().log(Level.INFO, "Invalid URL", e);
								}
								return null;
							}

							@Override
							public int getImageWidth() {
								return this.width;
							}

							@Override
							public int getImageHeight() {
								return this.height;
							}

							@Override
							public String getTitle() {
								return title;
							}

							@Override
							public byte[] getThumbnailImageBytes() throws IOException {
								return getImageBytes();
							}

							@Override
							public byte[] getImageBytes() throws IOException {
								return imageBytes;
							}
							
						};
					} catch(NoSuchElementException e1) {
						throw new ArrayIndexOutOfBoundsException();
					} catch(Exception e) {
						LoggerFactory.getLogger().log(Level.INFO, "Could not read zip file " + mobiFile, e);
					}					
					return null;
				}

				@Override
				public void remove() {
				}
			};
			
		}
		
	}

}
