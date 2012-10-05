package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.jeborker.event.ApplicationEvent;
import org.rr.jeborker.event.DefaultApplicationEventListener;
import org.rr.jeborker.event.EventManager;
import org.rr.jeborker.gui.MainController;

import com.l2fprod.common.propertysheet.DefaultProperty;

/**
 * Action which removes the currently selected metadata entry in the metadata sheet.
 */
class RemoveMetadataEntryAction extends AbstractAction {

	private static final long serialVersionUID = 1208674185052606967L;
	
	private static RemoveMetadataEntryAction removeMetadataEntryAction = null;
	
	private RemoveMetadataEntryAction() {
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("remove_16.gif")));
		putValue(ApplicationAction.SINGLETON_ACTION_KEY, Boolean.TRUE); //Singleton instance!!
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		setEnabled(false);
		initListener();
	}
	
	static RemoveMetadataEntryAction getInstance() {
		if(removeMetadataEntryAction==null) {
			removeMetadataEntryAction = new RemoveMetadataEntryAction();
		}
		return removeMetadataEntryAction;
	}	
	
	private void initListener() {
		EventManager.addListener(new RemoveMetadataEntryApplicationEventListener());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MainController.getController().removeSelectedMetadataProperty();
	}
	
	private static class RemoveMetadataEntryApplicationEventListener extends DefaultApplicationEventListener {
		
		@Override
		public void metaDataSheetSelectionChanged(ApplicationEvent evt) {
			final ApplicationAction action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_METADATA_ENTRY_ACTION, null);
			boolean isDeletable = evt.getMetadataProperty() instanceof DefaultProperty ? ((DefaultProperty)evt.getMetadataProperty()).isDeletable() : true;
			if(evt.getMetadataProperty() == null || !isDeletable) {
				action.setEnabled(false);
			} else {
				action.setEnabled(true);
			}
		}		
	}

}
