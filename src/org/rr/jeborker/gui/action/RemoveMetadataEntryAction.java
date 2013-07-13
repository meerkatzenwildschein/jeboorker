package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.collection.TransformValueList;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.event.ApplicationEvent;
import org.rr.jeborker.event.DefaultApplicationEventListener;
import org.rr.jeborker.event.EventManager;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;
import org.rr.jeborker.metadata.MetadataHandlerFactory;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;

/**
 * Action which removes the currently selected metadata entry in the metadata sheet.
 */
class RemoveMetadataEntryAction extends AbstractAction {

	private static final long serialVersionUID = 1208674185052606967L;
	
	private static RemoveMetadataEntryAction removeMetadataEntryAction = null;
	
	private RemoveMetadataEntryAction() {
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("remove_16.png"));
//		putValue(Action.LARGE_ICON_KEY, new ImageIcon(Bundle.getResource("remove_22.png")));
		putValue(ApplicationAction.SINGLETON_ACTION_KEY, Boolean.TRUE); //Singleton instance!!
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(SHORT_DESCRIPTION, Bundle.getString("RemoveMetadataEntryAction.tooltip")); //tooltip
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
			final List<IResourceHandler> itemResourceList = new TransformValueList<EbookPropertyItem, IResourceHandler>(evt.getItems()) {

				@Override
				public IResourceHandler transform(EbookPropertyItem source) {
					return source.getResourceHandler();
				}
			};
			
			if(MetadataHandlerFactory.hasWriterSupport(itemResourceList)) {
				final ApplicationAction action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_METADATA_ENTRY_ACTION, null);
				final int[] selectedEbookPropertyItemRows = MainController.getController().getSelectedEbookPropertyItemRows();
				if(selectedEbookPropertyItemRows.length == 1) {
					final Property metadataProperty = evt.getMetadataProperty();
					boolean isDeletable = metadataProperty instanceof DefaultProperty ? ((DefaultProperty)metadataProperty).isDeletable() : true;
					if(metadataProperty == null || !isDeletable) {
						action.setEnabled(false);
					} else {
						action.setEnabled(true);
					}
				} else {
					action.setEnabled(false);
				}
			}
		}		
	}

}
