package org.rr.jeborker.gui.model;

import java.util.List;

import javax.swing.table.TableModel;

import org.rr.jeborker.db.item.EbookPropertyItem;

public interface ReloadableTableModel extends TableModel {

	public void reloadEbookPropertyItemAt(int rowIndex);

	public EbookPropertyItem getEbookPropertyItemAt(int rowIndex);
	
	public List<EbookPropertyItem> getEbookPropertyItemsAt(int[] rowIndex);

	public void addRow(EbookPropertyItem item, int row);

	public int searchRow(EbookPropertyItem item);

	public boolean removeRow(EbookPropertyItem item);
	
}
