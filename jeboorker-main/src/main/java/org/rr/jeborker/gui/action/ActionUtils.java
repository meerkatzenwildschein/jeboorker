package org.rr.jeborker.gui.action;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.app.BasePathList;
import org.rr.jeborker.app.FileRefreshBackground;
import org.rr.jeborker.app.FileWatchService;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMenuBarController;
import org.rr.jeborker.gui.MainMonitor;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;
import org.rr.jeborker.gui.model.EbookSheetPropertyModel;
import org.rr.jeborker.gui.model.ReloadableTableModel;
import org.rr.jeborker.metadata.MetadataProperty;

import com.j256.ormlite.stmt.Where;
import com.l2fprod.common.propertysheet.Property;

public class ActionUtils {

	/**
	 * Adds a metadata item to the sheet.
	 * @param property The property to be added.
	 */
	static Property addMetadataItem(final MetadataProperty property, final EbookPropertyItem item) {
		final Property createProperty = EbookSheetPropertyModel.createProperty(property.clone(), Collections.singletonList(item), 0);
		final MainController controller = MainController.getController();
		controller.addMetadataProperty(createProperty);
		return createProperty;
	}

	/**
	 * Shutdown the application.
	 */
	static void quit() {
		MainController.getController().dispose();
		FileWatchService.shutdownWatchService();
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
	static void refreshEntry(final IResourceHandler resourceLoader) {
		final List<EbookPropertyItem> items = EbookPropertyItemUtils.getEbookPropertyItemByResource(resourceLoader);
		Iterator<EbookPropertyItem> iterator = items.iterator();
		if(iterator.hasNext()) {
			EbookPropertyItem item = iterator.next();
			refreshEbookPropertyItem(item, resourceLoader);
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					refreshFileSystemResourceParent(resourceLoader);
				}
			});
		}
	}

	/**
	 * Refresh the given {@link EbookPropertyItem}. Does also persist it to the db!
	 * @param item The item to be refreshed
	 * @param resourceLoader The IResourceHandler for the given item. Can be <code>null</code>.
	 */
	static void refreshEbookPropertyItem(final EbookPropertyItem item, final IResourceHandler resourceHandler) {
		final IResourceHandler refreshResourceHandler;
		if(resourceHandler == null) {
			refreshResourceHandler = ResourceHandlerFactory.getResourceHandler(item.getFile());
		} else {
			refreshResourceHandler = resourceHandler;
		}

		//remove the entry from db and view.
		if(!refreshResourceHandler.exists()) {
			ActionUtils.removeEbookPropertyItem(item);
			return;
		} else {
			EbookPropertyItemUtils.refreshEbookPropertyItem(item, refreshResourceHandler, true);
			DefaultDBManager.getInstance().updateObject(item);

			if(isSelectedItem(item)) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						MainController.getController().refreshTableSelectedItem(true);
						refreshFileSystemResourceParent(refreshResourceHandler);
					}
				});
			} else {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						ReloadableTableModel model = MainController.getController().getModel();
						int row = model.searchRow(item);
						MainController.getController().refreshTableItem(new int[] {row}, false);
						refreshFileSystemResourceParent(refreshResourceHandler);
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
	public static void toggleBasePathVisibility(String path) {
		final boolean isShow = MainMenuBarController.getController().isShowHideBasePathStatusShow(path);
		setBasePathVisibility(path, !isShow);
	}

	/**
	 * Sets the visibility of the given base path.
	 * @param path path which visibility should be set.
	 * @param show <code>true</code> if the base path should be shown and <code>false</code> for hide it.
	 */
	public static void setBasePathVisibility(final String path, boolean show) {
		final String queryIdentifier = ShowHideBasePathAction.class.getName() + "_" + path;
		final MainController controller = MainController.getController();
		try {
			EbookPropertyDBTableModel model = controller.changeToDatabaseModel();
			model.removeWhereCondition(queryIdentifier); //remove possibly existing queries.
			if(!show) {
				model.addWhereCondition(new EbookPropertyDBTableModel.EbookPropertyDBTableModelQuery() {

					@Override
					public String getIdentifier() {
						return queryIdentifier;
					}

					@Override
					public void appendQuery(Where<EbookPropertyItem, EbookPropertyItem> where) throws SQLException {
						where.ne("basePath", path);
					}
				});
			}

			MainMenuBarController.getController().setShowHideBasePathStatusShow(path, show);
		} catch (Exception ex) {
			LoggerFactory.log(Level.WARNING, ActionUtils.class, "Path " + path, ex);
		} finally {
			controller.getEbookTableHandler().refreshTable();
		}
	}

	/**
	 * Adds the given item to the database and to the ui.
	 * @param item The item to be added.
	 */
	public static void addAndStoreEbookPropertyItem(EbookPropertyItem item) {
		addAndStoreEbookPropertyItem(item, -1);
	}

	/**
	 * Adds the given item to the database and to the ui.
	 * @param item The item to be added.
	 * @param row The row where the item should be added to.
	 */
	public static void addAndStoreEbookPropertyItem(EbookPropertyItem item, int row) {
		MainController.getController().getProgressMonitor().setMessage(Bundle.getFormattedString("AddBasePathAction.add", item.getFileName()));
		storeEbookPropertyItem(item);
		addEbookPropertyItem(item, row);
	}

	/**
	 * Stores the given {@link EbookPropertyItem} in the database.
	 * @param item The database item to be stored.
	 */
	private static void storeEbookPropertyItem(EbookPropertyItem item) {
		DefaultDBManager.getInstance().storeObject(item);
	}

	/**
	 * Adds the given {@link EbookPropertyItem} to the ui model.
	 * @param item The item to be added.
	 * @param row The row where the item should be added. Can be -1 if it should be attached somewhere.
	 */
	public static void addEbookPropertyItem(final EbookPropertyItem item, final int row) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				MainController.getController().addEbookPropertyItem(item, row);
			}
		});
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
				controller.getMainTreeHandler().refreshFileSystemTreeEntry(item.getResourceHandler());
				if(!removed) {
					DefaultDBManager.getInstance().deleteObject(item);
				}
				progressMonitor.setMessage(Bundle.getFormattedString("RemoveBasePathAction.deleted", item.getFileName()));
			}
		});
	}

	/**
	 * Tells if this {@link AResourceHandler} instance file format is an image.
	 * @param force Open the given {@link IResourceHandler} and read magic bytes if the ebook mime could not be detected.
	 * @return <code>true</code> if the resource is an image or <code>false</code> otherwise.
	 */
	public static boolean isSupportedEbookFormat(IResourceHandler resource, boolean force) {
		if(resource.getFileExtension().equals("tmp")) {
			//no tmp files. tmp files are used to save a changed ebook temporary.
			return false;
		}

		if(isSupportedEbookFormatExtension(resource)) {
			return true;
		}

		if(isSupportedEbookFormatMime(resource, force)) {
			return true;
		}

		return false;
	}

	/**
	 * user file formats with basic/empty reader support
	 */
	private static boolean isSupportedEbookFormatExtension(IResourceHandler resource) {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		String supportedBasicTypes = preferenceStore.getEntryAsString(PreferenceStoreFactory.PREFERENCE_KEYS.BASIC_FILE_TYPES);
		String extension = resource.getFileExtension();
		if(!extension.isEmpty() && (supportedBasicTypes.startsWith(extension) || supportedBasicTypes.contains("," + extension))) {
			return true;
		}
		return false;
	}

	private static boolean isSupportedEbookFormatMime(IResourceHandler resource, boolean force) {
		final String mime = resource.getMimeType(force);
		if(StringUtil.isEmpty(mime)) {
			return false;
		}

		for(SUPPORTED_MIMES supportedMime : JeboorkerConstants.SUPPORTED_MIMES.values()) {
			if(supportedMime.getMime().equals(mime)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Imports the given transferedFiles {@link IResourceHandler} list into the given {@link IResourceHandler}  <code>targetRecourceDirectory</code>.
	 * The files will be copied and the {@link EbookPropertyItem}s will be created.
	 * @param deleteAnyway Deletes the resource after a successful import in any case.
	 * @return A list of all imported (target) file resources.
	 */
	public static List<IResourceHandler> importEbookResources(final int dropRow, final String basePath, final IResourceHandler targetRecourceDirectory,
			final List<IResourceHandler> sourceResourcesToTransfer, final boolean move) throws IOException {
		final ArrayList<IResourceHandler> importedResources = new ArrayList<>();

		Jeboorker.APPLICATION_THREAD_POOL.execute(new Runnable() {
			
			@Override
			public void run() {
				FileRefreshBackground.setDisabled(true);
				MainController controller = MainController.getController();
				MainMonitor progressMonitor = controller.getProgressMonitor();
				
				try {
					progressMonitor.monitorProgressStart("Start file copy");
					for(IResourceHandler sourceResource : sourceResourcesToTransfer) {
						IResourceHandler targetResource = ResourceHandlerFactory.getResourceHandler(targetRecourceDirectory, sourceResource.getName());
						progressMonitor.setMessage(Bundle.getFormattedString("CopyToTargetAction.copy", sourceResource.getName(), targetResource.getName()));
						if(isImportAllowed(sourceResource, targetResource)) {
							boolean transferSuccess = transferFile(sourceResource, targetResource, move);
							if(transferSuccess) {
								EbookPropertyItem newItem = EbookPropertyItemUtils.createEbookPropertyItem(targetResource, ResourceHandlerFactory.getResourceHandler(basePath));

								List<EbookPropertyItem> sourceItems = EbookPropertyItemUtils.getEbookPropertyItemByResource(sourceResource);
								if(ListUtils.isNotEmpty(sourceItems)) {
									ActionUtils.storeEbookPropertyItem(newItem);
									for(EbookPropertyItem sourceItem : sourceItems) {
										controller.removeEbookPropertyItem(sourceItem);
									}
								} else {
									ActionUtils.addAndStoreEbookPropertyItem(newItem, dropRow + 1);
								}
								controller.getMainTreeHandler().refreshFileSystemTreeEntry(targetRecourceDirectory);
								importedResources.add(targetResource);

								if(move) {
									refreshFileSystemResourceParents(Arrays.asList(sourceResource));
									removeMovedEbookResourceFromMainView(sourceResource);	
								}
							}
						} else {
							if(!ActionUtils.isSupportedEbookFormat(sourceResource, true)) {
								LoggerFactory.getLogger().log(Level.INFO, "Could not drop '" + sourceResource.getName() + "'. It's not a supported ebook format.");
							} else if(sourceResource.exists()){
								LoggerFactory.getLogger().log(Level.INFO, "File '" + sourceResource.getName() + "' already exists.");
							} else {
								LoggerFactory.getLogger().log(Level.INFO, "Could not drop '" + sourceResource.getName() + "'");
							}
						}
					}
				} catch (IOException e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to copy file " + basePath + " to " + targetRecourceDirectory, e);
				} finally {
					cleanup(sourceResourcesToTransfer, move, importedResources);

					progressMonitor.monitorProgressStop();
					FileRefreshBackground.setDisabled(false);
				}
			}

			protected void cleanup(final List<IResourceHandler> sourceResourcesToTransfer, final boolean move,
					final ArrayList<IResourceHandler> importedResources) {
				if(move) {
					refreshFileSystemResourceParents(sourceResourcesToTransfer);
				}
				
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						removeDeletedFileFromModelAndDatabase(importedResources);
						removeDeletedFileFromModelAndDatabase(sourceResourcesToTransfer);
					}
				});
			}
		});

		return importedResources;
	}
	
	public static void refreshFileSystemResourceParents(List<IResourceHandler> resources) {
		Set<IResourceHandler> alreadyRefreshedParents = new HashSet<>(resources.size());
		for (IResourceHandler resourceHandler : resources) {
			IResourceHandler parentResource = resourceHandler.getParentResource();
			if(!alreadyRefreshedParents.contains(parentResource)) {
				refreshFileSystemResourceParent(parentResource);
				alreadyRefreshedParents.add(parentResource);
			}
		}
	}

	public static void refreshFileSystemResourceParent(IResourceHandler parentResource) {
		MainController.getController().getMainTreeHandler().refreshFileSystemTreeEntry(parentResource);
	}
	
	/**
	 * If the main view shows a ebook entry from the file view and the book is imported into a managed folder, the ebook entry can be removed from the main view.
	 * 
	 * @param sourceResource
	 */
	private static void removeMovedEbookResourceFromMainView(IResourceHandler sourceResource) {
		MainController.getController().removeEbookPropertyItem(EbookPropertyItemUtils.createBasicEbookPropertyItem(sourceResource));
	}

	private static boolean transferFile(IResourceHandler sourceResource, IResourceHandler targetResource, boolean move) throws IOException {
		if(move) {
			sourceResource.moveTo(targetResource, false);
		} else {
			sourceResource.copyTo(targetResource, false);
		}
		return targetResource.exists();
	}

	private static boolean isImportAllowed(IResourceHandler sourceResource, IResourceHandler targetResource) {
		return sourceResource != null && ActionUtils.isSupportedEbookFormat(sourceResource, true) && !targetResource.exists();
	}

	/**
	 * Removes the deleted files from model and database if they're located in a base path and no longer exists
	 */
	private static void removeDeletedFileFromModelAndDatabase(List<IResourceHandler> movedEbookResources) {
		DefaultDBManager db = DefaultDBManager.getInstance();
		BasePathList basePathList = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getBasePath();
		for (IResourceHandler resource : movedEbookResources) {
			List<EbookPropertyItem> ebookPropertyItems = EbookPropertyItemUtils.getEbookPropertyItemByResource(resource);
			for (EbookPropertyItem item : ebookPropertyItems) {
				if (!resource.exists() && basePathList.getBasePathForFile(resource) != null) {
					db.deleteObject(item);
					FileRefreshBackground.getInstance().addEbook(item);
					LoggerFactory.getLogger().log(Level.INFO, "Removed deleted ebook " + resource);
				}
			}
		}
	}
}
