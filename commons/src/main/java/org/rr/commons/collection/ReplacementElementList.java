package org.rr.commons.collection;

import java.util.AbstractList;
import java.util.List;

public class ReplacementElementList<E> extends AbstractList<E> {

	private List<E> list;
	private int idx;
	private E replacement;
	
	public ReplacementElementList(List<E> list, int index, E replacement) {
		this.list = list;
		this.idx = index;
		this.replacement = replacement;
	}
	
	@Override
	public E get(int index) {
		if(index == idx) {
			return replacement;
		}
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}

}
