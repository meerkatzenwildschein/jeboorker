package org.rr.jeborker.db.item;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.jempbox.xmp.Thumbnail;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.jeborker.JeboorkerUtils;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

public class EbookPropertyItemUtils {
	
	private static final String thumbnailFolder = JeboorkerUtils.getConfigDirectory() + "thumbs/";
	static {
		IResourceHandler thumbnailFolderResource = ResourceHandlerFactory.getResourceLoader(thumbnailFolder);
		if(!thumbnailFolderResource.exists()) {
			try {
				thumbnailFolderResource.mkdirs();
			} catch (IOException e) {
				LoggerFactory.getLogger().log(Level.SEVERE, "Could not create thumbnail folder " + thumbnailFolder, e);
			}
		}
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
		final DefaultDBManager defaultDBManager = DefaultDBManager.getInstance();
		final List<EbookPropertyItem> items = defaultDBManager.getObject(EbookPropertyItem.class, "file", resourceLoader.toString());
		return items;
	}
	
    /**
     * Get all fields which are marked with a {@link ViewField} annotation.
     * @return The desired fields.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Field> getFieldsByAnnotation(final Class annotationClass, final Class<?> itemClass) {
		//get fields to be displayed in the combobox
		final List<Field> fields = ReflectionUtils.getFields(itemClass, ReflectionUtils.VISIBILITY_VISIBLE_ALL);
		final ArrayList<Field> listEntries = new ArrayList<Field>(fields.size());
		for (Field field : fields) {
			Object dbViewFieldAnnotation = field.getAnnotation(annotationClass);
			if(dbViewFieldAnnotation!=null) {
				listEntries.add(field);
			}
		} 
		
		return listEntries;  	
    } 	
	
	/**
	 * Creates a new {@link EbookPropertyItem} from the given resource.
	 * @param resource The resource where the {@link EbookPropertyItem} should be created from.
	 * @return The newly created {@link EbookPropertyItem}.
	 */
	public static EbookPropertyItem createEbookPropertyItem(final IResourceHandler resource, final IResourceHandler topLevelBaseFolder) {
		final EbookPropertyItem item = (EbookPropertyItem) DefaultDBManager.getInstance().newInstance(EbookPropertyItem.class);
		item.setCreatedAt(new Date());
		item.setFile(resource.getResourceString());
		item.setBasePath(topLevelBaseFolder.getResourceString());
		item.setMimeType(resource.getMimeType());
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
			resource = ResourceHandlerFactory.getResourceLoader(item.getFile());
		}

		IMetadataReader reader = MetadataHandlerFactory.getReader(resource);
		if(reader != null) {
			final List<MetadataProperty> metadataProperties = reader.readMetaData();
			reader.fillEbookPropertyItem(metadataProperties, item);
			if(refreshCover) {
				final byte[] imageData = reader.getCover();
				setupCoverData(item, imageData);
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
				IImageProvider imageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getVirtualResourceLoader(UUID.randomUUID().toString(), imageData));
				if(imageProvider != null) {
					BufferedImage thumbnailImage = ImageUtils.scaleToHeight(imageProvider.getImage(), 100);
					
					//much faster cropping the thumbnail than the original sized cover image.
					BufferedImage cropedImage = ImageUtils.crop(thumbnailImage);
					
					byte[] thumbnailImageBytes = ImageUtils.getImageBytes(cropedImage, "image/jpeg");
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
	 * Get a list of EbookKeywordItem for a list of keyword strings.
	 * @param keywords The keywords to be used for the reuslt list.
	 * @return The list over EbookKeywordItem instances.
	 */
	public static List<EbookKeywordItem> getAsEbookKeywordItem(List<String> keywords) {
		final ArrayList<EbookKeywordItem> result = new ArrayList<EbookKeywordItem>(keywords.size());
		for (String keyword : keywords) {
			result.add(new EbookKeywordItem(keyword));
		}
		return result;
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
	private static IResourceHandler getCoverThumbnailResourceHandler(final IResourceHandler ebookResource) {
		final String thumbnail = thumbnailFolder + UUID.nameUUIDFromBytes(ebookResource.toString().getBytes()) + ".jpg";
		return ResourceHandlerFactory.getResourceLoader(thumbnail);
	}
}
