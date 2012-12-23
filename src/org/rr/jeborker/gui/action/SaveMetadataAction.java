package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.event.ApplicationEvent;
import org.rr.jeborker.event.DefaultApplicationEventListener;
import org.rr.jeborker.event.EventManager;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMonitor;

import com.l2fprod.common.propertysheet.DefaultProperty;

/**
 * Action which saves the current selected metadata entries in the metadata sheet.
 */
class SaveMetadataAction extends AbstractAction {

	private static final long serialVersionUID = 1208674185052606967L;
	
	private static SaveMetadataAction saveMetadataEntryAction = null;
	
	private SaveMetadataAction() {
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("save_16.gif")));
		putValue(ApplicationAction.SINGLETON_ACTION_KEY, Boolean.TRUE); //Singleton instance!!
//		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(SHORT_DESCRIPTION, Bundle.getString("SaveMetadataAction.tooltip")); //tooltip
		putValue(Action.NAME, Bundle.getString("SaveMetadataAction.name"));
		setEnabled(false);
		initListener();
	}
	
	static SaveMetadataAction getInstance() {
		if(saveMetadataEntryAction == null) {
			saveMetadataEntryAction = new SaveMetadataAction();
		}
		return saveMetadataEntryAction;
	}	
	
	private void initListener() {
		EventManager.addListener(new SaveMetadataEntryApplicationEventListener());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final ApplicationAction action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null);
		
		if(action.isEnabled()) {
			final MainController controller = MainController.getController();
			final MainMonitor progressMonitor = controller.getProgressMonitor();
			progressMonitor.monitorProgressStart(Bundle.getString("SaveMetadataAction.message"));
			try {
				controller.saveProperties(-1, -1); //save selected properties
				action.setEnabled(false);
			} catch(Exception ex) {
				LoggerFactory.getLogger().log(Level.WARNING, "Saving metadata has failed.", ex);
			} finally {
				progressMonitor.monitorProgressStop();
			}
		}
	}
	
	private static class SaveMetadataEntryApplicationEventListener extends DefaultApplicationEventListener {
		
		@Override
		public void metaDataSheetContentChanged(ApplicationEvent evt) {
			final ApplicationAction action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null);
			boolean isEditable = evt.getMetadataProperty() instanceof DefaultProperty ? ((DefaultProperty)evt.getMetadataProperty()).isEditable() : true;
			if(evt.getMetadataProperty() == null || !isEditable) {
				action.setEnabled(false);
			} else {
				action.setEnabled(true);
			}
		}

		@Override
		public void ebookItemSelectionChanged(ApplicationEvent evt) {
			final ApplicationAction action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null);
			action.setEnabled(false);
		}
	}

}
