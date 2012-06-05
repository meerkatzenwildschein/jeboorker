package org.rr.jeborker.gui.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.rr.commons.collection.CompoundList;
import org.rr.commons.collection.IteratorList;
import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.OrderDirection;
import org.rr.jeborker.db.QueryCondition;
import org.rr.jeborker.db.item.EbookPropertyItem;

public class EbookPropertyDBTableModel implements TableModel {

    /** List of listeners */
    protected EventListenerList listenerList = new EventListenerList();
    
    private List<EbookPropertyItem> dbItems;
    
    private ArrayList<EbookPropertyItem> addedItems;
    
    private CompoundList<EbookPropertyItem> allItems;
    
    private List<Field> orderByColumns = new ArrayList<Field>();
    
    private OrderDirection orderDirection =null;
    
    private QueryCondition queryConditions;
    
    private boolean dirty = false;
    
    public EbookPropertyDBTableModel() {
    	super();
    }
    
    /**
     * Adds a listener to the list that's notified each time a change
     * to the data model occurs.
     *
     * @param l the TableModelListener
     */
    public void addTableModelListener(TableModelListener l) {
        listenerList.add(TableModelListener.class, l);
        
    }

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex) {
			case 0:
				return String.class;
			}
		return String.class;	
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch(columnIndex) {
			case 0:
				return Bundle.getString("EbookPropertyDBTableModel.headline");
		}
		return null;
	}

	@Override
	public int getRowCount() {
		final List<EbookPropertyItem> ebookItems = this.getEbookItems();
		if(ebookItems!=null) {
			return ebookItems.size();
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
			LoggerFactory.logInfo(this, "", ex);
			return null;
		}
		if(ebookPropertyItem!=null) {
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
			LoggerFactory.logInfo(this, "", ex);
			return null;
		}		
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 0) {
			return true;
		}
		return false;
	}

    /**
     * Removes a listener from the list that's notified each time a
     * change to the data model occurs.
     *
     * @param   l               the TableModelListener
     */
    public void removeTableModelListener(TableModelListener l) {
        listenerList.remove(TableModelListener.class, l);
    }

	@Override
	public void setValueAt(Object aValue, int row, int column) {
		fireTableCellUpdated(row, column);
	}
	
    /**
     * Notifies all listeners that the value of the cell at
     * <code>[row, column]</code> has been updated.
     *
     * @param row  row of cell which has been updated
     * @param column  column of cell which has been updated
     * @see TableModelEvent
     * @see EventListenerList
     */
    public void fireTableCellUpdated(int row, int column) {
        fireTableChanged(new TableModelEvent(this, row, row, column));
    }
    
    /**
     * Forwards the given notification event to all
     * <code>TableModelListeners</code> that registered
     * themselves as listeners for this table model.
     *
     * @param e  the event to be forwarded
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
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TableModelListener.class) {
                ((TableModelListener)listeners[i+1]).tableChanged(e);
            }
        }
    } 
    
    /**
     * Notifies all listeners that rows in the range
     * <code>[firstRow, lastRow]</code>, inclusive, have been inserted.
     *
     * @param  firstRow  the first row
     * @param  lastRow   the last row
     *
     * @see TableModelEvent
     * @see EventListenerList
     *
     */
    public void fireTableRowsInserted(int firstRow, int lastRow) {
        fireTableChanged(new TableModelEvent(this, firstRow, lastRow,
                             TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }   
    
    /**
     * Notifies all listeners that rows in the range
     * <code>[firstRow, lastRow]</code>, inclusive, have been deleted.
     *
     * @param firstRow  the first row
     * @param lastRow   the last row
     *
     * @see TableModelEvent
     * @see EventListenerList
     */
    public void fireTableRowsDeleted(int firstRow, int lastRow) {
        fireTableChanged(new TableModelEvent(this, firstRow, lastRow,
                             TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
    }    

    /**
     * Get the {@link EbookPropertyItem}s to be handled by this model.
     * @param refresh <code>true</code> if the data should be refreshed and <code>false</code>
     *  for using them from cache.
     * @return The desired {@link EbookPropertyItem}s.
     */
    public List<EbookPropertyItem> getEbookItems() {
    	if(Jeboorker.isRuntime) {
	    	if(isDirty() || dbItems==null) {
	    		this.dirty = false;
	    		Iterable<EbookPropertyItem> items = DefaultDBManager.getInstance().getItems(EbookPropertyItem.class, this.getQueryCondition(), this.getOrderByColumns(), this.getOrderDirection());
	    		
	    		this.dbItems = new IteratorList<EbookPropertyItem>(items);
	    		if(this.dbItems==null) {
	    			this.dbItems = Collections.emptyList();
	    		}
	    		if(this.addedItems==null) {
	    			this.addedItems = new ArrayList<EbookPropertyItem>();
	    		} else {
	    			this.addedItems.clear();
	    		}
	    		this.allItems = new CompoundList<EbookPropertyItem>(dbItems, addedItems);
	    	}
	    	return this.allItems;
    	} else {
    		return null;
    	}
    }
    
    /**
     * Attaches an {@link EbookPropertyItem} to the end.
     * @param item The item to be attached.
     */
    public void addRow(EbookPropertyItem item) {
    	final boolean added = this.allItems.add(item);
    	if(added) {
    		final int ins = this.allItems.size()-1;
	    	fireTableRowsInserted(ins, ins);
    	}
    }
    
	public boolean removeRow(final EbookPropertyItem item) {
		final Iterator<EbookPropertyItem> iterator = allItems.iterator();
		boolean removed = false;
		for (int i = 0; iterator.hasNext(); i++) {
			final EbookPropertyItem toRemove = iterator.next();
			try {
				if(toRemove!=null && toRemove.equals(item)) {
					int deleteObjects = DefaultDBManager.getInstance().deleteObject(toRemove);
					if(!isDirty() && deleteObjects>0) {
						allItems.remove(i);
						fireTableRowsDeleted(i, i);
					}
					removed = true;
					break;
				}
			} catch(Exception e) {
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
	 * Tells the model that the data has changed and next time the data should
	 * be refetched from the database.
	 * @see #isDirty()
	 */
	public void setDirty() {
		this.dirty = true;
	}

	public List<Field> getOrderByColumns() {
		return orderByColumns;
	}

	public void setOrderByColumns(List<Field> orderByColumns) {
		this.orderByColumns = orderByColumns;
	}

	/**
	 * Gets the current order direction. Ascending order is the default one.
	 * @return The current order direction.
	 */
	public OrderDirection getOrderDirection() {
		if(this.orderDirection == null) {
			this.orderDirection = new OrderDirection(OrderDirection.DIRECTION_ASC);
		}
		return this.orderDirection;
	}

	/**
	 * Sets the order direction for the displayed data.
	 * @param orderDirection The order direction to be used for the data.
	 */
	public void setOrderDirection(final OrderDirection orderDirection) {
		this.orderDirection = orderDirection;
	}

	/**
	 * Gets the current list of filter conditions.
	 * @return The current filter condition list. Never returns <code>null</code>.
	 */
	public QueryCondition getQueryCondition() {
		if(this.queryConditions == null) {
			//set as root condition
			this.queryConditions = new QueryCondition(null, null, null, "ROOT");
		}
		return queryConditions;
	}

}
