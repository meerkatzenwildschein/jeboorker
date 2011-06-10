package org.rr.jeborker.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.JEBorkerPreferences;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.model.EbookSheetProperty;
import org.rr.jeborker.metadata.IMetadataWriter;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

import com.l2fprod.common.propertysheet.Property;

class JEBorkerMainControllerUtils {
	
	/**
	 * Writes the application properties to the preference file
	 */
	static void storeApplicationProperties(JEborkerMainView mainWindow) {
		JEBorkerPreferences.addEntryNumber("mainWindowSizeWidth", mainWindow.getSize().width);
		JEBorkerPreferences.addEntryNumber("mainWindowSizeHeight", mainWindow.getSize().height);
		JEBorkerPreferences.addEntryNumber("mainWindowLocationX", mainWindow.getLocation().x);
		JEBorkerPreferences.addEntryNumber("mainWindowLocationY", mainWindow.getLocation().y);
		JEBorkerPreferences.addEntryNumber("mainWindowDividerLocation", Double.valueOf(mainWindow.mainSplitPane.getDividerLocation()));
		JEBorkerPreferences.addEntryNumber("lastRowCount", Integer.valueOf(mainWindow.table.getRowCount()));
		JEBorkerPreferences.addEntryNumber("descriptionDividerLocation", Integer.valueOf(mainWindow.propertySheet.getDescriptionDividerLocation()));
		JEBorkerPreferences.addEntryNumber("propertySheetImageSplitPaneDividerLocation", Integer.valueOf(mainWindow.propertySheetImageSplitPane.getDividerLocation()));
	}
	
	/**
	 * Restores the application properties 
	 */
	static void restoreApplicationProperties(JEborkerMainView mainWindow) {
		//restore the window size from the preferences.
		Number mainWindowSizeWidth = JEBorkerPreferences.getEntryAsNumber("mainWindowSizeWidth");
		Number mainWindowSizeHeight = JEBorkerPreferences.getEntryAsNumber("mainWindowSizeHeight");
		if(mainWindowSizeWidth!=null && mainWindowSizeHeight!=null) {
			mainWindow.setSize(mainWindowSizeWidth.intValue(), mainWindowSizeHeight.intValue());
		}
		
		//restore window location
		Point entryAsScreenLocation = JEBorkerPreferences.getEntryAsScreenLocation("mainWindowLocationX", "mainWindowLocationY");
		if(entryAsScreenLocation != null) {
			mainWindow.setLocation(entryAsScreenLocation);
		}
		
		//restore the divider location at the main window
		final Number mainWindowDividerLocation = JEBorkerPreferences.getEntryAsNumber("mainWindowDividerLocation");
		if(mainWindowDividerLocation!=null) {
			mainWindow.mainSplitPane.setDividerLocation(mainWindowDividerLocation.intValue());
		}
		
		//restore the divider location in the property sheet 
		final Number descriptionDividerLocation = JEBorkerPreferences.getEntryAsNumber("descriptionDividerLocation");
		if(descriptionDividerLocation!=null) {
			mainWindow.propertySheet.setDescriptionDividerLocation(descriptionDividerLocation.intValue());
		}
		
		final Number propertySheetImageSplitPaneDividerLocation = JEBorkerPreferences.getEntryAsNumber("propertySheetImageSplitPaneDividerLocation");
		if(propertySheetImageSplitPaneDividerLocation!=null) {
			mainWindow.propertySheetImageSplitPane.setDividerLocation(propertySheetImageSplitPaneDividerLocation.intValue());
		}			
	}
	
	/**
	 * Writes the given l2fprod sheet properties as metadata to the ebook.
	 * @param properties The properties to be written.
	 */
	static void writeProperties(final Property[] properties) {
		if(properties==null || properties.length==0) {
			return; //nothing to do.
		}
		
		//search for the property which has the ebook file as value
		IResourceHandler ebook = null;
		for (int i = 0; i < properties.length; i++) {
			if(properties[i].getValue() instanceof IResourceHandler) {
				ebook = (IResourceHandler) properties[i].getValue();
				break;
			}
		}
		
		if(ebook!=null) {
			final IMetadataWriter writer = MetadataHandlerFactory.getWriter(ebook);
			if(writer!=null) {
				try {
					final ArrayList<MetadataProperty> target = new ArrayList<MetadataProperty>();
					for (Property property : properties) {
						if(property instanceof EbookSheetProperty) {
							MetadataProperty[] metadataProperties = ((EbookSheetProperty)property).getMetadataProperties();
							for (MetadataProperty metadataProperty : metadataProperties) {
								target.add(metadataProperty);
							}
						}
					}
					writer.writeMetadata(target.iterator());
					
					//now the data was written, it's time to refresh the database entry
					List<EbookPropertyItem> items = DefaultDBManager.getInstance().getObject(EbookPropertyItem.class, "file", ebook.toString());
					for (EbookPropertyItem item : items) {
						EbookPropertyItemUtils.refreshEbookPropertyItem(item, ebook, false);						
					}
				} finally {
					writer.dispose();
				}
			}
		}
	}	
}
