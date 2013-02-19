package org.rr.commons.net.imagefetcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.truezip.TrueZipDataEntry;
import org.rr.commons.utils.truezip.TrueZipUtils;
import org.rr.commons.utils.zip.ZipFileFilter;

public class ImageZipFileFetcherFactory implements IImageFetcherFactory {
	
	private IResourceHandler zipFile;
	
	public ImageZipFileFetcherFactory(final IResourceHandler zipFile) {
		this.zipFile = zipFile;
	}

	@Override
	public IImageFetcher getImageFetcher(String fetcherName) {
		return getImageFetcher(fetcherName, null);
	}

	@Override
	public IImageFetcher getImageFetcher(String fetcherName, String searchTerm) {
		return new ZipFileImageFetcher();
	}

	@Override
	public List<String> getFetcherNames() {
		return Collections.singletonList("ZIP");
	}
	
	private class ZipFileImageFetcher implements IImageFetcher {

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
				Iterator<String> entries;
				{
					try {
						entries = TrueZipUtils.list(zipFile, new ZipFileFilter() {
							
							@Override
							public boolean accept(String entry) {
								entry = entry. toLowerCase();
								if(entry.endsWith(".jpg") || entry.endsWith(".jpeg") || entry.endsWith(".png") || entry.endsWith(".gif")) {
									return true;
								}
								return false;
							}
						}).iterator();
					} catch(Exception e) {
						LoggerFactory.getLogger().log(Level.INFO, "Could not read zip file " + zipFile, e);
					}
				}
				
				
				@Override
				public boolean hasNext() {
					return entries.hasNext();
				}

				@Override
				public IImageFetcherEntry next() {
					try {
						final String entry = entries.next();
						return new IImageFetcherEntry() {
							
							private int width = -1;
							
							private int height = -1;
							
							@Override
							public URL getThumbnailURL() {
								return getImageURL();
							}

							@Override
							public URL getImageURL() {
								try {
									return new URL("file://" + entry);
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
								return new File(entry).getName();
							}

							@Override
							public byte[] getThumbnailImageBytes() throws IOException {
								return getImageBytes();
							}

							@Override
							public byte[] getImageBytes() throws IOException {
								final TrueZipDataEntry extractZipEntry = TrueZipUtils.extract(zipFile, entry);
								final byte[] bytes = extractZipEntry.getBytes();
//								final IResourceHandler virtualResourceLoader = ResourceHandlerFactory.getVirtualResourceLoader(extractZipEntry.getName(), bytes);
//								final IImageProvider imageProvider = ImageProviderFactory.getImageProvider(virtualResourceLoader);
//								
//								this.height = imageProvider.getHeight();
//								this.width = imageProvider.getWidth();
								return bytes;
							}
							
						};
					} catch(NoSuchElementException e1) {
						throw new ArrayIndexOutOfBoundsException();
					} catch(Exception e) {
						LoggerFactory.getLogger().log(Level.INFO, "Could not read zip file " + zipFile, e);
					}					
					return null;
				}

				@Override
				public void remove() {
				}
			};
		}
		
	}

	@Override
	public boolean searchTermSupport() {
		return false;
	}

}
