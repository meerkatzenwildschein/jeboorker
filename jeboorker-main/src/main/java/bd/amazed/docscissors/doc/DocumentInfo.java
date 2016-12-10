package bd.amazed.docscissors.doc;

import java.util.HashMap;

import org.rr.commons.mufs.IResourceHandler;

/**
 * Contains data related to the pdf file. Use normalized pdf file for actual cropping.
 *
 */
public class DocumentInfo {

	// we basically read from a temp normalized file.
	// Original file reference is just stored.
	private IResourceHandler originalFile;
	private HashMap<String, String> pdfInfo;
	private int pageCount;
	private float normalizedPdfWidth;
	private float normalizedPdfHeight;

	private DocumentInfo() {
		super();
	}

	public DocumentInfo(IResourceHandler normalizedFile, IResourceHandler origiFile, int pageCount) {
		setOriginalFile(origiFile);
		setPageCount(pageCount);
	}

	public IResourceHandler getOriginalFile() {
		return originalFile;
	}

	public void setOriginalFile(IResourceHandler originalFile) {
		if (originalFile == null) {
			throw new IllegalArgumentException("Cannot set null originalFile to model");
		}
		this.originalFile = originalFile;
	}

	public HashMap<String, String> getDocInfo() {
		if (pdfInfo == null) {
			return new HashMap<String, String>();
		} else {
			return pdfInfo;
		}
	}

	public void setDocInfo(HashMap pdfInfo) {
		this.pdfInfo = pdfInfo;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public float getNormalizedWidth() {
		return normalizedPdfWidth;
	}

	public void setNormalizedWidth(float normalizedPdfWidth) {
		this.normalizedPdfWidth = normalizedPdfWidth;
	}

	public float getNormalizedHeight() {
		return normalizedPdfHeight;
	}

	public void setNormalizedHeight(float normalizedPdfHeight) {
		this.normalizedPdfHeight = normalizedPdfHeight;
	}

	public static DocumentInfo NullDoc() {
		return new DocumentInfo();
	}

}
