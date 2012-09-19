package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.Action;

public class ApplicationAction {
	
	private final Action realAction;
	
	ApplicationAction(Action realAction) {
		this.realAction = realAction;
	}
	
	public Action getAction() {
		return this.realAction;
	}

	public void invokeAction() {
		this.realAction.actionPerformed(null);
	}
	
	public void invokeAction(ActionEvent e) {
		this.realAction.actionPerformed(e);
	}	
	
}
