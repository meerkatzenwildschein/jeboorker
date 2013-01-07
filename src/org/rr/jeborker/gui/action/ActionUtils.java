package org.rr.jeborker.gui.action;

import static org.rr.jeborker.JeboorkerConstants.MIME_CBZ;
import static org.rr.jeborker.JeboorkerConstants.MIME_EPUB;
import static org.rr.jeborker.JeboorkerConstants.MIME_PDF;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.QueryCondition;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMenuBarController;
import org.rr.jeborker.gui.MainMonitor;
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
		
		System.exit(0);		
	}

	/**
	 * Refreshes the entries for the given handler.
	 * @param resourceLoader The handler of the entry to be refreshed.
	 */
	static void refreshEntry(IResourceHandler resourceLoader) {	
		final List<EbookPropertyItem> items = EbookPropertyItemUtils.getEbookPropertyItemByResource(resourceLoader);
		Iterator<EbookPropertyItem> iterator = items.iterator();
		if(iterator.hasNext()) {
			EbookPropertyItem item = iterator.next();
			refreshEbookPropertyItem(item, resourceLoader);
		}		
	}
	
	/**
	 * Refresh the given {@link EbookPropertyItem}. Does also persist it to the db!
	 * @param item The item to be refreshed
	 * @param resourceLoader The IResourceHandler for the given item. Can be <code>null</code>.
	 */
	static void refreshEbookPropertyItem(final EbookPropertyItem item, IResourceHandler resourceLoader) {
		if(resourceLoader == null) {
			resourceLoader = ResourceHandlerFactory.getResourceLoader(item.getFile());
		}
		
		//remove the entry from db and view.
		if(!resourceLoader.exists()) {
			ActionUtils.removeEbookPropertyItem(item);
			return;
		} else {
			EbookPropertyItemUtils.refreshEbookPropertyItem(item, resourceLoader, true);
			DefaultDBManager.getInstance().updateObject(item);
		
			if(isSelectedItem(item)) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						MainController.getController().refreshTableSelectedItem(true);
					}
				});
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						int row = MainController.getController().getTableModel().searchRow(item);
						MainController.getController().refreshTableItem(new int[] {row}, false);
					}
				});				
			}
			
		}
	}
	
	/**
	 * Tests if the given item is a selected one in the view.
	 * @param item The item to be tested if it's selected.
	 * @return <code>true</code> if the given item is selected in the view and false otherwise.
	 */
	private static boolean isSelectedItem(final EbookPropertyItem item) {
		List<EbookPropertyItem> selectedEbookPropertyItems = MainController.getController().getSelectedEbookPropertyItems();	
		for(EbookPropertyItem selected : selectedEbookPropertyItems) {
			if(item.equals(selected)) {
				return true;
			}
		}
		return false;
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
	
	/**
	 * Deletes the given item from the database and the view.
	 * @param item The item to be deleted.
	 */
	public static void removeEbookPropertyItem(final EbookPropertyItem item) {
		final MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
		final MainController controller = MainController.getController();
		
		progressMonitor.setMessage(Bundle.getFormattedString("RemoveBasePathAction.deleting", item.getFileName()));
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				boolean removed = controller.removeEbookPropertyItem(item);
				if(!removed) {
					DefaultDBManager.getInstance().deleteObject(item);
				}
				progressMonitor.setMessage(Bundle.getFormattedString("RemoveBasePathAction.deleted", item.getFileName()));
			}
		});
	}	
	
	/**
	 * Tells if this {@link AResourceHandler} instance file format is an image.
	 * @return <code>true</code> if the resource is an image or <code>false</code> otherwise.
	 */
	public static boolean isSupportedEbookFormat(IResourceHandler resource) {
		final String mime = resource.getMimeType();
		if(mime==null || mime.length()==0) {
			return false;
		}
		
		if(mime.equals(MIME_EPUB)) {
			return true;
		} else if(mime.equals(MIME_PDF)) {
			return true;
		} else if(mime.equals(MIME_CBZ)) {
			return true;
		}
		return false;
	}		
}
