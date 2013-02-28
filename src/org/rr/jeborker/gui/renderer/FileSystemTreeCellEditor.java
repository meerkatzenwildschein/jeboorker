package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.io.File;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.model.FileSystemTreeModel.IFolderNode;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class FileSystemTreeCellEditor extends DefaultTreeCellEditor {

	private IFolderNode editingNode = null;
	
	public FileSystemTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
		super(tree, renderer);
		addCellEditorListener(new CellEditorListener() {
			
			@Override
			public void editingStopped(ChangeEvent e) {
				if (editingNode != null) {
					IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(editingNode.getFile().getParentFile());
					MainController.getController().refreshFileSystemTreeEntry(resourceHandler);
				}			
			}
			
			@Override
			public void editingCanceled(ChangeEvent e) {
			}
		});
	}

	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		Component result = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		editingIcon = ((IFolderNode) value).getSystemIcon(); // setup the icon to be used while editing
		File file = ((IFolderNode) value).getFile();
		if(file.isDirectory()) {
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
		editingNode = (IFolderNode) value;
		return result;
	}

	public boolean isCellEditable(EventObject event) {
		return super.isCellEditable(event);
	}

}