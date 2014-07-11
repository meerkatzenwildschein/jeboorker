package bd.amazed.pdfscissors.model;

import java.util.Vector;

import org.rr.commons.mufs.IResourceHandler;

import bd.amazed.pdfscissors.doc.DocumentInfo;
import bd.amazed.pdfscissors.view.Rect;

public interface ModelListener {

	public void newDocLoaded(DocumentInfo docFile);

	public void docLoadFailed(IResourceHandler failedFile, Throwable cause);

	public void zoomChanged(double oldZoomFactor, double newZoomFactor);

	public void clipboardCopy(boolean isCut, Rect onClipboard);

	public void clipboardPaste(boolean isCut, Rect onClipboard);

	public void pageGroupChanged(Vector<PageGroup> pageGroups);
}
