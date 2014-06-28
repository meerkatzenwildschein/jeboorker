package org.rr.jeborker.gui.model;

import java.util.logging.Level;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;

public abstract class AbstractFileTreeModel extends DefaultTreeModel {

	JTree tree;

	public AbstractFileTreeModel(JTree tree, TreeNode root) {
		super(root);
		this.tree = tree;
	}

	@Override
	public void reload(TreeNode node) {
		if(node == null) {
			return;
		}

		if(node instanceof FileSystemNode) {
			((FileSystemNode)node).reset();
		}

		try {
			super.reload(node);
		} catch(Exception e) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Reload node " + node + " failed.", e);
		}
	}

	/**
	 * Reloads these node which represents the given {@link IResourceHandler} instance.
	 * If the node is not opened it will not be refreshed.
	 */
	public void reload(final IResourceHandler resourceToRefresh) {
		int rowCount = tree.getRowCount();
		for(int i = 0; i < rowCount; i++) {
			TreePath pathForRow = tree.getPathForRow(i);
			if(pathForRow.getLastPathComponent() instanceof FileSystemNode) {
				IResourceHandler resourceHandler = ((FileSystemNode) pathForRow.getLastPathComponent()).getResource();
				if(resourceHandler.equals(resourceToRefresh)) {
					reload((TreeNode) pathForRow.getLastPathComponent());
					break;
				}
			}
		}
	}
}
