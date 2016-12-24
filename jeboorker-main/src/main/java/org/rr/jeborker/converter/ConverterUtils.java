package org.rr.jeborker.converter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.gui.ConverterPreferenceController;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.IMetadataWriter;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;
import org.rr.pm.image.ImageUtils;

public class ConverterUtils {

	/**
	 * Transfers the main metadata between the given source and the target resource.
	 */
	public static void transferMetadata(final IResourceHandler sourceResource, final IResourceHandler targetResource) {
		IMetadataReader sourceReader = MetadataHandlerFactory.getReader(sourceResource);
		IMetadataReader targetReader = MetadataHandlerFactory.getReader(targetResource);
		List<MetadataProperty> sourceMetadata = sourceReader.readMetadata();
		List<MetadataProperty> targetMetadata = new ArrayList<>(sourceMetadata.size());

		for(IMetadataReader.COMMON_METADATA_TYPES type : IMetadataReader.COMMON_METADATA_TYPES.values()) {
			List<MetadataProperty> sourceMetadataByType = sourceReader.getMetadataByType(false, sourceMetadata, type);
			List<MetadataProperty> targetMetadataByType = targetReader.getMetadataByType(true, targetMetadata, type);

			if(!sourceMetadataByType.isEmpty()) {
				MetadataProperty sourceMetadataProperty = sourceMetadataByType.get(0);
				MetadataProperty targetMetadataProperty = targetMetadataByType.get(0);
				targetMetadataProperty.setPropertyClass(sourceMetadataProperty.getPropertyClass());
				targetMetadataProperty.setValues(sourceMetadataProperty.getValues());

				targetMetadata.add(targetMetadataProperty);
			}
		}

		IMetadataWriter writer = MetadataHandlerFactory.getWriter(targetResource);
		writer.writeMetadata(targetMetadata);
	}

	/**
	 * Applies the image modifications from the given {@link ConverterPreferenceController}.
	 * @param bufferedImage The image to be modified
	 * @param converterPreferenceController The {@link ConverterPreferenceController} instance containing the user selections.
	 * @return The processed images. Never returns <code>null</code>.
	 */
    static List<BufferedImage> processImageModifications(BufferedImage bufferedImage, int imageSize, ConverterPreferenceController converterPreferenceController) {
    	if(imageSize < 99) {
    		bufferedImage = ImageUtils.scalePercent(bufferedImage, (double)imageSize / 100);
    	}

    	final float imageWidth = ((float)bufferedImage.getWidth());
    	final float imageHeight = ((float)bufferedImage.getHeight());

    	if((double) imageWidth / (double)imageHeight > 1.2) {
	    	if(!converterPreferenceController.isLandscapePageRotate() && !converterPreferenceController.isLandscapePageSplit()) {
	    		//no rotation, no split, nothing to do
	    		return Collections.singletonList(bufferedImage);
	    	} else if(converterPreferenceController.isLandscapePageRotate()) {
	    		if(converterPreferenceController.isRotateClockwise()) {
	    			//rotate image clockwise
	    			return Collections.singletonList(ImageUtils.rotate90Degree(bufferedImage, true));
	    		} else {
	    			//rotate image counterclockwise
	    			return Collections.singletonList(ImageUtils.rotate90Degree(bufferedImage, false));
	    		}
	    	} else if(converterPreferenceController.isLandscapePageSplit()) {
	    		//split image
	    		List<BufferedImage> splitted = ImageUtils.splitHorizontal(bufferedImage, 2);
	    		if(converterPreferenceController.isMangaMode()) {
	    			Collections.reverse(splitted);
	    		}
	    		return splitted;
	    	}
    	}
    	return Collections.singletonList(bufferedImage);
    }

	/**
	 * Test if the given name have an image file extension.
	 */
	static boolean isImageFileName(String imageName) {
		if(imageName.endsWith(".jpg") || imageName.endsWith(".jpeg") || imageName.endsWith(".png") || imageName.endsWith(".gif")) {
			return true;
		}
		return false;
	}
	
}
