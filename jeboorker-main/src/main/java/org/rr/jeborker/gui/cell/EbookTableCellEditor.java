package org.rr.jeborker.gui.cell;

import java.awt.Component;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.cell.EbookTableCellRenderer.RendererComponent;

public class EbookTableCellEditor implements TableCellEditor, Serializable {
	
	private static final long serialVersionUID = -6770247928468695828L;
	
	private final EbookTableCellRenderer renderer;
	
	private EditListener editListener;
	
	private EbookPropertyItem value;
	
	private RendererComponent tableCellComponent;
	
	public EbookTableCellEditor(final EditListener editListener) {
		super();
		this.editListener = editListener;
		renderer = new EbookTableCellRenderer();
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
		if(tableCellComponent.getStarRaterSelection() > 0) {
			MainController.getController().setRatingToSelectedEntry(tableCellComponent.getStarRaterSelection() * 2);
		}
		tableCellComponent.setStarRaterSelection(0);
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
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		this.value = (EbookPropertyItem) value;
		tableCellComponent = renderer.getTableCellComponent(table, value, true, true, row, column);
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
