package org.rr.jeborker.gui.model;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.rr.common.swing.tree.TreeUtil;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ListUtils;

public class FileSystemTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = -456216843620742653L;

	private JTree tree;
	
	private DefaultMutableTreeNode root;
	
	public FileSystemTreeModel(JTree tree) {
		super(new DefaultMutableTreeNode("root"));
		this.root = (DefaultMutableTreeNode) getRoot();
		this.tree = tree;
		init();
	}
	
	public void init() {
		File[] listRoots = File.listRoots();
		Arrays.sort(listRoots);
		
		for(File root : listRoots) {
			IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(root);
			FileSystemNode basePathNode = new FileSystemNode(resourceHandler, null);
			this.root.add(basePathNode);
		}
	}	

	protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
		super.fireTreeStructureChanged(source, path, childIndices, children);
	}

	/**
	 * This sets the user object of the TreeNode identified by path and posts a node changed. If you use custom user objects in the TreeModel you're going to
	 * need to subclass this and set the user object of the changed node to something meaningful.
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		final FileSystemNode aNode = (FileSystemNode) path.getLastPathComponent();
		String oldPathName = aNode.getResource().getResourceString();
		String newPathName = oldPathName.substring(0, oldPathName.length() - aNode.getResource().getName().length()) + newValue;

		try {
			aNode.renameTo(ResourceHandlerFactory.getResourceHandler(newPathName));
		} catch (IOException e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Rename " + oldPathName +" to" + newPathName + " has failed.");
		}
		nodeChanged(aNode);
	}
	
	@Override
	public void reload(TreeNode node) {
		if(node instanceof FileSystemNode) {
			((FileSystemNode)node).reset();
		}
		super.reload(node);
	}	

	/**
	 * Reloads these node which represents the given {@link IResourceHandler} instance.
	 * If the node is not opened it will not be refreshed.
	 */
	public void reload(IResourceHandler resourceToRefresh) {
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
	
	public void dispose() {
		TreeModelListener[] treeModelListeners = getTreeModelListeners();
		for(TreeModelListener treeModelListener : treeModelListeners) {
			removeTreeModelListener(treeModelListener);
		}
	}
	
	public TreePath restoreExpansionState(JTree tree, List<String> fullPathSegments) {
		String treeExpansionPathString = ListUtils.join(fullPathSegments, TreeUtil.PATH_SEPARATOR);	
		TreePath lastExpandedRow = TreeUtil.restoreExpanstionState(tree, treeExpansionPathString);
		return lastExpandedRow;
	}
}
