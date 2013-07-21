package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.db.DefaultDBManager;

class ActionEventQueue {

	static synchronized void addActionEvent(final ApplicationAction action, final ActionEvent event) {
    	addActionEvent(action, event, null);
    }
    
	static synchronized void addActionEvent(final ApplicationAction action, final ActionEvent event, final Runnable invokeLater) {
		Jeboorker.APPLICATION_THREAD_POOL.submit(new Runnable() {
			@Override
			public void run() {
				DefaultDBManager.setDefaultDBThreadInstance();
				action.invokeRealAction(event);
				if(invokeLater != null) {
					invokeLater.run();
				}
			}
		});
	}

}
