package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.io.Serializable;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import org.rr.jeborker.metadata.IMetadataReader.METADATA_TYPES;
import org.rr.jeborker.remote.metadata.MetadataDownloadEntry;

public class MetadataDownloadTableCellEditor implements TableCellEditor, Serializable {
	
	private final MetadataDownloadTableCellRenderer renderer;
	
	private HashMap<METADATA_TYPES, JCheckBox> editingCheckboxValues;
	
	private HashMap<METADATA_TYPES, String> editingTextValues;
	
	public MetadataDownloadTableCellEditor(MetadataDownloadTableCellRenderer renderer) {
		super();
		this.renderer = renderer;
	}
	
	@Override
	public Object getCellEditorValue() {
		return editingCheckboxValues;
	}

	/**
	 * Get the checkboxes which tells if a metadata entry should be set to the ebook or not. 
	 */
	public HashMap<METADATA_TYPES, JCheckBox> getEditorCheckboxValue() {
		return editingCheckboxValues;
	}
	
	/**
	 * Get the downloaded metadata text values
	 */
	public HashMap<METADATA_TYPES, String> getEditorTextValue() {
		return editingTextValues;
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
		this.editingCheckboxValues = renderer.getEditingCheckboxValues();
		this.editingTextValues = renderer.getEditingTextValues();
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
		final Component tableCellComponent = renderer.getTableCellComponent(table, (MetadataDownloadEntry) value, true, true, row, column);
		return tableCellComponent;
	}

}
