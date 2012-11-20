package org.rr.jeborker.gui.action;

import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.JeboorkerUtils;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.QueryCondition;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMenuBarController;
import org.rr.jeborker.gui.model.EbookSheetPropertyModel;
import org.rr.jeborker.metadata.MetadataProperty;

import com.l2fprod.common.propertysheet.Property;

public class ActionUtils {
	
	/**
	 * Adds a metadata item to the sheet.
	 * @param property The property to be added.
	 */
	static Property addMetadataItem(MetadataProperty property, EbookPropertyItem item) {
		final Property createProperty = EbookSheetPropertyModel.createProperty(property.clone(), Collections.singletonList(item), 0);
		final MainController controller = MainController.getController();
		controller.addMetadataProperty(createProperty);
		return createProperty;
	}
	
	static void quit() {
		MainController.getController().dispose();
		try {
		DefaultDBManager.getInstance().shutdown();
		} catch(Exception e1) {
			LoggerFactory.logWarning(ActionUtils.class, "Database shutdown failed.", e1);
		}
		
		JeboorkerUtils.shutdown();
		System.exit(0);		
	}

	/**
	 * Refreshes the entries for the given handler.
	 * @param handler The handler of the entry to be refreshed.
	 */
	static void refreshEntry(IResourceHandler handler) {	
		final DefaultDBManager defaultDBManager = DefaultDBManager.getInstance();
		final Iterable<EbookPropertyItem> items = defaultDBManager.getObject(EbookPropertyItem.class, "file", handler.toString());
		Iterator<EbookPropertyItem> iterator = items.iterator();
		if(iterator.hasNext()) {
			EbookPropertyItem item = iterator.next();
			RefreshBasePathAction.refreshEbookPropertyItem(item, handler);
		}		
	}
	
	/**
	 * Toggles the visibility of the given base path entry. If it's visible
	 * it's set to hide an other way round. 
	 * @param path The path to be toggled.
	 */
	static void toggleBasePathVisibility(String path) {
		final boolean isShow = MainMenuBarController.getController().isShowHideBasePathStatusShow(path);
		setBasePathVisibility(path, !isShow);
	}	
	
	/**
	 * Sets the visibility of the given base path. 
	 * @param path path which visibility should be set. 
	 * @param show <code>true</code> if the base path should be shown and <code>false</code> for hide it.
	 */
	static void setBasePathVisibility(String path, boolean show) {
		final String queryIdentifier = ShowHideBasePathAction.class.getName() + "_" + path;
		final MainController controller = MainController.getController();
		try {
			QueryCondition queryCondition = controller.getTableModel().getQueryCondition();
			queryCondition.removeConditionByIdentifier(queryIdentifier); //remove possibly existing queries.
			if(!show) {
				queryCondition.addAndChild(new QueryCondition("basePath", path, "<>", queryIdentifier));
			}
				
			MainMenuBarController.getController().setShowHideBasePathStatusShow(path, show);
		} catch (Exception ex) {
			LoggerFactory.log(Level.WARNING, ActionUtils.class, "Path " + path, ex);
		} finally {
			controller.refreshTable(true);
		}
	}		
	
}
