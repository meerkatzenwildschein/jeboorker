package org.rr.jeborker.converter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
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

	protected IResourceHandler comicBookResource;
	
	public ACompressedImageToPdfConverter(IResourceHandler comicBookResource) {
		this.comicBookResource = comicBookResource;
	}	
	
	@Override
	public IResourceHandler convert() throws IOException {
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
		} catch(Exception e) { 
			LoggerFactory.getLogger(this).log(Level.WARNING, "Could not convert " + comicBookResource.getName() + " to Pdf." , e);
		} finally {
			if(pdfWriter != null) {
				pdfWriter.flush();
				try { pdfWriter.close(); } catch(Exception e) {}
			}
			if(contentOutputStream != null) {
				contentOutputStream.flush();
				IOUtils.closeQuietly(contentOutputStream);
			}
		}
		
		ConverterUtils.transferMetadata(this.comicBookResource, targetPdfResource);
		
		return targetPdfResource;
	}	
	
	private PdfWriter createPdfWriter(final Document document, final OutputStream targetPdfOutputStream) throws DocumentException, IOException {
		PdfWriter writer = PdfWriter.getInstance(document, targetPdfOutputStream);
		
		// add the image now and not later when itext think it's good to do that.
		writer.setStrictImageSequence(true);	
		
		return writer;
	}

    private void attachImagesToPdf(final List<String> compressedImageEntries, final Document document, final PdfWriter pdfWriter) throws IOException, DocumentException {
        for(int i= 0; i < compressedImageEntries.size(); i++) {
        	String imageEntry = compressedImageEntries.get(i);
        	BufferedImage bufferedImage = getBufferedImageFromArchive(imageEntry);
        	
        	float pageWidth = ((float)bufferedImage.getWidth());
        	float pageHeight = ((float)bufferedImage.getHeight());
        	document.setPageSize(new Rectangle(pageWidth, pageHeight));

        	if(i == 0) {
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
    }
    
    private BufferedImage getBufferedImageFromArchive(String imageEntry) {
    	InputStream compressionEntryStream = getCompressionEntryStream(this.comicBookResource, imageEntry);
    	IImageProvider imageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getResourceHandler(compressionEntryStream));
    	BufferedImage bufferedImage = imageProvider.getImage();
    	return bufferedImage;
    }

	protected abstract InputStream getCompressionEntryStream(IResourceHandler resourceHandler, String entry);
	
	protected abstract List<String> listEntries(IResourceHandler cbzResource);    
}
