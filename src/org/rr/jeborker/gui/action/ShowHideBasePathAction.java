package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.event.QueueableAction;

/**
 * Add a folder action.
 */
class ShowHideBasePathAction extends QueueableAction {

	private static final long serialVersionUID = -9066575818229620987L;
	
	private String path;
	
	ShowHideBasePathAction(String text) {
		super();
		if(text==null) {
			throw new RuntimeException("Need to specify path.");
		} else {
			putValue(Action.NAME, text);
			if(ResourceHandlerFactory.hasResourceLoader(text)) {
				path = text;
			}
		}
	}
	
	@Override
	public void doAction(ActionEvent event) {
		ActionUtils.toggleBasePathVisibility(path);
	}
	
}
