package org.rr.jeborker.converter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.MimeUtils;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.ResourceHandlerUtils;
import org.rr.commons.utils.compression.truezip.TrueZipUtils;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.ConverterPreferenceController;
import org.rr.jeborker.gui.MainController;
import org.rr.pm.image.ImageUtils;

import com.jmupdf.interfaces.Page;
import com.jmupdf.interfaces.PagePixels;
import com.jmupdf.page.PageRect;

public class PdfToCBZConverter implements IEBookConverter {
	
	private static String IMAGE_QUALITY_LABEL = Bundle.getString("MultipleConverter.imageQuality.label");
	
	private static String IMAGE_QUALITY_KEY = PdfToCBZConverter.class.getName() + "." + IMAGE_QUALITY_LABEL;
	
	private APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);

	private IResourceHandler pdfResource;
	
	private ConverterPreferenceController converterPreferenceController;
	
	public PdfToCBZConverter(IResourceHandler pdfResource) {
		this.pdfResource = pdfResource;
	}
	
	/**
	 * Renders a page of the given pdf data. 
	 * @param pdfdata The pdf bytes.
	 * @param pageNumber The page number to be rendered starting with 1.
	 * @return The rendered pdf data or <code>null</code> if the pdf could not be rendered.
	 * @throws IOException
	 */
	private List<byte[]> renderPage(ConverterPreferenceController converterPreferenceDialog, com.jmupdf.pdf.PdfDocument doc, int pageNumber) throws Exception {
		PagePixels pp = null;
		Page page = null;
		PageRect bb = null;
		try {
			page = doc.getPage(pageNumber);

			pp = page.getPagePixels();
			bb = page.getBoundBox();
			pp.getOptions().setRotate(Page.PAGE_ROTATE_NONE);
			pp.getOptions().setZoom(1.5f);
			pp.drawPage(null, bb.getX0(), bb.getY0(), bb.getX1(), bb.getY1());
			BufferedImage image = pp.getImage();
			List<BufferedImage> processImageModifications = ConverterUtils.processImageModifications(image, getImageQuality(), converterPreferenceDialog);
			List<byte[]> result = new ArrayList<byte[]>(processImageModifications.size());
			for(BufferedImage processedImage : processImageModifications) {
				byte[] imageBytes = ImageUtils.getImageBytes(processedImage, MimeUtils.MIME_JPEG);
				result.add(imageBytes);
			}
			return result;
		} finally {
			if (pp != null) {
				pp.dispose();
			}
		}
	}	

	@Override
	public IResourceHandler convert() throws IOException {
		ConverterPreferenceController converterPreferenceDialog = getConverterPreferenceController();
		if(converterPreferenceDialog.isConfirmed()) {
			IResourceHandler targetCbzResource = ResourceHandlerFactory.getUniqueResourceHandler(pdfResource, "cbz");
			
			com.jmupdf.pdf.PdfDocument doc = null;
			try {
				doc = new com.jmupdf.pdf.PdfDocument(pdfResource.getContent());
				int pageCount = doc.getPageCount();
				for(int pageNumber = 0, additional = 0; pageNumber < pageCount; pageNumber++) {
					List<byte[]> renderedPages = renderPage(converterPreferenceDialog, doc, pageNumber + 1);
					for(int i = 0; i < renderedPages.size(); i++) {
						if(i > 0) {
							additional++;
						}
						byte[] renderedPage = renderedPages.get(i);
						TrueZipUtils.add(targetCbzResource, getFileName(pageNumber + additional, pageCount), new ByteArrayInputStream(renderedPage));
					}
				}
			} catch(IOException e) { 
				 throw e;
			} catch(Exception e) {
				throw new IOException("Failed to convert PDF " + pdfResource.getName(), e);
			} finally {
				if (doc != null) {
					doc.dispose();
					doc = null;
				}
			}	
			
			ConverterUtils.transferMetadata(pdfResource, targetCbzResource);
			preferenceStore.addGenericEntryAsNumber(IMAGE_QUALITY_KEY, getImageQuality());
			return targetCbzResource;	
		}
		return null;
	}

	@Override
	public SUPPORTED_MIMES getConversionSourceType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_PDF;
	}

	@Override
	public SUPPORTED_MIMES getConversionTargetType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_CBZ;
	}
	
	private String getFileName(int pageNumber, int pageCount) {
		String name = ResourceHandlerUtils.getFileNameWithoutFileExtension(pdfResource);
		String pattern = StringUtils.repeat("0", String.valueOf(pageCount).length());
		
		DecimalFormat format = new DecimalFormat(pattern);
		String formattedPageNumber = format.format(pageNumber);
		
		return formattedPageNumber + "_" + name + ".jpg";
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
		ConverterPreferenceController preferenceController = MainController.getController().getConverterPreferenceController();
		preferenceController.addCommonSlider(IMAGE_QUALITY_LABEL, preferenceStore.getGenericEntryAsNumber(IMAGE_QUALITY_KEY, 100).intValue());
		preferenceController.setShowLandscapePageEntries(true);
		return preferenceController;
	}
	
	private int getImageQuality() {
		return getConverterPreferenceController().getCommonValueAsInt(IMAGE_QUALITY_LABEL);
	}
    
	public void setConverterPreferenceController(ConverterPreferenceController controller) {
		this.converterPreferenceController = controller;
	}
	
	public static void main(String[] args) {
		System.out.println(StringUtils.repeat("0", String.valueOf(101).length()));
	}

}
