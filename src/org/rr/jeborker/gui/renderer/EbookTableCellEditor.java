package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;

public class EbookTableCellEditor extends EbookTableCellComponent implements TableCellEditor, Serializable {
	
	private static final long serialVersionUID = -6770247928468695828L;
	
	private EbookPropertyItem value;
	
	public EbookTableCellEditor() {
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
		if(starRater.getSelection() > 0) {
			MainController.getController().setRatingToSelectedEntry(starRater.getSelection() * 2);
		}
		starRater.setSelection(0);
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
		this.value = (EbookPropertyItem) value;
		final Component tableCellComponent = super.getTableCellComponent(table, value, true, true, row, column);

		return tableCellComponent;
	}

}
