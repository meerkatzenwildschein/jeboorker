package org.rr.jeborker.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.swing.components.tree.JRTree;
import org.rr.commons.swing.components.tree.TreeUtil;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.jeborker.gui.model.BasePathTreeModel;
import org.rr.jeborker.gui.model.FileSystemNode;
import org.rr.jeborker.gui.model.FileSystemTreeModel;

public class MainViewTreeComponentHandler {

	private JRTree basePathTree;

	private JRTree fileSystemTree;

	private MainController controller;

	private MainView mainWindow;

	MainViewTreeComponentHandler(JRTree basePathTree, JRTree fileSystemTree, MainView mainWindow) {
		this.basePathTree = basePathTree;
		this.fileSystemTree = fileSystemTree;
		this.controller = MainController.getController();
		this.mainWindow = mainWindow;
	}

	/**
	 * Get the selected Tree items from that tree that is currently visible to the user
	 */
	public List<IResourceHandler> getSelectedTreeItems() {
		JTree selectedTreeComponent = mainWindow.getSelectedTreePathComponent();
		if(selectedTreeComponent != null) {
			TreePath[] selectionPaths = ((JTree)selectedTreeComponent).getSelectionPaths();
			if(selectionPaths != null) {
				ArrayList<IResourceHandler> result = new ArrayList<>(selectionPaths.length);
				if(selectionPaths != null) {
					for(TreePath selectionPath : selectionPaths) {
						TreeNode targetResource = (TreeNode) selectionPath.getLastPathComponent();
						if(targetResource instanceof FileSystemNode) {
							result.add( ((FileSystemNode)targetResource).getResource() );
						}
					}
				}
				return result;
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Expands the nodes for the given {@link IResourceHandler} instances in this
	 * tree that is currently be shown to the user.
	 */
	public void addExpandedTreeItems(final List<IResourceHandler> resourceHandlers) {
		JTree selectedComponent = mainWindow.getSelectedTreePathComponent();
		for(IResourceHandler resourceHandler : resourceHandlers) {
			TreeModel model = ((JTree) selectedComponent).getModel();

			List<String> pathSegments = resourceHandler.getPathSegments();
			List<String> fullPathSegments = new ArrayList<>(pathSegments.size());
			for(int i = 0; i < pathSegments.size(); i++) {
				if(ReflectionUtils.getOS() == ReflectionUtils.OS_WINDOWS) {
					List<String> extract = ListUtils.extract(pathSegments, 0, i + 1);
					if(i == 0) {
						extract.set(0, extract.get(0) + File.separator);
					}
					String join = ListUtils.join(extract, File.separator);
					fullPathSegments.add(join);
				} else {
					List<String> extract = ListUtils.extract(pathSegments, 1, i + 1);
					String join = ListUtils.join(extract, File.separator);
					fullPathSegments.add(File.separator + join);
				}
			}

			TreePath lastExpandedRow = null;
			if(model instanceof FileSystemTreeModel) {
				lastExpandedRow = restoreExpansionState(selectedComponent, fullPathSegments);
			} else if(model instanceof BasePathTreeModel) {
				lastExpandedRow = ((BasePathTreeModel) model).restoreExpansionState((JTree) selectedComponent, resourceHandler, fullPathSegments);
			}

			if(lastExpandedRow != null) {
				((JRTree) selectedComponent).scrollPathToVisibleVertical(lastExpandedRow, true);
			}
			if(controller.getSelectedEbookPropertyItemRows().length > 0) {
				controller.getEbookTableHandler().clearSelection();
			}
		}
	}
	
	public TreePath restoreExpansionState(JTree tree, List<String> fullPathSegments) {
		String treeExpansionPathString = ListUtils.join(fullPathSegments, TreeUtil.PATH_SEPARATOR);
		TreePath lastExpandedRow = TreeUtil.restoreExpanstionState(tree, treeExpansionPathString);
		return lastExpandedRow;
	}	

	/**
	 * Collapse all tree nodes in the tree with the given name.
	 */
	public void collapseAllTreeNodes(String name) {
		JRTree collpase = null;
		if(basePathTree.getName().equals(name)) {
			collpase = basePathTree;
		} else if(fileSystemTree.getName().equals(name)) {
			collpase = fileSystemTree;
		}

		if(collpase != null) {
			collpase.clearSelection();
			collpase.stopEditing();
			int rowCount = collpase.getRowCount();
			for(int i = rowCount; i > 0 ; i--) {
				collpase.collapseRow(i - 1);
			}
		}
	}
	
	/**
	 * Refresh the Tree for the base path's.
	 */
	public void refreshBasePathTree() {
		final TreeSelectionModel selectionModel = basePathTree.getSelectionModel();
		final TreePath selectionPath = basePathTree.getSelectionPath();

		final String expansionStates = TreeUtil.getExpansionStates(basePathTree);
		final BasePathTreeModel basePathTreeModel = (BasePathTreeModel) basePathTree.getModel();
		basePathTree.stopEditing();
		((BasePathTreeModel)basePathTreeModel).reload();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				TreeUtil.restoreExpanstionState(basePathTree, expansionStates);
				selectionModel.setSelectionPath(selectionPath);
			}
		});
	}

	/**
	 * Refresh the Tree for the file system. Only the node and it child's for the given {@link IResourceHandler} will be refreshed.
	 */
	public void refreshFileSystemTreeEntry(IResourceHandler resourceToRefresh) {
		final TreeModel fileSystemTreeModel = fileSystemTree.getModel();
		final TreeModel basePathTreeModel = basePathTree.getModel();

		if(fileSystemTreeModel instanceof FileSystemTreeModel) {
			if(!resourceToRefresh.exists() || resourceToRefresh.isFileResource()) {
				resourceToRefresh = resourceToRefresh.getParentResource();
			}
			final String expansionStates = TreeUtil.getExpansionStates(fileSystemTree);
			fileSystemTree.stopEditing();
			((FileSystemTreeModel) fileSystemTreeModel).reload(resourceToRefresh, fileSystemTree.getPathForRows());
			((BasePathTreeModel) basePathTreeModel).reload(resourceToRefresh, basePathTree.getPathForRows());

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					TreeUtil.restoreExpanstionState(fileSystemTree, expansionStates);
				}
			});
		}
	}

	/**
	 * Get the selection count of the tree component which currently have the focus.
	 * @return The number of selected items.
	 */
	public int getTreeItemSelectionCount() {
		JTree selectedTreePathComponent = mainWindow.getSelectedTreePathComponent();
		return selectedTreePathComponent.getSelectionModel().getSelectionCount();
	}
}
