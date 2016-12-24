package org.rr.jeborker.converter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.ConverterPreferenceController;
import org.rr.jeborker.gui.MainController;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

abstract class ACompressedImageToPdfConverter implements IEBookConverter {
	
	private static String IMAGE_QUALITY_LABEL = Bundle.getString("MultipleConverter.imageQuality.label");
	
	private static String IMAGE_QUALITY_KEY = ACompressedImageToPdfConverter.class.getName() + "." + IMAGE_QUALITY_LABEL;
	
	private APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);

	protected IResourceHandler comicBookResource;
	
	private ConverterPreferenceController converterPreferenceController = null;
	
	ACompressedImageToPdfConverter(IResourceHandler comicBookResource) {
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
		final Document document = new Document();
		final IResourceHandler targetPdfResource = ResourceHandlerFactory.getUniqueResourceHandler(this.comicBookResource, "pdf");
		
		OutputStream contentOutputStream = null;
		PdfWriter pdfWriter = null;
		try {
			contentOutputStream = targetPdfResource.getContentOutputStream(false);
			pdfWriter = this.createPdfWriter(document, contentOutputStream);
			attachImagesToPdf(compressedImageEntries, document, pdfWriter);
			contentOutputStream.flush();
		} catch(Exception e) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Could not convert " + comicBookResource.getName() + " to Pdf." , e);
		} finally {
			closePdfWriter(pdfWriter);
			IOUtils.closeQuietly(contentOutputStream);
		}
		
		ConverterUtils.transferMetadata(this.comicBookResource, targetPdfResource);
		preferenceStore.addGenericEntryAsNumber(IMAGE_QUALITY_KEY, getImageQuality());
		
		return targetPdfResource;
	}

	protected void closePdfWriter(PdfWriter pdfWriter) {
		if(pdfWriter != null) {
			pdfWriter.flush();
			try { 
				pdfWriter.close(); 
			} catch(Exception e) {}
		}
	}	
	
	private PdfWriter createPdfWriter(final Document document, final OutputStream targetPdfOutputStream) throws DocumentException, IOException {
		PdfWriter writer = PdfWriter.getInstance(document, targetPdfOutputStream);
		
		// add the image now and not later when itext think it's good to do that.
		writer.setStrictImageSequence(true);
		
		return writer;
	}

    private void attachImagesToPdf(final List<String> compressedImageEntries, final Document document, final PdfWriter pdfWriter) throws IOException, DocumentException {
    	boolean documentOpen = false;
        for(int i= 0; i < compressedImageEntries.size(); i++) {
        	String imageEntry = compressedImageEntries.get(i);
        	if(ConverterUtils.isImageFileName(imageEntry)) {
	        	BufferedImage image = getBufferedImageFromArchive(imageEntry);
	        	List<BufferedImage> processImageModifications = ConverterUtils.processImageModifications(image, getImageQuality(), getConverterPreferenceController());
	        	for(BufferedImage bufferedImage : processImageModifications) {
		        	float pageWidth = ((float)bufferedImage.getWidth());
		        	float pageHeight = ((float)bufferedImage.getHeight());
		        	document.setPageSize(new Rectangle(pageWidth, pageHeight));
		
		        	if(!documentOpen) {
		        		documentOpen = true;
		        		document.open();
		        	} else {
		        		document.newPage();
		        	}
		        	
		            PdfContentByte cb = pdfWriter.getDirectContent();
		            Image pdfImage = Image.getInstance(cb, bufferedImage, 1);
		
		            pdfImage.setAlignment(Element.ALIGN_CENTER);
		            pdfImage.setAbsolutePosition(0, 0);
		            
		            cb.addImage(pdfImage);
	        	}
	    		pdfWriter.flush();
        	}
        }
    }
    
    private BufferedImage getBufferedImageFromArchive(String imageEntry) {
    	InputStream compressionEntryStream = getCompressionEntryStream(this.comicBookResource, imageEntry);
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
    
	protected abstract InputStream getCompressionEntryStream(IResourceHandler resourceHandler, String entry);
	
	protected abstract List<String> listEntries(IResourceHandler cbzResource);    
}
