package org.rr.commons.collection;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

/**
 * The {@link InsertElementList} allows to map a value to a specific
 * index into the map without modifying the original {@link List}. 
 */
public class InsertElementList<E> extends AbstractList<E> {

	private List<E> list;
	private E value;
	private int idx;
	
	public InsertElementList(List<E> list, E value, int index) {
		this.list = list;
		this.value = value;
		this.idx = index;
	}
	
	@Override
	public E get(int index) {
		int internalListIndex = getInternalListIndex(index);
		if(internalListIndex == -1) {
			return value;
		} else {
			return list.get(internalListIndex);
		}
	}

	@Override
	public int size() {
		return list.size() + 1;
	}

	@Override
	public boolean add(E e) {
		return list.add(e);
	}

	@Override
	public void add(int index, E e) {
		if(index < idx) {
			list.add(index, e);
		} else if(index == idx) {
			idx++;
			list.add(index, e);
		} else {
			list.add(index - 1, e);
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return list.addAll(c);
	}

	private int getInternalListIndex(int index) {
		if(idx > index) {
			return index;
		} else if(idx == index) {
			return -1; //mapped object
		} else {
			return index - 1;
		}			
	}
}
