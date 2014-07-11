package bd.amazed.pdfscissors.model;

import java.util.Vector;

import org.rr.commons.mufs.IResourceHandler;

import bd.amazed.pdfscissors.doc.DocumentInfo;
import bd.amazed.pdfscissors.view.Rect;

public class Model {

	public static final String PROPERTY_LAST_FILE = "lastFile";

	public static final String PROPERTY_LAST_STACK_TYPE= "lastStackType";

	private static Model instance;

	private Vector<ModelListener> modelListeners;

	private DocumentInfo currentDoc = DocumentInfo.NullDoc();

	private double zoomFactor;

	private Rect clipboardRect;

	private boolean isClipboardCut;

	private Vector<PageGroup> pageGroups;

	private Model() {
		modelListeners = new java.util.Vector<ModelListener>();
		reset();
	}


	private void reset() {
		zoomFactor = 1;
		pageGroups = new Vector<PageGroup>();
	}



	public static Model getInstance() {
		if (instance == null) {
			instance = new Model();
		}
		return instance;
	}

	public void addListener(ModelListener listener) {
		if (!modelListeners.contains(listener)) {
			modelListeners.add(listener);
		}
	}

	/**
	 *
	 * @return true if listener has been removed, false if not found
	 */
	public boolean removeListener(ModelListener listener) {
		return modelListeners.remove(listener);
	}

	/**
	 *
	 * @param file file, must not be null. This should be a normalized temp file
	 * @param originalFile original file
	 * @param previewImage previewImage, must not be null
	 */
	public void setDoc(DocumentInfo docFile, Vector<PageGroup> pageGroups) {
		if (currentDoc == null) {
			throw new IllegalArgumentException("Cannot set null doc file");
		}
		this.currentDoc = docFile;
		reset(); //on new pdf load reset everything
		fireNewDoc(docFile);
		setPageGroups(pageGroups);
	}


	public DocumentInfo getDoc() {
		return this.currentDoc;
	}

	/**
	 * Notify model that some pdf loading has failed.
	 *
	 * @param file
	 */
	public void setDocLoadFailed(IResourceHandler file, Throwable err) {
		fireSetDocFailed(file, err);
	}

	private void setPageGroups(Vector<PageGroup> pageGroups) {
		this.pageGroups = pageGroups;
		firePageGroupChanged(pageGroups);
	}

	public void copyToClipboard(boolean isCut, Rect rect) {
		if (rect != null) {
			this.clipboardRect = rect;
			this.isClipboardCut = isCut;
			fireClipboardCopyEvent(clipboardRect, isClipboardCut);
		}
	}

	public void pasteFromClipboard() {
		if (clipboardRect != null) {
			fireClipboardPasteEvent(clipboardRect, isClipboardCut);
			if (isClipboardCut) {
				isClipboardCut = false; // clear clipboard
				clipboardRect = null;
			}
		}
	}

	public Rect getClipboardRect() {
		return clipboardRect;
	}

	public Vector<PageGroup> getPageGroups() {
		return pageGroups;
	}

	public PageRectsMap getPageRectsMap() {
		PageGroup pageGroup = null;
		Vector<Integer> pages = null;
		PageRectsMap pageRectsMap = new PageRectsMap();
		for (int i = 0; i < pageGroups.size(); i++) {
			pageGroup = pageGroups.elementAt(i);
			pages = pageGroup.getPages();
			for (int page = 0; page < pages.size(); page++) {
				pageRectsMap.putRects(pages.get(page), pageGroup.getRectangles());
			}
		}
		return pageRectsMap;
	}


	public double getZoomFactor() {
		return zoomFactor;
	}

	protected void fireNewDoc(DocumentInfo docFile) {
		for (ModelListener listener : modelListeners) {
			listener.newDocLoaded(docFile);
		}
	}

	protected void fireSetDocFailed(IResourceHandler failedFile, Throwable cause) {
		for (ModelListener listener : modelListeners) {
			listener.docLoadFailed(failedFile, cause);
		}
	}

	protected void firePageGroupChanged(Vector<PageGroup> pageGroups) {
		for (ModelListener listener : modelListeners) {
			listener.pageGroupChanged(pageGroups);
		}
	}


	protected void fireZoomChanged(double oldZoomFactor, double newZoomFactor) {
		for (ModelListener listener : modelListeners) {
			listener.zoomChanged(oldZoomFactor, newZoomFactor);
		}
	}

	protected void fireClipboardCopyEvent(Rect onClipboard, boolean isCut) {
		for (ModelListener listener : modelListeners) {
			listener.clipboardCopy(isCut, onClipboard);
		}
	}

	protected void fireClipboardPasteEvent(Rect onClipboard, boolean isCut) {
		for (ModelListener listener : modelListeners) {
			listener.clipboardPaste(isCut, onClipboard);
		}
	}


}
