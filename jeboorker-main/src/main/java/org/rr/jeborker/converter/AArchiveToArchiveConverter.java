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
import org.rr.jeborker.gui.ConverterPreferenceController;
import org.rr.jeborker.gui.MainController;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

public abstract class AArchiveToArchiveConverter implements IEBookConverter {
	
	private ConverterPreferenceController converterPreferenceController;
	
	private IResourceHandler archiveResource;
	
	public AArchiveToArchiveConverter(IResourceHandler archiveResource) {
		this.archiveResource = archiveResource;
	}
	
	@Override
	public IResourceHandler convert() throws IOException {
		final ConverterPreferenceController converterPreferenceController = getConverterPreferenceController();
		if(converterPreferenceController.isConfirmed()) {
			List<CompressedDataEntry> sourceFiles = extractArchive(archiveResource);
			return convertAndWriteToTargetArchive(converterPreferenceController, sourceFiles);
		}
		
		return null;
	}
	
	protected IResourceHandler convertAndWriteToTargetArchive(ConverterPreferenceController converterPreferenceController,
			List<CompressedDataEntry> sourceFiles) throws IOException {
		IResourceHandler targetArchiveResource = ResourceHandlerFactory.getUniqueResourceHandler(archiveResource, getTargetArchiveExtension());
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
						addToArchive(targetArchiveResource, sourceFile, i, imageBytes);
					} else {
						addToArchive(targetArchiveResource, sourceFile, imageBytes);						
					}
				}
			} else {
				addToArchive(targetArchiveResource, sourceFile, bytes);
			}
		}
		return targetArchiveResource;
			
	}

	protected abstract String getTargetArchiveExtension();

	protected abstract void addToArchive(IResourceHandler targetCbzResource, CompressedDataEntry sourceFile, byte[] imageBytes);

	protected abstract void addToArchive(IResourceHandler targetCbzResource, CompressedDataEntry sourceFile, int i, byte[] imageBytes);
	
	protected String injectCounterToFileName(String fileName, int count) {
		return FilenameUtils.getBaseName(fileName) + "_" + count + "." + FilenameUtils.getExtension(fileName);
	}

	public void setConverterPreferenceController(ConverterPreferenceController controller) {
		this.converterPreferenceController = controller;
	}
	
	/**
	 * Gets the {@link ConverterPreferenceController} for this instance. Creates a new
	 * {@link ConverterPreferenceController} if no one is created previously.
	 * @see #createConverterPreferenceController()
	 */
	protected ConverterPreferenceController getConverterPreferenceController() {
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
		controller.setShowImageSizeEntry(true);
		controller.setShowLandscapePageEntries(true);
		return controller;
	}

  protected BufferedImage getBufferedImageFromArchive(InputStream compressionEntryStream) {
  	IImageProvider imageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getResourceHandler(compressionEntryStream));
  	return imageProvider.getImage();
  }

	protected abstract List<CompressedDataEntry> extractArchive(IResourceHandler cbzResource);
}
