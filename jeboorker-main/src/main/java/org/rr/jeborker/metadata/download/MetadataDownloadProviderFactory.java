package org.rr.jeborker.metadata.download;

import java.util.ArrayList;
import java.util.List;

import org.rr.jeborker.db.item.EbookPropertyItem;

public class MetadataDownloadProviderFactory {
	
	public static interface DownloaderType {
		public String toString();
		
		public String getName();
		
		public MetadataDownloader getMetadataDownloader();
	}
	
	public static enum DOWNLOADER_TYPES implements DownloaderType {
		DNB {

			@Override
			public String getName() {
				return "Katalog der deutschen Nationalbibliothek";
			}
			
			@Override
			public String toString() {
				return getName();
			}
	
			@Override
			public MetadataDownloader getMetadataDownloader() {
				return new DNBMetadataDownloader();
			}
		},
		COMIC_BOOK_DB_COM {
			
			@Override
			public String getName() {
				return "comics.org";
			}
			
			@Override
			public String toString() {
				return getName();
			}
			
			@Override
			public MetadataDownloader getMetadataDownloader() {
				return new ComicsOrgDownloader();
			}
		},
		GOOGLE_BOOKS_DE {
			
			@Override
			public String getName() {
				return "Google Books Germany";
			}
			
			@Override
			public String toString() {
				return getName();
			}
			
			@Override
			public MetadataDownloader getMetadataDownloader() {
				return new GoogleBooksDeMetadataDownloader();
			}
		}
	}

	private static final List<String> DOWNLOADER_NAMES = new ArrayList<String>() {{
		for(DOWNLOADER_TYPES type: DOWNLOADER_TYPES.values()) {
			add(type.getName());
		}
	}};
	
	/**
	 * Get the downloader with the desired name.
	 * @param name The name of the downloader. All available names could be fetched with the
	 *     {@link #getDownloaderNames()} method.
	 * @return The desired downloader or <code>null</code> if there is no downloader with the given name.
	 */
	public static MetadataDownloader getDownloader(String name, List<EbookPropertyItem> selectedEbookPropertyItems) {
		for(DOWNLOADER_TYPES type: DOWNLOADER_TYPES.values()) {
			if(type.getName().equals(name)) {
				return type.getMetadataDownloader();
			}
		}
		return null;
	}
	
	/**
	 * Get the names of all available downloaders. With one of these names
	 * the desired {@link MetadataDownloader} instance could be fetched with
	 * the {@link #getDownloader(String)} method.
	 */
	public static List<String> getDownloaderNames() {
		return DOWNLOADER_NAMES;
	}
}
