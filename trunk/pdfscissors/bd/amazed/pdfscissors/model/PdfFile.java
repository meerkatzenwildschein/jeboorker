package bd.amazed.pdfscissors.model;

import java.io.File;
import java.util.HashMap;

/**
 * Contains data related to the pdf file. Use normalized pdf file for actual cropping.
 *
 */
public class PdfFile {

	// we basically read from a temp normalized file.
	// Original file reference is just stored.
	private File normalizedFile;
	private File originalFile;
	private HashMap<String, String> pdfInfo;
	private int pageCount;
	private float normalizedPdfWidth;
	private float normalizedPdfHeight;

	private PdfFile() {

	}

	public PdfFile(File normalizedFile, File origiFile, int pageCount) {
		setNormalizedFile(normalizedFile);
		setOriginalFile(origiFile);
		setPageCount(pageCount);
	}

	public File getNormalizedFile() {
		return normalizedFile;
	}

	public void setNormalizedFile(File normalizedFile) {
		if (normalizedFile == null) {
			throw new IllegalArgumentException("Cannot set null file to model");
		}
		this.normalizedFile = normalizedFile;
	}

	public File getOriginalFile() {
		return originalFile;
	}

	public void setOriginalFile(File originalFile) {
		if (originalFile == null) {
			throw new IllegalArgumentException("Cannot set null originalFile to model");
		}
		this.originalFile = originalFile;
	}


	public HashMap<String, String> getPdfInfo() {
		if (pdfInfo == null) {
			return new HashMap<String, String>();
		} else {
			return pdfInfo;
		}
	}

	public void setPdfInfo(HashMap<String, String> pdfInfo) {
		this.pdfInfo = pdfInfo;
	}



	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public float getNormalizedPdfWidth() {
		return normalizedPdfWidth;
	}

	public void setNormalizedPdfWidth(float normalizedPdfWidth) {
		this.normalizedPdfWidth = normalizedPdfWidth;
	}

	public float getNormalizedPdfHeight() {
		return normalizedPdfHeight;
	}

	public void setNormalizedPdfHeight(float normalizedPdfHeight) {
		this.normalizedPdfHeight = normalizedPdfHeight;
	}

	public static PdfFile NullPdf() {
		return new PdfFile();
	}

}
