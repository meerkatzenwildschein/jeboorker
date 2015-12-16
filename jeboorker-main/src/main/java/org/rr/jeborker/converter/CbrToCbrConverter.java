package org.rr.jeborker.converter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.MimeUtils;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.compression.CompressedDataEntry;
import org.rr.commons.utils.compression.FileEntryFilter;
import org.rr.commons.utils.compression.rar.RarUtils;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES;
import org.rr.jeborker.gui.ConverterPreferenceController;
import org.rr.jeborker.gui.MainController;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

public class CbrToCbrConverter implements IEBookConverter {

	private ConverterPreferenceController converterPreferenceController;
	
	private IResourceHandler cbrResource;
	
	public CbrToCbrConverter(IResourceHandler cbrResource) {
		this.cbrResource = cbrResource;
	}

	@Override
	public IResourceHandler convert() throws IOException {
		final ConverterPreferenceController converterPreferenceController = getConverterPreferenceController();
		if(converterPreferenceController.isConfirmed()) {
			List<CompressedDataEntry> sourceFiles = RarUtils.extract(cbrResource, (FileEntryFilter) null);
			return convertAndWriteToTargetCbr(converterPreferenceController, sourceFiles);
		}
		
		return null;
	}

	private IResourceHandler convertAndWriteToTargetCbr(final ConverterPreferenceController converterPreferenceController,
			List<CompressedDataEntry> sourceFiles) throws IOException {
		IResourceHandler targetCbrResource = ResourceHandlerFactory.getUniqueResourceHandler(cbrResource, "cbr");
		for (CompressedDataEntry sourceFile : sourceFiles) {
			byte[] bytes = sourceFile.getBytes();
			if(ConverterUtils.isImageFileName(sourceFile.getName())) {
				BufferedImage image = getBufferedImageFromArchive(new ByteArrayInputStream(bytes));
				List<BufferedImage> modifiedImages = ConverterUtils.processImageModifications(image, converterPreferenceController);
				for (int i = 0; i < modifiedImages.size(); i++) {
					BufferedImage modifiedImage = modifiedImages.get(i);
					String targetMime = MimeUtils.getImageMimeFromFileName(sourceFile.getName(), "image/" + FilenameUtils.getExtension(sourceFile.getName()));
					byte[] imageBytes = ImageUtils.getImageBytes(modifiedImage, targetMime);
					if(modifiedImages.size() > 1) {
						RarUtils.add(targetCbrResource, injectCounterToFileName(sourceFile.getName(), i), new ByteArrayInputStream(imageBytes));
					} else {
						RarUtils.add(targetCbrResource, sourceFile.getName(), new ByteArrayInputStream(imageBytes));						
					}
				}
			} else {
				RarUtils.add(targetCbrResource, sourceFile.getName(), new ByteArrayInputStream(bytes));
			}
		}
		return targetCbrResource;
	}
	
	private String injectCounterToFileName(String fileName, int count) {
		return FilenameUtils.getBaseName(fileName) + "_" + count + "." + FilenameUtils.getExtension(fileName);
	}
	
  private BufferedImage getBufferedImageFromArchive(InputStream compressionEntryStream) {
  	IImageProvider imageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getResourceHandler(compressionEntryStream));
  	BufferedImage bufferedImage = imageProvider.getImage();
  	return bufferedImage;
  }	
	
	/**
	 * Gets the {@link ConverterPreferenceController} for this instance. Creates a new
	 * {@link ConverterPreferenceController} if no one is created previously.
	 * @see #createConverterPreferenceController()
	 */
	private ConverterPreferenceController getConverterPreferenceController() {
		if(this.converterPreferenceController == null) {
			this.converterPreferenceController = this.createConverterPreferenceController();
		}

		if(!this.converterPreferenceController.hasShown()) {
			this.converterPreferenceController.showPreferenceDialog();
		}

		return this.converterPreferenceController;
	}	
	
	/**
	 * Create a new {@link ConverterPreferenceController} instance.
	 */
	public ConverterPreferenceController createConverterPreferenceController() {
		ConverterPreferenceController controller = MainController.getController().getConverterPreferenceController();
		return controller;
	}

	public void setConverterPreferenceController(ConverterPreferenceController controller) {
		this.converterPreferenceController = controller;
	}
	
	@Override
	public SUPPORTED_MIMES getConversionSourceType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_CBR;
	}

	@Override
	public SUPPORTED_MIMES getConversionTargetType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_CBR;
	}	

}
