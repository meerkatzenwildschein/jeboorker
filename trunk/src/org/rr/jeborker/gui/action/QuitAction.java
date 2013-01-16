package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.jeborker.gui.resources.ImageResourceBundle;


class QuitAction extends AbstractAction {

	private static final long serialVersionUID = -6464113132395695332L;

	QuitAction(String text) {
		if(text==null) {
			putValue(Action.NAME, Bundle.getString("QuitAction.name"));
		} else {
			putValue(Action.NAME, text);
		}
		putValue(Action.SMALL_ICON, new ImageIcon(ImageResourceBundle.getResource("quit_16.png")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(ImageResourceBundle.getResource("quit_22.png")));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		ActionUtils.quit();
	}
	

}
