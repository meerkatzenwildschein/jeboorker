package org.rr.jeborker.metadata.pdf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.pm.image.ImageUtils;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;

class JMuPDFRenderer extends PDFCommonRenderer {

	public byte[] renderPagetoJpeg(int pageNumber) {
		BufferedImage image;
		try {
			image = renderPage(pageNumber);
			byte[] imageBytes = ImageUtils.getImageBytes(image, "image/jpeg");
			return imageBytes;			
		} catch (IOException e) {
			LoggerFactory.getLogger().log(Level.INFO, "Failed to render image for " + getResourceHandler() , e);
		}
		return null;
	}

	@Override
	public BufferedImage renderPage(int pageNumber) throws IOException {
		com.jmupdf.pdf.PdfDocument doc = null;
		com.jmupdf.page.PagePixels pp = null;
		com.jmupdf.page.Page page = null;
		try {
			byte[] pdfdata = getResourceHandler().getContent();
			doc = new com.jmupdf.pdf.PdfDocument(pdfdata);

			page = doc.getPage(pageNumber);

			pp = new com.jmupdf.page.PagePixels(page);
			pp.setRotation(com.jmupdf.page.Page.PAGE_ROTATE_NONE);
			pp.drawPage(null, pp.getX0(), pp.getY0(), pp.getX1(), pp.getY1());
			BufferedImage image = pp.getImage();
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocException e) {
			LoggerFactory.getLogger().log(Level.INFO, "Failed to render image for " + getResourceHandler() , e);
		} catch (DocSecurityException e) {
			LoggerFactory.getLogger().log(Level.INFO, "Failed to render image for " + getResourceHandler() , e);
		} finally {
			if (pp != null) {
				pp.dispose();
			}
			if (doc != null) {
				doc.dispose();
			}
		}
		return null;
	}	
}