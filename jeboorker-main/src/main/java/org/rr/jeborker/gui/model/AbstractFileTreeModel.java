package org.rr.jeborker.gui.model;

import java.util.List;
import java.util.logging.Level;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;

public abstract class AbstractFileTreeModel extends DefaultTreeModel {

	public AbstractFileTreeModel(TreeNode root) {
		super(root);
	}

	@Override
	public void reload(TreeNode node) {
		if(node == null) {
			return;
		}

		if(node instanceof FileSystemNode) {
			((FileSystemNode) node).reset();
		}

		try {
			super.reload(node);
		} catch(NullPointerException e) {
			// happens sometimes if the node to be reloaded is not expanded.
			LoggerFactory.getLogger(this).log(Level.WARNING, "NullPointer for node " + node);
		} catch(Exception e) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Reload node " + node + " failed.", e);
		}
	}

	/**
	 * Reloads these node which represents the given {@link IResourceHandler} instance.
	 * If the node is not opened it will not be refreshed.
	 */
	public void reload(IResourceHandler resourceToRefresh, List<TreePath> treePathRows) {
		for (TreePath pathForRow : treePathRows) {
			if(pathForRow != null && pathForRow.getLastPathComponent() instanceof FileSystemNode) {
				IResourceHandler resourceHandler = ((FileSystemNode) pathForRow.getLastPathComponent()).getResource();
				if(resourceHandler.equals(resourceToRefresh)) {
					reload((TreeNode) pathForRow.getLastPathComponent());
				}
			}
		}
	}
}
