package org.rr.commons.collection;


/**
 * A Pair that can be used to store more than one value into a list.
 */
public class Pair<E,F> {
	
	private F f;
	private E e;

	public Pair(E e, F f) {
		this.e = e;
		this.f = f;
	}
	
	public E getE() {
		return e;
	}
	
	public F getF() {
		return f;
	}
}
