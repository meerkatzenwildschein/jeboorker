package org.rr.jeborker.gui.model;

import java.util.LinkedList;

import javax.swing.DefaultListSelectionModel;

public class EbookPropertyDBTableSelectionModel extends DefaultListSelectionModel {

	private static final long serialVersionUID = 8312660352718393880L;
	
	private int historySize = 3;
	
	LinkedList<Selection> history = new LinkedList<EbookPropertyDBTableSelectionModel.Selection>();

	public EbookPropertyDBTableSelectionModel() {
		setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}
	
	@Override
	public void setSelectionInterval(int index0, int index1) {
		super.setSelectionInterval(index0, index1);
		this.addHistory(index0, index1);
	}
	
	/**
	 * Adds both given indices to the history. Also removes the oldest
	 * history entry if the history gets too large.
	 */
	private void addHistory(int index0, int index1) {
		history.addFirst(new Selection(index0, index1));
		if(history.size() > historySize) {
			history.removeLast();
		}
	}
	
	/**
	 * Gets the selection from history. 
	 * @param idx 0 for the current entry and any bigger for previous selections
	 * 		in historical order.
	 * @return The Selection or <code>null</code> if no history is available for the given index.
	 */
	public Selection getHistory(int idx) {
		if(history.size() <= idx) {
			return null;
		}
		return history.get(idx);
	}

	/**
	 * Selection value holder.
	 */
	public class Selection {
		public int index0;
		public int index1;
		
		public Selection(int index0, int index1) {
			this.index0 = index0;
			this.index1 = index1;
		}
	}

	/**
	 * Get the current history size for the selection model.
	 * @return The size.
	 */
	public int getHistorySize() {
		return historySize;
	}

	/**
	 * Sets the history size. use 0 for no history feature.
	 * @param historySize The history size.
	 */
	public void setHistorySize(int historySize) {
		this.historySize = historySize;
	}
}
