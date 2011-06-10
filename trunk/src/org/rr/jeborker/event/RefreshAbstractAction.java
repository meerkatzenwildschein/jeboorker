package org.rr.jeborker.event;

import javax.swing.AbstractAction;

public abstract class RefreshAbstractAction extends AbstractAction {

	private int[] selectedRowsToRefresh;
	
	public void setSelectedRowsToRefresh(int[] selectedRowsToRefresh) {
		this.selectedRowsToRefresh = selectedRowsToRefresh;
	}

	public int[] getSelectedRowsToRefresh() {
		return selectedRowsToRefresh;
	}

}
