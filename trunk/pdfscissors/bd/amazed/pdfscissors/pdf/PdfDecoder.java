package bd.amazed.pdfscissors.pdf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.metadata.pdf.PDFCommonRenderer;

/**
 * Provides an object to decode pdf files and provide a rasterizer if required -
 * Normal usage is to create instance of PdfDecoder and access via public
 * methods. Examples showing usage in org.jpedal.examples
 * <p/>
 *  We recommend you access JPedal using only public methods listed in API
 */
public class PdfDecoder extends JPanel {

	private PDFCommonRenderer pdfRendererInstance;
	
	private BufferedImage currentPage;
    /**
     * routine to open PDF file and extract key info from pdf file so we can
     * decode any pages. Does not actually decode the pages themselves. Also
     * reads the form data. You must explicitly close any open files with
     * closePdfFile() to Java will not release all the memory
     */
	public void openPdfFile(IResourceHandler filePath) {
		closePdfFile();
		pdfRendererInstance = PDFCommonRenderer.getPDFRendererInstance(filePath);
	}
	
    /**
     * convenience method to close the current PDF file
     */
	public void closePdfFile() {
	}	

    /**
     * decode a page, - <b>page</b> must be between 1 and
     * <b>PdfDecoder.getPageCount()</b> - Will kill off if already running
     *
     * returns minus page if trying to open linearized page not yet available
     * @throws PdfException 
     */
	public void decodePage(int page) throws PdfException {
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
     * @throws PdfException 
     */
	public BufferedImage getPageAsImage(int pageIndex) throws PdfException {
		try {
			return pdfRendererInstance.renderPage(pageIndex);
		} catch (IOException e) {
			throw new PdfException(e);
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
