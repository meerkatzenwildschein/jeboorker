package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.jeborker.event.ApplicationEvent;
import org.rr.jeborker.event.DefaultApplicationEventListener;
import org.rr.jeborker.event.EventManager;
import org.rr.jeborker.gui.MainController;

/**
 * Action which removes the currently selected metadata entry in the metadata sheet.
 */
public class RemoveMetadataEntryAction extends AbstractAction {

	private static final long serialVersionUID = 1208674185052606967L;
	
	private static RemoveMetadataEntryAction removeMetadataEntryAction = null;
	
	private RemoveMetadataEntryAction() {
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("remove_16.gif")));
		setEnabled(false);
		initListener();
	}
	
	private void initListener() {
		EventManager.addListener(new DefaultApplicationEventListener() {
			
			@Override
			public void metaDataSheetSelectionChanged(ApplicationEvent evt) {
				if(evt.getMetadataProperty() == null || !evt.getMetadataProperty().isEditable()) {
					RemoveMetadataEntryAction.this.setEnabled(false);
				} else {
					RemoveMetadataEntryAction.this.setEnabled(true);
				}
			}
		});
	}
	
	static RemoveMetadataEntryAction getInstance() {
		if(removeMetadataEntryAction==null) {
			removeMetadataEntryAction = new RemoveMetadataEntryAction();
		}
		return removeMetadataEntryAction;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MainController.getController().removeSelectedMetadataProperty();
	}

}
