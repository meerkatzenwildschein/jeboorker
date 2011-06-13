package org.rr.commons.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Wrapps an {@link ArrayList} and provides a cursor which allows to navigate on the {@link ArrayList}. The
 * {@link CursableCollection} is synchronized and can be used by multiple threads.
 */
public class CursableCollection<E,A> implements Collection<E> {

	private final ListenerList<CursableCollectionListener<E, A>> listeners = new ListenerList<CursableCollectionListener<E, A>>();
	
	private ArrayList<E> list;
	
	private ArrayList<A> attachment;
	
	private int cursor;
	
	private int maxCapacity;
	
	public CursableCollection() {
		this(10, -1);
	}
	
	public CursableCollection(int initialCapacity, int maxCapacity) { 
		this.list = new ArrayList<E>(initialCapacity);
		this.attachment = new ArrayList<A>(initialCapacity);
		this.maxCapacity = maxCapacity;
	}
	
	/**
	 * Adds a cursor listener to this {@link CursableCollection} instance.
	 * @param listener The listener to be added.
	 */
	public void addCursorListener(CursableCollectionListener<E, A> listener) {
		listeners.removeListener(listener); //no duplicate listeners
		listeners.addListener(listener);
	}
	
	/**
	 * Removes a previously added listener from this {@link CursableCollection} instance.
	 * @param listener The listener to be removed.
	 */
	public void removeCursorListener(CursableCollectionListener<E, A> listener) {
		listeners.removeListener(listener);
	}
	
	/**
	 * Should be always invoked after the cursor points to a new element.
	 */
	private void fireAfterCursorChanged() {
		final Iterator<CursableCollectionListener<E, A>> listenersIterator = listeners.iterator();
		while (listenersIterator.hasNext()) {
			final CursableCollectionListener<E, A> eventListener = listenersIterator.next();
			eventListener.afterCursorChanged(this);
		}
	}
	
	/**
	 * Should be always invoked before the cursor points to a new element.
	 */
	private void fireBeforeCursorChanged() {
		final Iterator<CursableCollectionListener<E, A>> listenersIterator = listeners.iterator();
		while (listenersIterator.hasNext()) {
			final CursableCollectionListener<E, A> eventListener = listenersIterator.next();
			eventListener.beforeCursorChanged(this);
		}
	}	

	/**
	 * Prepends an element to the collection. The cursor is incremented by one. The cursor
	 * points to the same collection element as before.
	 * @param e The element to be prepended.
	 */
	public synchronized void prepend(E e, A attachment) {
		list.add(0, e);
		this.attachment.add(0, attachment);
		cursor ++;
	}
	
	public synchronized void prepend(E e) {
		this.prepend(e, null);
	}
	
	/**
	 * Appends an element at the cursor location and removes all
	 * elements behind the cursor location. The cursor is placed
	 * to the new appended element.
	 * 
	 * @param element The element to be added.
	 * @param attachment The object to be bind to the element.
	 */
	public synchronized boolean append(E element, A attachment) {
		for (int i = cursor+1; i < list.size(); i++) {
			list.remove(i);
			this.attachment.remove(i);
		}
		this.fireBeforeCursorChanged();
		final boolean succses = this.add(element, attachment);
		cursor = list.size()-1;
		this.fireAfterCursorChanged();
		return succses;
	}
	
	public synchronized boolean append(E element) {
		return this.append(element, null);
	}
	
