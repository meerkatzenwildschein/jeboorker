package org.rr.jeborker.converter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.MimeUtils;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.ConverterPreferenceController;
import org.rr.jeborker.gui.MainController;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.epub.EpubWriter;

/**
 * A converter for image archives to epub
 */
abstract class ACompressedImageToEpubConverter implements IEBookConverter {
	
	private static String IMAGE_QUALITY_LABEL = Bundle.getString("MultipleConverter.imageQuality.label");
	
	private static String IMAGE_QUALITY_KEY = ACompressedImageToEpubConverter.class.getName() + "." + IMAGE_QUALITY_LABEL;
	
	private APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);

	protected IResourceHandler comicBookResource;

	private ConverterPreferenceController converterPreferenceController;

	private ArrayList<IResourceHandler> tempFiles = new ArrayList<>();

	public ACompressedImageToEpubConverter(IResourceHandler comicBookResource) {
		this.comicBookResource = comicBookResource;
	}

	@Override
	public IResourceHandler convert() throws IOException {
		final ConverterPreferenceController converterPreferenceController = getConverterPreferenceController();
		if(!converterPreferenceController.isConfirmed()) {
			return null;
		}

		final List<String> compressedImageEntries = listEntries(this.comicBookResource);
		if(compressedImageEntries == null || compressedImageEntries.isEmpty()) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "The Comic book archive " + comicBookResource.getName() + " is empty.");
			return null;
		}
		final Book epub = this.createEpub(compressedImageEntries);
		final EpubWriter writer = new EpubWriter();

		IResourceHandler targetEpubResource = ResourceHandlerFactory.getUniqueResourceHandler(this.comicBookResource, "epub");
		OutputStream contentOutputStream = targetEpubResource.getContentOutputStream(false);
		try {
			writer.write(epub, contentOutputStream);
		} finally {
			contentOutputStream.flush();
			contentOutputStream.close();
		}

		deleteTemporaryFiles();
		ConverterUtils.transferMetadata(this.comicBookResource, targetEpubResource);
		
		preferenceStore.addGenericEntryAsNumber(IMAGE_QUALITY_KEY, getImageQuality());

		return targetEpubResource;
	}

	private Book createEpub(List<String> cbzEntries) throws IOException {
		final Book epub = new Book();
		final Spine spine = new Spine();

		List<Resource> resources = new ArrayList<>(cbzEntries.size());
		for(int i = 0; i < cbzEntries.size(); i++) {
			final String cbzEntry = cbzEntries.get(i);
			if(ConverterUtils.isImageFileName(cbzEntry)) {
				final InputStream imageInputStream = getCompressionEntryStream(this.comicBookResource, cbzEntry);
				final List<InputStream> convertedImageInputStream = getConvertedImageInputStream(imageInputStream, cbzEntry);
				for(int j = 0; j < convertedImageInputStream.size(); j++) {
					final InputStream imageIn = convertedImageInputStream.get(j);
					final String cbzHrefEntry = createHrefEntry(cbzEntry, epub, j);
					final Resource imageResource = new Resource(imageIn, cbzHrefEntry);

					resources.add(imageResource);
					epub.addResource(imageResource);

					this.attachSpineEntry(epub, spine, imageResource);

					//the first image from the cbz is the cover image.
					if(i == 0) {
						epub.setCoverImage(imageResource);
					}
				}
			}
		}
		epub.setSpine(spine);
		return epub;
	}

	/**
	 * Creates a xhtml doc for the given image and add it as spine to the given epub.
	 * @throws IOException
	 */
	private void attachSpineEntry(final Book epub, final Spine spine, final Resource imageResource) throws IOException {
		String imageName = imageResource.getHref();
		if(ConverterUtils.isImageFileName(imageName)) {
			if(imageName.lastIndexOf('.') != -1) {
				imageName = imageName.substring(0, imageName.lastIndexOf('.'));
			}
			Resource spineResource = new Resource(imageName + ".xhtml");
			InputStream spineTemplateIn = ACompressedImageToEpubConverter.class.getResourceAsStream("CbzToEpubSpineImageTemplate");
			String spineTemplate = IOUtils.toString(spineTemplateIn);
			String spineDoc = MessageFormat.format(spineTemplate, new Object[] {imageResource.getHref()});
			spineResource.setData(spineDoc.getBytes(StandardCharsets.UTF_8));
			spine.addResource(spineResource);
			epub.addResource(spineResource);
		}
	}

	/**
	 * Takes the given InputStream with image data and does the desired image conversion if necessary.
	 * @return The converted image(s). If no conversion is necessary, the original input stream is returned.
	 */
	private List<InputStream> getConvertedImageInputStream(InputStream imageIn, String imageName) throws IOException {
		if(isImageConversion()) {
			ArrayList<InputStream> result = new ArrayList<>();
			IImageProvider imageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getResourceHandler(imageIn));
			List<BufferedImage> processImageModifications = ConverterUtils.processImageModifications(imageProvider.getImage(), getImageQuality(), getConverterPreferenceController());
			for(BufferedImage image : processImageModifications) {
				String mime = MimeUtils.getImageMimeFromFileName(imageName, MimeUtils.MIME_JPEG);
				byte[] imageBytes = ImageUtils.getImageBytes(image, mime);

				//copy the converted data to HD because we possibly have not enough memory for the whole boo.
				IResourceHandler temporaryResource = ResourceHandlerFactory.getTemporaryResource(mime.substring(mime.indexOf('/') + 1));
				tempFiles.add(temporaryResource);
				temporaryResource.setContent(imageBytes);
				result.add(temporaryResource.getContentInputStream());
			}
			return result;
		} else {
			return Collections.singletonList(imageIn);
		}
	}

	/**
	 * Tells if some image conversion is needed.
	 */
	private boolean isImageConversion() {
		ConverterPreferenceController preferenceController = getConverterPreferenceController();
		return preferenceController.isLandscapePageRotate() || preferenceController.isLandscapePageSplit()
				|| preferenceController.getCommonValueAsInt(IMAGE_QUALITY_LABEL) < 99;
	}

	/**
	 * Creates a href entry for the given archive entry.
	 * @param cbzEntry The archive entry name
	 * @param epub The {@link Book} instance to be created.
	 * @param index The index of the href with the same name
	 * @return The desired href entry.
	 */
	private String createHrefEntry(final String cbzEntry, final Book epub, final int index) {
		try {
			StringBuilder result = new StringBuilder();
			for(int i = 0; i < cbzEntry.length(); i++) {
				char c = cbzEntry.charAt(i);
				if(Character.isWhitespace(c)) {
					result.append('_');
				} else if(Character.isDigit(c)) {
					result.append(c);
				} else if(c >= 'A' || c <= 'z') {
					result.append(c);
				} else if(c == '/') {
					result.setLength(0); //remove path segement
				}
			}
			String href = result.toString();
			while(epub.getResources().getByHref(href) != null) {
				if(href.lastIndexOf('.') != -1) {
					String ext = href.substring(href.lastIndexOf('.'));
					href = href.substring(0, href.lastIndexOf('.')) +  "_" + index + ext;
				} else {
					href = href + index + "_";
				}
			}

			return href;
		} catch(Exception e) {
			return cbzEntry;
		}
	}

	private void deleteTemporaryFiles() {
		for(IResourceHandler tempFile : tempFiles) {
			try {
				tempFile.delete();
			} catch (IOException e) {
				LoggerFactory.getLogger().log(Level.WARNING, "Could not delete temporary file " + tempFile.toString(), e);
			}
		}
	}


	protected abstract InputStream getCompressionEntryStream(IResourceHandler resourceHandler, String entry);

	protected abstract List<String> listEntries(IResourceHandler cbzResource);

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
	
	private int getImageQuality() {
		return getConverterPreferenceController().getCommonValueAsInt(IMAGE_QUALITY_LABEL);
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

	public void setConverterPreferenceController(ConverterPreferenceController controller) {
		this.converterPreferenceController = controller;
	}
}
