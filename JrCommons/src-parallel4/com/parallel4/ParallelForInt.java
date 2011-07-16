/**
 * 
 */
package com.parallel4;

import java.util.concurrent.atomic.AtomicInteger;
/**
 * (C) Copyright 2008 Markus Junginger.
 * 
 * @author Markus Junginger
 */
/*
 * This file is part of Parallel4.
 * 
 * Parallel4 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Parallel4 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Parallel4. If not, see <http://www.gnu.org/licenses/>.
 */
final class ParallelForInt {
	private final int stop;
	private IterationInt iterationDelegate;
	private final int step;
	private final AtomicInteger index;
	private final int start;
	private final Runner runner;
	private boolean breakLoop;
	private int[] iterationValues;
	private int numberIterations;

	// TODO exceptions: fail fast (default), collect, ignore

	public ParallelForInt(int stop) {
		this(0, stop, 1);
	}

	public ParallelForInt(int start, int stop) {
		this(start, stop, 1);
	}

	public ParallelForInt(int start, int stop, int step) {
		this.start = start;
		this.stop = stop;
		this.step = step;
		index = new AtomicInteger();
		runner = new Runner();
	}

	public void loop(IterationInt iterationDelegate) {
		if (start == stop) {
			return;
		} else if (start + step == stop) {
			iterationDelegate.iteration(start);
			return;
		} else {

			numberIterations = (stop - start) / step; 
			// TODO use double to check too big steps, too
			if (numberIterations < 0) {
				throw new ParallelException("Loop would run invinitely");
			}
			iterationValues = new int[numberIterations];
			int idx = 0;
			for (int i = start; i < stop; i += step) {
				iterationValues[idx++] = i;
			}

			index.set(0);
			this.iterationDelegate = iterationDelegate;
			Parallel.executeWithCorePool(runner);
			while (index.intValue() < numberIterations) {
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException ex) {
						// TODO
						ex.printStackTrace();
					}
				}
			}
			// TODO join all runners
		}
	}

	public void breakLoop() {
		breakLoop = true;
	}

	private class Runner implements Runnable {
		@Override
		public void run() {
			while (!breakLoop) {
				int idx = index.getAndIncrement();
				if (idx >= numberIterations) {
					break;
				}
				iterationDelegate.iteration(iterationValues[idx]);
			}
			synchronized (ParallelForInt.this) {
				ParallelForInt.this.notifyAll();
			}
		}
	}
}