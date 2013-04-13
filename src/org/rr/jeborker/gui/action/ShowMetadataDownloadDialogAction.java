package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.common.swing.SwingUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ListUtils;
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
			final List<MetadataProperty> allMetaData = reader.readMetaData();
			final List<MetadataProperty> newMetadata = new ArrayList<MetadataProperty>();
			final IMetadataWriter writer = MetadataHandlerFactory.getWriter(resourceHandler);
			
			boolean change = false;
			for(IMetadataReader.METADATA_TYPES type : IMetadataReader.METADATA_TYPES.values()) {
				List<MetadataProperty> metadata = reader.getMetadataByType(true, allMetaData, type);
				List<String> values = metadataDownloadController.getFileteredValues(type);
				for(int i = 0; i < values.size(); i++) {
					String value = values.get(i);
					change = setMetadataValue(value, metadata, allMetaData, newMetadata, reader, type, i);
				}
			}
			newMetadata.addAll(allMetaData);
			
			byte[] coverImage = transferCoverImageMetadata(metadataDownloadController, reader, newMetadata);
			
			if(change || coverImage != null) {
				writer.writeMetadata(newMetadata);
				ActionUtils.refreshEbookPropertyItem(ebookItem, resourceHandler);
				
				IResourceHandler virtualImageResourceHandler = ResourceHandlerFactory.getVirtualResourceHandler(UUID.randomUUID().toString(), coverImage);
				MainController.getController().setImageViewerResource(virtualImageResourceHandler);
			}
		}
	}
	
	private boolean setMetadataValue(String value, List<MetadataProperty> metadata, final List<MetadataProperty> allMetaData, final List<MetadataProperty> newMetadata, final IMetadataReader reader, IMetadataReader.METADATA_TYPES type, int num) {
		MetadataProperty metadataProperty = ListUtils.get(metadata, num);
		boolean result = false;
		if(metadataProperty != null) {
			String metadataPropertyValueAsString = metadataProperty.getValueAsString();
			
			//search for entries with the same value to be set to the new value
			for(int i = num; i < metadata.size(); i++) {
				if(metadata.get(i) != null && metadata.get(i).getValueAsString().equals(metadataPropertyValueAsString)) {
					metadata.get(i).setValue(value, 0);
					newMetadata.add(metadataProperty);
					allMetaData.remove(metadata.get(i));
					result = true;
				}
			}
		} else {
			//create a new one
			metadata = reader.getMetadataByType(true, Collections.<MetadataProperty>emptyList(), type);
			if(!metadata.isEmpty()) {
				metadataProperty = metadata.get(0);
				metadataProperty.setValue(value, 0);
				newMetadata.add(metadataProperty);
				result = true;		
			}
		}
		
		return result;
	}

	/**
	 * Transfers the cover from the metadata downloader to the metadata of the ebook. 
	 * @return the cover image bytes if there is a cover to download and set or <code>null</code> if
	 *     there is no cover or the cover checkbox in the gui wasn't checked.
	 */
	private byte[] transferCoverImageMetadata(final MetadataDownloadController metadataDownloadController, final IMetadataReader reader,
			final List<MetadataProperty> readMetaData) {
		byte[] coverImage = metadataDownloadController.getCoverImage();
		if(coverImage != null) {
			List<MetadataProperty> coverMetadataList = reader.getMetadataByType(true, readMetaData, IMetadataReader.METADATA_TYPES.COVER);
			if(!coverMetadataList.isEmpty()) {
				MetadataProperty metadataProperty = coverMetadataList.get(0);
				metadataProperty.setValue(coverImage, 0);
			}
		}
		return coverImage;
	}
	

}
