package org.rr.jeborker.gui.model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.components.tree.JRTree;
import org.rr.commons.swing.components.tree.NamedNode;
import org.rr.commons.swing.components.tree.TreeUtil;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;

public class BasePathTreeModel extends AbstractFileTreeModel {

	private static final long serialVersionUID = 1442592632931949573L;

	private static final UniqueAllEntryNode singletonUniqueAllEntryNode = new UniqueAllEntryNode();

	private DefaultMutableTreeNode root;

	private TreePath filterTreePath;

	public BasePathTreeModel(JRTree tree) {
		super(new DefaultMutableTreeNode("root"), tree);
		this.root = (DefaultMutableTreeNode) getRoot();
		this.init();
	}

	public TreePath getFilterTreePath() {
		return filterTreePath;
	}

	public void setFilterTreePath(TreePath filterTreePath) {
		this.filterTreePath = filterTreePath;
	}

	private void init() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final List<String> basePath = new ArrayList<>(preferenceStore.getBasePath());

		for(String path : basePath) {
			IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(path);
			FileSystemNode basePathNode = new FileSystemNode(resourceHandler, null, tree, false);
			root.add(basePathNode);
		}
		root.add(singletonUniqueAllEntryNode);
	}

	/**
	 * Reloads the model. Changes will be taken under account.
	 */
	public void reload() {
		root.removeAllChildren();
		init();
		try {
			super.reload();
		} catch(Exception e) {
			//happens for example if the tree is in edit mode.
			LoggerFactory.getLogger().log(Level.WARNING, "Reloading nodes has failed", e);
		}
	}

	public TreePath restoreExpansionState(JTree tree, IResourceHandler resourceHandler, List<String> fullPathSegments) {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final String basePathFor = preferenceStore.getBasePathFor(resourceHandler);
		final int segments = ResourceHandlerFactory.getResourceHandler(basePathFor).getPathSegments().size()  - 1;
		final List<String> basePathSegements = ListUtils.extract(fullPathSegments, segments, fullPathSegments.size());
		final String treeExpansionPathString = ListUtils.join(basePathSegements, TreeUtil.PATH_SEPARATOR);
		final TreePath lastExpandedRow = TreeUtil.restoreExpanstionState(tree, treeExpansionPathString);
		return lastExpandedRow;
	}

	/**
	 * This is a special node and did not represents a base path. It's the "All Entries" node
	 * that allows to toggle the visibility of all base path nodes.
	 */
	private static class UniqueAllEntryNode implements MutableTreeNode, NamedNode, Comparable<UniqueAllEntryNode> {

		private static final int HASH_CODE = UniqueAllEntryNode.class.getName().hashCode();

		private String localizedName;

		private TreeNode parent;

		UniqueAllEntryNode() {
			localizedName = Bundle.getString("BasePathTreeModel.nodeName.all");
		}

		@Override
		public TreeNode getChildAt(int childIndex) {
			return null;
		}

		@Override
		public int getChildCount() {
			return 0;
		}

		@Override
		public TreeNode getParent() {
			return this.parent;
		}

		@Override
		public int getIndex(TreeNode node) {
			return 0;
		}

		@Override
		public boolean getAllowsChildren() {
			return false;
		}

		@Override
		public boolean isLeaf() {
			return true;
		}

		@Override
		public  Enumeration<? extends TreeNode> children() {
			return null;
		}

		@Override
		public String getName() {
			return this.localizedName;
		}

		public String toString() {
			return this.localizedName;
		}

		@Override
		public void insert(MutableTreeNode child, int index) {
		}

		@Override
		public void remove(int index) {
		}

		@Override
		public void remove(MutableTreeNode node) {
		}

		@Override
		public void setUserObject(Object object) {
		}

		@Override
		public void removeFromParent() {
		}

		@Override
		public void setParent(MutableTreeNode newParent) {
			this.parent = newParent;
		}

		@Override
		public int compareTo(UniqueAllEntryNode o) {
			return 0;
		}

		public boolean equals(Object o) {
			 if(o instanceof UniqueAllEntryNode) {
				 return true;
			 }
			 return false;
		}

		@Override
		public int hashCode() {
			return HASH_CODE;
		}

	}

}
