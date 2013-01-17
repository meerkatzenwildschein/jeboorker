package org.rr.commons.collection;

import java.util.AbstractList;
import java.util.List;

/**
 * The {@link BlindElementList} allows to remove a value at a specific index
 * without modifying the original {@link List}. 
 */
public class BlindElementList<E> extends AbstractList<E> {

	private List<E> list;
	private int idx;
	
	public BlindElementList(List<E> list, int index) {
		this.list = list;
		this.idx = index;
	}
	
	@Override
	public E get(int index) {
		if(idx > index) {
			return list.get(index);
		} else {
			return list.get(index + 1);
		}
	}

	@Override
	public int size() {
		return list.size() - 1;
	}

}
