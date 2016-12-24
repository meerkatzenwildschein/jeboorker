package org.rr.jeborker.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.ConverterPreferenceController;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.pdf.PDFUtils;

import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfToPdfConverter implements IEBookConverter {
	
	private static String IMAGE_QUALITY_LABEL = Bundle.getString("MultipleConverter.imageQuality.label");
	
	private static String IMAGE_QUALITY_KEY = PdfToPdfConverter.class.getName() + "." + IMAGE_QUALITY_LABEL;
	
	private APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);

	private ConverterPreferenceController converterPreferenceController;
	
	private IResourceHandler pdfResource;
	
	PdfToPdfConverter(IResourceHandler pdfSource) {
		this.pdfResource = pdfSource;
	}

	@Override
	public IResourceHandler convert() throws IOException {
		ConverterPreferenceController converterPreferenceDialog = getConverterPreferenceController();
		if(!converterPreferenceDialog.isConfirmed()) {
			return null;
		}
		
		final Document document = new Document();
		final IResourceHandler targetPdfResource = ResourceHandlerFactory.getUniqueResourceHandler(this.pdfResource, "pdf");
		final OutputStream pdfOutputStream = targetPdfResource.getContentOutputStream(false);
		
		PdfReader reader = null;
		PdfWriter writer = null;
		try {
			reader = PDFUtils.getReader(this.pdfResource.toFile());
			writer = PdfWriter.getInstance(document, pdfOutputStream);
			if(writer == null) {
				throw new IOException("Failed to create PDF writer for " + pdfResource.getName());
			} else {
				transferPdfContent(document, reader, writer);
			}
			
		} catch (IOException e) {
				throw e;
		} catch (Exception e) {
			throw new IOException("Failed to convert PDF " + pdfResource.getName(), e);
		} finally {
			if(writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch(Exception e) {
					//It always throws a "already closed" exception because itext close the
					//output stream in the document and additional in the writer. Mute these exception.
					if(!e.getMessage().startsWith("Stream Closed")) {
						LoggerFactory.getLogger().log(Level.WARNING, "Failed to close pdf writer", e);						
					}
				}
			}
			
			IOUtils.closeQuietly(pdfOutputStream);
			
			if(reader != null) {
				try {
					reader.close();
				} catch(Exception e) {
					LoggerFactory.getLogger().log(Level.WARNING, "Failed to close pdf reader", e);
				}
			}			
		}
		
		preferenceStore.addGenericEntryAsNumber(IMAGE_QUALITY_KEY, getImageQuality());
		return targetPdfResource;
	}
	
	/**
	 * Transfers the pdf content from the reader to the writer.
	 */
	private void transferPdfContent(final Document document, final PdfReader reader, final PdfWriter writer) {
		final float scale = (float) getImageQuality() / 100f;
		final int pageCount = reader.getNumberOfPages();
		
		PdfContentByte directContent = null;		
		for(int i = 0; i < pageCount; i++) {
			Rectangle pageSize = reader.getPageSizeWithRotation(i +1);
			pageSize.setTop(pageSize.getTop() * scale);
			pageSize.setRight(pageSize.getRight() * scale);
			document.setPageSize(pageSize);		
			if(i == 0) {
				document.open();
				directContent = writer.getDirectContent();
			} else {				
				document.newPage();
			}
			
			PdfImportedPage page = writer.getImportedPage(reader, i + 1);
			directContent.addTemplate(page, scale, 0, 0, scale, 0, 0);
		}
		
	}

	@Override
	public SUPPORTED_MIMES getConversionSourceType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_PDF;
	}

	@Override
	public SUPPORTED_MIMES getConversionTargetType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_PDF;
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
		preferenceController.setShowLandscapePageEntries(false);
		preferenceController.addCommonSlider(IMAGE_QUALITY_LABEL, preferenceStore.getGenericEntryAsNumber(IMAGE_QUALITY_KEY, 100).intValue());
		return preferenceController;
  }	
  
	private int getImageQuality() {
		return getConverterPreferenceController().getCommonValueAsInt(IMAGE_QUALITY_LABEL);
	}
    
	public void setConverterPreferenceController(ConverterPreferenceController controller) {
		this.converterPreferenceController = controller;
	}   
}
