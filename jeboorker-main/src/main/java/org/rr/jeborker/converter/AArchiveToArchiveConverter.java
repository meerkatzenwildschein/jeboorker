package org.rr.jeborker.converter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.MimeUtils;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.ResourceHandlerUtils;
import org.rr.commons.utils.ThreadUtils;
import org.rr.commons.utils.ThreadUtils.RunnableImpl;
import org.rr.commons.utils.compression.CompressedDataEntry;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.ConverterPreferenceController;
import org.rr.jeborker.gui.MainController;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

public abstract class AArchiveToArchiveConverter implements IEBookConverter {
	
	private static String IMAGE_QUALITY_LABEL = Bundle.getString("MultipleConverter.imageQuality.label");
	
	private static String IMAGE_QUALITY_KEY = AArchiveToArchiveConverter.class.getName() + "." + IMAGE_QUALITY_LABEL;
	
	private APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
	
	private ConverterPreferenceController converterPreferenceController;
	
	private IResourceHandler archiveResource;
	
	public AArchiveToArchiveConverter(IResourceHandler archiveResource) {
		this.archiveResource = archiveResource;
	}
	
	@Override
	public IResourceHandler convert() throws IOException {
		ConverterPreferenceController converterPreferenceController = getConverterPreferenceController();
		
		if(converterPreferenceController.isConfirmed()) {
			try {
				List<CompressedDataEntry> sourceFiles = extractArchive(archiveResource);
				return convertAndWriteToTargetArchive(converterPreferenceController, sourceFiles);
			} finally {
				preferenceStore.addGenericEntryAsNumber(IMAGE_QUALITY_KEY, getImageQuality());
			}
		}
		
		return null;
	}
	
	protected IResourceHandler convertAndWriteToTargetArchive(ConverterPreferenceController converterPreferenceController,
			List<CompressedDataEntry> sourceFiles) throws IOException {
		IResourceHandler targetArchiveResource = ResourceHandlerFactory.getUniqueResourceHandler(archiveResource, getTargetArchiveExtension());
		IResourceHandler workingFolder = ResourceHandlerFactory.getTemporaryResourceFolder("conv");
		try {
			List<IResourceHandler> converted = convert(sourceFiles, workingFolder);
			for (IResourceHandler convertedSourceFile : converted) {
				String archiveFile = ResourceHandlerUtils.makeRelative(workingFolder, convertedSourceFile);
				addToArchive(targetArchiveResource, archiveFile, convertedSourceFile.toFile());
			}
		} finally {
			workingFolder.delete();
		}
		return targetArchiveResource;
	}
	
	private List<IResourceHandler> convert(List<CompressedDataEntry> sourceFiles, final IResourceHandler workingFolder) {
		List<List<IResourceHandler>> results = ThreadUtils.loopAndWait(sourceFiles, new RunnableImpl<CompressedDataEntry, List<IResourceHandler>>() {

			@Override
			public List<IResourceHandler> run(CompressedDataEntry sourceFile) {
				List<IResourceHandler> result = new ArrayList<>();				
				byte[] bytes = sourceFile.getBytes();
				try {
					if(ConverterUtils.isImageFileName(sourceFile.getName())) {
						BufferedImage image = getBufferedImage(new ByteArrayInputStream(bytes));
						List<BufferedImage> modifiedImages = ConverterUtils.processImageModifications(image, getImageQuality(), converterPreferenceController);
						for (int i = 0; i < modifiedImages.size(); i++) {
							BufferedImage modifiedImage = modifiedImages.get(i);
							String targetMime = MimeUtils.getImageMimeFromFileName(sourceFile.getName(), "image/" + FilenameUtils.getExtension(sourceFile.getName()));
							byte[] imageBytes = ImageUtils.getImageBytes(modifiedImage, targetMime);
							String targetFile = sourceFile.getPath();
							if(modifiedImages.size() > 1) {
								targetFile = injectCounterToFileName(sourceFile.getName(), i);
							}
							IResourceHandler targetResourceHandler = ResourceHandlerFactory.getResourceHandler(workingFolder, targetFile);
							targetResourceHandler.getParentResource().mkdirs();
							targetResourceHandler.setContent(imageBytes);
							result.add(targetResourceHandler);
						}
					} else {
						IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(workingFolder, sourceFile.getPath());
						resourceHandler.getParentResource().mkdirs();
						resourceHandler.setContent(bytes);
						result.add(resourceHandler);
					}
				} catch(Exception e) {
					LoggerFactory.getLogger(this.getClass()).log(Level.SEVERE, "Failed to convert file " + sourceFile.getName() , e);
				}
				return result;
			}
		});
		return flatten(results);
	}

	private List<IResourceHandler> flatten(List<List<IResourceHandler>> results) {
		List<IResourceHandler> flattenResult = new ArrayList<>();
		for (List<IResourceHandler> result : results) {
			flattenResult.addAll(result);
		}
		return flattenResult;
	}

	protected abstract String getTargetArchiveExtension();
	
	protected abstract void addToArchive(IResourceHandler targetCbzResource, String archiveFile, File imageBytes);

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
		ConverterPreferenceController preferenceController = MainController.getController().getConverterPreferenceController();
		preferenceController.addCommonSlider(IMAGE_QUALITY_LABEL, preferenceStore.getGenericEntryAsNumber(IMAGE_QUALITY_KEY, 100).intValue());
		preferenceController.setShowLandscapePageEntries(true);
		return preferenceController;
	}
	
	private int getImageQuality() {
		return getConverterPreferenceController().getCommonValueAsInt(IMAGE_QUALITY_LABEL);
	}

  protected BufferedImage getBufferedImage(InputStream compressionEntryStream) {
  	IImageProvider imageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getResourceHandler(compressionEntryStream));
  	return imageProvider.getImage();
  }

	protected abstract List<CompressedDataEntry> extractArchive(IResourceHandler cbzResource);
}
