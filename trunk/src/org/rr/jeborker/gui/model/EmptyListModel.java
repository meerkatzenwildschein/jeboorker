package org.rr.jeborker.gui.model;

import javax.swing.AbstractListModel;

/**
 * This list model is always empty. It's not possible to add something to it. 
 * It's useful for the initialization process.
 */
public class EmptyListModel<T> extends AbstractListModel<T> {

	@SuppressWarnings("rawtypes")
	private static final EmptyListModel<?> sharedInstance = new EmptyListModel();
    
	/**
	 * Get always the same {@link EmptyListModel} instance.
	 * @return The desired {@link EmptyListModel} instance. Never returns <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public static final <T> EmptyListModel<T> getSharedInstance() {
		return (EmptyListModel<T>) sharedInstance;
	}
	
	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public T getElementAt(int index) {
		return null;
	}
	
}
