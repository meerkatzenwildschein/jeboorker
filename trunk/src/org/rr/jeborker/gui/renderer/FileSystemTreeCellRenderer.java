package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.rr.jeborker.gui.model.FileSystemTreeModel.IFolderNode;
import org.rr.jeborker.gui.resources.ImageResourceBundle;


public class FileSystemTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -7057675192468615801L;
	
	public Component getTreeCellRendererComponent(final JTree tree, final Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if(value instanceof IFolderNode) {
			Icon icon =  ((IFolderNode)value).getSystemIcon();
			setIcon(icon);
			
			File file = ((IFolderNode) value).getFile();
			if(file != null && file.isDirectory()) {
				if(file.toString().equals(System.getProperty("user.home"))) {
					setIcon(ImageResourceBundle.getResourceAsImageIcon("folder_home_16.png"));
				} else if(tree.isExpanded(row)) {
					setIcon(ImageResourceBundle.FOLDER_OPEN_16_ICON);
				} else {
					setIcon(ImageResourceBundle.FOLDER_CLOSE_16_ICON);
				}
			} else {
				setIcon(ImageResourceBundle.FILE_16_ICON);
			}
		}
		return this;
	}
}