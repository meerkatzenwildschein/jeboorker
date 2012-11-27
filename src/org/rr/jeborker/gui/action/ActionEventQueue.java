package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingWorker;

import org.rr.jeborker.db.DefaultDBManager;



public class ActionEventQueue {
	
	private static SwingWorker<Void, Void> worker = null;
	
	private static final List<QueueableActionHolder> queue = Collections.synchronizedList(new ArrayList<QueueableActionHolder>());
	
	static synchronized void addActionEvent(final ApplicationAction action, final ActionEvent event) {
		final QueueableActionHolder holder = new QueueableActionHolder(action, event);
		queue.add(holder);
		
		if(worker == null) {
			// Construct a new SwingWorker
			worker = new SwingWorker<Void, Void>() {
	
				@Override
				protected Void doInBackground() {
					while(queue.size() > 0) {
						DefaultDBManager.getInstance().setLocalThreadDbInstance();
						QueueableActionHolder removed = queue.remove(0);
						removed.action.invokeRealAction(removed.event);
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
		final ApplicationAction action;
		final ActionEvent event;
		QueueableActionHolder(final ApplicationAction action, final ActionEvent event) {
			this.action = action;
			this.event = event;
		}
	}
}
