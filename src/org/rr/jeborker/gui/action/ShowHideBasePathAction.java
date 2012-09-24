package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.JeboorkerPreferences;

/**
 * Add a folder action.
 */
class ShowHideBasePathAction extends AbstractAction {

	private static final long serialVersionUID = -9066575818229620987L;
	
	private static final String HIDE_ALL = "hideall";
	
	private static final String SHOW_ALL = "showall";
	
	private String path;
	
	ShowHideBasePathAction(String text) {
		super();
		if(text==null) {
			throw new RuntimeException("Need to specify path.");
		} else {
			if(SHOW_ALL.equals(text)) {
				path = text;
				putValue(Action.NAME, Bundle.getString("ShowHideBasePathAction.showall.name"));
			} else if(HIDE_ALL.equals(text)) {
				path = text;
				putValue(Action.NAME, Bundle.getString("ShowHideBasePathAction.hideall.name"));
			} else if(ResourceHandlerFactory.hasResourceLoader(text)) {
				path = text;
				putValue(Action.NAME, text);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final List<String> basePaths = JeboorkerPreferences.getBasePath();
		if(SHOW_ALL.equals(path)) {
			for (String basePath : basePaths) {
				ActionUtils.setBasePathVisibility(basePath, true);
			}
		} else if(HIDE_ALL.equals(path)) {
			for (String basePath : basePaths) {
				ActionUtils.setBasePathVisibility(basePath, false);
			}
		} else {
			ActionUtils.toggleBasePathVisibility(path);
		}
	}
	
}
