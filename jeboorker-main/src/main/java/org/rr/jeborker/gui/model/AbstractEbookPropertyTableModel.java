package org.rr.jeborker.gui.model;

import java.util.ArrayList;
import java.util.List;

import org.rr.jeborker.db.item.EbookPropertyItem;

public abstract class AbstractEbookPropertyTableModel implements ReloadableTableModel {

	@Override
	public List<EbookPropertyItem> getEbookPropertyItemsAt(int[] rowIndex) {
		List<EbookPropertyItem> result = new ArrayList<>(rowIndex.length);
		for (int i : rowIndex) {
			result.add(getEbookPropertyItemAt(i));
		}
		return result;
	}
	
}
