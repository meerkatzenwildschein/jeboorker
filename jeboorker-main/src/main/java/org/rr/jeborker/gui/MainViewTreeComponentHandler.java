package org.rr.jeborker.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.swing.SwingUtils;
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
		return getSelectedResources(selectedTreeComponent);
	}

	private List<IResourceHandler> getSelectedResources(JTree selectedTreeComponent) {
		if (selectedTreeComponent != null) {
			TreePath[] selectionPaths = ((JTree) selectedTreeComponent).getSelectionPaths();
			if (selectionPaths != null) {
				ArrayList<IResourceHandler> result = new ArrayList<>(selectionPaths.length);
				if (selectionPaths != null) {
					for (TreePath selectionPath : selectionPaths) {
						TreeNode targetResource = (TreeNode) selectionPath.getLastPathComponent();
						if (targetResource instanceof FileSystemNode) {
							result.add(((FileSystemNode) targetResource).getResource());
						}
					}
				}
				return result;
			}
		}
		return Collections.emptyList();
	}
	
	/**
	 * Get the selected Tree items from the file system tree.
	 */
	public List<IResourceHandler> getSelectedFileSystemTreeItems() {
		return getSelectedResources(fileSystemTree);
	}
	
	/**
	 * Tells if the file system view is currently visible to the user.
	 */
	public boolean isFileTreeSelected() {
		JTree selectedComponent = mainWindow.getSelectedTreePathComponent();
		TreeModel model = selectedComponent.getModel();
		if(model instanceof FileSystemTreeModel) {
			return true;
		}
		return false;
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
					fullPathSegments.add(attachFileSeparator(join));
				} else {
					List<String> extract = ListUtils.extract(pathSegments, 1, i + 1);
					String join = ListUtils.join(extract, File.separator);
					fullPathSegments.add(File.separator + attachFileSeparator(join));
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
	
	private String attachFileSeparator(String fileName) {
		if(fileName != null && !fileName.isEmpty() && !fileName.endsWith(File.separator)) {
			return fileName + File.separator;
		}
		return fileName;
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
	
	public void refreshFileSystemTreeEntry(IResourceHandler resourceToRefresh) {
		refreshFileSystemTreeEntry(resourceToRefresh, fileSystemTree, basePathTree);
	}

	/**
	 * Refresh the Tree for the file system. Only the node and it child's for the given {@link IResourceHandler} will be refreshed.
	 */
	public static void refreshFileSystemTreeEntry(IResourceHandler resourceToRefresh, final JRTree fileSystemTree, final JRTree basePathTree) {
		final TreeModel fileSystemTreeModel = fileSystemTree.getModel();

		if (fileSystemTreeModel instanceof FileSystemTreeModel) {
			if (!resourceToRefresh.exists() || resourceToRefresh.isFileResource()) {
				resourceToRefresh = resourceToRefresh.getParentResource();
			}
			final int scrollBarLocation = getVerticalScrollBarLocation(fileSystemTree);
			final String expansionStates = TreeUtil.getExpansionStates(fileSystemTree);
			fileSystemTree.stopEditing();
			((FileSystemTreeModel) fileSystemTreeModel).reload(resourceToRefresh, fileSystemTree.getPathForRows());
			
			if(basePathTree != null) {
				TreeModel basePathTreeModel = basePathTree.getModel();
				((BasePathTreeModel) basePathTreeModel).reload(resourceToRefresh, basePathTree.getPathForRows());
			}

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					TreeUtil.restoreExpanstionState(fileSystemTree, expansionStates);
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							JScrollPane surroundingScrollPane = SwingUtils.getSurroundingScrollPane(fileSystemTree);
							if (scrollBarLocation > 0) {
								surroundingScrollPane.getVerticalScrollBar().setValue(scrollBarLocation);
							}
						}
					});
				}
			});
		}
	}

	private static int getVerticalScrollBarLocation(JRTree fileSystemTree) {
		final int scrollbarLocation;
		JScrollPane surroundingScrollPane = SwingUtils.getSurroundingScrollPane(fileSystemTree);
		if(surroundingScrollPane != null) {
			scrollbarLocation = surroundingScrollPane.getVerticalScrollBar().getValue();
		} else {
			scrollbarLocation = -1;
		}
		return scrollbarLocation;
	}
}
