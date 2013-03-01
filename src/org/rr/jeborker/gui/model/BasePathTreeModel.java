package org.rr.jeborker.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

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
	
}
