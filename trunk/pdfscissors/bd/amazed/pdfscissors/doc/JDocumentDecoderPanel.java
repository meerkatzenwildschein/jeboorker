package bd.amazed.pdfscissors.doc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JPanel;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.metadata.comicbook.ArchiveHandlerFactory;
import org.rr.jeborker.metadata.comicbook.IArchiveHandler;
import org.rr.jeborker.metadata.pdf.PDFRenderer;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

/**
 * Provides an object to decode pdf files and provide a rasterizer if required -
 * Normal usage is to create instance of PdfDecoder and access via public
 * methods. Examples showing usage in org.jpedal.examples
 * <p/>
 *  We recommend you access JPedal using only public methods listed in API
 */
public class JDocumentDecoderPanel extends JPanel {

	private interface IScissorsRenderer {
		public abstract BufferedImage renderPage(int pageIndex) throws IOException;
	}

	private IScissorsRenderer rendererInstance;

	private BufferedImage currentPage;

    /**
     * routine to open PDF file and extract key info from pdf file so we can
     * decode any pages. Does not actually decode the pages themselves. Also
     * reads the form data. You must explicitly close any open files with
     * closePdfFile() to Java will not release all the memory
     */
	public void openPdfFile(final DocumentInfo docInfo) {
		final IResourceHandler filePath = docInfo.getOriginalFile();
		if(JeboorkerConstants.SUPPORTED_MIMES.MIME_PDF.getMime().equals(filePath.getMimeType(true))) {
			rendererInstance = new IScissorsRenderer() {

				PDFRenderer pdfRendererInstance = PDFRenderer.getPDFRendererInstance(filePath);

				@Override
				public BufferedImage renderPage(int pageIndex) throws IOException {
					BufferedImage renderPage = pdfRendererInstance.renderPage(pageIndex);
					return ImageUtils.crop(renderPage, new Rectangle((int)docInfo.getNormalizedWidth(), (int) docInfo.getNormalizedHeight()), Color.WHITE);
				}};

		} else if(JeboorkerConstants.SUPPORTED_MIMES.MIME_CBZ.getMime().equals(filePath.getMimeType(true)) ||
				JeboorkerConstants.SUPPORTED_MIMES.MIME_CBR.getMime().equals(filePath.getMimeType(true))) {
			rendererInstance = new IScissorsRenderer() {

				IArchiveHandler handler = ArchiveHandlerFactory.getHandlerInitialized(filePath);

				@Override
				public BufferedImage renderPage(int pageIndex) throws IOException {
					String archiveEntry = handler.getArchiveEntries().get(pageIndex - 1);
					byte[] archiveEntryBytes = handler.getArchiveEntry(archiveEntry);
					IResourceHandler virtualResourceHandler = ResourceHandlerFactory.getVirtualResourceHandler(archiveEntry, archiveEntryBytes);
					IImageProvider imageProvider = ImageProviderFactory.getImageProvider(virtualResourceHandler);
					BufferedImage renderPage = imageProvider.getImage();
					return ImageUtils.crop(renderPage, new Rectangle((int)docInfo.getNormalizedWidth(), (int) docInfo.getNormalizedHeight()), Color.WHITE);
				}};
		}
	}

    /**
     * decode a page, - <b>page</b> must be between 1 and
     * <b>PdfDecoder.getPageCount()</b> - Will kill off if already running
     *
     * returns minus page if trying to open linearized page not yet available
     * @throws ScissorsDocumentException
     */
	public void decodePage(int page) throws ScissorsDocumentException {
		currentPage = getPageAsImage(page);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(currentPage, 0, 0, this.getWidth(), this.getHeight(), null);
	}

	public void paint(Graphics g) {
		super.paint(g);
	}

    /**
     * generate BufferedImage of a page in current file
     *
     * Page size is defined by CropBox
     * @throws ScissorsDocumentException
     */
	public BufferedImage getPageAsImage(int pageIndex) throws ScissorsDocumentException {
		try {
			return rendererInstance.renderPage(pageIndex);
		} catch (IOException e) {
			throw new ScissorsDocumentException(e);
		}
	}

    final public Dimension getMaximumSize() {
    	if(currentPage != null) {
    		return new Dimension(currentPage.getWidth(), currentPage.getHeight());
    	}
    	return new Dimension(800,600);
    }

    final public Dimension getMinimumSize() {
        return new Dimension(100,100);
    }

    /**
     * get sizes of panel <BR>
     * This is the PDF pagesize (as set in the PDF from pagesize) -
     * It now includes any scaling factor you have set (ie a PDF size 800 * 600
     * with a scaling factor of 2 will return 1600 *1200)
     */
    public Dimension getPreferredSize() {
        return getMaximumSize();
    }
}
