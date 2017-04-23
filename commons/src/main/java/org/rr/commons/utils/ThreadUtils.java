package org.rr.commons.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.rr.commons.collection.IteratorList;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.compression.CompressedDataEntry;

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

	public static List<List<IResourceHandler>> loopAndWait(List<CompressedDataEntry> sourceFiles,
			RunnableImpl<CompressedDataEntry, List<IResourceHandler>> runnableImpl) {
		return loopAndWait(sourceFiles, runnableImpl, Math.max(1, ThreadUtils.availableProcessors() -1));
	}

	/**
	 * Does a loop over the given list where the <code>each</code> parameter is
	 * invoked with any entry in the list. The
	 *
	 * @param l The list to be looped.
	 * @param each The {@link RunnableImpl} implementation which is executed with each list entry.
	 * @param maxThreads Maximum number of Threads to be executed to run the {@link RunnableImpl} implementations.
	 * @return A list of results for each RunnableImpl in the same order than given with <code>each</code>.
	 */
	private static <S, T> List<T> loop(final List<S> l, final RunnableImpl<S, T> each, int maxThreads, boolean wait) {
		final Thread[] slots = new Thread[maxThreads];
		final List<T> results = Collections.synchronizedList(l != null ? new ArrayList<T>(l.size()) : Collections.<T>emptyList());
		final List<S> working = Collections.synchronizedList(l != null ? new ArrayList<S>(l) : Collections.<S>emptyList());
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
									int index = l.indexOf(entry);
									ListUtils.set(results, each.run(entry), index);
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
		return results;
	}
	
	public static int availableProcessors() {
		return Runtime.getRuntime().availableProcessors();
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
