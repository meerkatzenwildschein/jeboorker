package org.rr.commons.swing.components.tree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.rr.commons.utils.ListUtils;

public class TreeUtil {

	public static final String PATH_SEPARATOR = "\t";

	/**
	 * Cleans the separator chars from the given node name.
	 */
	private static String cleanNodeName(String nodeName) {
		if(nodeName != null) {
			nodeName = nodeName.replaceAll("[\\t\\\n]", "_");
		}
		return nodeName;
	}

	private static List<TreePath> getExpandedNodeNames(JTree tree) {
		List<TreePath> result = new ArrayList<TreePath>();
		int rowCount = tree.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			TreePath rowPath = tree.getPathForRow(i);
			if(tree.isExpanded(rowPath)) {
				result.add(rowPath);
			}
		}
		return result;
	}

	/**
	 * Get the tree path names in a separated string
	 */
	private static String getTreePathName(TreePath path, String separator) {
		Object[] pathArray = path.getPath();
		StringBuilder result = new StringBuilder();
		for(Object p : pathArray) {
			if(p instanceof NamedNode) {
				String name = cleanNodeName(((NamedNode)p).getName());
				if(name != null) {
					if(result.length() > 0) {
						result.append(separator);
					}
					result.append(name);
				}
			}
		}
		return result.toString();
	}

	private static TreePath restoreTreePathByName(JTree tree, String pathString, String separator) {
		List<String> split = ListUtils.split(pathString, separator);
		int row = 0;
		TreePath lastRowPath = null;
		for(String s : split) {
			s = cleanNodeName(s);
			int rowCount = tree.getRowCount();
			for (; row < rowCount; row++) {
				TreePath rowPath = tree.getPathForRow(row);
				Object pathComponent = rowPath.getLastPathComponent();
				if(pathComponent instanceof NamedNode) {
					String cleanedNodeName = cleanNodeName(((NamedNode) pathComponent).getName());
					if(pathComponent instanceof NamedNode && cleanedNodeName.equals(s)) {
						tree.expandRow(row);
						lastRowPath = rowPath;
						break;
					}
				}
			}
		}
		return lastRowPath;
	}

	/**
	 * Get the expansion states for the expanded nodes of the given {@link JTree} as string.
	 * Only those nodes gets collected that implements the {@link NamedNode} interface.
	 * @see #restoreExpanstionState(JRTree, String)
	 */
	public static String getExpansionStates(JTree tree) {
		List<TreePath> expandedNodeNames = getExpandedNodeNames(tree);
		StringBuilder result = new StringBuilder();
		for(TreePath path : expandedNodeNames) {
			String treePathName = getTreePathName(path, PATH_SEPARATOR);
			if(result.length() > 0) {
				result.append("\n");
			}
			result.append(treePathName);
		}
		return result.toString();
	}

	/**
	 * Restores the expansion states from the given String.
	 * @see TreeUtil#getExpansionStates(JRTree)
	 */
	public static TreePath restoreExpanstionState(JTree tree, String expansionStates) {
		List<String> expansionStatesList = ListUtils.split(expansionStates, "\n");
		TreePath lastExpandedRow = null;
		for(String expansionState : expansionStatesList) {
			lastExpandedRow = restoreTreePathByName(tree, expansionState, PATH_SEPARATOR);
		}
		return lastExpandedRow;
	}

	/**
	 * Tells if the given rootPath ends with a member of the given childPath.
	 * @param path1 The root path
	 * @param path2 The path which is tested if the root is the same.
	 * @return <code>true</code> if the rootPath ends with a member of the given childPath and <code>false</code> otherwise.
	 */
	public static boolean isChild(TreePath rootPath, TreePath childPath) {
		Object lastRootPathComponent = rootPath.getLastPathComponent();
		TreePath childParentPath = childPath;
		do {
			if(childParentPath.getLastPathComponent().equals(lastRootPathComponent)) {
				return true;
			}

			childParentPath = childParentPath.getParentPath();
		} while(childParentPath != null);

		return false;
	}

}
