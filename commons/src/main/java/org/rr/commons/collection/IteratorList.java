package org.rr.commons.collection;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.ReflectionUtils;

/**
 * Encapsulates an Iterator and provide it's data with a list
 * interface. The iterator content is only be copied if needed (lazy).
 */
public class IteratorList<E> implements ICloseableList<E> {
	
	private Iterator<E> iterator;
	
	private int size;
	
	private List<E> list = new ArrayList<>();
	
	private boolean completlyCopied = false;
	
	public IteratorList(Iterator<E> iterator, int size) {
		this.iterator = iterator;
		this.size = size;
	}

	@Override
	public boolean add(E e) {
		this.copyIterator();
		return this.list.add(e);
	}

	@Override
	public void add(int index, E element) {
		this.copyIterator();
		this.list.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		this.copyIterator();
		return this.list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		this.copyIterator();
		return this.list.addAll(index, c);
	}

	@Override
	public void clear() {
		completlyCopied = true;
		this.list.clear();
	}

	@Override
	public boolean contains(Object o) {
		this.copyIterator();
		return this.list.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		this.copyIterator();
		return this.list.containsAll(c);
	}

	@Override
	public E get(int index) {
		if(list.size() > index) {
			return list.get(index);
		} else {
			return fillListToIndex(index);
		}
	}

	@Override
	public int indexOf(Object o) {
		int indexOf = list.indexOf(o);
		if(list.indexOf(o)!=-1) {
			return indexOf;
		}
		
		while(iterator.hasNext()) {
			E next = iterator.next();
			list.add(next);
			if(o==null && next==null) {
				return list.size()-1;
			} else if(o!=null && o.equals(next)) {
				return list.size()-1;
			}
		}
		return -1;
	}

	@Override
	public boolean isEmpty() {
		if(list.isEmpty() && !iterator.hasNext()) {
			return true;
		}
		return false;
	}

	@Override
	public Iterator<E> iterator() {
		return new ArrayIterator<E>(this);
//		this.copyIterator();
//		return this.list.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		this.copyIterator();
		return this.list.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		this.copyIterator();
		return this.list.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		this.copyIterator();
		return this.list.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		this.copyIterator();
		return this.list.remove(o);
	}

	@Override
	public E remove(int index) {
		this.copyIterator();
		return this.list.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		this.copyIterator();
		return this.list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		this.copyIterator();
		return this.list.retainAll(c);
	}

	@Override
	public E set(int index, E element) {
		fillListToIndex(index);
		return this.list.set(index, element);
	}

	@Override
	public int size() {
		if(completlyCopied) {
			return list.size();
		}
		
		if(this.size >= 0) {
			return (int) this.size;
		}

		this.copyIterator();
		return list.size();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		this.copyIterator();
		return this.list.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		this.copyIterator();
		return this.list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		this.copyIterator();
		return this.list.toArray(a);
	}
	
	@Override
	public void close() {
		if(this.iterator instanceof Closeable) {
			try {
				((Closeable)this.iterator).close();
			} catch (IOException e) {
				LoggerFactory.log(Level.WARNING, this, "Can not close Iterator", e);
			}
		} else {
			Method closeMethod = ReflectionUtils.getMethod(this.iterator.getClass(), "close", new Class[0], ReflectionUtils.VISIBILITY_VISIBLE_ALL);
			if(closeMethod != null) {
				closeMethod.setAccessible(true);
				try {
					closeMethod.invoke(this.iterator, new Object[0]);
				} catch (Exception e) {
					LoggerFactory.log(Level.WARNING, this, "Can not close Iterator", e);
				}
			}
		}
	}

	/**
	 * Fills the internal list with the data from the iterator and returns it's content.
	 * @param index The index needed to fetch.
	 * @return The value from the desired index.
	 */
	private E fillListToIndex(int index) {
		if(!completlyCopied) {
			while(list.size() <= index) {
				list.add(iterator.next());
			}
			
			if(!iterator.hasNext()) {
				completlyCopied = true;
			}
		}
		return list.get(index);
	}
	
	private void copyIterator() {
		LoggerFactory.logInfo(this, "Full Iterator copy is triggered.", null);
		if(!completlyCopied) {
			completlyCopied = true;
			while(iterator.hasNext()) {
				list.add(iterator.next());
			}
		}
	}
}
