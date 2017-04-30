package org.rr.jeborker.gui.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.components.tree.JRTree;

public class FileSystemTreeModel extends AbstractFileTreeModel {

	private static final long serialVersionUID = -456216843620742653L;

	private DefaultMutableTreeNode root;

	public FileSystemTreeModel(JRTree tree) {
		super(new DefaultMutableTreeNode("root"), tree);
		this.root = (DefaultMutableTreeNode) getRoot();
		init();
	}

	private void init() {
		List<File> specialFolders = getSpecialFolders();
		for(File specialFolder : specialFolders) {
			IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(specialFolder);
			FileSystemNode basePathNode = new FileSystemNode(resourceHandler, null, tree);
			this.root.add(basePathNode);
		}

		File[] listRoots = File.listRoots();
		Arrays.sort(listRoots);
		for(File root : listRoots) {
			if(!root.toString().equalsIgnoreCase("A:\\")) {
				IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(root);
				FileSystemNode basePathNode = new FileSystemNode(resourceHandler, null, tree);
				this.root.add(basePathNode);
			}
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
			LoggerFactory.getLogger().log(Level.WARNING, "Rename " + oldPathName +" to" + newPathName + " has failed.", e);
		}
		nodeChanged(aNode);
	}

	public void dispose() {
		TreeModelListener[] treeModelListeners = getTreeModelListeners();
		for(TreeModelListener treeModelListener : treeModelListeners) {
			removeTreeModelListener(treeModelListener);
		}
	}

	/**
	 * Get some special folder to be shown at the root file levels.
	 */
	private List<File> getSpecialFolders() {
		final ArrayList<File> result = new ArrayList<>();
		final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

		File defaultDirectory = fileSystemView.getDefaultDirectory();
		if(defaultDirectory != null) {
			result.add(defaultDirectory);
		}
		File homeDirectory = fileSystemView.getHomeDirectory();
		if(homeDirectory != null && !homeDirectory.equals(defaultDirectory)) {
			result.add(homeDirectory);
		}
		return result;
	}
}
