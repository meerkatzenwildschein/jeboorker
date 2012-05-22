package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.Action;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.db.QueryCondition;
import org.rr.jeborker.event.QueueableAction;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMenuController;

/**
 * Add a folder action.
 */
class ShowHideBasePathAction extends QueueableAction {

	private static final long serialVersionUID = -9066575818229620987L;
	
	private final String QUERY_IDENTIFER;
	
	private String path;
	
	ShowHideBasePathAction(String text) {
		super();
		this.QUERY_IDENTIFER = ShowHideBasePathAction.class.getName() + "_" + text;
		if(text==null) {
			throw new RuntimeException("Need to specify path.");
		} else {
			putValue(Action.NAME, text);
			if(ResourceHandlerFactory.hasResourceLoader(text)) {
				path = text;
			}
		}
	}
	
	@Override
	public void doAction(ActionEvent event) {
		final MainController controller = MainController.getController();
		try {
			boolean isShow = MainMenuController.getController().isShowHideBasePathStatusShow(path);
			QueryCondition queryCondition = controller.getTableModel().getQueryCondition();
			queryCondition.removeConditionByIdentifier(QUERY_IDENTIFER); //remove possibly existing queries.
			if(isShow) {
				queryCondition.addAndChild(new QueryCondition("basePath", path, "<>", QUERY_IDENTIFER));
			}
				
			MainMenuController.getController().setShowHideBasePathStatusShow(path, !isShow);
		} catch (Exception ex) {
			LoggerFactory.log(Level.WARNING, this, "Path " + path, ex);
		} finally {
			controller.refreshTable(true);
		}
	}
	
}
