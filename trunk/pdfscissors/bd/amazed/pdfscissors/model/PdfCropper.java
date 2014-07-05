package bd.amazed.pdfscissors.model;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import javax.swing.ProgressMonitor;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.metadata.pdf.PDFUtils;

import bd.amazed.pdfscissors.pdf.PdfException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 *
 * @author Gagan
 */
public class PdfCropper {

	private IResourceHandler mainFile;
	private PdfDecoderMod pdfDecoder;
	private boolean isCancel;

	public PdfCropper(IResourceHandler file) {
		if (file == null) {
			throw new IllegalArgumentException("PDFCropper does not accept null file.");
		}
		this.mainFile = file;
	}

	public BufferedImage getImage(PropertyChangeListener listener, PageGroup pageGroup) throws PdfException {
		int endPage = pageGroup.getLastPage();
		PdfDecoderMod pdfDecoder = getPdfDecoder();
		BufferedImage lastPage = pdfDecoder.getPageAsImage(endPage);

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
				BufferedImage pageImage = getPdfDecoder().getPageAsImage(i);
				if (pageImage != null) {
					g2d.drawImage(pageImage, 0, 0, null);
				}
			}
		}

		return lastPage;
	}

	private PdfDecoderMod getPdfDecoder() throws PdfException {
		if (pdfDecoder == null) {
			pdfDecoder = new PdfDecoderMod(true);
			try {
				pdfDecoder.openPdfFile(mainFile);
			} catch (RuntimeException ex) {
				if (ex.toString().contains("bouncy")) { // hah, stupid way of doing this, but what can i do if library
					// is throwing RuntimeException :(
					throw new PdfException("PDF is encrypted or password protected.\nTry to create an unencrypted pdf first using some other tool.");
				}
			}
		}
		return pdfDecoder;
	}

	/**
	 * Normalizing takes care of different widths / heights of pages by setting max width, height to all pages.a
	 */
	public static PdfFile getNormalizedPdf(IResourceHandler originalFile) throws DocumentException, IOException {
		PdfReader reader = null;
		Document doc = null;
		PdfWriter writer = null;
		OutputStream fout = null;
		try  {
			IResourceHandler tempFile = ResourceHandlerFactory.getTemporaryResource("pdfscissor");
			reader = PDFUtils.getReader(originalFile.toFile());
			int endPage = reader.getNumberOfPages();
			Rectangle maxBoundingBox = getMaxBoundingBox(reader, endPage);
			doc = new Document(maxBoundingBox, 0, 0, 0, 0);
			writer = PdfWriter.getInstance(doc, fout = tempFile.getContentOutputStream(false));
			doc.open();
			PdfContentByte cb = writer.getDirectContent();

			for (int i = 1; i <= endPage; i++) {
				PdfImportedPage page = writer.getImportedPage(reader, i);
				float Scale = 1f;
				cb.addTemplate(page, Scale, 0, 0, Scale, 0, 0);
				doc.newPage();
			}

			// put the information, like author name etc.
			HashMap<String, String> info = reader.getInfo();
			String keywords = info.get("Keywords");
			if (keywords == null) {
				keywords = "";
			}
			if (keywords.length() > 0 && !keywords.endsWith(" ")) {
				keywords += " ";
			}
			keywords += "Cropped by pdfscissors.com";
			info.put("Keywords", keywords);

			PdfFile pdfFile = new PdfFile(tempFile, originalFile, endPage);
			pdfFile.setPdfInfo(info);
			pdfFile.setPageCount(endPage);
			pdfFile.setNormalizedPdfWidth(Math.abs(maxBoundingBox.getWidth()));
			pdfFile.setNormalizedPdfHeight(Math.abs(maxBoundingBox.getHeight()));

			return pdfFile;

		} finally {
			if (doc != null) {
				doc.close();
			}

			if (writer != null) {
				writer.flush();
				writer.close();
			}

			if (reader != null) {
				reader.close();
			}

			IOUtils.closeQuietly(fout);
		}
	}

	private static com.itextpdf.text.Rectangle getMaxBoundingBox(PdfReader reader, int endPage) {
		com.itextpdf.text.Rectangle maxBoundingBox = new com.itextpdf.text.Rectangle(0, 0, 0, 0);
		for (int i = 1; i <= endPage; i++) {
			com.itextpdf.text.Rectangle boundingBox = reader.getPageSize(i);

			if (boundingBox.getWidth() > maxBoundingBox.getWidth())
				maxBoundingBox.setRight(boundingBox.getWidth());

			if (boundingBox.getHeight() > maxBoundingBox.getHeight())
				maxBoundingBox.setBottom(boundingBox.getHeight());
		}
		return maxBoundingBox;
	}

	public static void cropPdf(PdfFile pdfFile, File targetFile, PageRectsMap pageRectsMap, int viewWidth,
			int viewHeight, ProgressMonitor progressMonitor) throws IOException, DocumentException {

		IResourceHandler originalFile = pdfFile.getOriginalFile();
		HashMap<String, String> pdfInfo = pdfFile.getPdfInfo();

		PdfReader reader1 = PDFUtils.getReader(originalFile.toFile());
		PdfReader reader2 = null;

		float pdfWidth = pdfFile.getNormalizedPdfWidth();
		float pdfHeight = pdfFile.getNormalizedPdfHeight();

		LoggerFactory.getLogger(PdfCropper.class).log(Level.INFO, "Finding ratio : viewSize " + viewWidth + "x" + viewHeight + ", pdf size " + pdfWidth + "x" + pdfHeight);
		double widthRatio = pdfWidth / viewWidth;
		double heightRatio = pdfHeight / viewHeight;
		if (widthRatio != heightRatio) {
			System.err.println("WARNING>>> RATION NOT SAME ?! " + widthRatio + " " + heightRatio);
		}


		Document document = null;
		PdfCopy writer = null;
		PdfStamper stamper = null;
		IResourceHandler tempFile = null;
		OutputStream tempFileOut = null;

		// TODO handle bookmarks
		// List bookmarks = SimpleBookmark.getBookmark(reader);
		// if (bookmarks != null) {
		// if (pageOffset != 0) {
		// SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset,
		// null);
		// }
		// master.addAll(bookmarks);
		// }

		// open the original
		try {
			reader1.consolidateNamedDestinations();
			int originalPageCount = reader1.getNumberOfPages();
			document = new Document(reader1.getPageSizeWithRotation(1));
			tempFile = ResourceHandlerFactory.getTemporaryResource("tmp");
			writer = new PdfCopy(document, tempFileOut = tempFile.getContentOutputStream(false));
			document.open();

			PdfImportedPage page;

			int newPageCount = 0;
			for (int i = 0; i < originalPageCount;) {
				++i;
				ArrayList<java.awt.Rectangle> cropRects = pageRectsMap.getRects(i);
				int cropCellCount = 0;
				if (cropRects != null) {
					cropCellCount = cropRects.size();
				}

				if (cropCellCount == 0) {
					cropCellCount = 1; //we will assume there is one crop cell that covers the whole page
				}
				newPageCount += cropCellCount;
				for (int iCell = 0; iCell < cropCellCount; iCell++) {
					progressMonitor.setNote("Writing page " + ((i - 1) * cropCellCount + iCell) + " of " + newPageCount);
					progressMonitor.setProgress(i * 100 / originalPageCount);
					page = writer.getImportedPage(reader1, i);
					writer.addPage(page);
				}
			}

			document.close();
			document = null;

			reader2 = PDFUtils.getReader(tempFile.toFile());

			stamper = new PdfStamper(reader2, new FileOutputStream(targetFile));
			int pageCount = reader2.getNumberOfPages();
			newPageCount = 0;
			for (int iOriginalPage = 1; iOriginalPage <= originalPageCount; iOriginalPage++) {
				ArrayList<java.awt.Rectangle> cropRectsInIPDFCoords = pageRectsMap.getConvertedRectsForCropping(iOriginalPage, viewWidth, viewHeight, pdfWidth, pdfHeight);
				int cropCellCount = 0;
				if (cropRectsInIPDFCoords != null) {
					cropCellCount = cropRectsInIPDFCoords.size();
				}

				if (cropCellCount == 0) {
					newPageCount++; // we will still add one full page
					LoggerFactory.getLogger(PdfCropper.class).log(Level.INFO, "Cropping page " + newPageCount + " ... full page size");
				} else {
					for (int i = 0; i < cropCellCount;) {
						++i;
						newPageCount++;
						// http://stackoverflow.com/questions/4089757/how-do-i-resize-an-existing-pdf-with-coldfusion-itext
						progressMonitor.setNote("Cropping page " + newPageCount + " of " + pageCount);
						progressMonitor.setProgress(newPageCount * 100 / pageCount);
						if (cropRectsInIPDFCoords != null) {
							PdfDictionary pdfDictionary = reader2.getPageN(newPageCount);
							PdfArray cropCell = new PdfArray();
							java.awt.Rectangle awtRect = cropRectsInIPDFCoords.get(i - 1);
							LoggerFactory.getLogger(PdfCropper.class).log(Level.INFO, "Cropping page " + newPageCount + " with " + awtRect);
							cropCell.add(new PdfNumber(awtRect.x));// lower left x
							cropCell.add(new PdfNumber(awtRect.y));// lower left y
							cropCell.add(new PdfNumber(awtRect.x + awtRect.width)); // up right x
							cropCell.add(new PdfNumber(awtRect.y + awtRect.height));// up righty
							pdfDictionary.put(PdfName.CROPBOX, cropCell);
							pdfDictionary.put(PdfName.MEDIABOX, cropCell);
							pdfDictionary.put(PdfName.TRIMBOX, cropCell);
							pdfDictionary.put(PdfName.BLEEDBOX, cropCell);
						}
					}
				}
			}

			// put the information, like author name etc.
			stamper.setMoreInfo(pdfInfo);
		} finally {
			if (document != null) {
				document.close();
			}

			if (stamper != null) {
				stamper.close();
			}

			if (reader1 != null) {
				reader1.close();
			}

			if (reader2 != null) {
				reader2.close();
			}

			if(writer != null) {
				writer.flush();
				writer.close();
			}

			IOUtils.closeQuietly(tempFileOut);

			if (tempFile != null) {
				tempFile.delete();
			}
		}
	}

	public void cancel() {
		isCancel = true;
	}

}