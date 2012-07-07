package org.rr.commons.collection;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A list which maps two lists into one without copy
 */
public class CompoundList<E> extends AbstractList<E> {

	private List<E> first; 
	private List<E> second;
	
	public CompoundList(List<E> first, List<E> second) {
		if(first == null) {
			throw new NullPointerException("First list is null");
		}
		if(second == null) {
			throw new NullPointerException("Second list is null");
		}
		this.first = first;
		this.second = second;
	}
	
	@Override
	public boolean add(E e) {
		return second.add(e);
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return second.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		first.clear();
		second.clear();
	}

	@Override
	public boolean contains(Object o) {
		return first.contains(o) || second.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E get(int index) {
		if(index >= first.size()) {
			final int indexOnSecond = index - first.size();
			try {
				return second.get(indexOnSecond);
			} catch (IndexOutOfBoundsException e) {
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
			}
		} else {
			return first.get(index);
		}
	}

	@Override
	public int indexOf(Object o) {
		int firstIndexOf = first.indexOf(o);
		if(firstIndexOf!=-1) {
			return firstIndexOf;
		} else {
			final int secondIndexOf = second.indexOf(o);
			if(secondIndexOf!=-1) {
				return secondIndexOf + first.size();
			} else {
				return -1;
			}
		}
	}

	@Override
	public boolean isEmpty() {
		return first.isEmpty() && second.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return new CompoundIterator<E>(first.iterator(), second.iterator());
	}

	@Override
	public int lastIndexOf(Object o) {
		int secondLastIndexOf = second.lastIndexOf(o);
		if(secondLastIndexOf!=-1) {
			return secondLastIndexOf + first.size();
		} else {
			return first.lastIndexOf(o);
		}
	}

	@Override
	public ListIterator<E> listIterator() {
		return super.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return super.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		if(first.remove(o)) {
			return true;
		} else {
			return second.remove(o);
		}
	}

	@Override
	public E remove(int index) {
		if(index >= first.size()) {
			final int indexOnSecond = index - first.size();
			return second.remove(indexOnSecond);
		} else {
			return first.remove(index);
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object object : c) {
			if(this.remove(object)) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return super.retainAll(c);
	}

	@Override
	public E set(int index, E element) {
		if(index >= first.size()) {
			final int indexOnSecond = index - first.size();
			return second.set(indexOnSecond, element);
		} else {
			return first.set(index, element);
		}
	}

	@Override
	public int size() {
		return first.size() + second.size();
	}
}