	/**
	 * Gets the attached value for the given element.
	 * @param element The element having an attached value 
	 * @return The attached value or <code>null</code> if there is no attached value present.
	 */
	public synchronized A getAttachment(E element) {
		for (int i = 0; i < this.list.size(); i++) {
			if(this.list.get(i)==element) {
				return this.attachment.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Gets the cursor location on the list.
	 * @return The cursor location.
	 */
	public int getCursorLocation() {
		return cursor;
	}
	
	/**
	 * Moves the cursor to the given index.
	 * @param index The index where the cursor should be moved to.
	 */
	public void moveCursorTo(int index) {
		if(list.size() <= index+1) {
			throw new IndexOutOfBoundsException("could not set index to " + index + ". Element count is " + list.size());
		} else if (index < 0) {
			throw new IndexOutOfBoundsException("could not set index to " + index);
		}
		this.fireBeforeCursorChanged();
		this.cursor = index;
		this.fireAfterCursorChanged();
	}
	
	/**
	 * Moves the cursor to the next element and returns the element.
	 * @return The next element.
	 */
	public synchronized E next() {
		if(!hasNext()) {
			return null;
		}
		this.fireBeforeCursorChanged();
		cursor++;
		this.fireAfterCursorChanged();
		return list.get(cursor);
	}
	
	/**
	 * Tells if there is an element behind the cursor.
	 * @return <code>true</code> if there is an element or <code>false</code> otherwise.
	 */
	public synchronized boolean hasNext() {
		if(list.size() <= cursor+1) {
			return false;
		}
		return true;
	}
	
	/**
	 * Moves the cursor to the previous element and returns the element.
	 * @return The next element.
	 */
	public synchronized E previous() {
		if(!hasPrevious()) {
			return null;
		}
		
		this.fireBeforeCursorChanged();
		cursor--;
		this.fireAfterCursorChanged();
		return list.get(cursor);
	}
	
	/**
	 * Tells if there is an element before the cursor.
	 * @return <code>true</code> if there is an element or <code>false</code> otherwise.
	 */
	public synchronized boolean hasPrevious() {
		if(cursor == 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the element where the cursor is currently placed at.
	 * @return The current element. If there is no element, <code>null</code> is returned.
	 */
	public synchronized E current() {
		if(this.isEmpty()) {
			this.cursor = 0;
			return null;
		}
		return list.get(this.cursor);
	}

	/**
	 * Removes the given element. If the element to be removed is before or at the
	 * cursor, the cursor will moves down by one.
	 */
	@Override
	public synchronized boolean remove(Object o) {
		for (int i = 0; i < list.size(); i++) {
			E e = list.get(i);
			if(e==o) {
				boolean fireBefore = false;
				if(i<=cursor) {
					if(cursor-1 < 0) {
						fireBeforeCursorChanged();
						fireBefore = true;
					} else {
						cursor--;
					}
				}
				final boolean succsess = this.list.remove(i)!=null;
				if(succsess) {
					this.attachment.remove(i);
				}
				if(succsess && fireBefore) {
					fireAfterCursorChanged();
				}
				return succsess;
			}
		}
		return false;
	}
	
	@Override
	public synchronized boolean add(E e) {
		return this.add(e, null);
	}

	/**
	 * Adds an element at the end of the collection but did not moves the cursor.
	 */
	public synchronized boolean add(E e, A attachment) {
		final boolean succsess = this.list.add(e);
		if(succsess) {
			this.attachment.add(attachment);
		}
		if(maxCapacity!=-1 && this.list.size() > maxCapacity) {
			this.remove(this.list.get(0));
		}
		return succsess;
	}

	/**
	 * Adds all given elements at the end of the collection but did not moves the cursor.
	 */
	@Override
	public synchronized boolean addAll(Collection<? extends E> c) {
		boolean succsess = this.list.addAll(c);
		for (int i = 0; i < c.size(); i++) {
			this.attachment.add(null);
		}
		
		return succsess;
	}

	/**
	 * Clears the collection and resets the cursor.
	 */
	@Override
	public synchronized void clear() {
		this.fireBeforeCursorChanged();
		this.list.clear();
		this.attachment.clear();
		this.cursor = 0;
		this.fireAfterCursorChanged();
	}

	@Override
	public boolean contains(Object o) {
		return this.list.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.list.containsAll(c);
	}

	@Override
	public synchronized boolean isEmpty() {
		return this.list.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return this.list.iterator();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		//TODO
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		//TODO
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public synchronized int size() {
		return this.list.size();
	}

	@Override
	public Object[] toArray() {
		return this.list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.list.toArray(a);
	}
	
}
