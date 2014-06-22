package org.rr.jeborker.metadata.pdf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;

import com.itextpdf.text.io.FileChannelRandomAccessSource;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;

public class PDFUtils {

	/**
	 * Create a {@link PdfReader} for the given file. A {@link RandomAccessFile} will be used so the result {@link PdfReader} did not load the whole pdf at
	 * once.
	 * 
	 * @param pdfFile
	 *            The pdf file to be loaded with the {@link PdfReader}.
	 * @return The desired {@link PdfReader}
	 * @throws IOException
	 */
	public static PdfReader getReader(File pdfFile) throws IOException {
		RandomAccessFile file = new RandomAccessFile(pdfFile, "r");
		FileChannel fileChannelI = file.getChannel();
		FileChannelRandomAccessSource fileChannelRandomAccessSource = new FileChannelRandomAccessSource(fileChannelI);
		RandomAccessFileOrArray rafPdfIn = new RandomAccessFileOrArray(fileChannelRandomAccessSource);
		return new PDFReaderDelegate(rafPdfIn, file, fileChannelI);
	}

	private static class PDFReaderDelegate extends PdfReader {

		private FileChannel fileChannelI;

		private RandomAccessFile file;

		PDFReaderDelegate(RandomAccessFileOrArray rafPdfIn, RandomAccessFile file, FileChannel fileChannelI) throws IOException {
			super(rafPdfIn, null);
			this.file = file;
			this.fileChannelI = fileChannelI;
		}

		@Override
		public void close() {
			super.close();
			try {
				this.file.close();
			} catch (IOException e) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to close File " + file, e);
			}

			if (fileChannelI != null) {
				try {
					fileChannelI.close();
					this.fileChannelI = null;
				} catch (IOException e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to close File " + file, e);
				}
			}
		}

	}
}
