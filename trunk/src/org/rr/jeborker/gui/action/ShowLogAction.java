package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.jeborker.gui.LogMonitorController;


class ShowLogAction extends AbstractAction {

	private static final long serialVersionUID = -6464113132395695332L;

	ShowLogAction(String text) {
		if(text==null) {
			putValue(Action.NAME, Bundle.getString("ShowLogAction.name"));
		} else {
			putValue(Action.NAME, text);
		}
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("view_log_16.gif")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(Bundle.getResource("view_log_22.gif")));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		LogMonitorController.getInstance().showLogMonitorDialog();
	}
	

}
