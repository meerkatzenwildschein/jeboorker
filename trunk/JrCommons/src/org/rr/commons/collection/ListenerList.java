package org.rr.commons.collection;

import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.EventListener;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Using a {@link CopyOnWriteArrayList} which allows to add or remove listeners while 
 * iterating without causing a {@link ConcurrentModificationException}.
 */
public class ListenerList<L extends EventListener> implements Serializable, Iterable<L> {
	
	private static final long serialVersionUID = 1510397925290428532L;

	private final CopyOnWriteArrayList<L> listeners;

	public ListenerList() {
		listeners = new CopyOnWriteArrayList<L>();
	}

	public void addListener(L listener) {
		listeners.add(listener);
	}

	public void removeListener(L listener) {
		listeners.remove(listener);
	}

	public int getListenerCount() {
		return listeners.size();
	}

	/**
	 * Return an {@link Iterator} for the {@link EventListener} instances
	 */
	public Iterator<L> iterator() {
		return listeners.iterator();
	}

}
