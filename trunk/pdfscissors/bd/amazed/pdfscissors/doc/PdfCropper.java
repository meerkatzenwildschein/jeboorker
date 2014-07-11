package bd.amazed.pdfscissors.doc;

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

import bd.amazed.pdfscissors.model.PageRectsMap;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 *
 * @author Gagan
 */
class PdfCropper extends DocumentCropper {

	public PdfCropper(IResourceHandler file) {
		super(file);
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

	@Override
	public void crop(DocumentInfo pdfFile, File targetFile, PageRectsMap pageRectsMap, int viewWidth,
			int viewHeight, ProgressMonitor progressMonitor) throws IOException, DocumentException {
		IResourceHandler originalFile = pdfFile.getOriginalFile();
		HashMap<String, String> pdfInfo = pdfFile.getDocInfo();

		PdfReader reader1 = PDFUtils.getReader(originalFile.toFile());
		PdfReader reader2 = null;

		float normalizedPdfWidth = pdfFile.getNormalizedWidth();
		float normalizedPdfHeight = pdfFile.getNormalizedHeight();

		LoggerFactory.getLogger(PdfCropper.class).log(Level.INFO, "Finding ratio : viewSize " + viewWidth + "x" + viewHeight + ", pdf size " + normalizedPdfWidth + "x" + normalizedPdfHeight);
		double widthRatio = normalizedPdfWidth / viewWidth;
		double heightRatio = normalizedPdfHeight / viewHeight;
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
				normalizedPdfHeight = (int) reader1.getPageSize(iOriginalPage).getHeight();
				ArrayList<java.awt.Rectangle> cropRectsInIPDFCoords = pageRectsMap.getConvertedRectsForCropping(iOriginalPage, viewWidth, viewHeight, normalizedPdfWidth, normalizedPdfHeight);
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
							java.awt.Rectangle cropRect = cropRectsInIPDFCoords.get(i - 1);
							applyCropToPage(reader2, newPageCount, cropRect);
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

	private void applyCropToPage(PdfReader reader, int page, java.awt.Rectangle cropRect) {
		PdfDictionary pdfDictionary = reader.getPageN(page);
		PdfArray cropCell = new PdfArray();
		LoggerFactory.getLogger(PdfCropper.class).log(Level.INFO, "Cropping page " + page + " with " + cropRect);
		cropCell.add(new PdfNumber(cropRect.x));// lower left x
		cropCell.add(new PdfNumber(cropRect.y));// lower left y
		cropCell.add(new PdfNumber(cropRect.x + cropRect.width)); // up right x
		cropCell.add(new PdfNumber(cropRect.y + cropRect.height));// up righty
		pdfDictionary.put(PdfName.CROPBOX, cropCell);
		pdfDictionary.put(PdfName.MEDIABOX, cropCell);
		pdfDictionary.put(PdfName.TRIMBOX, cropCell);
		pdfDictionary.put(PdfName.BLEEDBOX, cropCell);
	}

	@Override
	public DocumentInfo getDocumentInfo() throws DocumentException, IOException {
		PdfReader reader = null;
		Document doc = null;
		try  {
			reader = PDFUtils.getReader(mainFile.toFile());
			int endPage = reader.getNumberOfPages();
			Rectangle maxBoundingBox = getMaxBoundingBox(reader, endPage);

			// put the information, like author name etc.
			HashMap<String, String> info = reader.getInfo();
			DocumentInfo docFile = new DocumentInfo(mainFile, mainFile, endPage);
			docFile.setDocInfo(info);
			docFile.setPageCount(endPage);
			docFile.setNormalizedWidth(Math.abs(maxBoundingBox.getWidth()));
			docFile.setNormalizedHeight(Math.abs(maxBoundingBox.getHeight()));

			return docFile;
		} finally {
			if (doc != null) {
				doc.close();
			}

			if (reader != null) {
				reader.close();
			}
		}
	}

}
