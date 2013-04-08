package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import org.rr.jeborker.remote.metadata.MetadataDownloadEntry;

public class MetadataDownloadTableCellEditor implements TableCellEditor, Serializable {
	
	private MetadataDownloadTableCellRenderer renderer = new MetadataDownloadTableCellRenderer();
	
	private MetadataDownloadEntry value;
	
	public MetadataDownloadTableCellEditor() {
		super();
	}
	
	@Override
	public Object getCellEditorValue() {
		return value;
	}

	@Override
	public boolean isCellEditable(EventObject anEvent) {
		return true;
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}

	@Override
	public boolean stopCellEditing() {
		return true;
	}

	@Override
	public void cancelCellEditing() {
	}

	@Override
	public void addCellEditorListener(CellEditorListener l) {
	}

	@Override
	public void removeCellEditorListener(CellEditorListener l) {
	}

	@Override
	public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
		this.value = (MetadataDownloadEntry) value;
		final Component tableCellComponent = renderer.getTableCellComponent(table, (MetadataDownloadEntry) value, true, true, row, column);

		return tableCellComponent;
	}

}
