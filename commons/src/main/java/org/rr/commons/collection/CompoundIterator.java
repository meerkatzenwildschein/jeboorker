package org.rr.commons.collection;

import java.util.Iterator;

/**
 * Maps two iterators into one without copy.
 */
public class CompoundIterator<E> implements Iterator<E> {

	private final Iterator<E> first;
	
	private final Iterator<E> second;
	
	private boolean processFirst = true;
	
	public CompoundIterator(Iterator<E> first, Iterator<E> second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public boolean hasNext() {
		if(processFirst && first.hasNext()) {
			return true;
		} else if(second.hasNext()) {
			processFirst = false;
			return true;
		}
		return false;
	}

	@Override
	public E next() {
		if(processFirst && first.hasNext()) {
			final E next = first.next();
			if(!first.hasNext()) {
				processFirst = false;
			}
			return next;
		} else if(second.hasNext()) {
			return second.next();
		}
		return null;
	}

	@Override
	public void remove() {
		if(processFirst) {
			first.remove();
		} else  {
			second.remove();
		}		
	}

}
