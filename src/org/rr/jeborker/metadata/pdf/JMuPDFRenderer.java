package org.rr.jeborker.metadata.pdf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.MimeUtils;
import org.rr.pm.image.ImageUtils;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.interfaces.Page;
import com.jmupdf.interfaces.PagePixels;
import com.jmupdf.page.PageRect;

class JMuPDFRenderer extends PDFRenderer {

	public byte[] renderPagetoJpeg(int pageNumber) {
		BufferedImage image;
		try {
			image = renderPage(pageNumber);
			byte[] imageBytes = ImageUtils.getImageBytes(image, MimeUtils.MIME_JPEG);
			return imageBytes;			
		} catch (IOException e) {
			LoggerFactory.getLogger().log(Level.INFO, "Failed to render image for " + getResourceHandler() , e);
		}
		return null;
	}

	@Override
	public BufferedImage renderPage(int pageNumber) throws IOException {
		com.jmupdf.pdf.PdfDocument doc = null;
		PagePixels pp = null;
		Page page = null;
		PageRect bb = null;
		try {
			byte[] pdfdata = getResourceHandler().getContent();
			doc = new com.jmupdf.pdf.PdfDocument(pdfdata);

			page = doc.getPage(pageNumber);
			
			pp = page.getPagePixels();
			bb = page.getBoundBox();
			pp.getOptions().setRotate(Page.PAGE_ROTATE_NONE);
			pp.drawPage(null, bb.getX0(), bb.getY0(), bb.getX1(), bb.getY1());
			BufferedImage image = pp.getImage();
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocException e) {
			LoggerFactory.getLogger().log(Level.INFO, "Failed to render image for " + getResourceHandler() , e);
		} catch (DocSecurityException e) {
			LoggerFactory.getLogger().log(Level.INFO, "Failed to render image for " + getResourceHandler() , e);
		} catch (PageException e) {
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