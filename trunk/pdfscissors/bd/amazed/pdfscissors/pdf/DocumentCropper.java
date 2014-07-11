package bd.amazed.pdfscissors.pdf;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ProgressMonitor;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.pm.image.ImageUtils;

import bd.amazed.pdfscissors.model.Bundle;
import bd.amazed.pdfscissors.model.PageGroup;
import bd.amazed.pdfscissors.model.PageRectsMap;

import com.itextpdf.text.DocumentException;

public abstract class DocumentCropper {

	private JDocumentDecoderPanel pdfDecoder;

	protected boolean isCancel;

	protected IResourceHandler mainFile;

	public DocumentCropper(IResourceHandler file) {
		if (file == null) {
			throw new IllegalArgumentException("PDFCropper does not accept null file.");
		}
		this.mainFile = file;
	}

	public static DocumentCropper getCropper(IResourceHandler file) {
		if(JeboorkerConstants.SUPPORTED_MIMES.MIME_PDF.getMime().equals(file.getMimeType(true))) {
			return new PdfCropper(file);
		} else if(JeboorkerConstants.SUPPORTED_MIMES.MIME_CBZ.getMime().equals(file.getMimeType(true)) ||
				JeboorkerConstants.SUPPORTED_MIMES.MIME_CBR.getMime().equals(file.getMimeType(true))) {
			return new CbxCropper(file);
		}
		throw new IllegalArgumentException("File type " + file + " not supported");
	}

	public abstract DocumentInfo getDocumentInfo() throws DocumentException, IOException;

	public void cancel() {
		isCancel = true;
	}

	public abstract void cropPdf(DocumentInfo docFile, File targetFile, PageRectsMap pageRectsMap, int viewWidth, int viewHeight, ProgressMonitor progressMonitor) throws IOException, DocumentException;

	public BufferedImage getNormalizedImage(DocumentInfo docFile, PropertyChangeListener listener, PageGroup pageGroup) throws ScissorsDocumentException {
		BufferedImage image = getImage(docFile, listener, pageGroup);
		image = ImageUtils.crop(image, new Rectangle((int)docFile.getNormalizedWidth(),(int) docFile.getNormalizedHeight()), Color.WHITE);
		return image;
	}

	public BufferedImage getImage(DocumentInfo docFile, PropertyChangeListener listener, PageGroup pageGroup) throws ScissorsDocumentException {
		int endPage = pageGroup.getLastPage();
		JDocumentDecoderPanel docDecoder = getDocumentDecoder(docFile);
		BufferedImage lastPage = docDecoder.getPageAsImage(endPage);

		if (lastPage != null) {
			listener.propertyChange(new PropertyChangeEvent(this, "message", null, Bundle.getString("PdfCropper.stacking") +
					" " + pageGroup));
			lastPage = new BufferedImage(lastPage.getWidth(), lastPage.getHeight(), BufferedImage.TYPE_INT_ARGB);

			Graphics2D g2d = (Graphics2D) lastPage.getGraphics();

			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, lastPage.getWidth(), lastPage.getHeight());

			float alpha = 0.5f;
			int type = AlphaComposite.SRC_OVER;
			AlphaComposite composite = AlphaComposite.getInstance(type, alpha);
			g2d.setComposite(composite);
			int pageCount = pageGroup.getPageCount();
			for (int iPageInGroup = pageCount - 1; iPageInGroup >= 0 && !isCancel; iPageInGroup--) {
				int i = pageGroup.getPageNumberAt(iPageInGroup);
				int percentageDone = (100 * (pageCount - iPageInGroup)) / pageCount;
//				System.out.println("Stacking page: " + i + ", completed " + percentageDone);
				listener.propertyChange(new PropertyChangeEvent(this, "progress", null, percentageDone));
				BufferedImage pageImage = getDocumentDecoder(docFile).getPageAsImage(i);
				if (pageImage != null) {
					g2d.drawImage(pageImage, 0, 0, null);
				}
			}
		}

		return lastPage;
	}

	private JDocumentDecoderPanel getDocumentDecoder(DocumentInfo docFile) throws ScissorsDocumentException {
		if (pdfDecoder == null) {
			pdfDecoder = new JDocumentDecoderPanel();
			try {
				pdfDecoder.openPdfFile(docFile);
			} catch (RuntimeException ex) {
				if (ex.toString().contains("bouncy")) { // hah, stupid way of doing this, but what can i do if library
					// is throwing RuntimeException :(
					throw new ScissorsDocumentException("PDF is encrypted or password protected.\nTry to create an unencrypted pdf first using some other tool.");
				}
			}
		}
		return pdfDecoder;
	}
}
