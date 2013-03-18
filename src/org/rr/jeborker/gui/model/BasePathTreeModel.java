package org.rr.jeborker.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.rr.common.swing.tree.NamedNode;
import org.rr.common.swing.tree.TreeUtil;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.JeboorkerPreferences;

public class BasePathTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 1442592632931949573L;
	
	private DefaultMutableTreeNode root;
	
	public BasePathTreeModel() {
		super(new DefaultMutableTreeNode("root"));
		this.root = (DefaultMutableTreeNode) getRoot();
		this.init();
	}

	public void init() {
		List<String> basePath = new ArrayList<String>(JeboorkerPreferences.getBasePath());
		Collections.sort(basePath);
		for(String path : basePath) {
			IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(path);
			FileSystemNode basePathNode = new FileSystemNode(resourceHandler, null, false);
			root.add(basePathNode);
		}
		root.add(new AllEntryNode());
	}
	
	public void dispose() {
		TreeModelListener[] treeModelListeners = getTreeModelListeners();
		for(TreeModelListener treeModelListener : treeModelListeners) {
			removeTreeModelListener(treeModelListener);
		}
	}
	
	public TreePath restoreExpanstionState(JTree tree, IResourceHandler resourceHandler, List<String> fullPathSegments) {
		String basePathFor = JeboorkerPreferences.getBasePathFor(resourceHandler);
		int segments = ResourceHandlerFactory.getResourceHandler(basePathFor).getPathSegments().size()  - 1;
		List<String> basePathSegements = ListUtils.extract(fullPathSegments, segments, fullPathSegments.size());
		String treeExpansionPathString = ListUtils.join(basePathSegements, TreeUtil.PATH_SEPARATOR);	
		TreePath lastExpandedRow = TreeUtil.restoreExpanstionState(tree, treeExpansionPathString);
		return lastExpandedRow;
	}
	
	class AllEntryNode implements MutableTreeNode, NamedNode {

		private String localizedName;

		private TreeNode parent;
		
		AllEntryNode() {
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
		public Enumeration children() {
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
		
	}
	
}
