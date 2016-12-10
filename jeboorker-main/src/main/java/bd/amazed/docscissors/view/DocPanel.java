package bd.amazed.docscissors.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;

import bd.amazed.docscissors.doc.DocumentInfo;
import bd.amazed.docscissors.doc.JDocumentDecoderPanel;
import bd.amazed.docscissors.model.ModelListener;
import bd.amazed.docscissors.model.PageGroup;
import bd.amazed.docscissors.model.RectChangeListener;

/**
 *
 * @author Gagan
 */
public class DocPanel extends JDocumentDecoderPanel implements ModelListener, RectChangeListener, UIHandlerListener {

	protected UIHandler uiHandler;

	public DocPanel(UIHandler uiHandler) {
		super();
		this.uiHandler = uiHandler;
		MouseHandler handler = new MouseHandler();
		addMouseListener(handler);
		addMouseMotionListener(handler);
	}

	private void debug(String string) {
		LoggerFactory.getLogger(DocPanel.class).log(Level.INFO, string);
	}

	@Override
	public void paintComponent(Graphics g) {
		if (uiHandler.isShowMergedMode()) {
			Image image = getImage();
			if (image != null) {
				g.drawImage(image, 0, 0, this);
			} else {
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.black);
				String text = "No stacked view. View individual pages using the bottom toolbar.";
				int textWidth = g.getFontMetrics().stringWidth(text);
				int textHeight = g.getFontMetrics().getHeight();
				g.setColor(Color.black);
				g.drawString(text, (getWidth() - textWidth)/2, (getHeight() - textHeight)/2);
			}
		} else {
			super.paintComponent(g);
		}

		Rectangle clipRect = g.getClipBounds();

