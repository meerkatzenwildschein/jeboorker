package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.awt.Dimension;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

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
					if (resourceHandler != null) {
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
		if (file.isDirectoryResource()) {
			if (file.toString().equals(System.getProperty("user.home"))) {
				editingIcon = ImageResourceBundle.getResourceAsImageIcon("folder_home_16.png");
			} else if (tree.isExpanded(row)) {
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
		if(tree != null) {
			TreePath selectionPath = tree.getSelectionPath();
			if(selectionPath != null) {
				TreeNode node = (TreeNode) selectionPath.getLastPathComponent();
				if (node != null && node.isLeaf()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This is invoked if a <code>TreeCellEditor</code> is not supplied in the constructor. It returns a <code>TextField</code> editor.
	 * 
	 * @return a new <code>TextField</code> editor
	 */
	protected TreeCellEditor createTreeCellEditor() {
		FileSystemRenameTreeCellEditor editor = new FileSystemRenameTreeCellEditor(this) {
			public boolean shouldSelectCell(EventObject event) {
				boolean retValue = super.shouldSelectCell(event);
				return retValue;
			}
		};

		// One click to edit.
		editor.setClickCountToStart(1);
		return editor;
	}
	
    /**
     * Get the PreferredSize from the renderer component
     */
    public Dimension getRendererPreferredSize() {
        if(renderer != null) {
            Dimension rSize = renderer.getPreferredSize();
            return rSize;
        }
        return editingComponent.getPreferredSize();
    }	

}