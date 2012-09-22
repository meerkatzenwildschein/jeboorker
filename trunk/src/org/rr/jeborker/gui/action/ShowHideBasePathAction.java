package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.mufs.ResourceHandlerFactory;

/**
 * Add a folder action.
 */
class ShowHideBasePathAction extends AbstractAction {

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
	public void actionPerformed(ActionEvent e) {
		ActionUtils.toggleBasePathVisibility(path);
	}
	
}
