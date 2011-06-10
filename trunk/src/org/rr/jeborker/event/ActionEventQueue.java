package org.rr.jeborker.event;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingWorker;


public class ActionEventQueue {
	
	private static SwingWorker<Void, Void> worker = null;
	
	private static final List<QueueableActionHolder> queue = Collections.synchronizedList(new ArrayList<QueueableActionHolder>());
	
	public static synchronized void addActionEvent(final QueueableAction action, final ActionEvent event) {
		final QueueableActionHolder holder = new QueueableActionHolder(action, event);
		queue.add(holder);
		
		if(worker == null) {
			// Construct a new SwingWorker
			worker = new SwingWorker<Void, Void>() {
	
				@Override
				protected Void doInBackground() {
					while(queue.size()>0) {
						QueueableActionHolder removed = queue.remove(0);
						removed.action.doAction(removed.event);
					}
					
					return null;
				}
	
				@Override
				protected void done() {
					worker = null;
				}
			};
			worker.execute();
		}
	}
	
	private static class QueueableActionHolder {
		final QueueableAction action;
		final ActionEvent event;
		QueueableActionHolder(final QueueableAction action, final ActionEvent event) {
			this.action = action;
			this.event = event;
		}
	}
}
