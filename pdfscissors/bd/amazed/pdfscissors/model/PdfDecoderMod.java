package bd.amazed.pdfscissors.model;

import java.awt.image.BufferedImage;

import bd.amazed.pdfscissors.pdf.PdfDecoder;
import bd.amazed.pdfscissors.pdf.PdfException;

/**
 * 
 * Only to get the page image with transparency support
 * @author Gagan
 *
 */
public class PdfDecoderMod extends PdfDecoder{

	public PdfDecoderMod() {
		super();
	}
	
	public PdfDecoderMod(boolean newRender) {
		//super(newRender);
		super();
	}
	
	public BufferedImage getPageAsImage(int pageIndex) throws PdfException {
		//stupid looking code is to get the transparency support. Too bad getPageAsTransparent was a private method
		BufferedImage image = super.getPageAsImage(pageIndex);
		
		return image;
	}
	
}
