package org.rr.jeborker.db.item;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.jempbox.xmp.Thumbnail;
import org.rr.commons.collection.TransformValueList;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.MimeUtils;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.app.BasePathList;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

public class EbookPropertyItemUtils {

	private static final String ALL_BOOK_PATH_COLLECTION = "allBookPathCollection";
	private static final String thumbnailFolder = APreferenceStore.getConfigDirectory() + "thumbs/";
	static {
		IResourceHandler thumbnailFolderResource = ResourceHandlerFactory.getResourceHandler(thumbnailFolder);
		if(!thumbnailFolderResource.exists()) {
			try {
				thumbnailFolderResource.mkdirs();
			} catch (IOException e) {
				LoggerFactory.getLogger().log(Level.SEVERE, "Could not create thumbnail folder " + thumbnailFolder, e);
			}
		}
	}

	/**
	 * Reloads the given item from the database.
	 * @param item Item to be reloaded.
	 * @return The reloaded item or <code>null</code> if the item no longer exists in the database.
	 */
	public static EbookPropertyItem reloadEbookPropertyItem(EbookPropertyItem item) {
		EbookPropertyItem refreshed = (EbookPropertyItem) DefaultDBManager.getInstance().reload(item);
		return refreshed;
	}

	/**
	 * Get the {@link EbookPropertyItem}s for the given {@link IResourceHandler}.
	 * @param resourceLoader The {@link IResourceHandler} instance where the {@link EbookPropertyItem} should be fetched from the database.
	 * @return The desired {@link EbookPropertyItem}s.
	 */
	public static List<EbookPropertyItem> getEbookPropertyItemByResource(IResourceHandler resourceLoader) {
		if(resourceLoader == null) {
			return Collections.emptyList();
		}
		
		// Look for selected resources
		List<EbookPropertyItem> selectedEbookPropertyItems = MainController.getController().getSelectedEbookPropertyItems();
		for (EbookPropertyItem ebookPropertyItem : selectedEbookPropertyItems) {
			if(ebookPropertyItem.getResourceHandler().equals(resourceLoader)) {
				return Collections.singletonList(ebookPropertyItem);
			}
		}

		// Query resource from database
		DefaultDBManager defaultDBManager = DefaultDBManager.getInstance();
		return defaultDBManager.getObject(EbookPropertyItem.class, "file", resourceLoader.toString());
	}
	
	/**
	 * Creates a new {@link EbookPropertyItem} from the given resource but without any setup excepting the resource file name.
	 *  
	 * @param resource The resource where the {@link EbookPropertyItem} should be created from.
	 * @return The newly created {@link EbookPropertyItem}.
	 */
	public static EbookPropertyItem createBasicEbookPropertyItem(final IResourceHandler resource) {
		final EbookPropertyItem item = (EbookPropertyItem) DefaultDBManager.getInstance().newInstance(EbookPropertyItem.class);
		item.setFile(resource.getResourceString());
		return item;
	}
	
	/**
	 * Creates a new {@link EbookPropertyItem} from the given resource.
	 * 
	 * @param resource The resource where the {@link EbookPropertyItem} should be created from.
	 * @param topLevelBaseFolder The base folder where the {@link EbookPropertyItem} is located at.
	 * @return The newly created {@link EbookPropertyItem}.
	 */
	public static EbookPropertyItem createEbookPropertyItem(final IResourceHandler resource, final IResourceHandler topLevelBaseFolder) {
		final EbookPropertyItem item = createBasicEbookPropertyItem(resource);
		item.setCreatedAt(new Date());
		if(topLevelBaseFolder != null) {
			item.setBasePath(topLevelBaseFolder.getResourceString());
		}
		item.setMimeType(resource.getMimeType(false));
		item.setTimestamp(resource.getModifiedAt().getTime());
		refreshEbookPropertyItem(item, resource, true);

		return item;
	}

