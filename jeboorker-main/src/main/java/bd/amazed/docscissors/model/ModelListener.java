package bd.amazed.docscissors.model;

import java.util.List;

import org.rr.commons.mufs.IResourceHandler;

import bd.amazed.docscissors.doc.DocumentInfo;
import bd.amazed.docscissors.view.Rect;

public interface ModelListener {

	public void newDocLoaded(DocumentInfo docFile);

	public void docLoadFailed(IResourceHandler failedFile, Throwable cause);

	public void zoomChanged(double oldZoomFactor, double newZoomFactor);

	public void clipboardCopy(boolean isCut, Rect onClipboard);

	public void clipboardPaste(boolean isCut, Rect onClipboard);

	public void pageGroupChanged(List<PageGroup> pageGroups);
}
