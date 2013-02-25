package org.rr.jeborker.gui.renderer;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.rr.jeborker.gui.model.FileSystemTreeModel.IFolderNode;


public class FileSystemTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -7057675192468615801L;

	public Component getTreeCellRendererComponent(final JTree tree, final Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if(value instanceof IFolderNode) {
			Icon icon =  ((IFolderNode)value).getSystemIcon();
			setIcon(icon);

		}
		return this;
	}
}