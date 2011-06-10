package org.rr.jeborker.db.item;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

public class EbookPropertyItemUtils {
	
	/**
	 * Creates a new {@link EbookPropertyItem} from the given resource.
	 * @param resource The resource where the {@link EbookPropertyItem} should be created from.
	 * @return The newly created {@link EbookPropertyItem}.
	 */
	public static EbookPropertyItem createEbookPropertyItem(final IResourceHandler resource, final IResourceHandler topLevelBaseFolder) {
		final EbookPropertyItem item = new EbookPropertyItem();
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
				item.setCoverThumbnail(thumbnailImageBytes);
			} else {
				item.setCoverThumbnail(null);
			}
		} 
	}	
}
