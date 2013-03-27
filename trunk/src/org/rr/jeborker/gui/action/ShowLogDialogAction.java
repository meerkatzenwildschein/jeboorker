package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.common.swing.SwingUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;


class ShowLogDialogAction extends AbstractAction {

	private static final long serialVersionUID = -6464113132395695332L;

	ShowLogDialogAction(String text) {
		String name = Bundle.getString("ShowLogAction.name");
		if(text == null) {
			putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		} else {
			putValue(Action.NAME, text);
		}
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("view_log_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("view_log_22.png"));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		MainController.getController().getLoggerController().showLoggerDialog();
	}
	

}
