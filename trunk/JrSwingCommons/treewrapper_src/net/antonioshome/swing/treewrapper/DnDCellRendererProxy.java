/*
 * $Id: DnDCellRendererProxy.java,v 1.4 2006-06-06 10:18:48 antonio Exp $
 * Read the "license.txt" file for licensing information.
 * (C) Antonio Vieiro. All rights reserved.
 */
package net.antonioshome.swing.treewrapper;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;

/**
 * DnDCellRendererProxy is a TreeCellRenderer that proxies operations to a true TreeCellRenderer, but that draws a border around specific TreeNodes.
 * 
 * @author Antonio Vieiro (antonio@antonioshome.net), $Author: antonio $
 * @version $Revision: 1.4 $
 */
class DnDCellRendererProxy extends Component implements TreeCellRenderer {
	private TreeCellRenderer originalTreeCellRenderer;
	private DnDBorderFactory borderFactory;
	private TreeNode draggedNode;
	private TreeNode dropNode;
	private int dropNodeRow;
	private boolean fetchBorder;
	private Border originalBorder;

	/**
	 * Creates a new instance of DragAndDropCellRenderer.
	 * 
	 * @param trueCellRenderer
	 *            the original cell renderer.
	 */
	public DnDCellRendererProxy(TreeCellRenderer trueCellRenderer) {
		originalTreeCellRenderer = trueCellRenderer;
		borderFactory = new DnDBorderFactory();
		fetchBorder = true;
	}

	public TreeCellRenderer getOriginalTreeCellRenderer() {
		return originalTreeCellRenderer;
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		Component c = originalTreeCellRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		TreeNode nodeToRender = (TreeNode) value;

		if (c instanceof JComponent) {
			if (fetchBorder) {
				fetchBorder = false;
				originalBorder = ((JComponent) c).getBorder();
			}
			// TODO: This *REMOVES* the border in c.
			// TODO: Use compound borders to draw BOTH borders.
			JComponent jComponent = (JComponent) c;
			if (nodeToRender.equals(dropNode)) {
				Border border = null;
				if (isDropAllowed()) {
					border = borderFactory.getDropAllowedBorder();
					dropNodeRow = row;
				} else {
					border = borderFactory.getDropNotAllowedBorder();
					dropNodeRow = -2;
				}
				jComponent.setBorder(border);
			} else if (isDropAllowed() && row == dropNodeRow + 1) {
				jComponent.setBorder(borderFactory.getOffsetBorder());
			} else {
				jComponent.setBorder(originalBorder);
				dropNodeRow = -2;
			}
		}
		return c;
	}

	/**
	 * Getter for property draggedNode.
	 * 
	 * @return Value of property draggedNode.
	 */
	public TreeNode getDraggedNode() {
		return this.draggedNode;
	}

	/**
	 * Setter for property draggedNode.
	 * 
	 * @param draggedNode
	 *            New value of property draggedNode.
	 */
	public void setDraggedNode(TreeNode draggedNode) {
		this.draggedNode = draggedNode;
	}

	/**
	 * Getter for property dropNode.
	 * 
	 * @return Value of property dropNode.
	 */
	public TreeNode getDropNode() {
		return this.dropNode;
	}

	/**
	 * Setter for property dropNode.
	 * 
	 * @param dropNode
	 *            New value of property dropNode.
	 */
	public void setDropNode(TreeNode dropNode) {
		this.dropNode = dropNode;
		if (dropNode == null)
			dropNodeRow = -2;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[DnDCellRendererProxy for : ").append(originalTreeCellRenderer).append("]");
		return sb.toString();
	}

	/**
	 * Holds value of property dropAllowed.
	 */
	private boolean dropAllowed;

	/**
	 * Getter for property dropAllowed.
	 * 
	 * @return Value of property dropAllowed.
	 */
	public boolean isDropAllowed() {
		return this.dropAllowed;
	}

	/**
	 * Setter for property dropAllowed.
	 * 
	 * @param dropAllowed
	 *            New value of property dropAllowed.
	 */
	public void setDropAllowed(boolean dropAllowed) {
		this.dropAllowed = dropAllowed;
		if (!dropAllowed)
			dropNodeRow = -2;
	}

}
