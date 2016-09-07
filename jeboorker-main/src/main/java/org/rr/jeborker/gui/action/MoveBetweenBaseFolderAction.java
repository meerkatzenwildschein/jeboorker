package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.collection.TransformValueList;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.app.FileRefreshBackground;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class MoveBetweenBaseFolderAction extends AbstractAction {

	private String targetBasePath;
	
	MoveBetweenBaseFolderAction(String targetBasePath) {
		this.targetBasePath = targetBasePath;
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("import_16.png"));
		putValue(Action.NAME, targetBasePath);
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(SHORT_DESCRIPTION, Bundle.getString("MoveBetweenBaseFolderAction.tooltip")); //tooltip
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		FileRefreshBackground.runWithDisabledRefresh(new Runnable() {
			
			@Override
			public void run() {
				MainController controller = MainController.getController();
				int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();
				List<EbookPropertyItem> selectedEbookPropertyItems = controller.getModel().getEbookPropertyItemsAt(selectedEbookPropertyItemRows);
				IResourceHandler targetFolderResourceHandler = ResourceHandlerFactory.getResourceHandler(targetBasePath);

				for (int i = 0; i < selectedEbookPropertyItemRows.length; i++) {
					try {
						EbookPropertyItem ebookPropertyItem = selectedEbookPropertyItems.get(i);
						IResourceHandler sourceResourceHandler = ebookPropertyItem.getResourceHandler();
						IResourceHandler targetResourceHandler = ResourceHandlerFactory.getResourceHandler(targetFolderResourceHandler, sourceResourceHandler.getName());
						if(targetResourceHandler.exists()) {
							LoggerFactory.log(Level.WARNING, this, "Failed to move " + sourceResourceHandler + " to " + targetBasePath + ". Target file already exists.");
							continue;
						}
						
						sourceResourceHandler.moveTo(targetResourceHandler, false);
						ActionUtils.refreshFileSystemResourceParent(sourceResourceHandler.getParentResource());
						
						ebookPropertyItem.setBasePath(targetBasePath);
						ebookPropertyItem.setFile(targetResourceHandler.toFile().getAbsolutePath());
						
						DefaultDBManager.getInstance().updateObject(ebookPropertyItem);
						
						ActionUtils.refreshEbookPropertyItem(ebookPropertyItem, targetResourceHandler);
					} catch(Exception ex) {
						LoggerFactory.log(Level.WARNING, this, "Failed to move file to " + targetBasePath, ex);
					}
				}
				refreshFileSystemResourceParents(selectedEbookPropertyItems);			}
		});
	}

	private static void refreshFileSystemResourceParents(List<EbookPropertyItem> resources) {
		ActionUtils.refreshFileSystemResourceParents(new TransformValueList<EbookPropertyItem, IResourceHandler>(resources) {
	
			@Override
			public IResourceHandler transform(EbookPropertyItem source) {
				return source.getResourceHandler();
			}
		});
	}

}
