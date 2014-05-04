package org.rr.commons.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An abstract list that can be extended. It provides only forward 
 * methods to the original, wrapped list instance. It's useful for
 * providing some additional methods to a predefined list instance. 
 */
public abstract class WrapperList<T> implements List<T> {

	protected List<T> toWrap;
	
	public WrapperList() {
		this.toWrap = new ArrayList<T>();
	}
	
	public WrapperList(List<T> toWrap) {
		this.toWrap = toWrap;
	}

	@Override
	public int size() {
		return toWrap.size();
	}

	@Override
	public boolean isEmpty() {
		return toWrap.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return toWrap.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return toWrap.iterator();
	}

	@Override
	public Object[] toArray() {
		return toWrap.toArray();
	}

	@Override
	public boolean add(T e) {
		return toWrap.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return toWrap.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return toWrap.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return toWrap.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		return toWrap.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return toWrap.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return toWrap.retainAll(c);
	}

	@Override
	public void clear() {
		toWrap.clear();
	}

	@Override
	public T get(int index) {
		return toWrap.get(index);
	}

	@Override
	public T set(int index, T element) {
		return toWrap.set(index, element);
	}

	@Override
	public void add(int index, T element) {
		toWrap.add(index, element);
	}

	@Override
	public T remove(int index) {
		return toWrap.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return toWrap.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return toWrap.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return toWrap.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return toWrap.listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return toWrap.subList(fromIndex, toIndex);
	}

	@Override
    public <E> E[] toArray(E[] a) {
		return toWrap.toArray(a);
    }

}