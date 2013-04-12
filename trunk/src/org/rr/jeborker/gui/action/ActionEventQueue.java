package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.rr.jeborker.db.DefaultDBManager;

public class ActionEventQueue {

	public static final ExecutorService APPLICATION_THREAD_POOL = new ThreadPoolExecutor(0, 1024,
            60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ApplicationThreadFactory()) {};

	static synchronized void addActionEvent(final ApplicationAction action, final ActionEvent event) {
		APPLICATION_THREAD_POOL.submit(new Runnable() {
			@Override
			public void run() {
				DefaultDBManager.setDefaultDBThreadInstance();
				action.invokeRealAction(event);
			}
		});
	}

	private static class ApplicationThreadFactory implements ThreadFactory {
		
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		
		private final ThreadGroup group;
		
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		
		private final String namePrefix;

		ApplicationThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
		}

		public Thread newThread(final Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon()) {
				t.setDaemon(false);
			}
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}
	}

}
