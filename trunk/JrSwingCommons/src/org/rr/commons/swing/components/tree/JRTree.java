package org.rr.commons.swing.components.tree;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.rr.commons.swing.SwingUtils;
import org.rr.commons.utils.MathUtils;

public class JRTree extends JTree {

	private boolean toggleExpandOnDoubleClick = false;
	
	private boolean autoMoveHorizontalSliders = false;
	
	private boolean repaintAllOnChange = false;
	
	public JRTree() {
		super();

		this.addMouseListener(new ToggleExpandOnDoubleClickMouseListener());
		this.getSelectionModel().addTreeSelectionListener(new RepaintChangeListener());
		this.addMouseListener(new MouseAdapter() {
			// fixes that the renderer did not fill the tree horizontally and
			// on clicks behind the renderer the selection did not change.

			@Override
			public void mouseClicked(MouseEvent e) {
				Point point = e.getPoint();
				int row = JRTree.this.getRowForLocation(0, point.y);
				for (int i = 0; row < 0 && i < JRTree.this.getWidth(); i += 10) {
					row = JRTree.this.getRowForLocation(i, point.y);
				}
				if (getSelectionModel().getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION
						|| !(e.isAltDown() || e.isControlDown() || e.isShiftDown())) {
					int[] selectedRows = JRTree.this.getSelectionRows();
					if (selectedRows != null && selectedRows.length > 0 && row != selectedRows[0]) {
						JRTree.this.setSelectionRow(row);
						if(JRTree.this.isEditable()) {
							TreePath path = JRTree.this.getPathForRow(row);
							JRTree.this.startEditingAtPath(path);
						}
					}
				}
			}

		});
		
		final AutoMoveHorizontalMouseListener autoMoveHorizontalMouseListener = new AutoMoveHorizontalMouseListener();
		this.addMouseMotionListener(autoMoveHorizontalMouseListener);
		
		this.addAncestorListener(new AncestorListener() {
			
			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}
			
			@Override
			public void ancestorMoved(AncestorEvent event) {
			}
			
			@Override
			public void ancestorAdded(AncestorEvent event) {
				JScrollPane surroundingScrollPane = SwingUtils.getSurroundingScrollPane(JRTree.this);
				surroundingScrollPane.addMouseWheelListener(autoMoveHorizontalMouseListener);
			}
		});

	}

	/**
	 * Scrolls to the given path. Scroll is only performed in the y direction.
	 */
	public void scrollPathToVisibleVertical(TreePath path, boolean select) {
		super.scrollPathToVisible(path);
		JScrollPane surroundingScrollPane = SwingUtils.getSurroundingScrollPane(this);
		if (surroundingScrollPane != null) {
			int rowForPath = getRowForPath(path);
			int y = rowForPath * getRowHeight();
			
			int halfHeight = surroundingScrollPane.getPreferredSize().height / 2;
			surroundingScrollPane.getVerticalScrollBar().setValue(y - halfHeight);
			surroundingScrollPane.getHorizontalScrollBar().setValue(0);
			if(select) {
				setSelectionPath(path);
			}
		}
	}
	
	public boolean isToggleExpandOnDoubleClick() {
		return toggleExpandOnDoubleClick;
	}

	public void setToggleExpandOnDoubleClick(boolean expandOnDoubleClick) {
		this.toggleExpandOnDoubleClick = expandOnDoubleClick;
	}
	
	private class ToggleExpandOnDoubleClickMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			Point point = e.getPoint();
			int row = JRTree.this.getRowForLocation(0, point.y);
			
			if(toggleExpandOnDoubleClick && e.getClickCount() == 2) {
				if(JRTree.this.isExpanded(row)) {
					JRTree.this.collapseRow(row);
				} else {
					JRTree.this.expandRow(row);						
				}
			}
		}
	}

	public boolean isAutoMoveHorizontalSliders() {
		return autoMoveHorizontalSliders;
	}

	public void setAutoMoveHorizontalSliders(boolean autoMoveHorizontalSliders) {
		this.autoMoveHorizontalSliders = autoMoveHorizontalSliders;
	}
	
	private class AutoMoveHorizontalMouseListener extends MouseAdapter {

		private int latestX;
		private int latestRow;
		
		@Override
		public void mouseMoved(MouseEvent e) {
			if(isAutoMoveHorizontalSliders()) {
				move(e);
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if(isAutoMoveHorizontalSliders()) {
				if(e.getSource() instanceof JScrollPane) {
					int value = ((JScrollPane)e.getSource()).getVerticalScrollBar().getValue();
					e = new MouseWheelEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), e.getX(), e.getY() + value, e.getClickCount(), e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(), e.getWheelRotation());
				}
				move(e);
			}
		}		
		
		private void move(MouseEvent e) {
			int row = JRTree.this.getRowForLocation(0, e.getX());
			for (int paddingLeft = 0; row < 0 && paddingLeft < JRTree.this.getWidth(); paddingLeft += 10) {
				row = JRTree.this.getRowForLocation(paddingLeft, e.getY());
				if(row >= 0) {
					int movement = latestX - e.getX();
					if(latestRow != row || !MathUtils.between(movement, -8, +8)) {
						//row change or a min move by 8 pix in one direction.
						final JScrollPane surroundingScrollPane = SwingUtils.getSurroundingScrollPane(JRTree.this);
						int value = calculateScrollBarLocation(e.getLocationOnScreen().x, surroundingScrollPane, paddingLeft, row);
						int aboveValue = value;
						int underValue = value;
						if(row > 0) {
							aboveValue = calculateScrollBarLocation(e.getLocationOnScreen().x, surroundingScrollPane, paddingLeft, row - 1);
						}
						if(row < JRTree.this.getRowCount()) {
							underValue = calculateScrollBarLocation(e.getLocationOnScreen().x, surroundingScrollPane, paddingLeft, row + 1);
						}
								
						if(value >= 0) {
							value = Math.max(value, aboveValue);
							value = Math.max(value, underValue);		
							int oldValueDiff = surroundingScrollPane.getHorizontalScrollBar().getValue() - value;
							
							if(!MathUtils.between(oldValueDiff, 11, -11)) {
								surroundingScrollPane.getHorizontalScrollBar().setValue(value);
							}
						}
						latestX = e.getX();
						latestRow = row;
					}
				}
			}
		}

		private int calculateScrollBarLocation(int locationOnScreenX, JScrollPane surroundingScrollPane, int paddingLeft, final int row) {
			final TreePath pathForLocation = JRTree.this.getPathForRow(row);
			if(pathForLocation == null) {
				return -1;
			}
			final TreeNode node = (TreeNode) pathForLocation.getLastPathComponent();
			final boolean isLeaf = node.isLeaf();
			final Component treeCellRendererComponent = JRTree.this.getCellRenderer().getTreeCellRendererComponent(JRTree.this, pathForLocation.getLastPathComponent(), false, isExpanded(row), isLeaf, row, false);
			final int rendererWidth = treeCellRendererComponent.getPreferredSize().width;
			
			if(surroundingScrollPane != null) {
				final int visibleComponentWidth = surroundingScrollPane.getBounds().width ;
				final int scrollBarWidth = surroundingScrollPane.getVerticalScrollBar().getPreferredSize().width;

				final int leafPadding = (isLeaf ? 15 : 25);				
				final int visibleWidth = visibleComponentWidth - paddingLeft - rendererWidth - scrollBarWidth; // a minus value for the hidden width
				if(visibleWidth < -10) { //scroll to hidden
					int rendererBegin = paddingLeft - leafPadding; //begin of the renderer location
//					int rendererEnd = (paddingLeft - rendererWidth) * -1; //end of the renderer location
//					int horizontalScrollbarLocation = surroundingScrollPane.getHorizontalScrollBar().getValue();
//					if(horizontalScrollbarLocation < rendererEnd) {
						if(locationOnScreenX + 50 > (surroundingScrollPane.getLocationOnScreen().x + visibleComponentWidth - scrollBarWidth)) {
							int value = rendererBegin + (rendererWidth + leafPadding - visibleComponentWidth + scrollBarWidth + 15);
							if(surroundingScrollPane.getHorizontalScrollBar().getValue() < value) {
								//scroll to the end of the renderer component
								return value;
							}
						} else { 
							//scroll to the beginning of the renderer component
							return rendererBegin;
						}								
//					} 
				} else if(visibleWidth > 10) {
					int rendererBegin = paddingLeft - leafPadding; //begin of the renderer location
					int horizontalScrollbarLocation = surroundingScrollPane.getHorizontalScrollBar().getValue();
					if(rendererBegin < horizontalScrollbarLocation -10) {
						return rendererBegin;
					}
				}
			}
			return -1;
		}
	}

	/**
	 * Tells if all tree nodes are repainted after the selection has changed.
	 * @see #setRepaintAllOnChange
	 */
	public boolean isRepaintAllOnChange() {
		return repaintAllOnChange;
	}

	/**
	 * Enables / disables that the all tree nodes are repainted after
	 * the selection has changed.
	 */
	public void setRepaintAllOnChange(boolean repaintAllOnChnage) {
		this.repaintAllOnChange = repaintAllOnChnage;
	}
	
	private class RepaintChangeListener implements TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			if(isRepaintAllOnChange()) {
				JRTree.this.repaint();
			}
		}
	}
	
    /**
     * Returns the row that displays the node identified by the specified
     * path.
     *
     * @param path  the <code>TreePath</code> identifying a node
     * @return an integer specifying the display row, where 0 is the first
     *         row in the display, or -1 if any of the elements in path
     *         are hidden under a collapsed parent.
     */
    public int getRowForPath(TreePath path) {
        TreeUI tree = getUI();

        if(tree != null) {
            int row = tree.getRowForPath(this, path);
            if(row >= 0) {
            	return row;
            }
        }
        return -1;
    }	

}
