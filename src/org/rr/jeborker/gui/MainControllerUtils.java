package org.rr.jeborker.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.swing.components.tree.TreeUtil;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.jeborker.app.FileRefreshBackground;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
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
	 * Writes the application properties to the preference file
	 */
	static void storeApplicationProperties(MainView mainWindow) {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		preferenceStore.addGenericEntryAsNumber("mainWindowSizeWidth", mainWindow.getSize().width);
		preferenceStore.addGenericEntryAsNumber("mainWindowSizeHeight", mainWindow.getSize().height);
		preferenceStore.addGenericEntryAsNumber("mainWindowLocationX", mainWindow.getLocation().x);
		preferenceStore.addGenericEntryAsNumber("mainWindowLocationY", mainWindow.getLocation().y);
		preferenceStore.addGenericEntryAsNumber("mainWindowDividerLocation", CommonUtils.toNumber(mainWindow.mainSplitPane.getDividerLocation()));
		preferenceStore.addGenericEntryAsNumber("lastRowCount", Integer.valueOf(mainWindow.mainTable.getRowCount()));
		preferenceStore.addGenericEntryAsNumber("descriptionDividerLocation", Integer.valueOf(mainWindow.propertySheet.getDescriptionDividerLocation()));
		preferenceStore.addGenericEntryAsNumber("treeMainTableDividerLocation", Integer.valueOf(mainWindow.treeMainTableSplitPane.getDividerLocation()));
		preferenceStore.addGenericEntryAsNumber("propertySheetImageSplitPaneDividerLocation", Integer.valueOf(mainWindow.propertySheetImageSplitPane.getDividerLocation()));
		preferenceStore.addGenericEntryAsString("basePathTreeSelection", TreeUtil.getExpansionStates(mainWindow.basePathTree));
		preferenceStore.addGenericEntryAsString("fileSystemTreeSelection", TreeUtil.getExpansionStates(mainWindow.fileSystemTree));
	}
	
	/**
	 * Restores the application properties 
	 */
	static void restoreApplicationProperties(MainView mainWindow) {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		
		//restore the window size from the preferences.
		Number mainWindowSizeWidth = preferenceStore.getGenericEntryAsNumber("mainWindowSizeWidth");
		Number mainWindowSizeHeight = preferenceStore.getGenericEntryAsNumber("mainWindowSizeHeight");
		if(mainWindowSizeWidth!=null && mainWindowSizeHeight!=null) {
			mainWindow.setSize(mainWindowSizeWidth.intValue(), mainWindowSizeHeight.intValue());
		}
		
		//restore window location
		Point entryAsScreenLocation = preferenceStore.getGenericEntryAsScreenLocation("mainWindowLocationX", "mainWindowLocationY");
		if(entryAsScreenLocation != null) {
			mainWindow.setLocation(entryAsScreenLocation);
		}
		
		//restore the divider location at the main window
		final Number mainWindowDividerLocation = preferenceStore.getGenericEntryAsNumber("mainWindowDividerLocation");
		if(mainWindowDividerLocation != null) {
			int add = 0;
			if(ReflectionUtils.getOS() == ReflectionUtils.OS_LINUX) {
				//however, the splitpane has a difference of 9 between setting and getting the location.				
				add = -9;
			}
			mainWindow.mainSplitPane.setDividerLocation(mainWindowDividerLocation.intValue() + add);
		}
		
		//restore the divider location at the main window
		final Number treeMainTableDividerLocation = preferenceStore.getGenericEntryAsNumber("treeMainTableDividerLocation");
		if(treeMainTableDividerLocation != null) {
			int add = 0;
			if(ReflectionUtils.getOS() == ReflectionUtils.OS_LINUX) {
				//however, the splitpane has a difference of 9 between setting and getting the location.				
//				add = 9;
			}
			mainWindow.treeMainTableSplitPane.setDividerLocation(treeMainTableDividerLocation.intValue() + add);
		}
		
		//restore the divider location in the property sheet 
		final Number descriptionDividerLocation = preferenceStore.getGenericEntryAsNumber("descriptionDividerLocation");
		if(descriptionDividerLocation != null) {
			mainWindow.propertySheet.setDescriptionDividerLocation(descriptionDividerLocation.intValue());
		}
		
		final Number propertySheetImageSplitPaneDividerLocation = preferenceStore.getGenericEntryAsNumber("propertySheetImageSplitPaneDividerLocation");
		if (propertySheetImageSplitPaneDividerLocation != null) {
			mainWindow.propertySheetImageSplitPane.setDividerLocation(propertySheetImageSplitPaneDividerLocation.intValue());
		}
		
		final String basePathTreeSelection = preferenceStore.getGenericEntryAsString("basePathTreeSelection");
		if(basePathTreeSelection != null) {
			TreeUtil.restoreExpanstionState(mainWindow.basePathTree, basePathTreeSelection);
		}
		
		final String fileSystemTreeSelection = preferenceStore.getGenericEntryAsString("fileSystemTreeSelection");
		if(fileSystemTreeSelection != null) {
			TreeUtil.restoreExpanstionState(mainWindow.fileSystemTree, fileSystemTreeSelection);
		}
	}
	
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
					final List<IResourceHandler> selectedEbookResources = new ArrayList<IResourceHandler>();
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
		final ArrayList<MetadataProperty> target = new ArrayList<MetadataProperty>();
		for (Property property : properties) {
			
			final EbookSheetPropertyModel model = MainController.getController().getEbookSheetPropertyModel();
			List<MetadataProperty> metadataProperties = model.getMetadataProperties(property);
			for (MetadataProperty metadataProperty : metadataProperties) {
				target.add(metadataProperty);
			}
		}
		return target;
	}

}
