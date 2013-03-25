package org.rr.jeborker.metadata.pdf;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.sun.jna.Pointer;

public class PopplerPDFRenderer extends PDFCommonRenderer {

	@Override
	public BufferedImage renderPage(int pageNumber) throws IOException {
		PopplerPDFReader popplerDataSource = new PopplerPDFReader(getResourceHandler().toFile(), null);
		Pointer page = null;
		try {
			page = popplerDataSource.getPage(pageNumber - 1);
			BufferedImage renderedPage = popplerDataSource.renderPage(page);
			return renderedPage;
		} finally {
			if(page != null) {
				popplerDataSource.disposePage(page);
			}
			popplerDataSource.dispose();
		}
	}
}
