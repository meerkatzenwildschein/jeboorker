package org.rr.jeborker.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.app.FileRefreshBackground;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.model.EbookSheetPropertyModel;
import org.rr.jeborker.metadata.IMetadataWriter;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

import com.l2fprod.common.propertysheet.Property;

class MainControllerUtils {
	
	/**
	 * Writes the given l2fprod sheet properties as metadata to the ebook.
	 * @param properties The properties to be written.
	 */
	static void writeProperties(final List<MetadataProperty> properties, List<IResourceHandler> ebookResources) {
		if(properties==null || properties.isEmpty()) {
			return; //nothing to do.
		}
		
		if(ebookResources != null && !ebookResources.isEmpty()) {
			final IMetadataWriter writer = MetadataHandlerFactory.getWriter(ebookResources);
			if(writer != null) {
				try {
					FileRefreshBackground.setDisabled(true);
					writer.writeMetadata(properties);
					
					//now the data was written, it's time to refresh the database entry
					final List<EbookPropertyItem> selectedEbookPropertyItems = MainController.getController().getSelectedEbookPropertyItems();
					final List<IResourceHandler> selectedEbookResources = new ArrayList<>();
					for(EbookPropertyItem selectedEbookPropertyItem : selectedEbookPropertyItems) {
						selectedEbookResources.add(selectedEbookPropertyItem.getResourceHandler());
					}

					final List<IResourceHandler> notSelectedEbookResources = ListUtils.difference(selectedEbookResources, ebookResources);
					for (IResourceHandler notSelectedEbookResource : notSelectedEbookResources) {
						List<EbookPropertyItem> items = EbookPropertyItemUtils.getEbookPropertyItemByResource(notSelectedEbookResource);
						selectedEbookPropertyItems.addAll(items);
					}
					
					for (EbookPropertyItem item : selectedEbookPropertyItems) {
						EbookPropertyItemUtils.refreshEbookPropertyItem(item, item.getResourceHandler(), false);
						DefaultDBManager.getInstance().updateObject(item);
					}
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							MainController.getController().refreshTableSelectedItem(true);
						}
					});
				} catch(Exception e) {
					 LoggerFactory.getLogger().log(Level.WARNING, "Could not write multiple Metadata", e);
				} finally {
					FileRefreshBackground.setDisabled(false);
				}
			}
		}
	}

	/**
	 * Unwraps the {@link MetadataProperty} from the given {@link Property} list.
	 * @param properties The properties to be unwrapped.
	 * @return The desired {@link MetadataProperty}
	 */
	static ArrayList<MetadataProperty> createMetadataProperties(final List<Property> properties) {
		final ArrayList<MetadataProperty> target = new ArrayList<>();
		for (Property property : properties) {
			
			final EbookSheetPropertyModel model = MainController.getController().getPropertySheetHandler().getModel();
			List<MetadataProperty> metadataProperties = model.getMetadataProperties(property);
			for (MetadataProperty metadataProperty : metadataProperties) {
				target.add(metadataProperty);
			}
		}
		return target;
	}

}
