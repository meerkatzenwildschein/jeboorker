package net.antonioshome.swing.treewrapper;

import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

class AutoScrollingTreeDropTarget extends DropTarget {
	private JViewport viewport;
	private int scrollUnits;
	private JTree tree;

	AutoScrollingTreeDropTarget(JTree aTree, DropTargetListener listener) {
		super(aTree, DnDConstants.ACTION_COPY_OR_MOVE, listener);
		viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, aTree);
		scrollUnits = Math.max(aTree.getRowHeight(), 16);
		this.tree = aTree;
	}

	private Point lastDragCursorLocn = new Point(0, 0);

	protected void updateAutoscroll(Point dragCursorLocn) {
		if (lastDragCursorLocn.equals(dragCursorLocn))
			return;
		else
			lastDragCursorLocn.setLocation(dragCursorLocn);
		doAutoscroll(dragCursorLocn);
	}

	protected void initializeAutoscrolling(Point p) {
		doAutoscroll(p);
	}

	protected void clearAutoscroll() {
	}

	private static final int AUTOSCROLL_MARGIN = 16;

	protected void doAutoscroll(Point aPoint) {
		if (viewport == null)
			return;

		Point treePosition = viewport.getViewPosition();
		int vH = viewport.getExtentSize().height;
		int vW = viewport.getExtentSize().width;
		Point nextPoint = null;
		if ((aPoint.y - treePosition.y) < AUTOSCROLL_MARGIN) {
			nextPoint = new Point(treePosition.x, Math.max(treePosition.y - scrollUnits, 0));
		} else if (treePosition.y + vH - aPoint.y < AUTOSCROLL_MARGIN) {
			nextPoint = new Point(treePosition.x, Math.min(aPoint.y + AUTOSCROLL_MARGIN, tree.getHeight() - vH));
		} else if (aPoint.x - treePosition.x < AUTOSCROLL_MARGIN) {
			nextPoint = new Point(Math.max(treePosition.x - AUTOSCROLL_MARGIN, 0), treePosition.y);
		} else if (treePosition.x + vW - aPoint.x < AUTOSCROLL_MARGIN) {
			nextPoint = new Point(Math.min(treePosition.x + AUTOSCROLL_MARGIN, tree.getWidth() - vW), treePosition.y);
		}
		if (nextPoint != null)
			viewport.setViewPosition(nextPoint);
	}
}