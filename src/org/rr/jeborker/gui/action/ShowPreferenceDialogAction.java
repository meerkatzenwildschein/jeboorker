package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.common.swing.SwingUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;


class ShowPreferenceDialogAction extends AbstractAction {

	private static final long serialVersionUID = -6464113132395695332L;

	ShowPreferenceDialogAction(String text) {
		String name = Bundle.getString("ShowPreferenceAction.name");
		if(text == null) {
			putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		} else {
			putValue(Action.NAME, text);
		}
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("preferences_16.png"));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		MainController.getController().getPreferenceController().showPreferenceDialog();
	}
	

}
