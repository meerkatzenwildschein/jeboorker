package org.rr.jeborker.gui.cell;

import java.awt.Component;
import java.io.Serializable;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.IMetadataReader.METADATA_TYPES;
import org.rr.jeborker.remote.metadata.MetadataDownloadEntry;

public class MetadataDownloadTableCellEditor implements TableCellEditor, Serializable {
	
	private final MetadataDownloadTableCellRenderer renderer;


	private MetadataDownloadEntry editingEntry;


	private Map<METADATA_TYPES, List<Entry<JCheckBox, String>>> editingValues;
	
	public MetadataDownloadTableCellEditor(MetadataDownloadTableCellRenderer renderer) {
		super();
		this.renderer = renderer;
	}
	
	/**
	 * Get the MetadataDownloadEntry for the editing component.
	 */
	public MetadataDownloadEntry getEditorMetadataDownloadEntry() {
		return editingEntry;
	}
	
	/**
	 * Get the values for the editing component.
	 */
	public Map<IMetadataReader.METADATA_TYPES, List<Map.Entry<JCheckBox, String>>> getEditingValues() {
		return editingValues;
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
		this.editingValues = renderer.getEditingValues();
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

	@Override
	public Object getCellEditorValue() {
		return this.editingValues;
	}

}
