package org.rr.commons.collection;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link InsertList} allows to map a value to a specific
 * index into the map without modifying the original {@link List}. 
 */
public class InsertList<E> extends AbstractList<E> {

	private List<E> list;
	private E value;
	private int idx;
	
	public InsertList(List<E> list, E value, int index) {
		//use a CompoundList with a empty list for taking sure that adding is supported.
		this.list = new CompoundList<E>(list, new ArrayList<E>());
		this.value = value;
		this.idx = index;
	}
	
	@Override
	public E get(int index) {
		if(idx > index) {
			return list.get(index);
		} else if(idx == index) {
			return value;
		} else {
			return list.get(index - 1);
		}
	}

	@Override
	public int size() {
		return list.size() + 1;
	}

}
