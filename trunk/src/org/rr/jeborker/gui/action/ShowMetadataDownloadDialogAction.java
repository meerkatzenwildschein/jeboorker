package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.common.swing.SwingUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
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
			
			byte[] coverImage = transferCoverImageMetadata(metadataDownloadController, reader, readMetaData);
			
			if(change || coverImage != null) {
				writer.writeMetadata(readMetaData);
				ActionUtils.refreshEbookPropertyItem(ebookItem, resourceHandler);
				
				IResourceHandler virtualImageResourceHandler = ResourceHandlerFactory.getVirtualResourceHandler(UUID.randomUUID().toString(), coverImage);
				MainController.getController().setImageViewerResource(virtualImageResourceHandler);
			}
		}
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
