package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class FileSystemCollapseAllAction extends AbstractAction {

	private String treeName;
	
	FileSystemCollapseAllAction(String treeName) {
		this.treeName = treeName;
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("collapse_16.png"));
//		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("collapse_22.png"));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(SHORT_DESCRIPTION, Bundle.getString("FileSystemCollapseAllAction.tooltip")); //tooltip
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		MainController controller = MainController.getController();
		controller.getMainTreeController().collapseAllTreeNodes(treeName);
	}

}
