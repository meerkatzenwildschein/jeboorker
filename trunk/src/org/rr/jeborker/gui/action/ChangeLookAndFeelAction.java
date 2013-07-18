package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.gui.MainController;


class ChangeLookAndFeelAction extends AbstractAction {

	private static final long serialVersionUID = -6464113152395695332L;

	private String laf;
	
	ChangeLookAndFeelAction(String text) {
		this.laf = text;
		putValue(Action.NAME, text);
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JFrame mainWindow = MainController.getController().getMainWindow();
		try {
			String lafClassName = Jeboorker.LOOK_AND_FEELS.get(laf);
			if(lafClassName != null) {
				UIManager.setLookAndFeel(lafClassName);
				JeboorkerPreferences.addEntryAsString(JeboorkerPreferences.PREFERENCE_KEYS.LOOK_AND_FEEL, lafClassName);
			} else {
				LoggerFactory.getLogger().log(Level.WARNING, "Could not set look and feel " + laf);
			}
		} catch (Exception ex) {
			LoggerFactory.getLogger().log(Level.WARNING, "Could not set look and feel " + laf, ex);
		} 
		SwingUtilities.updateComponentTreeUI(mainWindow);
		mainWindow.pack();
	}
	

}