		Iterator<Rect> iter = uiHandler.getRectIterator();
		while (iter.hasNext()) {
			(iter.next()).draw(g, clipRect);
		}
		// whole page area
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
	}

	private Image getImage() {
		Image image = null;
		if(uiHandler.getCurrentPageGroup() != null)
			image = uiHandler.getCurrentPageGroup().getStackImage();
		return image;
	}

	private void updateSize() {
		Image image = getImage();
		if (image != null) {
			int width = image.getWidth(this);
			int height = image.getHeight(this);
			debug("Setting pdf size : " + width + "x" + height);
			setPreferredSize(new Dimension(width, height));
			setSize(new Dimension(width, height));
		}
		invalidate();
		repaint();
	}

	@Override
	public void newDocLoaded(DocumentInfo pdfFile) {
		try {
			super.openPdfFile(pdfFile);
			super.decodePage(uiHandler.getPage());
			invalidate();
		} catch (Exception e) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Loading pdf " + pdfFile + " failed", e);
			e.printStackTrace();
		}
	}

	@Override
	public void docLoadFailed(IResourceHandler failedFile, Throwable cause) {
		// nothing to do
	}

	@Override
	public void zoomChanged(double oldZoomFactor, double newZoomFactor) {
		updateSize();
	}

	@Override
	public void clipboardCopy(boolean isCut, Rect onClipboard) {

	}

	@Override
	public void clipboardPaste(boolean isCut, Rect onClipboard) {
		if (onClipboard != null) {
			try {
				Rect cloned = (Rect) onClipboard.clone();
				cloned.translate(5, 5, getWidth(), getHeight()); // a little to right bottom, so that user can see there
				// is a new one on top
				if (isCut) {
					uiHandler.delete(onClipboard);
				}
				uiHandler.addRect(cloned);
				uiHandler.setSelectedRect(cloned);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			} // we dont modify the old
		}
	}

	@Override
	public void rectUpdated(Rect rect, Rectangle repaintArea) {
		if (repaintArea != null) {
			repaint(repaintArea);
		} else {
			repaint();
		}
	}

	protected Rect getRectAt(Point pt) {
		ArrayList<Rect> allRects = uiHandler.getAllRects();
		for (int i = allRects.size() - 1; i >= 0; i--) {
			Rect r = (Rect) allRects.get(i);
			if (r.inside(pt))
				return r;
		}
		return null;
	}

	public void updateCursor() {
		if (uiHandler.getEditingMode() == UIHandler.EDIT_MODE_DRAW) {
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		} else {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	@Override
	public void editingModeChanged(int newMode) {
		updateCursor();
		repaint();
	}

	@Override
	public void pageChanged(int index) {

	}

	@Override
	public void pageGroupChanged(List<PageGroup> pageGroups) {

	}

	@Override
	public void pageGroupSelected(PageGroup pageGroup) {
		repaint();
	}

	@Override
	public void rectsStateChanged() {

	}

	protected class MouseHandler extends MouseAdapter implements MouseMotionListener {
		private Point dragAnchor; // variables using to track state during drag operations
		private int dragStatus;
		private static final int DRAG_NONE = 0;
		private static final int DRAG_CREATE = 1;
		private static final int DRAG_RESIZE = 2;
		private static final int DRAG_MOVE = 3;

		/**
		 * When the mouse is pressed we need to figure out what action to take. If the tool mode is arrow, the click
		 * might be a select, move or reisze. If the tool mode is one of the rects, the click initiates creation of a
		 * new rect.
		 */
		public void mousePressed(MouseEvent event) {
			Rect clicked = null;
			Point curPt = event.getPoint();

			if (uiHandler.getEditingMode() == UIHandler.EDIT_MODE_SELECT) {
				// first, determine if click was on resize knob of selected rect
				if (uiHandler.getSelectedRect() != null && (dragAnchor = uiHandler.getSelectedRect().getAnchorForResize(curPt)) != null) {
					dragStatus = DRAG_RESIZE; // drag will resize this rect
				} else if ((clicked = getRectAt(curPt)) != null) { // if not check if any rect was clicked
					uiHandler.setSelectedRect(clicked);
					dragStatus = DRAG_MOVE; // drag will move this rect
					dragAnchor = curPt;
				} else { // else this was a click in empty area, deselect
					// selected rect,
					uiHandler.setSelectedRect(null);
					dragStatus = DRAG_NONE; // drag does nothing in this case
				}
			} else {
				Rect newRect = new Rect(curPt, uiHandler); // create rect here
				newRect.addListener(DocPanel.this);
				uiHandler.addRect(newRect);
				uiHandler.setSelectedRect(newRect);
				dragStatus = DRAG_CREATE; // drag will create (resize) this rect
				dragAnchor = curPt;
			}
		}

		/**
		 * As the mouse is dragged, our listener will receive periodic updates as mouseDragged events. When we get an
		 * update position, we update the move/resize event that is in progress.
		 */
		public void mouseDragged(MouseEvent event) {
			Point pointer = event.getPoint();
			switch (dragStatus) {
			case DRAG_MOVE:
				uiHandler.getSelectedRect().translate(pointer.x - dragAnchor.x, pointer.y - dragAnchor.y, getWidth(), getHeight());
				uiHandler.notifyRectsStateChanged();
				dragAnchor = pointer; // update for next dragged event
				break;
			case DRAG_CREATE:
			case DRAG_RESIZE:
				uiHandler.getSelectedRect().resize(dragAnchor, pointer, getWidth(), getHeight());
				uiHandler.notifyRectsStateChanged();
				break;
			}
		}

		@Override
		public void mouseMoved(MouseEvent event) {
			Point pointer = event.getPoint();
			if (uiHandler.getEditingMode() == UIHandler.EDIT_MODE_SELECT) { // select mode
				if (getRectAt(pointer) != null) { // move
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else if (uiHandler.getSelectedRect() != null) { // resize
					int whichCornerbox = uiHandler.getSelectedRect().getCornerboxContainingPoint(pointer);
					if (whichCornerbox != Rect.CORNER_NONE) {
						switch (whichCornerbox) {
						case Rect.CORNER_TOP_LEFT:
							setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
							break;
						case Rect.CORNER_TOP_RIGHT:
							setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
							break;
						case Rect.CORNER_BOTTOM_LEFT:
							setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
							break;
						case Rect.CORNER_BUTTOM_RIGHT:
							setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
							break;
						}
					} else {
						setCursor(Cursor.getDefaultCursor());
					}
				} else {// normal
					setCursor(Cursor.getDefaultCursor());
				}
			} else if (uiHandler.getEditingMode() == UIHandler.EDIT_MODE_DRAW) {
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			} else {
				setCursor(Cursor.getDefaultCursor());
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			Rect selectedRect = uiHandler.getSelectedRect();
			if (selectedRect != null && selectedRect.bounds != null && (selectedRect.bounds.getWidth() <= 0 || selectedRect.bounds.getHeight() <= 10)) { // TOO
				// small, we dont add those
				uiHandler.deleteSelected();
			}
			if (uiHandler.getEditingMode() == UIHandler.EDIT_MODE_DRAW) {
				uiHandler.setEditingMode(UIHandler.EDIT_MODE_SELECT);
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			super.mouseEntered(e);
			updateCursor();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			super.mouseExited(e);
			setCursor(Cursor.getDefaultCursor());
		}

	}

}