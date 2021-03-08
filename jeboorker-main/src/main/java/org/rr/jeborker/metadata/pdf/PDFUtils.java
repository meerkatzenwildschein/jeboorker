package org.rr.jeborker.metadata.pdf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.converter.ConverterUtils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.io.FileChannelRandomAccessSource;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;

public class PDFUtils {

	/**
	 * Create a {@link PdfReader} for the given file. A {@link RandomAccessFile} will be used so the result {@link PdfReader} did not load the
	 * whole pdf at once.
	 * 
	 * @param pdfFile The pdf file to be loaded with the {@link PdfReader}.
	 * @return The desired {@link PdfReader}
	 * @throws IOException
	 */
	public static PdfReader getReader(File pdfFile) throws IOException {
		if (isLargePdfFile(pdfFile)) {
			return new InputStreamPDFReaderDelegate(new BufferedInputStream(new FileInputStream(pdfFile)), pdfFile);
		}

		// more performant but needs memory in one piece 
		RandomAccessFile file = new RandomAccessFile(pdfFile, "r");
		FileChannel fileChannelI = file.getChannel();
		FileChannelRandomAccessSource fileChannelRandomAccessSource = new FileChannelRandomAccessSource(fileChannelI);
		RandomAccessFileOrArray rafPdfIn = new RandomAccessFileOrArray(fileChannelRandomAccessSource);
		return new RandomAccessFilePDFReaderDelegate(rafPdfIn, file, fileChannelI);
	}

	private static boolean isLargePdfFile(File pdfFile) {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.SYSTEM_STORE);
		int thresholdInMb = preferenceStore.getGenericEntryAsNumber("pdf.random_access.threshold.mb", Integer.valueOf(100)).intValue();
		long fileSizeInMb = pdfFile.length() / 1024 / 1024;
		boolean isLargePdfFile = fileSizeInMb > thresholdInMb;
		return isLargePdfFile;
	}

	private static class RandomAccessFilePDFReaderDelegate extends PdfReader {

		private FileChannel fileChannelI;

		private RandomAccessFile file;

		RandomAccessFilePDFReaderDelegate(RandomAccessFileOrArray rafPdfIn, RandomAccessFile file, FileChannel fileChannelI)
				throws IOException {
			super(rafPdfIn, null);
			this.file = file;
			this.fileChannelI = fileChannelI;
		}

		RandomAccessFilePDFReaderDelegate(InputStream in) throws IOException {
			super(in, null);
		}

		@Override
		public void close() {
			super.close();
			try {
				if (file != null) {
					file.close();
				}
			} catch (IOException e) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to close File " + file, e);
			}

			if (fileChannelI != null) {
				try {
					fileChannelI.close();
					fileChannelI = null;
				} catch (IOException e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to close File " + file, e);
				}
			}
		}
	}

	private static class InputStreamPDFReaderDelegate extends PdfReader {

		private BufferedInputStream in;

		private File file;

		InputStreamPDFReaderDelegate(BufferedInputStream in, File file) throws IOException {
			super(in, null);
			this.in = in;
			this.file = file;
		}

		@Override
		public void close() {
			super.close();
			try {
				if (in != null) {
					in.close();
					in = null;
				}
			} catch (IOException e) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to close File " + file, e);
			}
		}
	}

	/**
	 * Merge multiple pdf into one pdf and transfers the metadata from the first document in <code>inputPdfDocuments</code> to the new pdf.
	 * 
	 * @param inputPdfDocuments of pdf input stream
	 * @param outputStream output file output stream
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static void merge(List<IResourceHandler> inputPdfDocuments, IResourceHandler outputPdfDocument)
			throws DocumentException, IOException {
		Document document = new Document();
		try (OutputStream outputStream = outputPdfDocument.getContentOutputStream(false)) {
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			document.open();
			PdfContentByte cb = writer.getDirectContent();

			for (IResourceHandler in : inputPdfDocuments) {
				PdfReader reader = getReader(in.toFile());
				for (int i = 1; i <= reader.getNumberOfPages(); i++) {
					document.setPageSize(reader.getPageSize(i));
					document.newPage();
					// import the page from source pdf
					PdfImportedPage page = writer.getImportedPage(reader, i);
					// add the page to the destination pdf
					cb.addTemplate(page, 0, 0);
				}
			}
			outputStream.flush();
			document.close();
		}

		if (!inputPdfDocuments.isEmpty()) {
			ConverterUtils.transferMetadata(inputPdfDocuments.get(0), outputPdfDocument);
		}
	}
}
