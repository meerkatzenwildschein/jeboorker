package org.rr.jeborker.gui.action;

interface IDoOnlyOnceAction<E> {

	/**
	 * Method which is invoked once. Take sure that other calls
	 * did not do the action again.
	 */
	public E doOnce();

	/**
	 * Provides a possibility to push the value from the previously 
	 * invoked {@link #doOnce()} method to another action instance. 
	 */
	public void setDoOnceResult(E result);
	
	public void prepareFor(int index, int size);
	
}
