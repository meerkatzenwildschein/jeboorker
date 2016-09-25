package org.rr.jeborker.gui;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
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
import javax.swing.table.TableModel;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.dialogs.JDirectoryChooser;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.app.BasePathList;
import org.rr.jeborker.app.FileWatchService;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.event.ApplicationEvent;
import org.rr.jeborker.event.EventManager;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;
import org.rr.jeborker.gui.model.EbookPropertyFileTableModel;
import org.rr.jeborker.gui.model.EbookSheetPropertyModel;
import org.rr.jeborker.gui.model.ReloadableTableModel;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.IMetadataReader.COMMON_METADATA_TYPES;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;

public class MainController {

	private static long startupTime = System.currentTimeMillis();

	private final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);

	private MainView mainWindow;
	
	/**
	 * The controller singleton.
	 */
	private static MainController controller;

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
				controller.initialize();
			} catch (Exception e) {
				LoggerFactory.getLogger(MainController.class).log(Level.SEVERE, "Startup failed.", e);
			}
		}
		return controller;
	}

	/**
	 * Gets the handler which handles the base path and file system tree.
	 */
	public MainViewTreeComponentHandler getMainTreeHandler() {
		return mainWindow.getTreeComponentHandler();
	}

	/**
	 * Gets the handler which handles the main table
	 */
	public MainViewEbookTableComponentHandler getEbookTableHandler() {
		return mainWindow.getEbookTableHandler();
	}

	public MainViewPropertySheetHandler getPropertySheetHandler() {
		return mainWindow.getPropertySheetHandler();
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

		Toolkit.getDefaultToolkit().setDynamicLayout(false);
		SwingUtils.setLookAndFeel(lookAndFeel);

		mainWindow = new MainView(this);
		mainWindow.initialize();

		mainWindow.initListeners();

		mainWindow.restoreComponentProperties();
		MainMenuBarController.getController().restoreProperties();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				mainWindow.setVisible(true);
				LoggerFactory.getLogger(this).log(Level.INFO, (System.currentTimeMillis() - startupTime) + "ms startup time");

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
				EbookPropertyDBTableModel tableModel = changeToDatabaseModel();
				ebookPropertyDBTableModel = new EbookPropertyDBTableModel(tableModel, false);
				ebookPropertyDBTableModel.getRowCount();
				return null;
			}

			@Override
			protected void done() {
				try {
					setMainEbookTableModel(ebookPropertyDBTableModel);
				} finally {
					getMainWindow().getGlassPane().setVisible(false);
					LoggerFactory.getLogger(this).log(Level.INFO, (System.currentTimeMillis() - startupTime) + "ms until data model loaded");
					getProgressMonitor().setEnabled(true);
				}
			}


		}.execute();
	}
	
	private void setMainEbookTableModel(TableModel model) {
		mainWindow.getEbookTableHandler().setModel(model);
	}
	
	public EbookPropertyFileTableModel changeToFileModel(List<IResourceHandler> resources) {
		EbookPropertyFileTableModel fileModel = new EbookPropertyFileTableModel(resources);
		setMainEbookTableModel(fileModel);
		return fileModel;
	}
	
	public EbookPropertyDBTableModel changeToDatabaseModel() {
		EbookPropertyDBTableModel model = mainWindow.getEbookTableHandler().getDBModel();
		setMainEbookTableModel(model);
		return model;
	}
	
	public boolean isEbookPropertyFileTableModel() {
		return mainWindow.getEbookTableHandler().getModel() instanceof EbookPropertyFileTableModel;
	}

	public void refreshTableItem(int[] selectedRows, boolean refreshMetadataSheet) {
		mainWindow.getEbookTableHandler().refreshTableItem(selectedRows);

		if(refreshMetadataSheet) {
			getPropertySheetHandler().refreshSheetProperties();
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
		if (rows == null || rows.length == 0) {
			return;
		} else {
			mainWindow.getEbookTableHandler().refreshTableRows(rows);

			int[] selectedEbookPropertyItemRows = getSelectedEbookPropertyItemRows();
			for (int i = 0; i < rows.length; i++) {
				for (int j = 0; j < selectedEbookPropertyItemRows.length; j++) {
					if(refreshMetadataSheet && selectedEbookPropertyItemRows[j] == rows[i]) {
						getPropertySheetHandler().refreshSheetProperties();
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
		mainWindow.setImageViewerResource(imageResource);
	}

	/**
	 * Gets the {@link IResourceHandler} for the image which is displayed in the image viewer.
	 * @return The desired {@link IResourceHandler} or <code>null</code>.
	 */
	public IResourceHandler getImageViewerResource() {
		return mainWindow.getImageViewerResource();
	}

	/**
	 * Gets the {@link BufferedImage} for the image which is displayed in the image viewer.
	 * @return The desired {@link BufferedImage} or <code>null</code>.
	 */
	public BufferedImage getImageViewerImage() {
		return mainWindow.getImageViewerImage();
	}

	/**
	 * Removes the metadata property which is currently selected in the property sheet.
	 */
	public void removeSelectedMetadataProperty() {
		Property selectedMetadataProperty = getSelectedMetadataProperty();
		if (selectedMetadataProperty != null) {
			PropertySheetTableModel model = getPropertySheetHandler().getModel();
			model.removeProperty(selectedMetadataProperty);
		}
	}

	/**
	 * Adds the given property to the end of the property sheet.
	 * @param property The property to be added.
	 */
	public void addMetadataProperty(Property property) {
		mainWindow.getPropertySheetHandler().addMetadataProperty(property);
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
		final EbookSheetPropertyModel model = getPropertySheetHandler().getModel();
		final Property ratingProperty = model.getRatingProperty();
		final int ratingIdx = model.getRatingIndex();

		if(ratingIdx >= 0) {
			mainWindow.getPropertySheetHandler().getSelectionModel().setSelectionInterval(ratingIdx, ratingIdx);
		}

		if(ratingProperty != null) {
			ratingProperty.setValue(rating);
		} else {
			List<EbookPropertyItem> selectedEbookPropertyItems = getSelectedEbookPropertyItems();
			for (EbookPropertyItem item : selectedEbookPropertyItems) {
				IMetadataReader reader = MetadataHandlerFactory.getReader(ResourceHandlerFactory.getResourceHandler(item.getFile()));
				List<MetadataProperty> metadataByType = reader.getMetadataByType(true, model.getAllMetadata(), COMMON_METADATA_TYPES.RATING);
				if(!metadataByType.isEmpty()) {
					MetadataProperty ratingMetadata = metadataByType.get(0);

					final Property createProperty = EbookSheetPropertyModel.createProperty(ratingMetadata, Collections.singletonList(item), 0);
					createProperty.setValue(rating);
					this.addMetadataProperty(createProperty);
				} else {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Rating entry is not available.");
				}
			}
		}
	}

	/**
	 * Saves/Writes the metadata properties if something has changed.
	 */
	public void saveMetadataProperties(int minSelectionIndex, int maxSelectionIndex) {
		EbookSheetPropertyModel sheetModel = getPropertySheetHandler().getModel();
		List<MetadataProperty> sheetProperties = sheetModel.getAllMetadata();
		List<IResourceHandler> propertyResourceHandler = sheetModel.getPropertyResourceHandler();
		if(getPropertySheetHandler().getModel().isChanged()) {
			MainControllerUtils.writeProperties(sheetProperties, propertyResourceHandler);
			ReloadableTableModel tableModel = getModel();
			int rowCount = tableModel.getRowCount();

			if(minSelectionIndex >= 0 && minSelectionIndex < rowCount) {
				tableModel.reloadEbookPropertyItemAt(minSelectionIndex);
			}

			if(maxSelectionIndex >= 0 && maxSelectionIndex != minSelectionIndex && maxSelectionIndex < rowCount) {
				tableModel.reloadEbookPropertyItemAt(maxSelectionIndex);
			}

			if(minSelectionIndex < 0 || maxSelectionIndex < 0) {
				int[] selectedRows = mainWindow.getEbookTableHandler().getSelectedRows();
				for (int selectedRow : selectedRows) {
					tableModel.reloadEbookPropertyItemAt(selectedRow);
				}
			}
			refreshTableSelectedItem(false);

			//a repaint does a refresh to all visible table rows.
			mainWindow.getEbookTableHandler().repaint();
		}
	}

	/**
	 * Adds the given {@link EbookPropertyItem} to the model. The added
	 * {@link EbookPropertyItem} will be shown to the ui.
	 * @param item The item to be added.
	 */
	public void addEbookPropertyItem(final EbookPropertyItem item, final int row) {
		if(preferenceStore.isBasePathVisible(item.getBasePath())) {
			ReloadableTableModel model = getModel();
			getEbookTableHandler().clearSelection();
			model.addRow(item, row);
			mainWindow.getEbookTableHandler().stopEdit();
		}
	}

	/**
	 * Gets all selected items from the main table.
	 * @return The selected items. Never returns <code>null</code>.
	 */
	public List<EbookPropertyItem> getSelectedEbookPropertyItems() {
		return mainWindow.getEbookTableHandler().getSelectedEbookPropertyItems();
	}

	/**
	 * Gets all selected rows from the main table.
	 * @return all selected rows or an empty array if no row is selected. Never returns <code>null</code>.
	 */
	public int[] getSelectedEbookPropertyItemRows() {
		if(mainWindow != null) {
			return mainWindow.getEbookTableHandler().getSelectedEbookPropertyItemRows();
		}
		return new int[0];
	}

	public List<Field> getSelectedSortColumnFields() {
		return mainWindow.getSortColumnSelectedFields();
	}

	/**
	 * Removes the given {@link EbookPropertyItem} from the model. This
	 * change the view automatically.
	 * @param item The {@link EbookPropertyItem} to be removed.
	 */
	public boolean removeEbookPropertyItem(EbookPropertyItem item) {
		ReloadableTableModel model = controller.getModel();
		mainWindow.getEbookTableHandler().clearSelection();
		mainWindow.getEbookTableHandler().editingStopped(new ChangeEvent(this));
		return model.removeRow(item);
	}

	/**
	 * gets the current model for the main table.
	 * @return The desired model. <code>null</code> if the model is not initialized.
	 */
	public ReloadableTableModel getModel() {
		return mainWindow.getEbookTableHandler().getModel();
	}	

	/**
	 * Gets the progress indicator.
	 * @return The desired monitor instance of <code>null</code> if the monitor is not ready to use.
	 */
	public MainMonitor getProgressMonitor() {
		if(mainWindow != null) {
			return mainWindow.getProgressMonitor();
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
		String localized = StringUtil.replace(name.toLowerCase(), new String[] {"/", " ", ":"}, EMPTY);
		localized = Bundle.getString(localized);
		if(localized != null && localized.length() > 0) {
			return StringUtil.capitalize(localized);
		} else {
			return StringUtil.capitalize(name);
		}
	}

	public void dispose() {
		mainWindow.storeApplicationProperties();
		MainMenuBarController.getController().storeProperties();
	}

	/**
	 * Gets the application main window. Needed for having modal dialogs.
	 * @return The main window instance.
	 */
	public JFrame getMainWindow() {
		return mainWindow;
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

	public List<Field> getSelectedFilterFields() {
		return mainWindow.getSelectedFilterFields();
	}

	public void setFilterColorEnabled(boolean enabled) {
		mainWindow.setFilterColorEnabled(enabled);
	}

	public String getFilterText() {
		return mainWindow.getFilterText();
	}

	public void addFilterFieldSearch(String filterText) {
		mainWindow.addFilterFieldSearch(filterText);
	}

}

