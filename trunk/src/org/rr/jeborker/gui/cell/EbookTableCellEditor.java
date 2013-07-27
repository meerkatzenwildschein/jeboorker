package org.rr.jeborker.gui.cell;

import java.awt.Component;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;

public class EbookTableCellEditor implements TableCellEditor, Serializable {
	
	private static final long serialVersionUID = -6770247928468695828L;
	
	private EbookTableCellRenderer renderer = new EbookTableCellRenderer();
	
	private EditListener editListener;
	
	private EbookPropertyItem value;
	
	public EbookTableCellEditor(final EditListener editListener) {
		super();
		this.editListener = editListener;
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
		if(editListener != null) {
			editListener.editingStoped();
		}
		if(renderer.starRater.getSelection() > 0) {
			MainController.getController().setRatingToSelectedEntry(renderer.starRater.getSelection() * 2);
		}
		renderer.starRater.setSelection(0);
		return true;
	}

	@Override
	public void cancelCellEditing() {
		if(editListener != null) {
			editListener.editingCanceled();
		}
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
		final Component tableCellComponent = renderer.getTableCellComponent(table, value, true, true, row, column);
		if(editListener != null) {
			editListener.editingStarted();
		}
		return tableCellComponent;
	}
	
	public interface EditListener {
		void editingStarted();
		void editingStoped();
		void editingCanceled();
		
	}
}
