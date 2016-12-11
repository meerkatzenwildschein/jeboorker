package org.rr.jeborker.gui;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JTree;

import org.rr.commons.collection.TransformValueList;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.db.item.EbookPropertyItem;

public class MainViewSelectionUtils {

	/**
	 * Tells if the {@link JTree} component which currently have the focus hold one selection.
	 * @return <code>true</code> if there is a selection to the current {@link JTree}.
	 */
	static boolean isTreeItemSingleSelection() {
		return getTreeItemSelectionCount() == 1;
	}

	/**
	 * Tells if the {@link JTree} component which currently have the focus hold one or more selections.
	 * @return <code>true</code> if there is a selection to the current {@link JTree}.
	 */
	static boolean isTreeItemSelection() {
		return getTreeItemSelectionCount() > 0;
	}

	/**
	 * Get the selection count of the tree component which currently have the focus.
	 * @return The number of selected items.
	 */
	static int getTreeItemSelectionCount() {
		return MainController.getController().getMainTreeHandler().getSelectedTreeItems().size();
	}

	/**
	 * Tells if the main table component hold one selection.
	 * @return <code>true</code> if there is one selection on the main table.
	 */
	static boolean isMainTableSingleSelection() {
		return getMainTableSelectionCount() == 1;
	}

	/**
	 * Tells if the main table component hold more than one selection.
	 * @return <code>true</code> if there is more than one selection on the main table.
	 */
	static boolean isMainTableMultiSelection() {
		return getMainTableSelectionCount() > 1;
	}
	
	/**
	 * Tells if the main table component hold one or more selections.
	 * @return <code>true</code> if there is a selection on the main table.
	 */
	static boolean isMainTableSelection() {
		return getMainTableSelectionCount() > 0;
	}

	/**
	 * Get the selection count of the main table which shows the ebooks.
	 * @return The number of selected items.
	 */
	static int getMainTableSelectionCount() {
		return MainController.getController().getEbookTableHandler().getSelectedRowCount();
	}
	
	static Collection<String> getMimeTypesFromSelection() {
		List<EbookPropertyItem> selectedEbookPropertyItems = MainController.getController().getEbookTableHandler().getSelectedEbookPropertyItems();
		TransformValueList<EbookPropertyItem, String> mimeTypesFromSelection = new TransformValueList<EbookPropertyItem, String>(selectedEbookPropertyItems) {

			@Override
			public String transform(EbookPropertyItem source) {
				return source.getMimeType();
			}
		};
		return new HashSet<String>(mimeTypesFromSelection);
	}

	/**
	 * Tells if there is a directory selected.
	 * @return <code>true</code> if there is a directory selection and <code>false</code> otherwise.
	 */
	static boolean isDirectorySelectionIncluded() {
		List<IResourceHandler> selectedTreeItems = MainController.getController().getMainTreeHandler().getSelectedTreeItems();
		for(IResourceHandler selectedTreeItem : selectedTreeItems) {
			if(selectedTreeItem.isDirectoryResource()) {
				break;
			}
		}
		return false;
	}
}
