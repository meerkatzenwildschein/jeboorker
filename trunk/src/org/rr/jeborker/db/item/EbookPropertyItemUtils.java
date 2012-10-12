package org.rr.jeborker.db.item;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

import com.orientechnologies.orient.core.record.impl.ORecordBytes;

public class EbookPropertyItemUtils {
	
    /**
     * Get all fields which are marked with a {@link ViewField} annotation.
     * @param itemClass TODO
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
		refreshEbookPropertyItem(item, resource, true);
		
		return item;
	}

	/**
	 * Refreshed the data of the given {@link EbookPropertyItem} by rereading it's metadata.
	 * @param item The item to be refreshed.
	 * @param resource The resource file for the {@link EbookPropertyItem}. it's optional and can be null.
	 */
	public static void refreshEbookPropertyItem(final EbookPropertyItem item, IResourceHandler resource, boolean refreshCover) {
		if(resource==null) {
			resource = ResourceHandlerFactory.getResourceLoader(item.getFile());
		}

		IMetadataReader reader = MetadataHandlerFactory.getReader(resource);
		if(reader!=null) {
			try {
				final List<MetadataProperty> metadataProperties = reader.readMetaData();
				reader.fillEbookPropertyItem(metadataProperties, item);
				if(refreshCover) {
					final byte[] imageData = reader.getCover();
					setupCoverData(item, imageData);
				}
			} finally {
				reader.dispose();
			}
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
		if(imageData!=null && imageData.length > 0) {
			IImageProvider imageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getVirtualResourceLoader(UUID.randomUUID().toString(), imageData));
			if(imageProvider!=null) {
				BufferedImage thumbnailImage = ImageUtils.scaleToHeight(imageProvider.getImage(), 100);
				
				//much faster cropping the thumbnail than the original sized cover image.
				BufferedImage cropedImage = ImageUtils.crop(thumbnailImage);
				
				byte[] thumbnailImageBytes = ImageUtils.getImageBytes(cropedImage, "image/jpeg");
				if(thumbnailImageBytes != null) {
					ORecordBytes oRecordBytes = new ORecordBytes(DefaultDBManager.getInstance().getDB().getUnderlying(), thumbnailImageBytes);
					item.setCoverThumbnail(oRecordBytes);
				} else {
					item.setCoverThumbnail(null);
				}
			} else {
				item.setCoverThumbnail(null);
			}
		} else {
			item.setCoverThumbnail(null);
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
}
