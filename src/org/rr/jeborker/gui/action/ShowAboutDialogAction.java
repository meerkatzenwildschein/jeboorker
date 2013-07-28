package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.gui.AboutDialogController;


class ShowAboutDialogAction extends AbstractAction {

	private static final long serialVersionUID = -6464113132395695332L;

	ShowAboutDialogAction() {
		String name = Bundle.getString("ShowAboutDialogAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
//		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("view_log_16.png"));
//		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("view_log_22.png"));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		AboutDialogController aboutDialog = new AboutDialogController();
		aboutDialog.showAboutDialog();
	}
	

}
