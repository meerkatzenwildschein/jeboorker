package org.rr.jeborker.gui.action;

public interface IDoOnlyOnceAction<E> {

	public E doOnce();
	
	public E getResult();
	
	public void setResult(E result);
	
	public void prepareFor(int index, int size);
	
}
