package org.rr.jeborker.gui.action;

import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.swing.dialogs.SimpleInputDialog;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.event.RefreshAbstractAction;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.IMetadataWriter;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;
import org.rr.jeborker.metadata.MetadataUtils;

abstract class ASetCommonMetadataAction extends RefreshAbstractAction implements IDoOnlyOnceAction<SimpleInputDialog> {

	/**
	 * Transfers the given value to each entry in the given MetadataProperty list.
	 * @param value The value to be set.
	 * @param specificMetaData The metadata getting the value.
	 */
	protected void transferValueToMetadata(final String value, List<MetadataProperty> specificMetaData) {
		for (MetadataProperty authorMetadataProperty : specificMetaData) {
			List<String> authors = ListUtils.split(value, ',');
			for (int i = 0; i < authors.size(); i++) {
				authorMetadataProperty.setValue(authors.get(i), i);
			}
		}
	}
	

	/**
	 * Merge and write the metadata.
	 * @param writer  Writer to be used for writing the metadata
	 * @param allMetaData The complete metadata set for the file. 
	 * @param specificMetaData The metadata which should be added or overwrites this ones in the allMetaData parameter.
	 */
	protected void mergeAndWrite(final IMetadataWriter writer, List<MetadataProperty> allMetaData, List<MetadataProperty> specificMetaData) {
		List<MetadataProperty> mergeMetadata = MetadataUtils.mergeMetadata(allMetaData, specificMetaData);
		writer.writeMetadata(mergeMetadata);
	}	
	
	protected void setMetaData(IResourceHandler resourceHandler, IMetadataReader.METADATA_TYPES type) {
		final MainController controller = MainController.getController();
		try {
			final List<EbookPropertyItem> items = DefaultDBManager.getInstance().getObject(EbookPropertyItem.class, "file", resourceHandler.toString());
			
			if(!items.isEmpty()) {
				final EbookPropertyItem item = items.get(0);
				
				SimpleInputDialog inputDialog = this.doOnce();
				if (inputDialog.getReturnValue() == JFileChooser.APPROVE_OPTION) {
					final IMetadataWriter writer = MetadataHandlerFactory.getWriter(resourceHandler);
					final IMetadataReader reader = MetadataHandlerFactory.getReader(resourceHandler);
					final String input = inputDialog.getInput();
					
					//get author metadata an set the entered author.
					List<MetadataProperty> readMetaData = reader.readMetaData();
					List<MetadataProperty> metaData = reader.getMetadataByType(true, readMetaData, type);
					
					transferValueToMetadata(input, metaData);

					mergeAndWrite(writer, readMetaData, metaData);
					
					//do some refresh to the changed entry.
					RefreshBasePathAction.refreshEbookPropertyItem(item, resourceHandler);
					
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							MainController.getController().refreshTableRows(getSelectedRowsToRefresh(), true);
						}
					});
				}
			} else {
				LoggerFactory.logInfo(this, "No database item found for " + resourceHandler, null);
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "could not set author for " + resourceHandler, e);
		} finally {
			controller.getProgressMonitor().monitorProgressStop(null);
		}
	}		
}
