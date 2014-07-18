package org.rr.jeborker.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.components.tree.TreeUtil;
import org.rr.commons.swing.dialogs.JDirectoryChooser;
import org.rr.commons.swing.dialogs.JSplashScreen;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.app.BasePathList;
import org.rr.jeborker.app.FileWatchService;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.event.ApplicationEvent;
import org.rr.jeborker.event.EventManager;
import org.rr.jeborker.gui.model.BasePathTreeModel;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;
import org.rr.jeborker.gui.model.EbookSheetPropertyModel;
import org.rr.jeborker.gui.model.FileSystemTreeModel;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.IMetadataReader.METADATA_TYPES;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;

public class MainController {

	private final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);

	MainView mainWindow;

	private JSplashScreen splashScreen;

	/**
	 * The controller singleton.
	 */
	private static MainController controller;

	/**
	 * Controller for the file system and base path tree.
	 */
	private static MainTreeController treeController;

	/**
	 * The controller for the SortColumn combobox.
	 */
	private static SortOrderComponentController sortOrderComponentController;

	/**
	 * The filter panel controller.
	 */
	private static FilterPanelView filterPanelController;

	/**
	 * No public instantiation. The {@link #getController()} method is
	 * used for creating a new {@link MainController} instance because
	 * the {@link MainController} is a singleton.
	 */
	private MainController() {
	}

	/**
	 * Gets the controller instance. because we have only one main window
	 * We have a singleton here.
	 * @return The desired EBorkerMainController.
	 */
	public static MainController getController() {
		if(controller == null) {
			try {
				controller = new MainController();
				controller.splashScreen = JSplashScreen.getInstance();
				controller.splashScreen.startProgress(null);
				try {
					controller.initialize();
				} finally {
					controller.splashScreen.setLoadingValue(100);
					controller.splashScreen.endProgress();
				}
			} catch (Exception e) {
				LoggerFactory.getLogger(MainController.class).log(Level.SEVERE, "Startup failed.", e);
			}
		}
		return controller;
	}

	/**
	 * Gets the controller for the sort column Combobox. Thats these combobox where the order
	 * could be selected which should be used for the ebook items.
	 */
	public SortOrderComponentController getSortOrderComponentController() {
		return sortOrderComponentController;
	}

	/**
	 * Gets the controller which handles the filter panel functions.
	 */
	public FilterPanelView getFilterPanelController() {
		return filterPanelController;
	}

	/**
	 * Gets the controller which handles the base path and file system tree.
	 */
	public MainTreeController getMainTreeController() {
		return treeController;
	}

	/**
	 * Gets the controller which handles the preference dialog.
	 */
	public PreferenceController getPreferenceController() {
		return PreferenceController.getInstance();
	}

	/**
	 * Gets the controller which handles the preference dialog.
	 */
	public LoggerController getLoggerController() {
		return LoggerController.getInstance(mainWindow);
	}

	/**
	 * Gets the controller which is used by the converter classes to
	 * ask the user for some options.
	 */
	public ConverterPreferenceController getConverterPreferenceController() {
		return ConverterPreferenceController.getInstance();
	}

	public MetadataDownloadController getMetadataDownloadController() {
		return MetadataDownloadController.getInstance(mainWindow);
	}

	public RenameFileController getRenameFileController() {
		return RenameFileController.getInstance(mainWindow);
	}

	/**
	 * Tells if the {@link MainController} is already initialized.
	 * @return <code>true</code> if the {@link MainController} is initialized and <code>false</code> otherwise.
	 */
	public static boolean isInitialized() {
		return controller != null;
	}

	private void initialize() throws Exception {
		final String lookAndFeel = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.PREFERENCE_KEYS.LOOK_AND_FEEL)
				.getEntryAsString(PreferenceStoreFactory.PREFERENCE_KEYS.LOOK_AND_FEEL);

		SwingUtils.setLookAndFeel(lookAndFeel);

		splashScreen.setLoadingText(Bundle.getString("MainMenuBarController.opendb"));
		splashScreen.setLoadingValue(35);

		mainWindow = new MainView(this);

		splashScreen.setLoadingText(Bundle.getString("MainMenuBarController.init"));
		splashScreen.setLoadingValue(60);

		initSubController();

		splashScreen.setLoadingText(Bundle.getString("MainMenuBarController.restore"));
		splashScreen.setLoadingValue(80);

		mainWindow.restoreApplicationProperties();
		MainMenuBarController.getController().restoreProperties();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				mainWindow.setVisible(true);
				LoggerFactory.getLogger(this).log(Level.INFO, (System.currentTimeMillis() - Jeboorker.startupTime) + "ms startup time");

				MainController.getController().getMainWindow().getGlassPane().setVisible(true);
				initMainModel();
			}
		});

		BasePathList basePath = preferenceStore.getBasePath();
		FileWatchService.addWatchPath(basePath);
		FileWatchService.addWatchPath(EbookPropertyItemUtils.fetchPathElements());
	}

	/**
	 * Model loading and setup
	 */
	private void initMainModel() {
		getProgressMonitor().setEnabled(false);

		new SwingWorker<Void, Void>() {

			EbookPropertyDBTableModel ebookPropertyDBTableModel;

			@Override
			protected Void doInBackground() throws Exception {
				EbookPropertyDBTableModel tableModel = getTableModel();
				ebookPropertyDBTableModel = new EbookPropertyDBTableModel(tableModel, false);
				ebookPropertyDBTableModel.getRowCount();
				return null;
			}

			@Override
			protected void done() {
				try {
					mainWindow.mainTable.setModel(ebookPropertyDBTableModel);
				} finally {
					getMainWindow().getGlassPane().setVisible(false);
					LoggerFactory.getLogger(this).log(Level.INFO, (System.currentTimeMillis() - Jeboorker.startupTime) + "ms until data model loaded");
					getProgressMonitor().setEnabled(true);
				}
			}
		}.execute();
	}

	/**
	 * Initialize the controllers for the different parts uf the application window.
	 */
	private void initSubController() {
		sortOrderComponentController = new SortOrderComponentController(mainWindow.sortOrderAscButton, mainWindow.sortOrderDescButton);
		filterPanelController = new FilterPanelView();
		treeController = new MainTreeController(mainWindow.basePathTree, mainWindow.fileSystemTree);
	}

	/**
	 * Saves/Writes the metadata properties if something has changed.
	 */
	public void saveProperties(int minSelectionIndex, int maxSelectionIndex) {
		final EbookSheetPropertyModel sheetModel = (EbookSheetPropertyModel) mainWindow.propertySheet.getModel();
		final List<MetadataProperty> sheetProperties = sheetModel.getAllMetaData();
		List<IResourceHandler> propertyResourceHandler = sheetModel.getPropertyResourceHandler();
		if(getEbookSheetPropertyModel().isChanged()) {
			//write properties
			MainControllerUtils.writeProperties(sheetProperties, propertyResourceHandler);
			EbookPropertyDBTableModel tableModel = getTableModel();
			int rowCount = tableModel.getRowCount();

			if(minSelectionIndex >= 0 && minSelectionIndex < rowCount) {
				tableModel.reloadEbookPropertyItemAt(minSelectionIndex);
			}

			if(maxSelectionIndex >= 0 && maxSelectionIndex != minSelectionIndex && maxSelectionIndex < rowCount) {
				tableModel.reloadEbookPropertyItemAt(maxSelectionIndex);
			}

			if(minSelectionIndex < 0 || maxSelectionIndex < 0) {
				int[] selectedRows = mainWindow.mainTable.getSelectedRows();
				for (int selectedRow : selectedRows) {
					tableModel.reloadEbookPropertyItemAt(selectedRow);
				}
			}
			refreshTableSelectedItem(false);

			//a repaint does a refresh to all visible table rows.
			mainWindow.mainTable.repaint();
		}
	}

	/**
	 * Refresh the whole table.
	 */
	public void refreshTable() {
		final EbookPropertyDBTableModel model = getTableModel();
		if(model instanceof EbookPropertyDBTableModel) {
			((EbookPropertyDBTableModel)model).setDirty();
		}

		if(mainWindow.mainTable.isEditing()) {
			mainWindow.mainTable.stopEdit();
		}

		mainWindow.mainTable.tableChanged(new TableModelEvent(model));
		mainWindow.mainTableScrollPane.getVerticalScrollBar().setValue(0);
	}

	/**
	 * Refresh the Tree for the base path's.
	 */
	public void refreshBasePathTree() {
		final TreeSelectionModel selectionModel = mainWindow.basePathTree.getSelectionModel();
		final TreePath selectionPath = mainWindow.basePathTree.getSelectionPath();

		final String expansionStates = TreeUtil.getExpansionStates(mainWindow.basePathTree);
		final BasePathTreeModel basePathTreeModel = (BasePathTreeModel) mainWindow.basePathTree.getModel();
		mainWindow.basePathTree.stopEditing();
		((BasePathTreeModel)basePathTreeModel).reload();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				TreeUtil.restoreExpanstionState(mainWindow.basePathTree, expansionStates);
				selectionModel.setSelectionPath(selectionPath);
			}
		});
	}

	/**
	 * Refresh the Tree for the file system. Only the node and it child's for the given {@link IResourceHandler} will be refreshed.
	 */
	public void refreshFileSystemTreeEntry(IResourceHandler resourceToRefresh) {
		final TreeModel fileSystemTreeModel = mainWindow.fileSystemTree.getModel();
		final TreeModel basePathTreeModel = mainWindow.basePathTree.getModel();

		if(fileSystemTreeModel instanceof FileSystemTreeModel) {
			if(!resourceToRefresh.exists() || resourceToRefresh.isFileResource()) {
				resourceToRefresh = resourceToRefresh.getParentResource();
			}
			final String expansionStates = TreeUtil.getExpansionStates(mainWindow.fileSystemTree);
			mainWindow.fileSystemTree.stopEditing();
			((FileSystemTreeModel) fileSystemTreeModel).reload(resourceToRefresh);
			((BasePathTreeModel) basePathTreeModel).reload(resourceToRefresh);

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					TreeUtil.restoreExpanstionState(mainWindow.fileSystemTree, expansionStates);
				}
			});
		}
	}

	public void refreshTableItem(final int[] selectedRows, final boolean refreshMetadataSheet) {
		final EbookPropertyDBTableModel model = getTableModel();
		if(selectedRows==null || selectedRows.length == 0) {
			return;
		} else {
			int editingRow = mainWindow.mainTable.getEditingRow();
			for (int i = 0; i < selectedRows.length; i++) {
				if(editingRow != -1 && editingRow == selectedRows[i]) {
					mainWindow.mainTable.stopEdit();
				}

				model.reloadEbookPropertyItemAt(selectedRows[i]);
				mainWindow.mainTable.tableChanged(new TableModelEvent(model, selectedRows[i]));
			}
		}

		if(refreshMetadataSheet) {
			refreshSheetProperties();
		}
	}

	/**
	 * Refresh the selected table rows.
	 */
	public void refreshTableSelectedItem(final boolean refreshMetadataSheet) {
		int[] selectedRows = getSelectedEbookPropertyItemRows() ;
		refreshTableItem(selectedRows, refreshMetadataSheet);
	}

	/**
	 * Refresh the given table rows.
	 * @param rows The rows to be refreshed.
	 * @param refreshMetadataSheet do also refresh the metadata sheet.
	 */
	public void refreshTableRows(final int[] rows, boolean refreshMetadataSheet) {
		final EbookPropertyDBTableModel model = getTableModel();
		if(rows==null || rows.length==0) {
			return;
		} else {
			int editingRow = mainWindow.mainTable.getEditingRow();
			int[] selectedEbookPropertyItemRows = getSelectedEbookPropertyItemRows();
			for (int i = 0; i < rows.length; i++) {
				if(editingRow != -1 && editingRow == rows[i]) {
					mainWindow.mainTable.editingStopped(null);
				}
				model.reloadEbookPropertyItemAt(rows[i]);
				mainWindow.mainTable.tableChanged(new TableModelEvent(model, rows[i]));

				for (int j = 0; j < selectedEbookPropertyItemRows.length; j++) {
					if(refreshMetadataSheet && selectedEbookPropertyItemRows[j] == rows[i]) {
						refreshSheetProperties();
					}
				}
			}
		}
	}

	/**
	 * Sets the image which is provided by the given {@link IResourceHandler} to the
	 * image viewer in the main view.
	 * @param imageResource The {@link IResourceHandler} instance providing the image
	 * data to be displayed. <code>null</code>if no image should be displayed.
	 */
	public void setImageViewerResource(IResourceHandler imageResource) {
		mainWindow.imageViewer.setImageViewerResource(imageResource);
	}

	/**
	 * Gets the {@link IResourceHandler} for the image which is displayed in the image viewer.
	 * @return The desired {@link IResourceHandler} or <code>null</code>.
	 */
	public IResourceHandler getImageViewerResource() {
		return mainWindow.imageViewer.getImageResource();
	}

	/**
	 * Gets the {@link BufferedImage} for the image which is displayed in the image viewer.
	 * @return The desired {@link BufferedImage} or <code>null</code>.
	 */
	public BufferedImage getImageViewerImage() {
		return mainWindow.imageViewer.getImage();
	}

	/**
	 * Removes the metadata property which is currently selected in the property sheet.
	 */
	public void removeSelectedMetadataProperty() {
		Property selectedMetadataProperty = getSelectedMetadataProperty();
		if(selectedMetadataProperty!=null) {
			if(selectedMetadataProperty.isEditable()) {
				PropertySheetTableModel model = getEbookSheetPropertyModel();
				model.removeProperty(selectedMetadataProperty);
			}
		}
	}

	/**
	 * Adds the given property to the end of the property sheet.
	 * @param property The property to be added.
	 */
	public void addMetadataProperty(Property property) {
		mainWindow.propertySheet.addProperty(property);
		EventManager.fireEvent(EventManager.EVENT_TYPES.METADATA_SHEET_CONTENT_CHANGE, new ApplicationEvent(controller.getSelectedEbookPropertyItems(), property, property));
	}

	/**
	 * Get the currently selected metadata property from the metadata sheet.
	 * @return The desired {@link Property} instance or <code>null</code> if no selection is made.
	 */
	public Property getSelectedMetadataProperty() {
		return mainWindow.getSelectedMetadataProperty();
	}

	/**
	 * Set the given property as selected one in the metadata sheet.
	 * @param property The property to be set as selected.
	 */
	public void setSelectedMetadataProperty(final Property property) {
		mainWindow.setSelectedMetadataProperty(property);
	}

	/**
	 * Sets the rating value (between 1 and 10).
	 * @param rating The rating value.
	 */
	public void setRatingToSelectedEntry(int rating) {
		final EbookSheetPropertyModel model = getEbookSheetPropertyModel();
		final Property ratingProperty = model.getRatingProperty();
		final int ratingIdx = model.getRatingIndex();

		if(ratingIdx >= 0) {
			mainWindow.propertySheet.getSelectionModel().setSelectionInterval(ratingIdx, ratingIdx);
		}

		if(ratingProperty != null) {
			ratingProperty.setValue(rating);
		} else {
			List<EbookPropertyItem> selectedEbookPropertyItems = getSelectedEbookPropertyItems();
			for (EbookPropertyItem item : selectedEbookPropertyItems) {
				IMetadataReader reader = MetadataHandlerFactory.getReader(ResourceHandlerFactory.getResourceHandler(item.getFile()));
				List<MetadataProperty> metadataByType = reader.getMetadataByType(true, model.getAllMetaData(), METADATA_TYPES.RATING);
				if(!metadataByType.isEmpty()) {
					MetadataProperty ratingMetaData = metadataByType.get(0);

					final Property createProperty = EbookSheetPropertyModel.createProperty(ratingMetaData, Collections.singletonList(item), 0);
					createProperty.setValue(rating);
					this.addMetadataProperty(createProperty);
				} else {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Rating entry is not available.");
				}
			}
		}
	}

	/**
	 * Gets the model for the metadata sheet.
	 * @return The metadata sheet model.
	 */
	public EbookSheetPropertyModel getEbookSheetPropertyModel() {
		final EbookSheetPropertyModel model = (EbookSheetPropertyModel) mainWindow.propertySheet.getModel();
		return model;
	}

	/**
	 * Adds the given {@link EbookPropertyItem} to the model. The added
	 * {@link EbookPropertyItem} will be shown to the ui.
	 * @param item The item to be added.
	 */
	public void addEbookPropertyItem(final EbookPropertyItem item, final int row) {
		if(preferenceStore.isBasePathVisible(item.getBasePath())) {
			TableModel model = getTableModel();
			if (model instanceof EbookPropertyDBTableModel) {
				clearSelection();
				((EbookPropertyDBTableModel) model).addRow(item, row);
				mainWindow.mainTable.stopEdit();
			}
		}
	}

	/**
	 * Gets all selected items from the main table.
	 * @return The selected items. Never returns <code>null</code>.
	 */
	public List<EbookPropertyItem> getSelectedEbookPropertyItems() {
		return mainWindow.getSelectedEbookPropertyItems();
	}

	/**
	 * Gets all selected rows from the main table.
	 * @return all selected rows or an empty array if no row is selected. Never returns <code>null</code>.
	 */
	public int[] getSelectedEbookPropertyItemRows() {
		if(mainWindow != null) {
			return mainWindow.getSelectedEbookPropertyItemRows();
		}
		return new int[0];
	}
	
	public List<Field> getSelectedSortColumnFields() {
		return mainWindow.sortColumnComponent.getSelectedFields();
	}

	/**
	 * Clears the selection on the main table.
	 */
	public void clearSelection() {
		mainWindow.clearMainTableSelection();
	}

	/**
	 * Removes the given {@link EbookPropertyItem} from the model. This
	 * change the view automatically.
	 * @param item The {@link EbookPropertyItem} to be removed.
	 */
	public boolean removeEbookPropertyItem(EbookPropertyItem item) {
		TableModel model = getTableModel();
		if(model instanceof EbookPropertyDBTableModel) {
			mainWindow.mainTable.clearSelection();
			mainWindow.mainTable.editingStopped(new ChangeEvent(this));
			boolean result = ((EbookPropertyDBTableModel)model).removeRow(item);
			return result;
		}
		return false;
	}

	/**
	 * gets the current model for the main table.
	 * @return The desired model. <code>null</code> if the model is not initialized.
	 */
	public EbookPropertyDBTableModel getTableModel() {
		return (EbookPropertyDBTableModel) mainWindow.mainTable.getModel();
	}

	/**
	 * Gets the progress indicator.
	 * @return The desired monitor instance of <code>null</code> if the monitor is not ready to use.
	 */
	public MainMonitor getProgressMonitor() {
		if(mainWindow != null && mainWindow.progressBar != null) {
			return MainMonitor.getInstance(mainWindow.progressBar);
		}
		return null;
	}

	/**
	 * Opens a {@link JFileChooser} and returns the selected folder or
	 * <code>null</code> if no folder was selected.
	 * @return The selected folder or <code>null</code>.
	 */
	public List<File> getDirectorySelection() {
		String lastEbookFolder = preferenceStore.getGenericEntryAsString("lastEbookFolder");
		IResourceHandler lastEbookFolderResourceLoader = ResourceHandlerFactory.getResourceHandler(lastEbookFolder);
		if(lastEbookFolderResourceLoader == null || !lastEbookFolderResourceLoader.isDirectoryResource()) {
			lastEbookFolder = null;
		}

		final JDirectoryChooser chooser = new JDirectoryChooser(true);

		//restore directory chooser location
		String location = preferenceStore.getGenericEntryAsString("JDirectoryChooserLocation");
		if(location != null && location.indexOf(';') != -1) {
			String[] split = location.split(";");
			chooser.setDialogLocation(new Point(CommonUtils.toNumber(split[0]).intValue(), CommonUtils.toNumber(split[1]).intValue()));
		}

		//restore directory chooser size
		String size = preferenceStore.getGenericEntryAsString("JDirectoryChooserSize");
		if(size != null && size.indexOf(';') != -1) {
			String[] split = size.split(";");
			chooser.setDialogSize(new Dimension(CommonUtils.toNumber(split[0]).intValue(), CommonUtils.toNumber(split[1]).intValue()));
		}

		List<File> selectedDirectory = chooser.getDirectorySelections(lastEbookFolder, mainWindow);
		if(selectedDirectory!=null && !selectedDirectory.isEmpty()) {
			preferenceStore.addGenericEntryAsString("lastEbookFolder", selectedDirectory.get(selectedDirectory.size() - 1).toString());
		}

		//store directory chooser size and location
		preferenceStore.addGenericEntryAsString("JDirectoryChooserLocation", chooser.getDialogLocation().x + ";" + chooser.getDialogLocation().y);
		preferenceStore.addGenericEntryAsString("JDirectoryChooserSize", chooser.getDialogSize().width + ";" + chooser.getDialogSize().height);
		return selectedDirectory;
	}

	/**
	 * Tries to get localized Text from the bundle.
	 *
	 * @param name The name to be translated.
	 * @return The translated name.
	 */
	public String getLocalizedString(final String name) {
		if(name == null) {
			return null;
		}
		String localized = StringUtils.replace(name.toLowerCase(), new String[] {"/", " ", ":"}, "");
		localized = Bundle.getString(localized);
		if(localized != null && localized.length() > 0) {
			return StringUtils.capitalize(localized);
		} else {
			return StringUtils.capitalize(name);
		}
	}

	public void dispose() {
		//Writes the application properties to the preference file
		mainWindow.storeApplicationProperties();
		getSortOrderComponentController().dispose();
		getFilterPanelController().dispose();
		MainMenuBarController.getController().dispose();
	}

	/**
	 * Gets the application main window. Needed for having modal dialogs.
	 * @return The main window instance.
	 */
	public JFrame getMainWindow() {
		return mainWindow;
	}

	/**
	 * Rereads the metadata properties and set them to the sheet.
	 */
	public void refreshSheetProperties() {
		mainWindow.refreshSheetProperties();
	}

	/**
	 * Shows a dialog to the user.
	 * @param message The message of the dialog
	 * @param title The dialog title.
	 * @param option The dialog option: JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.YES_NO_OPTION, JOptionPane.OK_CANCEL_OPTION
	 * @param defaultValue The value which is stored if the user do not want to see the dialog again.
	 * 		-1 for store the result value instead.
	 * @param isConfirmDialog Tells if the dialog to be shown is a confirm (Yes/No) Dialog or a simple OK Message-Box
	 * @return 0: yes/ok, 1: no, 2:cancel, -1 none
	 */
	public int showMessageBox(String message, String title, int option, String showAgainKey, int defaultValue, boolean isConfirmDialog) {
		return mainWindow.showMessageBox(message, title, option, showAgainKey, defaultValue, isConfirmDialog);
	}

	/**
	 * Refresh some UI components. Should be invoked after changing the look and feel.
	 */
	public void refreshUI() {
		if(mainWindow != null) {
			mainWindow.refreshUI();
		}
	}
}

