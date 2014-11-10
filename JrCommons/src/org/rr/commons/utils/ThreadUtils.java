package org.rr.commons.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.rr.commons.collection.IteratorList;

public class ThreadUtils {

	private static final Object MUTEX = new Object();
	
	public static <S,T> List<T> loopAndWait(final Iterable<S> l, final RunnableImpl<S,T> each, final int maxThreads) {
		return loopAndWait(l.iterator(), each, maxThreads);
	}
	
	public static <S,T> List<T> loopAndWait(final Iterator<S> l, final RunnableImpl<S,T> each, final int maxThreads) {
		return loopAndWait(new IteratorList<S>(l, -1), each, maxThreads);
	}
	
	public static <S,T> List<T> loopAndWait(final List<S> l, final RunnableImpl<S,T> each, int maxThreads) {
		return loop(l, each, maxThreads, true);
	}
	
	/**
	 * Does a loop over the given list where the <code>each</code> parameter is
	 * invoked with any entry in the list. The
	 *
	 * @param l The list to be looped.
	 * @param each The {@link RunnableImpl} implementation which is executed with each list entry.
	 * @param maxThreads Maximum number of Threads to be executed to run the {@link RunnableImpl} implementations.
	 */
	private static <S, T> List<T> loop(final List<S> l, final RunnableImpl<S, T> each, int maxThreads, boolean wait) {
		final Thread[] slots = new Thread[maxThreads];
		final List<T> results = Collections.synchronizedList(new ArrayList<T>(l.size()));
		final List<S> working = Collections.synchronizedList(new ArrayList<S>(l));
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
								S entry = null;
								try {
									entry = working.remove(0);
								} catch(IndexOutOfBoundsException e) {
								}
								
								if(entry != null) {
									results.add(each.run(entry));
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
		return results;
	}
	
	private static boolean containsOnlyNull(Object[] values) {
		for (int i = 0; i < values.length; i++) {
			if(values[i] != null) {
				return false;
			}
		}
		return true;
	}
	
	public static abstract class RunnableImpl<S, T> {
		public abstract T run(S entry);
	}

}
