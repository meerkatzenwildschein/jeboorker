package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.event.ApplicationEvent;
import org.rr.jeborker.event.DefaultApplicationEventListener;
import org.rr.jeborker.event.EventManager;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class SyncFolderAction extends AbstractAction {

	private static SyncFolderAction basePathFolderSyncAction;
	
	SyncFolderAction() {
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("sync_16.png"));
		putValue(ApplicationAction.SINGLETON_ACTION_KEY, Boolean.TRUE); //Singleton instance!!
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(SHORT_DESCRIPTION, Bundle.getString("SyncFolderAction.tooltip")); //tooltip
		setEnabled(false);
		initListener();
	}
	
	static SyncFolderAction getInstance() {
		if(basePathFolderSyncAction == null) {
			basePathFolderSyncAction = new SyncFolderAction();
		}
		return basePathFolderSyncAction;
	}
	
	private void initListener() {
		EventManager.addListener(new SyncButtonApplicationEventListener());
	}	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		MainController controller = MainController.getController();
		List<EbookPropertyItem> selectedEbookPropertyItems = controller.getSelectedEbookPropertyItems();
		if(selectedEbookPropertyItems.size() == 1) {
			IResourceHandler resource = selectedEbookPropertyItems.get(0).getResourceHandler();
			controller.getMainTreeHandler().addExpandedTreeItems(Arrays.asList(resource));
		}
	}
	
	private static class SyncButtonApplicationEventListener extends DefaultApplicationEventListener {

		@Override
		public void ebookItemSelectionChanged(ApplicationEvent evt) {
			setupButtonEnablement();
		}

		protected void setupButtonEnablement() {
			MainController controller = MainController.getController();
			List<EbookPropertyItem> selectedEbookPropertyItems = controller.getSelectedEbookPropertyItems();
			final ApplicationAction action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SYNC_FOLDER_ACTION, null);
			if(selectedEbookPropertyItems.size() == 1) {
				if(locatedInHiddenFolder(selectedEbookPropertyItems.get(0)) && controller.getMainTreeHandler().isFileTreeSelected()) {
					action.setEnabled(false);
				} else {
					action.setEnabled(true);
				}
			} else {
				action.setEnabled(false);
			}
		}
		
		@Override
		public void mainTreeVisibilityChanged(ApplicationEvent evt) {
			setupButtonEnablement();
		}

		private boolean locatedInHiddenFolder(EbookPropertyItem item) {
			IResourceHandler resourceHandler = item.getResourceHandler();
			while((resourceHandler = resourceHandler.getParentResource()) != null) {
				if(resourceHandler.isHidden()) {
					return true;
				}
			}
			return false;
		}
	}
	

}
