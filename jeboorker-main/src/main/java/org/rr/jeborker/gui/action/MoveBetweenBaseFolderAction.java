package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class MoveBetweenBaseFolderAction extends AbstractAction {

	private String targetBasePath;
	
	MoveBetweenBaseFolderAction(String targetBasePath) {
		this.targetBasePath = targetBasePath;
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("import_16.png"));
		putValue(Action.NAME, targetBasePath);
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(SHORT_DESCRIPTION, Bundle.getString("MoveBetweenBaseFolderAction.tooltip")); //tooltip
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		MainController controller = MainController.getController();
		int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();
		List<EbookPropertyItem> selectedEbookPropertyItems = controller.getModel().getEbookPropertyItemsAt(selectedEbookPropertyItemRows);
		ActionUtils.moveEbookResources(selectedEbookPropertyItems, targetBasePath, true);
	}

}
