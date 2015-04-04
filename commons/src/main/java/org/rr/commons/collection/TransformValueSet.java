package org.rr.commons.collection;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * List that allows to transform it values
 *
 * @param <T> The target, transformed type.
 * @param <S> The source type.
 */
public abstract class TransformValueSet<S, T> extends AbstractSet<T> {

	Set<S> set;
	
	public TransformValueSet(Set<S> sourceValuesSet) {
		this.set = sourceValuesSet;
	}
	
	public abstract T transform(S source);
	
	@Override
	public int size() {
		return set.size();
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<S> iterator = set.iterator();
		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				return transform(iterator.next());
			}

			@Override
			public void remove() {
				iterator.remove();
			}
		};
	}

}