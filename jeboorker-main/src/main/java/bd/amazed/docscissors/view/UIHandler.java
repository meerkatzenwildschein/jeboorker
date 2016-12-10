package bd.amazed.docscissors.view;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import bd.amazed.docscissors.model.PageGroup;

public class UIHandler {
	public static int EDIT_MODE_SELECT = 1;
	public static int EDIT_MODE_DRAW = 0;

	private int editingMode;
	protected ArrayList<Rect> rects; //TODO remove this and use current page group?
	protected Rect selectedRect;
	protected PageGroup currentPageGroup;

	private int page;
	private boolean showMergeMode;

	private List<UIHandlerListener> listeners;

	public UIHandler() {
		rects = new ArrayList<>();
		listeners = Collections.synchronizedList(new ArrayList<UIHandlerListener>());
		reset();
	}

	public void setPageGroup(PageGroup pageGroup) {
		rects = pageGroup.getRects();
		selectedRect = null;
		this.currentPageGroup = pageGroup;
		firePageGroupSelectionChanged(pageGroup);
	}

	public PageGroup getCurrentPageGroup() {
		return currentPageGroup;
	}

	public int getEditingMode() {
		return editingMode;
	}

	public void setEditingMode(int mode) {
		if (mode != EDIT_MODE_DRAW && mode != EDIT_MODE_SELECT) {
			throw new IllegalArgumentException("Invalid edit mode");
		}
		this.editingMode = mode;
		fireEditModeChanged();
	}

	public Iterator<Rect> getRectIterator() {
		return rects.iterator();
	}

	public Rect getSelectedRect() {
		return selectedRect;
	}

	protected void setSelectedRect(Rect rectToSelect) {
		if (selectedRect != rectToSelect) { // if change in selection
			if (selectedRect != null) // deselect previous selection
				selectedRect.setSelected(false);
			selectedRect = rectToSelect; // set selection to new rect
			if (selectedRect != null) {
				rectToSelect.setSelected(true);
			}
		}
	}

	public void addRect(Rect rect) {
		rects.add(rect);
		notifyRectsStateChanged();
	}

	public ArrayList<Rect> getAllRects() {
		return rects;
	}


	public void deleteSelected() {
		delete(selectedRect);
	}

	public void splitHorizontalSelected(DocPanel pdfPanel) {
		if (selectedRect != null) {
			Rectangle bounds = selectedRect.bounds;
			Point location = bounds.getLocation();

			Rectangle leftBounds = new Rectangle(selectedRect.bounds.x, selectedRect.bounds.y, selectedRect.bounds.width / 2, selectedRect.bounds.height);
			Rect leftRect = new Rect(location, this);
			leftRect.addListener(pdfPanel);
			leftRect.setBounds(leftBounds);

			Rectangle rightBounds = new Rectangle(selectedRect.bounds.x + selectedRect.bounds.width / 2, selectedRect.bounds.y, selectedRect.bounds.width / 2, selectedRect.bounds.height);
			Rect rightRect = new Rect(location, this);
			rightRect.addListener(pdfPanel);
			rightRect.setBounds(rightBounds);

			int indexSelected = rects.indexOf(selectedRect);
			delete(selectedRect);
			rects.add(indexSelected, rightRect);
			rects.add(indexSelected, leftRect);
			notifyRectsStateChanged();
		}
	}

	public void splitVerticalSelected(DocPanel pdfPanel) {
		if (selectedRect != null) {
			Rectangle bounds = selectedRect.bounds;
			Point location = bounds.getLocation();

			Rectangle upBounds = new Rectangle(selectedRect.bounds.x, selectedRect.bounds.y, selectedRect.bounds.width, selectedRect.bounds.height / 2);
			Rect upRect = new Rect(location, this);
			upRect.addListener(pdfPanel);
			upRect.setBounds(upBounds);

			Rectangle downBounds = new Rectangle(selectedRect.bounds.x, selectedRect.bounds.y + selectedRect.bounds.height / 2, selectedRect.bounds.width, selectedRect.bounds.height / 2);
			Rect downRect = new Rect(location, this);
			downRect.addListener(pdfPanel);
			downRect.setBounds(downBounds);

			int indexSelected = rects.indexOf(selectedRect);
			delete(selectedRect);
			rects.add(indexSelected, downRect);
			rects.add(indexSelected, upRect);
			notifyRectsStateChanged();
		}
	}

	public void delete(Rect rect) {
		if (rect != null) {
			rects.remove(rect);
			if (selectedRect == rect) {
				selectedRect = null;
			}
			rect.fireEvent(null);
			notifyRectsStateChanged();
		}
	}

	public int getRectCount() {
		return rects.size();
	}

	public int getIndexOf(Rect rect) {
		return rects.indexOf(rect);
	}

	public void deleteAll() {
		Rect anyRect = null;
		if (rects.size() > 0) {
			anyRect = rects.get(0);
		}
		rects.clear();
		if (anyRect != null) {
			anyRect.fireEvent(null); // if canvas repaints whole area once, that
			// will do.
		}
		notifyRectsStateChanged();
	}

	public void reset() {
		deleteAll();
		selectedRect = null; // we are removing all rects, so all rects listners should vanish too.
		page = 1;
		showMergeMode = true;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
		firePageChanged();
	}

	public void setMergeMode(boolean showMergeMode) {
		this.showMergeMode = showMergeMode;
	}

	public boolean isShowMergedMode() {
		return showMergeMode;
	}

	public void addListener(UIHandlerListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeAllListeners() {
		listeners.clear();
	}

	private void fireEditModeChanged() {
		for (UIHandlerListener listener : listeners) {
			listener.editingModeChanged(editingMode);
		}
	}

	private void firePageChanged() {
		for (UIHandlerListener listener : listeners) {
			listener.pageChanged(page);
		}
	}

	private void firePageGroupSelectionChanged(PageGroup pageGroup) {
		for (UIHandlerListener listener : listeners) {
			listener.pageGroupSelected(pageGroup);
		}
	}

	public void notifyRectsStateChanged() {
		for (UIHandlerListener listener : listeners) {
			listener.rectsStateChanged();
		}
	}

	public void equalizeWidthOfSelected(int viewWidth) {
		int maxWidth = -1;
		for (Rect rect : rects) {
			if (rect.bounds.width > maxWidth) {
				maxWidth = rect.bounds.width;
			}
		}
		for (Rect rect : rects) {
			rect.bounds.width = maxWidth;
			if (rect.bounds.x + maxWidth > viewWidth) {
				rect.bounds.x = viewWidth - maxWidth;
			}
		}

		if (rects.size() > 0) {
			rects.get(0).fireEvent(null);
		}
		notifyRectsStateChanged();
	}

	public void equalizeHeightOfSelected(int viewHeight) {
		int maxHeight = -1;
		for (Rect rect : rects) {
			if (rect.bounds.height > maxHeight) {
				maxHeight = rect.bounds.height;
			}
		}
		for (Rect rect : rects) {
			rect.bounds.height = maxHeight;
			if (rect.bounds.y + maxHeight > viewHeight) {
				rect.bounds.y = viewHeight - maxHeight;
			}
		}
		if (rects.size() > 0) {
			rects.get(0).fireEvent(null);
		}
		notifyRectsStateChanged();
	}
}
