package org.rr.jeborker.metadata.pdf;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.pm.image.ImageUtils;

public abstract class PDFRenderer {

	private IResourceHandler pdfFile;

	public static PDFRenderer getPDFRendererInstance(IResourceHandler pdfFile) {
		PDFRenderer result = new JMuPDFRenderer();
		result.setResourceHandler(pdfFile);
		return result;
	}

	/**
	 * Renders a page of the given pdf data.
	 * @param pdfdata The pdf data bytes.
	 * @param pageNumber The page number to be rendered starting with 1.
	 * @return The rendered pdf image or <code>null</code> if the pdf could not be rendered.
	 * @throws IOException
	 */
	public abstract BufferedImage renderPage(int pageNumber) throws IOException;

	/**
	 * Renders a page of the given pdf data.
	 * @param pdfdata The pdf data bytes.
	 * @param pageNumber The page number to be rendered starting with 1.
	 * @return The rendered pdf data or <code>null</code> if the pdf could not be rendered.
	 * @throws IOException
	 */
	public byte[] renderPagetoJpeg(int pageNumber) throws IOException {
		BufferedImage image = renderPage(pageNumber);
		byte[] imageBytes = ImageUtils.getImageBytes(image, "image/jpeg");
		return imageBytes;
	}

	/**
	 * Set the pdf file containing the pdf data used for the {@link PDFDocument} instance.
	 * @param pdfFile The pdf file to be used.
	 */
	protected void setResourceHandler(IResourceHandler pdfFile)  {
		this.pdfFile = pdfFile;
	}

	/**
	 * get the pdf file for this {@link PDFDocument} instance.
	 * @return The desired {@link PDFDocument}.
	 */
	public IResourceHandler getResourceHandler() {
		return this.pdfFile;
	}

}
