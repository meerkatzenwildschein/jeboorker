package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.MainController;


class ChangeLookAndFeelAction extends AbstractAction {

	private static final long serialVersionUID = -6464113152395695332L;

	private String lafName;
	
	ChangeLookAndFeelAction(String text) {
		this.lafName = text;
		putValue(Action.NAME, text);
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final JFrame mainWindow = MainController.getController().getMainWindow();
		try {
			SwingUtils.resetColors();
			String lafClassName = JeboorkerConstants.LOOK_AND_FEELS.get(lafName);
			if(lafClassName != null) {
				ActionUtils.setLookAndFeel(lafClassName);
				SwingUtilities.updateComponentTreeUI(mainWindow);
				PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.PREFERENCE_KEYS.LOOK_AND_FEEL)
					.addEntryAsString(PreferenceStoreFactory.PREFERENCE_KEYS.LOOK_AND_FEEL, lafClassName);
			} else {
				LoggerFactory.getLogger().log(Level.WARNING, "Could not set look and feel " + lafName);
			}
		} catch (Exception ex) {
			LoggerFactory.getLogger().log(Level.WARNING, "Could not set look and feel " + lafName, ex);
		} 
		SwingUtilities.updateComponentTreeUI(mainWindow);
		mainWindow.pack();
	}

}
