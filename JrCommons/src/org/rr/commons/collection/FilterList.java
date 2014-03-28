package org.rr.commons.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A List implementation which allows to define a filter which prevent to add some kind of values
 * to this list. 
 */
public class FilterList<T> extends WrapperList<T> {
	
	public static interface Filter<T> {
		public boolean isFiltered(T value);
	}
	
	private Filter<T> filter;
	
	public FilterList(List<T> wrap, Filter<T> filter) {
		super(wrap);
		this.filter = filter;
		doFilter();
	}
	
	private void doFilter() {
		List<T> toRemove = new ArrayList<T>();
		for(T t : toWrap) {
			if(filter.isFiltered(t)) {
				toRemove.add(t);
			}
		}
		toWrap.removeAll(toRemove);
	}

	@Override
	public boolean add(T e) {
		if(!filter.isFiltered(e)) {
			return super.add(e);
		}
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return super.addAll(new FilterList<T>(new ArrayList<T>(c), filter));
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		return super.addAll(index, new FilterList<T>(new ArrayList<T>(c), filter));
	}

	@Override
	public T set(int index, T e) {
		if(!filter.isFiltered(e)) {
			return super.set(index, e);
		}
		return super.get(index);
	}

	@Override
	public void add(int index, T e) {
		if(!filter.isFiltered(e)) {
			super.add(index, e);
		}
	}
}