	/**
	 * Refreshed the data of the given {@link EbookPropertyItem} by rereading it's metadata.
	 * @param item The item to be refreshed.
	 * @param resource The resource file for the {@link EbookPropertyItem}. it's optional and can be null.
	 */
	public static void refreshEbookPropertyItem(final EbookPropertyItem item, IResourceHandler resource, boolean refreshCover) {
		if(resource == null) {
			resource = ResourceHandlerFactory.getResourceHandler(item.getFile());
		}

		IMetadataReader reader = MetadataHandlerFactory.getReader(resource);
		if(reader != null) {
			final List<MetadataProperty> metadataProperties = reader.readMetadata();
			reader.fillEbookPropertyItem(metadataProperties, item);
			if(refreshCover) {
				boolean refreshed = false;
				List<MetadataProperty> metadataByType = reader.getMetadataByType(false, metadataProperties, IMetadataReader.COMMON_METADATA_TYPES.COVER);
				if(metadataByType != null && !metadataByType.isEmpty()) {
					MetadataProperty metadataProperty = metadataByType.get(0);
					if(metadataProperty.getValues() != null && !metadataProperty.getValues().isEmpty()) {
						Object value = metadataProperty.getValues().get(0);
						if(value instanceof byte[]) {
							setupCoverData(item, (byte[]) value);
							refreshed = true;
						}
					}
				}
				if(!refreshed) {
					setupCoverData(item, null);
				}
			}
			item.setTimestamp(resource.getModifiedAt().getTime());
		}
	}

