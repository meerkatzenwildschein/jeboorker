package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.event.ApplicationEvent;
import org.rr.jeborker.event.DefaultApplicationEventListener;
import org.rr.jeborker.event.EventManager;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMonitor;
import org.rr.jeborker.gui.resources.ImageResourceBundle;
import org.rr.jeborker.metadata.MetadataHandlerFactory;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;

/**
 * Action which saves the current selected metadata entries in the metadata sheet.
 */
class SaveMetadataAction extends AbstractAction {

	private static final long serialVersionUID = 1208674185052606967L;

	private static SaveMetadataAction saveMetadataEntryAction = null;

	private SaveMetadataAction() {
		String name = Bundle.getString("SaveMetadataAction.name");
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("save_16.png"));
//		putValue(Action.LARGE_ICON_KEY, new ImageIcon(Bundle.getResource("save_22.png")));
		putValue(ApplicationAction.SINGLETON_ACTION_KEY, Boolean.TRUE); //Singleton instance!!
//		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(SHORT_DESCRIPTION, Bundle.getString("SaveMetadataAction.tooltip")); //tooltip
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
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
				controller.saveMetadataProperties(-1, -1); //save selected properties
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
			final List<IResourceHandler> itemResourceList = EbookPropertyItemUtils.createIResourceHandlerList(evt.getItems());

			if(MetadataHandlerFactory.hasWriterSupport(itemResourceList)) {
				final ApplicationAction action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null);
				final Property metadataProperty = evt.getMetadataProperty();
				boolean isEditable = isPropertyEditable(metadataProperty) || isPropertyDeletable(metadataProperty);
				if(metadataProperty == null || !isEditable) {
					action.setEnabled(false);
				} else {
					action.setEnabled(true);
				}
			}
		}

		private boolean isPropertyEditable(Property metadataProperty) {
			return metadataProperty instanceof DefaultProperty ? ((DefaultProperty) metadataProperty).isEditable() : true;
		}
		
		private boolean isPropertyDeletable(Property metadataProperty) {
			return metadataProperty instanceof DefaultProperty ? ((DefaultProperty) metadataProperty).isDeletable() : true;
		}
		
		@Override
		public void ebookItemSelectionChanged(ApplicationEvent evt) {
			final ApplicationAction action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null);
			action.setEnabled(false);
		}
	}

}
