package org.rr.commons.collection;

import java.util.AbstractList;
import java.util.List;

/**
 * List that allows to transform it values
 * 
 * @param <T> The target, transformed type.
 * @param <S> The source type.
 */
public abstract class TransformValueList<S, T> extends AbstractList<T> {

	List<S> list;
	
	public TransformValueList(List<S> sourceValuesList) {
		this.list = sourceValuesList;
	}
	
	public abstract T transform(S source);
	
	@Override
	public T get(int index) {
		S source = list.get(index);
		return transform(source);
	}

	@Override
	public int size() {
		return list.size();
	}

}