package org.rr.jeborker.gui.model;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.rr.commons.collection.BlindElementList;
import org.rr.commons.collection.CompoundList;
import org.rr.commons.collection.ICloseableList;
import org.rr.commons.collection.InsertElementList;
import org.rr.commons.collection.ReplacementElementList;
import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.app.FileRefreshBackground;
import org.rr.jeborker.db.DBUtils;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.OrderDirection;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;

import com.j256.ormlite.stmt.NullArgHolder;
import com.j256.ormlite.stmt.Where;

public class EbookPropertyDBTableModel extends AbstractEbookPropertyTableModel {
	
	public abstract static class EbookPropertyDBTableModelQuery {
		public boolean isVolatile() {
			return false;
		}
		
		public void appendQuery(Where<EbookPropertyItem, EbookPropertyItem> where) throws SQLException {
			where.raw("true = true", new NullArgHolder[0]);
		};
		
		public void appendKeyword(List<String> keyword) {};
		
		public abstract String getIdentifier();

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			} else if(obj instanceof EbookPropertyDBTableModelQuery) {
				return ((EbookPropertyDBTableModelQuery)obj).getIdentifier().equals(this.getIdentifier());
			}
			return false;
		}
		
	};
	
	/** List of listeners */
	private EventListenerList listenerList = new EventListenerList();

	private ICloseableList<EbookPropertyItem> dbItems;

	private List<EbookPropertyItem> allItems;

	private final List<EbookPropertyDBTableModelQuery> whereConditions = new ArrayList<>();

	private final List<Field> orderByColumns = new ArrayList<>();

	private OrderDirection orderDirection = null;

	private boolean dirty = false;

	private int oldSize = 0;

	private boolean emptyModel = false;

	public EbookPropertyDBTableModel(boolean emptyModel) {
		super();
		this.emptyModel = emptyModel;
	}

	public EbookPropertyDBTableModel(EbookPropertyDBTableModel copy, boolean emptyModel) {
		super();
		this.emptyModel = emptyModel;
		setOrderByColumns(copy.getOrderByColumns());
		setOrderDirection(copy.getOrderDirection());
		whereConditions.addAll(copy.whereConditions);
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
		final List<EbookPropertyItem> ebookItems = this.getEbookItems();
		if(ebookItems != null) {
			final int size = ebookItems.size();
			if(size > oldSize) {
				final int old = oldSize;
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						fireTableRowsInserted(old-1, size-1);
					}
				});
				
			}
			this.oldSize = size;
			return size;
		} else {
			return 0;
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		List<EbookPropertyItem> ebookItems = this.getEbookItems();
		final EbookPropertyItem ebookPropertyItem;
		try {
			if(ebookItems.size() < rowIndex) {
				setDirty();
				ebookItems = this.getEbookItems();
			}
			ebookPropertyItem = ebookItems.get(rowIndex);
		} catch(IndexOutOfBoundsException ex) {
			return null;
		}
		
		if(ebookPropertyItem != null) {
			FileRefreshBackground.getInstance().addEbook(ebookPropertyItem);
			switch(columnIndex) {
				case 0:
					return ebookPropertyItem;
			}
		}
		return null;
	}
	
	/**
	 * Gets the {@link EbookPropertyItem} displayed in the given row.
	 * @return The desired {@link EbookPropertyItem} or possibly <code>null</code>.
	 */
	public EbookPropertyItem getEbookPropertyItemAt(int rowIndex) {
		final List<EbookPropertyItem> ebookItems = this.getEbookItems();
		try {
			return ebookItems.get(rowIndex);
		} catch(IndexOutOfBoundsException ex) {
			LoggerFactory.logInfo(this, EMPTY, ex);
			return null;
		}
	}
	
	/**
	 * Loads the {@link EbookPropertyItem} element at the given index from the database
	 * and set this instance as current one.
	 * @param rowIndex Index of {@link EbookPropertyItem} to be reloaded.
	 */
	public void reloadEbookPropertyItemAt(int rowIndex) {
		final List<EbookPropertyItem> ebookItems = this.getEbookItems();
		if(rowIndex >= 0) {
			final EbookPropertyItem modelItem = this.getEbookPropertyItemAt(rowIndex);
			if(modelItem != null) {
				EbookPropertyItem item = EbookPropertyItemUtils.reloadEbookPropertyItem(modelItem);
				
				if(item != null) {
					EbookPropertyItem dbItem = item;
					if(dbItem.getFile().equals(modelItem.getFile())) {
						this.allItems = new ReplacementElementList<>(ebookItems, rowIndex, dbItem);
					}
				}
			}
		}
	}
	
	public int searchRow(EbookPropertyItem item) {
		if(item == null) {
			return -1;
		}
		
		final List<EbookPropertyItem> ebookItems = this.getEbookItems();
		for(int i = 0; i < ebookItems.size(); i++) {
			EbookPropertyItem ebookPropertyItem = ebookItems.get(i);
			if(ebookPropertyItem.equals(item)) {
				return i;
			}
		}
		
		return -1;
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
	public void setValueAt(Object aValue, int row, int column) {
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

	/**
	 * Get the {@link EbookPropertyItem}s to be handled by this model.
	 * 
	 * @param refresh <code>true</code> if the data should be refreshed and <code>false</code> for using them from cache.
	 * @return The desired {@link EbookPropertyItem}s.
	 */
	private List<EbookPropertyItem> getEbookItems() {
		// NO BREAKPOINT HERE!
		if (emptyModel) {
			this.allItems = Collections.emptyList();
		} else if (isDirty() || dbItems == null) {
			this.dirty = false;
			Where<EbookPropertyItem, EbookPropertyItem> whereConditions = prepareQuery();
			List<String> keywords = prepareKeywords();
			ICloseableList<EbookPropertyItem> items = DefaultDBManager.getInstance().queryFullTextSearch(EbookPropertyItem.class, whereConditions,
					keywords, getOrderByColumns(), getOrderDirection());
			clearVolatileConditions();

			if (this.dbItems != null) {
				this.dbItems.close();
			}
			this.dbItems = items;
			List<EbookPropertyItem> addedItems = new ArrayList<>();
			this.allItems = new CompoundList<>(dbItems, addedItems);
		}
		return this.allItems;
	}

	private List<String> prepareKeywords() {
		ArrayList<String> result = new ArrayList<>();
		for (EbookPropertyDBTableModelQuery whereCondition : whereConditions) {
			whereCondition.appendKeyword(result);
		}
		return result;
	}

	private Where<EbookPropertyItem, EbookPropertyItem> prepareQuery() {
		List<EbookPropertyDBTableModelQuery> toRemove = new ArrayList<>();
		Where<EbookPropertyItem, EbookPropertyItem> where = DefaultDBManager.getInstance().getQueryBuilder(EbookPropertyItem.class).where();
		for (EbookPropertyDBTableModelQuery whereCondition : whereConditions) {
			try {
				if (!DBUtils.isEmpty(where)) {
					where.and();
				}
				whereCondition.appendQuery(where);
			} catch (SQLException e) {
				LoggerFactory.log(Level.SEVERE, this, "Failed to prepare Query", e);
			}
			if (whereCondition.isVolatile()) {
				toRemove.add(whereCondition);
			}
		}
		whereConditions.removeAll(toRemove);
		return where;
	}

	/**
	 * Attaches an {@link EbookPropertyItem} to the specified row. If the row parameter is -1 the value is added to the end of the list.
	 * 
	 * @param item The item to be attached.
	 * @param row The row where the {@link EbookPropertyItem} should be added to.
	 */
	public void addRow(EbookPropertyItem item, int row) {
		if (row < 0) {
			boolean isInsert = false;
			try {
				isInsert = this.allItems.add(item);
			} catch (java.lang.UnsupportedOperationException e) {
				// not allowed to add
				this.allItems = new CompoundList<EbookPropertyItem>(this.allItems, new ArrayList<>(Arrays.asList(item)));
				isInsert = true;
			}
			if (isInsert) {
				final int ins = this.allItems.size() - 1;
				fireTableRowsInserted(ins, ins);
			}
		} else {
			this.allItems = new InsertElementList<>(this.allItems, item, row);
			fireTableRowsInserted(row, row);
		}
	}

	public boolean removeRow(final EbookPropertyItem item) {
		final Iterator<EbookPropertyItem> iterator = this.allItems.iterator();
		boolean removed = false;
		for (int i = 0; iterator.hasNext(); i++) {
			final EbookPropertyItem toRemove = iterator.next();
			try {
				if (toRemove != null && toRemove.equals(item)) {
					DefaultDBManager.getInstance().deleteObject(toRemove);
					if (!isDirty()) {
						this.allItems = new BlindElementList<>(this.allItems, i);
						fireTableRowsDeleted(i, i);
					}
					removed = true;
					break;
				}
			} catch (Exception e) {
				this.setDirty();
				break;
			}
		}
		return removed;
	}

	/**
	 * @return <code>true</code> if a refresh is needed and <code>false</code> otherwise.
	 * @see #setDirty()
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Tells the model that the data has changed and next time the data should be refetched from the database.
	 * 
	 * @see #isDirty()
	 */
	public void setDirty() {
		this.dirty = true;
	}

	public List<Field> getOrderByColumns() {
		return orderByColumns;
	}

	/**
	 * Set the order by columns to the table model. To apply the new order invoke the {@link #setDirty()} method or directly do a
	 * <code>MainController.getController().refreshTable(true);</code>
	 *
	 * @param orderByColumns Order columns to be set to the model. Previously set order columns will be removed.
	 */
	public void setOrderByColumns(List<Field> orderByColumns) {
		this.orderByColumns.clear();
		this.orderByColumns.addAll(orderByColumns);
	}

	/**
	 * Gets the current order direction. Ascending order is the default one.
	 * 
	 * @return The current order direction.
	 */
	public OrderDirection getOrderDirection() {
		if (this.orderDirection == null) {
			this.orderDirection = new OrderDirection(OrderDirection.DIRECTION_ASC);
		}
		return this.orderDirection;
	}

	/**
	 * Sets the order direction for the displayed data.
	 * 
	 * @param orderDirection The order direction to be used for the data.
	 */
	public void setOrderDirection(final OrderDirection orderDirection) {
		this.orderDirection = orderDirection;
	}

	/**
	 * Remove all conditions marked as volatile conditions.
	 */
	private void clearVolatileConditions() {
		List<EbookPropertyDBTableModelQuery> toRemove = new ArrayList<EbookPropertyDBTableModel.EbookPropertyDBTableModelQuery>();
		for (EbookPropertyDBTableModelQuery whereCondition : whereConditions) {
			if (whereCondition.isVolatile()) {
				toRemove.add(whereCondition);
			}
		}
		whereConditions.removeAll(toRemove);
	}

	/**
	 * Adds a {@link EbookPropertyDBTableModelQuery} which will be used to create the model's sql query.
	 */
	public void addWhereCondition(EbookPropertyDBTableModelQuery query) {
		removeWhereCondition(query.getIdentifier());
		whereConditions.add(query);
	}

	/**
	 * Removes a {@link EbookPropertyDBTableModelQuery} which was previously added with the
	 * {@link #addWhereCondition(EbookPropertyDBTableModelQuery)} method.
	 * 
	 * @param identifier The identifier of the {@link EbookPropertyDBTableModelQuery} instance to be removed. the
	 *          {@link EbookPropertyDBTableModelQuery#getIdentifier()} method is used to identify the {@link EbookPropertyDBTableModelQuery}
	 *          which gets removed.
	 * @return <code>true</code> if a condition was removed and <code>false</code> otherwise.
	 */
	public boolean removeWhereCondition(String identifier) {
		List<EbookPropertyDBTableModelQuery> toRemove = new ArrayList<EbookPropertyDBTableModel.EbookPropertyDBTableModelQuery>();
		for (EbookPropertyDBTableModelQuery whereCondition : whereConditions) {
			if (whereCondition.getIdentifier().compareTo(identifier) == 0) {
				toRemove.add(whereCondition);
			}
		}
		return whereConditions.removeAll(toRemove);
	}
	
	/**
	 * Remove all existing where conditions previously defined.
	 */
	public void removeWhereConditions() {
		whereConditions.clear();
	}

}
