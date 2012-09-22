package org.rr.jeborker.gui.model;

import javax.swing.AbstractListModel;

/**
 * This list model is always empty. It's not possible to add something to it. 
 * It's useful for init reasons.
 */
public class EmptyListModel extends AbstractListModel {

	private static final EmptyListModel sharedInstance = new  EmptyListModel();
	
	/**
	 * Get always the same {@link EmptyListModel} instance.
	 * @return The desired {@link EmptyListModel} instance. Never returns <code>null</code>.
	 */
	public static EmptyListModel getSharedInstance() {
		return sharedInstance;
	}
	
	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public Object getElementAt(int index) {
		return null;
	}
	
}
