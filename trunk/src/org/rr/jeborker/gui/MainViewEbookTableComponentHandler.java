package org.rr.jeborker.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;

import org.rr.commons.swing.components.JRTable;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;

public class MainViewEbookTableComponentHandler {

	private JRTable mainTable;
	private JScrollPane mainTableScrollPane;

	public MainViewEbookTableComponentHandler(JRTable mainTable, JScrollPane mainTableScrollPane) {
		this.mainTable = mainTable;
		this.mainTableScrollPane = mainTableScrollPane;
	}

	public void clearSelection() {
		mainTable.clearSelection();
	}

	public void editingStopped(ChangeEvent changeEvent) {
		mainTable.editingStopped(changeEvent);
	}

	public void setModel(EbookPropertyDBTableModel ebookPropertyDBTableModel) {
		mainTable.setModel(ebookPropertyDBTableModel);
	}

	public void stopEdit() {
		mainTable.stopEdit();
	}
	
	public EbookPropertyDBTableModel getModel() {
		return (EbookPropertyDBTableModel) mainTable.getModel();
	}
	
	public void refreshTableItem(int[] selectedRows) {
		final EbookPropertyDBTableModel model = getModel();
		if(selectedRows==null || selectedRows.length == 0) {
			return;
		} else {
			int editingRow = mainTable.getEditingRow();
			for (int i = 0; i < selectedRows.length; i++) {
				if(editingRow != -1 && editingRow == selectedRows[i]) {
					mainTable.stopEdit();
				}

				model.reloadEbookPropertyItemAt(selectedRows[i]);
				mainTable.tableChanged(new TableModelEvent(model, selectedRows[i]));
			}
		}
	}
	
	/**
	 * Refresh the whole table.
	 */
	public void refreshTable() {
		final EbookPropertyDBTableModel model = (EbookPropertyDBTableModel) mainTable.getModel();
		if(model instanceof EbookPropertyDBTableModel) {
			((EbookPropertyDBTableModel)model).setDirty();
		}

		if(mainTable.isEditing()) {
			mainTable.stopEdit();
		}

		mainTable.tableChanged(new TableModelEvent(model));
		if(mainTableScrollPane != null) {
			mainTableScrollPane.getVerticalScrollBar().setValue(0);
		}
	}
	
	/**
	 * Refresh the given table rows.
	 * @param rows The rows to be refreshed.
	 * @param refreshMetadataSheet do also refresh the metadata sheet.
	 */
	public void refreshTableRows(final int[] rows) {
		final EbookPropertyDBTableModel model = getModel();
		if (rows == null || rows.length == 0) {
			return;
		} else {
			int editingRow = mainTable.getEditingRow();
			for (int i = 0; i < rows.length; i++) {
				if (editingRow != -1 && editingRow == rows[i]) {
					mainTable.editingStopped(null);
				}
				model.reloadEbookPropertyItemAt(rows[i]);
				mainTable.tableChanged(new TableModelEvent(model, rows[i]));
			}
		}
	}

	/**
	 * Gets all selected rows from the main table.
	 * @return all selected rows or an empty array if no row is selected. Never returns <code>null</code>.
	 */
	public int[] getSelectedRows() {
		if (mainTable != null) {
			return mainTable.getSelectedRows();
		} else {
			return new int[0];
		}
	}
	
	/**
	 * Gets all selected items from the main table.
	 * @return The selected items. Never returns <code>null</code>.
	 */
	public List<EbookPropertyItem> getSelectedEbookPropertyItems() {
		final int[] selectedRows = getSelectedRows();
		final ArrayList<EbookPropertyItem> result = new ArrayList<>(selectedRows.length);
		for (int i = 0; i < selectedRows.length; i++) {
			EbookPropertyItem valueAt = (EbookPropertyItem) getModel().getValueAt(selectedRows[i], 0);
			result.add(valueAt);
		}

		return result;
	}
	
	/**
	 * Gets all selected rows from the main table.
	 * @return all selected rows or an empty array if no row is selected. Never returns <code>null</code>.
	 */
	public int[] getSelectedEbookPropertyItemRows() {
		if (mainTable != null) {
			final int[] selectedRows = mainTable.getSelectedRows();
			return selectedRows;
		} else {
			return new int[0];
		}
	}

	public void repaint() {
		mainTable.repaint();
	}

	public int getSelectedRowCount() {
		return mainTable.getSelectedRowCount();
	}

}
