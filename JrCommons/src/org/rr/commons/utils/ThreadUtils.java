package org.rr.commons.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThreadUtils {

	private static final Object MUTEX = new Object();
	
	/**
	 * Does a loop over the given list where the <code>each</code> parameter is
	 * invoked with any entry in the list. The  
	 *  
	 * @param l The list to be looped.
	 * @param each The {@link RunnableImpl} implementation which is executed with each list entry.
	 * @param maxThreads Maximum number of Threads to be executed to run the {@link RunnableImpl} implementations.
	 */
	public static <T> void loop(final List<T> l, final RunnableImpl<T> each, final int maxThreads) {
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
					} catch (InterruptedException e) {}					
				}
			}
		}
	}
	
	public static abstract class RunnableImpl<T> {
		public abstract void run(T entry);
	}

}
