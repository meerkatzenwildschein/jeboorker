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

	private MetadataDownloadEntry editingEntry;
	
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
	
	/**
	 * Get the MetadataDownloadEntry for the editing component.
	 */
	public MetadataDownloadEntry getEditorMetadataDownloadEntry() {
		return editingEntry;
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
		this.editingEntry = renderer.getEditingMetadataDownloadEntry();
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
	
	/**
	 * Tells if the user has selected the cover image checkbox for set/replace
	 * the cover of the ebook file with the downloaded one. 
	 */
	public boolean isCoverImageChecked() {
		return renderer.isCoverImageChecked();
	}	

}
