package org.rr.jeborker.gui.action;

public interface IDoOnlyOnceAction<E> {

	public E doOnce();
	
	public void setResult(E result);
}
