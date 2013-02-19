package org.rr.jeborker.converter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.ResourceHandlerUtils;
import org.rr.commons.utils.truezip.TrueZipUtils;
import org.rr.jeborker.JeboorkerConstants;
import org.rr.jeborker.JeboorkerConstants.SUPPORTED_MIMES;
import org.rr.pm.image.ImageUtils;

public class PdfToCBZConverter implements IEBookConverter {

	private IResourceHandler pdfResource;
	
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
	private byte[] renderPage(com.jmupdf.pdf.PdfDocument doc, int pageNumber) throws Exception {
		com.jmupdf.page.PagePixels pp = null;
		com.jmupdf.page.Page page = null;
		try {
			page = doc.getPage(pageNumber);

			pp = new com.jmupdf.page.PagePixels(page);
			pp.setRotation(com.jmupdf.page.Page.PAGE_ROTATE_NONE);
			pp.setZoom(1.5f);
			pp.drawPage(null, pp.getX0(), pp.getY0(), pp.getX1(), pp.getY1());
			BufferedImage image = pp.getImage();
			byte[] imageBytes = ImageUtils.getImageBytes(image, "image/jpeg");
			return imageBytes;
		} finally {
			if (pp != null) {
				pp.dispose();
			}
		}
	}	

	@Override
	public IResourceHandler convert() throws IOException {
		IResourceHandler targetCbzResource = ResourceHandlerFactory.getUniqueResourceHandler(pdfResource, "cbz");
		
		com.jmupdf.pdf.PdfDocument doc = null;
		try {
			doc = new com.jmupdf.pdf.PdfDocument(pdfResource.getContent());
			int pageCount = doc.getPageCount();
			for(int pageNumber = 0; pageNumber < pageCount; pageNumber++) {
				byte[] renderPage = renderPage(doc, pageNumber + 1);
				TrueZipUtils.add(targetCbzResource, getFileName(pageNumber, pageCount), new ByteArrayInputStream(renderPage));
			}
		} catch(Exception e) { 
			if(e instanceof IOException) {
				throw (IOException) e;
			}
			throw new IOException("Failed to convert PDF " + pdfResource.getName(), e);
		} finally {
			if (doc != null) {
				doc.dispose();
				doc = null;
			}
		}	
		
		ConverterUtils.transferMetadata(pdfResource, targetCbzResource);
		return targetCbzResource;		
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
		String pattern = "000";
		if(pageCount < 10) {
			pattern = "0";
		} else if(pageCount < 100) {
			pattern = "00";
		} else if(pageCount < 1000) {
			pattern = "000";
		} else if(pageCount < 10000) {
			pattern = "0000";
		} else if(pageCount < 100000) {
			pattern = "00000";
		}
		DecimalFormat format = new DecimalFormat(pattern);
		String formattedPageNumber = format.format(pageNumber);
		
		return formattedPageNumber + "_" + name + ".jpg";
	}
}