	/**
	 * Does the setup for the cover image bytes. A new {@link EbookPropertyCoverItem} is created
	 * and stored and the reference is set to the given {@link EbookPropertyItem}.
	 *
	 * @param item The {@link EbookPropertyItem} instance to be setup.
	 * @param imageData The data to be stored in a {@link EbookPropertyCoverItem}
	 * @return The newly created {@link EbookPropertyCoverItem} with the cover data.
	 */
	public static void setupCoverData(final EbookPropertyItem item, byte[] imageData) {
		//create thumbnail to be stored
		try {
			if(imageData != null && imageData.length > 0) {
				IImageProvider imageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getVirtualResourceHandler(UUID.randomUUID().toString(), imageData));
				if(imageProvider != null) {
					BufferedImage thumbnailImage = ImageUtils.scaleToHeight(imageProvider.getImage(), 100);

					//much faster cropping the thumbnail than the original sized cover image.
					BufferedImage cropedImage = ImageUtils.crop(thumbnailImage);

					byte[] thumbnailImageBytes = ImageUtils.getImageBytes(cropedImage, MimeUtils.MIME_JPEG);
					if(thumbnailImageBytes != null) {
						setCoverThumbnail(thumbnailImageBytes, item.getResourceHandler());
					} else {
						deleteCoverThumbnail(item.getResourceHandler());
					}
				} else {
					deleteCoverThumbnail(item.getResourceHandler());
				}
			} else {
				deleteCoverThumbnail(item.getResourceHandler());
			}
		} catch(Exception e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Failed to store thumbnail for " + item.getResourceHandler().getName(), e);
		}
	}

	/**
	 * Deletes the cover thumbnail for the given ebook {@link IResourceHandler}.
	 * @param ebookResource The ebook where the cover thumbnail should be deleted for.
	 * @throws IOException
	 */
	public static void deleteCoverThumbnail(final IResourceHandler ebookResource) throws IOException {
		IResourceHandler thumbnailResourceLoader = getCoverThumbnailResourceHandler(ebookResource);
		if(thumbnailResourceLoader.exists()) {
			thumbnailResourceLoader.delete();
		}
	}

	/**
	 * Stores the given thumbnail bytes for the given ebook {@link IResourceHandler}.
	 * @param thumbnailData The thumbnail data to be stored.
	 * @param ebookResource The ebook where the cover thumbnail should be stored for.
	 * @throws IOException
	 */
	private static void setCoverThumbnail(byte[] thumbnailData, IResourceHandler ebookResource) throws IOException {
		if(thumbnailData != null && thumbnailData.length != 0) {
			IResourceHandler thumbnailResourceLoader = getCoverThumbnailResourceHandler(ebookResource);
			thumbnailResourceLoader.setContent(thumbnailData);
		}
	}

	/**
	 * Get the cover thumbnail bytes for the given ebook resource.
	 * @param ebookResource The ebook resource wehere the cover thumbnail bytes should be loaded for.
	 * @return The desired cover thumbnail bytes or <code>null</code> if no cover is stored for the given ebook {@link IResourceHandler}.
	 */
	public static byte[] getCoverThumbnailBytes(final IResourceHandler ebookResource) {
		IResourceHandler coverThumbnail = getCoverThumbnail(ebookResource);
		if(coverThumbnail != null) {
			try {
				return coverThumbnail.getContent();
			} catch (IOException e) {
				LoggerFactory.getLogger().log(Level.WARNING, "Failed to load cover thumbnail for " + ebookResource.getName(), e);
			}
		}
		return null;
	}

	/**
	 * Get the cover thumbnail {@link IResourceHandler} for the given ebook {@link IResourceHandler}.
	 * @param ebookResource The ebook {@link IResourceHandler} where the {@link Thumbnail} should be fetched for.
	 * @return The desired {@link IResourceHandler} instance for the cover thumbnail. Returns <code>null</code> if
	 *   no cover is stored for the given ebook {@link IResourceHandler}.
	 */
	private static IResourceHandler getCoverThumbnail(final IResourceHandler ebookResource) {
		IResourceHandler coverThumbnailResourceHandler = getCoverThumbnailResourceHandler(ebookResource);
		if(coverThumbnailResourceHandler.exists()) {
			return coverThumbnailResourceHandler;
		}
		return null;
	}

	/**
	 * Get the {@link IResourceHandler} pointing to the cover thumbnail for the given ebook {@link IResourceHandler}.
	 * @param ebookResource The ebook {@link IResourceHandler} where the cover thumbnail {@link IResourceHandler} should be fetched for.
	 * @return The desired cover thumbnail {@link IResourceHandler} instance.
	 */
	private static IResourceHandler getCoverThumbnailResourceHandler(IResourceHandler ebookResource) {
		String thumbnail = thumbnailFolder + UUID.nameUUIDFromBytes(ebookResource.toString().getBytes()) + ".jpg";
		return ResourceHandlerFactory.getResourceHandler(thumbnail);
	}

	/**
	 * Renames the cover thumbnail for the given source resource to one which matches to the given target {@link IResourceHandler}.
	 * This always happens if the file name changes.
	 * @param source The source to be renamed.
	 * @param target The target {@link IResourceHandler} for the cover thumbnail.
	 */
	public static void renameCoverThumbnail(IResourceHandler source, IResourceHandler target) {
		IResourceHandler sourceThumbnailResourceHandler = getCoverThumbnailResourceHandler(source);
		if(sourceThumbnailResourceHandler.exists()) {
			IResourceHandler targetThumbnailResourceHandler = getCoverThumbnailResourceHandler(target);
			try {
				sourceThumbnailResourceHandler.moveTo(targetThumbnailResourceHandler, true);
			} catch (IOException e) {
				LoggerFactory.getLogger().log(Level.WARNING,
						String.format("Failed to rename thumbnail from %s to %s", sourceThumbnailResourceHandler, targetThumbnailResourceHandler), e);
			}
		}
	}

	/**
	 * Stores the given path collection to the database.
	 * @param path The path elements to be stored.
	 */
	public static void storePathElements(final Collection<String> path) {
		final List<String> oldPathElements = fetchPathElements();
		final BasePathList basePathList = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getBasePath();
		String indexedString;
		if(oldPathElements != null && !oldPathElements.isEmpty()) {
			final HashSet<String> allElements = new HashSet<>(path.size());
			for(String oldPathElement : oldPathElements) {
				if(basePathList.containsBasePathFor(oldPathElement)) {
					allElements.add(oldPathElement);
				}
			}
			allElements.addAll(path);
			indexedString = ListUtils.toIndexedString(allElements, File.pathSeparatorChar);
		} else {
			indexedString = ListUtils.toIndexedString(path, File.pathSeparatorChar);
		}
		PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).addGenericEntryAsString(ALL_BOOK_PATH_COLLECTION, indexedString);
	}

	/**
	 * Fetches these path elements previously stored with the {@link #storePathElements(Collection)}
	 * method.
	 */
	public static List<String> fetchPathElements() {
		String indexedListString = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getGenericEntryAsString(ALL_BOOK_PATH_COLLECTION, null);
		if(indexedListString == null) {
			return Collections.emptyList();
		}
		return ListUtils.fromIndexString(indexedListString, File.pathSeparatorChar);
	}

	/**
	 * Creates a list of {@link IResourceHandler} for the given {@link EbookPropertyItem} list.
	 * @param ebookPropertyItems The {@link EbookPropertyItem} list which {@link IResourceHandler} should be returned.
	 * @return The desired list, never returns <code>null</code>.
	 */
	public static List<IResourceHandler> createIResourceHandlerList(List<EbookPropertyItem> ebookPropertyItems) {
		return new TransformValueList<EbookPropertyItem, IResourceHandler>(ebookPropertyItems) {

			@Override
			public IResourceHandler transform(EbookPropertyItem source) {
				return source.getResourceHandler();
			}
		};
	}
}
