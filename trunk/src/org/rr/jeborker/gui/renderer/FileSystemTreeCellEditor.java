package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.rr.jeborker.gui.model.FileSystemTreeModel.IFolderNode;

public class FileSystemTreeCellEditor extends DefaultTreeCellEditor {

	public FileSystemTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
		super(tree, renderer);
	}

	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		Component result = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		editingIcon = ((IFolderNode) value).getSystemIcon(); // setup the icon to be used while editing
		return result;
	}

	public boolean isCellEditable(EventObject event) {
		return super.isCellEditable(event);
	}
}