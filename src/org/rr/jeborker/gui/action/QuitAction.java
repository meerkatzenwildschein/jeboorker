package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.JEBorkerUtils;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.event.QueueableAction;
import org.rr.jeborker.gui.JEBorkerMainController;

public class QuitAction extends QueueableAction {

	private static final long serialVersionUID = -6464113132395695332L;

	QuitAction(String text) {
		if(text==null) {
			putValue(Action.NAME, Bundle.getString("QuitAction.name"));
		} else {
			putValue(Action.NAME, text);
		}
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("quit_16.gif")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(Bundle.getResource("quit_22.gif")));		
	}
	
	@Override
	public void doAction(ActionEvent e) {
		JEBorkerMainController.getController().dispose();
		try {
		DefaultDBManager.getInstance().shutdown();
		} catch(Exception e1) {
			LoggerFactory.logWarning(this, "Database shutdown failed.", e1);
		}
		
		JEBorkerUtils.shutdown();
		System.exit(0);
	}
	

}
