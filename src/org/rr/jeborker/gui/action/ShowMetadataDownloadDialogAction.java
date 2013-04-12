package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.common.swing.SwingUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MetadataDownloadController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.IMetadataWriter;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;


class ShowMetadataDownloadDialogAction extends AbstractAction {

	private static final long serialVersionUID = -6464113132395695332L;

	ShowMetadataDownloadDialogAction(String text) {
		String name = Bundle.getString("ShowMetadataDownloadDialogAction.name");
		if(text == null) {
			putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		} else {
			putValue(Action.NAME, text);
		}
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("download_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("download_22.png"));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final MainController controller = MainController.getController();
		final MetadataDownloadController metadataDownloadController = controller.getMetadataDownloadController();
		final List<EbookPropertyItem> selectedEbookPropertyItems = controller.getSelectedEbookPropertyItems();
		
		if(!selectedEbookPropertyItems.isEmpty()) {
			metadataDownloadController.showDialog();
			
			if(metadataDownloadController.isConfirmed()) {
				transferMetadata(metadataDownloadController, selectedEbookPropertyItems);
			}
		}
	}

	/**
	 * Set/replace the metadata of the ebook files given with the <code>ebookPropertyItems</code> parameter
	 * from the given {@link MetadataDownloadController} where the user has selected some data.
	 */
	private void transferMetadata(final MetadataDownloadController metadataDownloadController, final List<EbookPropertyItem> ebookPropertyItems) {
		for(EbookPropertyItem ebookItem : ebookPropertyItems) {
			IResourceHandler resourceHandler = ebookItem.getResourceHandler();
			final IMetadataReader reader = MetadataHandlerFactory.getReader(resourceHandler);
			final List<MetadataProperty> readMetaData = reader.readMetaData();
			final IMetadataWriter writer = MetadataHandlerFactory.getWriter(resourceHandler);
			
			boolean change = false;
			for(IMetadataReader.METADATA_TYPES type : IMetadataReader.METADATA_TYPES.values()) {
				List<MetadataProperty> metadata = reader.getMetadataByType(true, readMetaData, type);
				if(!metadata.isEmpty()) {
					String value = metadataDownloadController.getValue(type);
					if(value != null) {
						for(MetadataProperty meta : metadata) {
							if(!meta.getValueAsString().equals(value)) {
								meta.setValue(value, 0);
								if(!readMetaData.contains(metadata)) {
									readMetaData.add(meta);
								}
								change = true;
							}
						}
					}
				}
			}
			
			if(change) {
				writer.writeMetadata(readMetaData);
				ActionUtils.refreshEbookPropertyItem(ebookItem, resourceHandler);
			}
		}
	}
	

}
