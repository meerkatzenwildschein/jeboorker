package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class ApplicationAction extends AbstractAction {
	
	private final Action realAction;
	
	ApplicationAction(Action realAction) {
		this.realAction = realAction;
		this.setEnabled(realAction.isEnabled());
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

	@Override
	public void actionPerformed(ActionEvent e) {
		realAction.actionPerformed(e);
	}	
	
	@Override
	public Object getValue(String key) {
		return realAction.getValue(key);
	}
}
