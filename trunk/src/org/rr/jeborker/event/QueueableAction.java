package org.rr.jeborker.event;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public abstract class QueueableAction extends AbstractAction {

	private static final long serialVersionUID = -3236971067162416069L;

	public abstract void doAction(ActionEvent e);
	
	@Override
	public void actionPerformed(ActionEvent e) {
		ActionEventQueue.addActionEvent(this, e);
	}
}
