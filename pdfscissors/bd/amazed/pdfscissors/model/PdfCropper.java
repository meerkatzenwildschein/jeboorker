/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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

	private static final String TEMP_PREFIX_PDFSCISSOR = "~pdfscissor_";
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
		// TODO validate page number
		int endPage = pageGroup.getLastPage();
		PdfDecoderMod pdfDecoder = getPdfDecoder();
		BufferedImage lastPage = pdfDecoder.getPageAsImage(endPage);
		pdfDecoder.closePdfFile();

		if (lastPage != null) {
			listener.propertyChange(new PropertyChangeEvent(this, "message", null, "Stacking " + pageGroup));
			lastPage = new BufferedImage(lastPage.getWidth(), lastPage.getHeight(), BufferedImage.TYPE_INT_ARGB);

			Graphics2D g2d = (Graphics2D) lastPage.getGraphics();

			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, lastPage.getWidth(), lastPage.getHeight());

			float alpha = 0.5f;
			int type = AlphaComposite.SRC_OVER;
			AlphaComposite composite = AlphaComposite.getInstance(type, alpha);
			g2d.setComposite(composite);
			int argb;
			int r;
			int g;
			int b;
			int pageCount = pageGroup.getPageCount();
			for (int iPageInGroup = pageCount - 1; iPageInGroup >= 0 && !isCancel; iPageInGroup--) {
				int i = pageGroup.getPageNumberAt(iPageInGroup);
				int percentageDone = (100 * (pageCount - iPageInGroup)) / pageCount;
//				System.out.println("Stacking page: " + i + ", completed " + percentageDone);
				listener.propertyChange(new PropertyChangeEvent(this, "progress", null, percentageDone));
				BufferedImage pageImage = getPdfDecoder().getPageAsImage(i);
				if (pageImage != null) {
//					for (int x = 0; x < pageImage.getWidth(); x++) {
//						for (int y = 0; y < pageImage.getHeight(); y++) {
//							argb = pageImage.getRGB(x, y);
//							r = (argb >> 16) & 0x000000FF;
//							g = (argb >> 8) & 0x000000FF;
//							b = (argb) & 0x000000FF;
//							if ((r == g && g == b && b == r && r > 150)) {
//								pageImage.setRGB(x, y, 0x00FF0000);// transparent
//							}
//						}
//					}
					g2d.drawImage(pageImage, 0, 0, null);
				}
			}
		}

		return lastPage;
	}

	public void close() {
		if (pdfDecoder != null) {
			pdfDecoder.closePdfFile();
		}
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
			IResourceHandler tempFile = ResourceHandlerFactory.getTemporaryResource(".pdfscissor");
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

		PdfReader reader = PDFUtils.getReader(originalFile.toFile());

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
		File tempFile = null;

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

			reader.consolidateNamedDestinations();
			int originalPageCount = reader.getNumberOfPages();
			document = new Document(reader.getPageSizeWithRotation(1));
			tempFile = TempFileManager.getInstance().createTempFile(TEMP_PREFIX_PDFSCISSOR + System.currentTimeMillis(), null, true);
			writer = new PdfCopy(document, new FileOutputStream(tempFile));
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
					cropCellCount = 1;//we will assume there is one crop cell that covers the whole page
				}
				newPageCount += cropCellCount;
				for (int iCell = 0; iCell < cropCellCount; iCell++) {
					progressMonitor.setNote("Writing page " + ((i - 1) * cropCellCount + iCell) + " of " + newPageCount);
					progressMonitor.setProgress(i * 100 / originalPageCount);

//					java.awt.Rectangle awtRect = cropRectsInIPDFCoords.get(iCell);
					// Rectangle itextRect = new Rectangle(awtRect.x , awtRect.y +
					// awtRect.height, awtRect.x + awtRect.width, awtRect.y);
					// Rectangle itextRect = new Rectangle(100,50);
					// writer.setBoxSize("crop", itextRect);
					// writer.setBoxSize("trim", itextRect);
					// writer.setBoxSize("art", itextRect);
					// writer.setBoxSize("bleed", itextRect);
					page = writer.getImportedPage(reader, i);
					// writer.setPageSize(itextRect);
					writer.addPage(page);
				}
			}
			// PRAcroForm form = reader.getAcroForm();
			// if (form != null) {
			// writer.copyAcroForm(reader);
			// }
			// f++;
			// if (!master.isEmpty()) {
			// writer.setOutlines(master);
			// }
			document.close();
			document = null;
			reader = new PdfReader(tempFile.getAbsolutePath());

			stamper = new PdfStamper(reader, new FileOutputStream(targetFile));
			int pageCount = reader.getNumberOfPages();
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
							PdfDictionary pdfDictionary = reader.getPageN(newPageCount);
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

			if (tempFile != null) {
				tempFile.delete();
			}

			if (reader != null) {
				reader.close();
			}
		}
	}

	public void cancel() {
		isCancel = true;
	}


	// helper code: //http://itext-general.2136553.n4.nabble.com/How-to-Shrink-Content-and-Add-Margins-td2167577.html
	// public static void solution2() throws IOException, DocumentException {
	// PdfReader reader = new PdfReader("sample.pdf");
	// int n = reader.getNumberOfPages();
	// PdfDictionary pageDict;
	// ArrayList old_mediabox;
	// PdfArray new_mediabox;
	// PdfNumber value;
	// PdfStamper stamper = new PdfStamper(reader, new FileOutputStream("solution2.pdf"));
	// BaseFont font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
	// PdfContentByte directcontent;
	// for (int i = 1; i <= n; i++) {
	// pageDict = reader.getPageN(i);
	// new_mediabox = new PdfArray();
	// old_mediabox = pageDict.getAsArray(PdfName.MEDIABOX).getArrayList();
	// value = (PdfNumber)old_mediabox.get(0);
	// new_mediabox.add(new PdfNumber(value.floatValue() - 36));
	// value = (PdfNumber)old_mediabox.get(1);
	// new_mediabox.add(new PdfNumber(value.floatValue() - 36));
	// value = (PdfNumber)old_mediabox.get(2);
	// new_mediabox.add(new PdfNumber(value.floatValue() + 36));
	// value = (PdfNumber)old_mediabox.get(3);
	// new_mediabox.add(new PdfNumber(value.floatValue() + 36));
	// pageDict.put(PdfName.MEDIABOX, new_mediabox);
	// directcontent = stamper.getOverContent(i);
	// directcontent.beginText();
	// directcontent.setFontAndSize(font, 12);
	// directcontent.showTextAligned(Element.ALIGN_LEFT, "TEST", 0, -18, 0);
	// directcontent.endText();
	// }
	// stamper.close();
	// }

}
