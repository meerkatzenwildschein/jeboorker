package org.rr.commons.swing.components.tree;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Enumeration;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

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

    /** Expands all the nodes in this tree. */
    public void expandAll() {
        expandOrCollapsePath(new TreePath(getModel().getRoot()), true);
    }

    /** Collapses all the nodes in this tree. */
    public void collapseAll() {
        expandOrCollapsePath(new TreePath(getModel().getRoot()), false);
    }

    /** Expands or collapses all nodes beneath the given path represented as an array of nodes. */
    public void expandOrCollapsePath(TreeNode[] nodes, boolean expand) {
        expandOrCollapsePath(new TreePath(nodes), expand);
    }

    /** Expands or collapses all nodes beneath the given path. */
    private void expandOrCollapsePath(TreePath parent, boolean expand) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandOrCollapsePath(path, expand);
            }
        }
        if (expand) {
            expandPath(parent);
        } else {
            collapsePath(parent);
        }
    }

    /**
     * Makes JTree's implementation less width-greedy. Left to JTree, we'll
     * grow to be wide enough to show our widest node without using a scroll
     * bar. While this is seemingly widely acceptable (ho ho), it's no good
     * in Evergreen's "Find in Files" dialog. If long lines match, next time you
     * open the dialog, it can be so wide it doesn't fit on the screen. Here,
     * we go for the minimum width, and assume that an ETree is never packed
     * on its own (in which case, it might end up rather narrow by default).
     */
    public Dimension getPreferredScrollableViewportSize() {
        Dimension size = super.getPreferredScrollableViewportSize();
        size.width = getMinimumSize().width;
        return size;
    }

    /**
     * Selects the nodes matching the given string. The matching is
     * a case-insensitive substring match. The selection is not cleared
     * first; you must do this yourself if it's the behavior you want.
     *
     * If ensureVisible is true, the first selected node in the model
     * will be made visible via scrollPathToVisible.
     */
    public void selectNodesMatching(String string, boolean ensureVisible) {
        TreePath path = new TreePath(getModel().getRoot());
        selectNodesMatching(path, string.toLowerCase());
        if (ensureVisible) {
            scrollPathToVisible(getSelectionPath());
        }
    }

    private void selectNodesMatching(TreePath parent, String string) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                selectNodesMatching(path, string);
            }
        }
        if (node.toString().toLowerCase().contains(string)) {
            addSelectionPath(parent);
        }
    }

    /** Scrolls the path to the middle of the scroll pane. */
    public void scrollPathToVisible(TreePath path) {
        if (path == null) {
            return;
        }
        makeVisible(path);
        Rectangle pathBounds = getPathBounds(path);
        if (pathBounds != null) {
            Rectangle visibleRect = getVisibleRect();
            if (getHeight() > visibleRect.height) {
                int y = pathBounds.y - visibleRect.height / 2;
                visibleRect.y = Math.min(Math.max(0, y), getHeight() - visibleRect.height);
                scrollRectToVisible(visibleRect);
            }
        }
    }

	/**
     * Returns the path for the node at the specified location.
     *
     * @param x an integer giving the number of pixels horizontally from
     *          the left edge of the display area, minus any left margin
     * @param y an integer giving the number of pixels vertically from
     *          the top of the display area, minus any top margin
     * @return  the <code>TreePath</code> for the node at that location
     */
	public TreePath getPathForLocation(int x, int y) {
		TreePath closestPath = getClosestPathForLocation(x, y);

		if (closestPath != null) {
			Rectangle pathBounds = getPathBounds(closestPath);

			if (pathBounds != null && y >= pathBounds.y && y < (pathBounds.y + pathBounds.height)) {
				return closestPath;
			}
		}
		return null;
	}
}
