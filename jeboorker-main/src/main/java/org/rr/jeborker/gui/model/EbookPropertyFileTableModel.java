package org.rr.jeborker.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.commons.lang.ObjectUtils;
import org.rr.commons.collection.BlindElementList;
import org.rr.commons.collection.CacheValueList;
import org.rr.commons.collection.CompoundList;
import org.rr.commons.collection.TransformValueList;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;

public class EbookPropertyFileTableModel extends AbstractEbookPropertyTableModel {
	
	private List<EbookPropertyItem> ebookPropertyItems;
	
	/** List of listeners */
	protected EventListenerList listenerList = new EventListenerList();
	
	public EbookPropertyFileTableModel(List<IResourceHandler> resources) {
		ebookPropertyItems = new CacheValueList<EbookPropertyItem>(new TransformValueList<IResourceHandler, EbookPropertyItem>(resources) {

			@Override
			public EbookPropertyItem transform(IResourceHandler fileResource) {
				return EbookPropertyItemUtils.createEbookPropertyItem(fileResource, fileResource.getParentResource());
			}
		});
	}
	
	/**
	 * Adds a listener to the list that's notified each time a change to the data model occurs.
	 *
	 * @param l the TableModelListener
	 */
	@Override
	public void addTableModelListener(TableModelListener l) {
		listenerList.add(TableModelListener.class, l);

	}
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return Bundle.getString("EbookPropertyDBTableModel.headline");
	}

	@Override
	public int getRowCount() {
		return ebookPropertyItems.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return ebookPropertyItems.get(rowIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Removes a listener from the list that's notified each time a change to the data model occurs.
	 *
	 * @param l the TableModelListener
	 */
	public void removeTableModelListener(TableModelListener l) {
		listenerList.remove(TableModelListener.class, l);
	}

	@Override
	public void setValueAt(Object arg0, int row, int column) {
		fireTableCellUpdated(row, column);
	}
	
	/**
	 * Notifies all listeners that the value of the cell at <code>[row, column]</code> has been updated.
	 *
	 * @param row row of cell which has been updated
	 * @param column column of cell which has been updated
	 * @see TableModelEvent
	 * @see EventListenerList
	 */
	public void fireTableCellUpdated(int row, int column) {
		fireTableChanged(new TableModelEvent(this, row, row, column));
	}
	
	/**
	 * Forwards the given notification event to all <code>TableModelListeners</code> that registered themselves as listeners for this table
	 * model.
	 *
	 * @param e the event to be forwarded
	 *
	 * @see #addTableModelListener
	 * @see TableModelEvent
	 * @see EventListenerList
	 */
	public void fireTableChanged(TableModelEvent e) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TableModelListener.class) {
				((TableModelListener) listeners[i + 1]).tableChanged(e);
			}
		}
	}
	
	/**
	 * Notifies all listeners that rows in the range <code>[firstRow, lastRow]</code>, inclusive, have been inserted.
	 *
	 * @param firstRow the first row
	 * @param lastRow the last row
	 *
	 * @see TableModelEvent
	 * @see EventListenerList
	 */
	public void fireTableRowsInserted(int firstRow, int lastRow) {
		fireTableChanged(new TableModelEvent(this, firstRow, lastRow + 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}
	
	/**
	 * Notifies all listeners that rows in the range <code>[firstRow, lastRow]</code>, inclusive, have been deleted.
	 *
	 * @param firstRow the first row
	 * @param lastRow the last row
	 *
	 * @see TableModelEvent
	 * @see EventListenerList
	 */
	public void fireTableRowsDeleted(int firstRow, int lastRow) {
		fireTableChanged(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
	}

	@Override
	public void reloadEbookPropertyItemAt(int rowIndex) {
		IResourceHandler resource = ebookPropertyItems.get(rowIndex).getResourceHandler();
		ArrayList<EbookPropertyItem> ebookPropertyItems = new ArrayList<>(this.ebookPropertyItems);
		ebookPropertyItems.set(rowIndex, EbookPropertyItemUtils.createEbookPropertyItem(resource, resource.getParentResource()));
	}
	

	@Override
	public EbookPropertyItem getEbookPropertyItemAt(int rowIndex) {
		return ebookPropertyItems.get(rowIndex);
	}
	@Override
	public void addRow(EbookPropertyItem item, int row) {
		ebookPropertyItems = new CompoundList<>(ebookPropertyItems, Collections.singletonList(item));
		fireTableRowsInserted(ebookPropertyItems.size() -1, ebookPropertyItems.size());
	}

	@Override
	public int searchRow(EbookPropertyItem item) {
		for (int i = 0; i < ebookPropertyItems.size(); i++) {
			EbookPropertyItem ebookPropertyItem = ebookPropertyItems.get(i);
			if(ObjectUtils.equals(item, ebookPropertyItem)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean removeRow(EbookPropertyItem item) {
		int row = searchRow(item);
		if(row >= 0) {
			ebookPropertyItems = new BlindElementList<>(ebookPropertyItems, row);
			fireTableRowsDeleted(row, row);
			return true;
		}
		
		return false;
	}

}
