package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MetadataDownloadController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.IMetadataWriter;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;
import org.rr.jeborker.metadata.MetadataUtils;


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
				List<MetadataProperty> availableMetadata = new ArrayList<MetadataProperty>(reader.getMetadataByType(false, allMetaData, type));
				List<String> downloadedValues = metadataDownloadController.getFilteredValues(type);
				for(int i = 0; i < downloadedValues.size(); i++) {
					String downloadedValue = downloadedValues.get(i);
					change = setMetadataValue(downloadedValue, availableMetadata, allMetaData, newMetadata, reader, type, i);
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
	
	private boolean setMetadataValue(String value, List<MetadataProperty> availableMetadata, final List<MetadataProperty> allMetaData, final List<MetadataProperty> newMetadata, final IMetadataReader reader, IMetadataReader.METADATA_TYPES type, int num) {
		boolean result = false;
		if(!availableMetadata.isEmpty()) {
			//set the value to the existing ones.
			List<MetadataProperty> sameMetadata = MetadataUtils.getSameProperties(availableMetadata.get(0), availableMetadata);
			availableMetadata.removeAll(sameMetadata);
			for(MetadataProperty same : sameMetadata) {
				same.setValue(value, 0);
				allMetaData.remove(same);
				newMetadata.add(same);
				result = true;
			}
		} else {
			//create a new metadata
			availableMetadata = reader.getMetadataByType(true, Collections.<MetadataProperty>emptyList(), type);
			if(!availableMetadata.isEmpty()) {
				MetadataProperty metadataProperty = availableMetadata.get(0);
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
