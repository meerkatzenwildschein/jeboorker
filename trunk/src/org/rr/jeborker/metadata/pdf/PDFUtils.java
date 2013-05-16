package org.rr.jeborker.metadata.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.apache.commons.io.IOUtils;

import com.itextpdf.text.io.FileChannelRandomAccessSource;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;

public class PDFUtils {

	/**
	 * Create a {@link PdfReader} for the given file. A {@link RandomAccessFile} will be used so the
	 * result {@link PdfReader} did not load the whole pdf at once.
	 * @param pdfFile The pdf file to be loaded with the {@link PdfReader}.
	 * @return The desired {@link PdfReader}
	 * @throws IOException
	 */
	public static PdfReader getReader(File pdfFile) throws IOException {
		FileInputStream fileInputStreamI = new FileInputStream(pdfFile);
		FileChannel fileChannelI = fileInputStreamI.getChannel();
		FileChannelRandomAccessSource fileChannelRandomAccessSource = new FileChannelRandomAccessSource(fileChannelI);
		RandomAccessFileOrArray rafPdfIn = new RandomAccessFileOrArray(fileChannelRandomAccessSource); 
		PdfReader pdfReaderI = new PDFReaderDelegate(rafPdfIn, fileInputStreamI, fileChannelI);
		return pdfReaderI;
	}
	
	private static class PDFReaderDelegate extends PdfReader {
		
		private FileInputStream fileInputStreamI;
		
		private FileChannel fileChannelI;

		PDFReaderDelegate(RandomAccessFileOrArray rafPdfIn, FileInputStream fileInputStreamI, FileChannel fileChannelI) throws IOException {
			super(rafPdfIn, null);
			this.fileInputStreamI = fileInputStreamI;
			this.fileChannelI = fileChannelI;
		}

		@Override
		public void close() {
			super.close();
			this.dispose();
		}
		
		private void dispose() {
			if(fileInputStreamI != null) {
				IOUtils.closeQuietly(fileInputStreamI);
				this.fileInputStreamI = null;
			}
			if(fileChannelI != null) {
				try {
					fileChannelI.close();
					this.fileChannelI = null;
				} catch (IOException e) {}
			}
		}		
		
	}
}
