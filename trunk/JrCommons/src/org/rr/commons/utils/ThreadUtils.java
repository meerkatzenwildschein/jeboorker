package org.rr.commons.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.rr.commons.collection.IteratorList;

public class ThreadUtils {

	private static final Object MUTEX = new Object();
	
	public static <T> void loop(final Iterable<T> l, final RunnableImpl<T> each, final int maxThreads) {
		loop(l.iterator(), each, maxThreads);
	}
	
	public static <T> void loop(final Iterator<T> l, final RunnableImpl<T> each, final int maxThreads) {
		loopAndWait(new IteratorList<T>(l, -1), each, maxThreads);
	}
	
	public static <T> void loopAndWait(final List<T> l, final RunnableImpl<T> each, int maxThreads) {
		loop(l, each, maxThreads, true);
	}
	
	/**
	 * Does a loop over the given list where the <code>each</code> parameter is
	 * invoked with any entry in the list. The
	 *
	 * @param l The list to be looped.
	 * @param each The {@link RunnableImpl} implementation which is executed with each list entry.
	 * @param maxThreads Maximum number of Threads to be executed to run the {@link RunnableImpl} implementations.
	 */
	private static <T> void loop(final List<T> l, final RunnableImpl<T> each, int maxThreads, boolean wait) {
		final Thread[] slots = new Thread[maxThreads];
		final List<T> working = Collections.synchronizedList(new ArrayList<T>(l));
		while(!working.isEmpty()) {
			//thread slot searching and execution must be synchronized.
			synchronized(MUTEX) {
				boolean emptySlotFound = false;
				for(int i = 0; i < slots.length; i++) {
					final int slot = i;
					if(slots[slot] == null) {
						//free slot to use.
						slots[slot] = new Thread(new Runnable() {
							
							@Override
							public void run() {
								T entry = null;
								try {
									entry = working.remove(0);
								} catch(IndexOutOfBoundsException e) {
								}
								
								if(entry != null) {
									each.run(entry);
								}
								slots[slot] = null;
							}
						});
						slots[slot].start();
						emptySlotFound = true;
					} else {
						//no free slot
						emptySlotFound = false;
					}
				}
				
				if(!emptySlotFound && !working.isEmpty()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
				
				if(wait) {
					while(!containsOnlyNull(slots)) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
	}
	
	private static boolean containsOnlyNull(Object[] values) {
		for (int i = 0; i < values.length; i++) {
			if(values[i] != null) {
				return false;
			}
		}
		return true;
	}
	
	public static abstract class RunnableImpl<T> {
		public abstract void run(T entry);
	}

}
