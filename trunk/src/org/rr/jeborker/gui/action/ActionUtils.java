package org.rr.jeborker.gui.action;

import java.util.Iterator;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.JeboorkerUtils;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.QueryCondition;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMenuController;
import org.rr.jeborker.gui.model.EbookSheetProperty;
import org.rr.jeborker.metadata.MetadataProperty;

import com.l2fprod.common.propertysheet.Property;

public class ActionUtils {
	
	/**
	 * Adds a metadata item to the sheet.
	 * @param property The property to be added.
	 */
	static Property addMetadataItem(MetadataProperty property) {
		final Property createProperty = EbookSheetProperty.createProperty(property, 0);
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
	
	static void toggleBasePathVisibility(String path) {
		final String queryIdentifier = ShowHideBasePathAction.class.getName() + "_" + path;
		final MainController controller = MainController.getController();
		try {
			boolean isShow = MainMenuController.getController().isShowHideBasePathStatusShow(path);
			QueryCondition queryCondition = controller.getTableModel().getQueryCondition();
			queryCondition.removeConditionByIdentifier(queryIdentifier); //remove possibly existing queries.
			if(isShow) {
				queryCondition.addAndChild(new QueryCondition("basePath", path, "<>", queryIdentifier));
			}
				
			MainMenuController.getController().setShowHideBasePathStatusShow(path, !isShow);
		} catch (Exception ex) {
			LoggerFactory.log(Level.WARNING, ActionUtils.class, "Path " + path, ex);
		} finally {
			controller.refreshTable(true);
		}
	}	
	
}
