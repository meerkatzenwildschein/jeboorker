package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.model.FileSystemNode;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class FileSystemTreeCellEditor extends DefaultTreeCellEditor {

	private FileSystemNode editingNode = null;
	
	public FileSystemTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
		super(tree, renderer);
		addCellEditorListener(new CellEditorListener() {
			
			@Override
			public void editingStopped(ChangeEvent e) {
				if (editingNode != null) {
					IResourceHandler resourceHandler = editingNode.getResource().getParentResource();
					if(resourceHandler != null) {
						MainController.getController().refreshFileSystemTreeEntry(resourceHandler);
					}
				}			
			}
			
			@Override
			public void editingCanceled(ChangeEvent e) {
			}
		});
	}

	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		Component result = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		IResourceHandler file = ((FileSystemNode) value).getResource();
		if(file.isDirectoryResource()) {
			if(file.toString().equals(System.getProperty("user.home"))) {
				editingIcon = ImageResourceBundle.getResourceAsImageIcon("folder_home_16.png");
			} else if(tree.isExpanded(row)) {
				editingIcon = ImageResourceBundle.FOLDER_OPEN_16_ICON;
			} else {
				editingIcon = ImageResourceBundle.FOLDER_CLOSE_16_ICON;
			}
		} else {
			editingIcon = ImageResourceBundle.FILE_16_ICON;
		}
		editingNode = (FileSystemNode) value;
		return result;
	}

	@Override
	public boolean isCellEditable(EventObject event) {
		TreeNode node = (TreeNode) tree.getSelectionPath().getLastPathComponent();
		if(node.isLeaf()) {
			return true;
		}
		return false;
	}
	
}