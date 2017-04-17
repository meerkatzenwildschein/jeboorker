package org.rr.jeborker.gui.action;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtil;
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
				applyBasePathFilter(path, queryIdentifier, model);
			}

			MainMenuBarController.getController().setShowHideBasePathStatusShow(path, show);
		} catch (Exception ex) {
			LoggerFactory.log(Level.WARNING, ActionUtils.class, "Path " + path, ex);
		} finally {
			controller.getEbookTableHandler().refreshTable();
		}
	}

	public static void applyBasePathFilter(final String path, final String queryIdentifier, EbookPropertyDBTableModel model) {
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
	
	public static void moveEbookResources(final List<EbookPropertyItem> sourceEbookPropertyItems, final String basePath, final boolean move) {
		FileRefreshBackground.runWithDisabledRefresh(new Runnable() {

			@Override
			public void run() {
				IResourceHandler targetFolderResourceHandler = ResourceHandlerFactory.getResourceHandler(basePath);
				List<IResourceHandler> resourcesToRefresh = new ArrayList<>();
				
				for (int i = 0; i < sourceEbookPropertyItems.size(); i++) {
					try {
						EbookPropertyItem ebookPropertyItem = sourceEbookPropertyItems.get(i);
						IResourceHandler sourceResourceHandler = ebookPropertyItem.getResourceHandler();
						IResourceHandler targetResourceHandler = ResourceHandlerFactory.getResourceHandler(targetFolderResourceHandler, sourceResourceHandler.getName());
						if (!isImportAllowed(sourceResourceHandler, targetResourceHandler)) {
							LoggerFactory.log(Level.WARNING, this, "Not allowed to move " + sourceResourceHandler + " to " + targetResourceHandler + ".");
							continue;
						}

						resourcesToRefresh.add(sourceResourceHandler);
						transferFile(sourceResourceHandler, targetResourceHandler, move);

						ebookPropertyItem.setBasePath(basePath);
						ebookPropertyItem.setFile(targetResourceHandler.toFile().getAbsolutePath());

						ActionUtils.refreshEbookPropertyItem(ebookPropertyItem, targetResourceHandler);
					} catch (Exception ex) {
						LoggerFactory.log(Level.WARNING, this, "Failed to move file to " + basePath, ex);
					}
				}
				refreshFileSystemResourceParents(resourcesToRefresh);
			}
		});		
	}
	
	public static List<IResourceHandler> importEbookResources(final List<IResourceHandler> sources, final String basePath, final boolean move) {
		return importEbookResources(sources, null, basePath, move);
	}
	
	public static List<IResourceHandler> importEbookResources(final List<IResourceHandler> sources, final IResourceHandler targetResourceDirectory, final String basePath, final boolean move) {
		final List<IResourceHandler> importedResources = new ArrayList<>();
		FileRefreshBackground.runWithDisabledRefresh(new Runnable() {

			@Override
			public void run() {
				IResourceHandler targetFolder;
				if(targetResourceDirectory != null) {
					targetFolder = targetResourceDirectory;
				} else {
					targetFolder = ResourceHandlerFactory.getResourceHandler(basePath);
				}
				
				for (int i = 0; i < sources.size(); i++) {
					try {
						IResourceHandler sourceResourceHandler = sources.get(i);
						IResourceHandler targetResourceHandler = ResourceHandlerFactory.getResourceHandler(targetFolder, sourceResourceHandler.getName());
						if (!isImportAllowed(sourceResourceHandler, targetResourceHandler)) {
							LoggerFactory.log(Level.WARNING, this, "Not allowed to move " + sourceResourceHandler + " to " + targetResourceHandler + ".");
							continue;
						}

						transferFile(sourceResourceHandler, targetResourceHandler, move);
						EbookPropertyItem ebookPropertyItem = EbookPropertyItemUtils.createEbookPropertyItem(targetResourceHandler, ResourceHandlerFactory.getResourceHandler(basePath));

						ActionUtils.refreshEbookPropertyItem(ebookPropertyItem, targetResourceHandler);
						importedResources.add(targetResourceHandler);
					} catch (Exception ex) {
						LoggerFactory.log(Level.WARNING, this, "Failed to move file to " + basePath, ex);
					}
				}
				refreshFileSystemResourceParents(sources);
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
	
	public static void applyFileNameFilter(final List<IResourceHandler> resources, final boolean regardBasePath) {
		EbookPropertyDBTableModel model = MainController.getController().changeToDatabaseModel();
		model.removeWhereConditions();
		model.addWhereCondition(new EbookPropertyDBTableModel.EbookPropertyDBTableModelQuery() {

			@Override
			public String getIdentifier() {
				return getClass().getName();
			}

			@Override
			public void appendQuery(Where<EbookPropertyItem, EbookPropertyItem> where) throws SQLException {
				APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
				String filter = StringUtils.join(resources, ",");
				LoggerFactory.getLogger().log(Level.INFO, "Apply file name filter '" + filter + "'");
				for (int i = 0; i < resources.size(); i++) {
					IResourceHandler resource = resources.get(i);
					if(i == 0) {
						where.eq("fileName", resource.getName())
						.and().like("basePath", preferenceStore.getBasePathFor(resource) + "%");
					} else {
						where.or().eq("fileName", resource.getName())
						.and().like("basePath", preferenceStore.getBasePathFor(resource) + "%");
					}
				}
			}

			@Override
			public boolean isVolatile() {
				return true;
			}
		});

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				MainController.getController().getEbookTableHandler().refreshTable();
			}
		});
	}

}
