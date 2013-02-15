package org.rr.jeborker.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.VirtualStaticResourceDataLoader;
import org.rr.commons.swing.dialogs.JDirectoryChooser;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.event.ApplicationEvent;
import org.rr.jeborker.event.EventManager;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;
import org.rr.jeborker.gui.model.EbookSheetPropertyModel;
import org.rr.jeborker.gui.model.EbookSheetPropertyMultiSelectionModel;
import org.rr.jeborker.gui.model.EmptyListModel;
import org.rr.jeborker.gui.model.MetadataAddListModel;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.IMetadataReader.METADATA_TYPES;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

public class MainController {

	private MainView mainWindow;
	
	/**
	 * The controller singleton.
	 */
	private static MainController controller;
	
	/**
	 * The controller for the SortColumn combobox.
	 */
	private static SortColumnComponentController sortColumnComponentController;

	/**
	 * The controller for the SortColumn combobox.
	 */
	private static SortOrderComponentController sortOrderComponentController;
	
	/**
	 * The filter panel controller.
	 */
	private static FilterPanelController filterPanelController;
	
	/**
	 * No public instantiation. The {@link #getController()} method is
	 * used for creating a new {@link MainController} instance because
	 * the {@link MainController} is a singleton.
	 */
	private MainController() {
	}
	
	/**
	 * ListSelectionListener which is invoked by changing the selection in the main table. It saves and sets the metadata properties of
	 * the {@link PropertySheet}.
	 */
	private class PropertySheetListSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(!e.getValueIsAdjusting()) {
				Property selectedMetadataProperty = getSelectedMetadataProperty();				
				refreshSheetProperties(); 
				setSelectedMetadataProperty(selectedMetadataProperty);
			}
		}
		
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
			EbookPropertyDBTableModel tableModel = (EbookPropertyDBTableModel) mainWindow.table.getModel();
			int rowCount = tableModel.getRowCount();

			if(minSelectionIndex >= 0 && minSelectionIndex < rowCount) {
				tableModel.reloadEbookPropertyItemAt(minSelectionIndex);
			}
			
			if(maxSelectionIndex >= 0 && maxSelectionIndex != minSelectionIndex && maxSelectionIndex < rowCount) {
				tableModel.reloadEbookPropertyItemAt(maxSelectionIndex);
			}
			
			if(minSelectionIndex < 0 || maxSelectionIndex < 0) {
				int[] selectedRows = mainWindow.table.getSelectedRows();
				for (int selectedRow : selectedRows) {
					tableModel.reloadEbookPropertyItemAt(selectedRow);
				}
			}
			refreshTableSelectedItem(false);
			
			//a repaint does a refresh to all visible table rows.
			mainWindow.table.repaint();
		}
	}	
	
	/**
	 * Mouse listener which handles the right click / popup menu on the main table.
	 */
	private class MainTablePopupMouseListener extends MouseAdapter {

		public void mouseReleased(MouseEvent event) {
			if (event.getButton() == MouseEvent.BUTTON3) {
				final int rowAtPoint = mainWindow.table.rowAtPoint(event.getPoint());
				
				//set selection for the right click
				if(mainWindow.table.getSelectedRowCount() <= 1 ) {
					mainWindow.table.getSelectionModel().setSelectionInterval(rowAtPoint, rowAtPoint);
				}
				
				MainMenuBarController.getController().showMainPopupMenu(event.getPoint(), mainWindow.table);
			}
		}
	}
	
	/**
	 * Mouse listener which handles the right click / popup menu on the main table.
	 */
	private class CoverPopupMouseListener extends MouseAdapter {

		public void mouseReleased(MouseEvent event) {
			if (event.getButton() == MouseEvent.BUTTON3) {
				MainMenuBarController.getController().showCoverPopupMenu(event.getPoint(), mainWindow.imageViewer);
			}
		}
	}	
	
	/**
	 * Gets the controller instance. because we have only one main window
	 * We have a singleton here.
	 * @return The desired EBorkerMainController.
	 */
	public static MainController getController() {
		if(controller == null) {
			controller = new MainController();
			controller.initialize();
		}
		return controller;
	}
	
	/**
	 * Tells if the {@link MainController} is already initialized.
	 * @return <code>true</code> if the {@link MainController} is initialized and <code>false</code> otherwise.
	 */
	public static boolean isInitialized() {
		return controller!=null;
	}
	
	private void initialize() {
		mainWindow = new MainView();
		initListeners();
		initSubController();
		
		MainControllerUtils.restoreApplicationProperties(mainWindow);
		MainMenuBarController.getController().restoreProperties();
		mainWindow.setVisible(true);
	}

	private void initSubController() {
		sortColumnComponentController = new SortColumnComponentController(mainWindow.sortColumnComboBox);
		sortOrderComponentController = new SortOrderComponentController(mainWindow.sortOrderAscButton, mainWindow.sortOrderDescButton);
		filterPanelController = new FilterPanelController();
	}
	
	private void initListeners() {
		mainWindow.table.getSelectionModel().addListSelectionListener(new PropertySheetListSelectionListener());
		mainWindow.table.addMouseListener(new MainTablePopupMouseListener());
		
		mainWindow.imageViewer.addMouseListener(new CoverPopupMouseListener());
		
		mainWindow.propertySheet.addPropertySheetChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if("value".equals(e.getPropertyName())) {
					//sheet has been edited
					EventManager.fireEvent(EventManager.EVENT_TYPES.METADATA_SHEET_CONTENT_CHANGE, new ApplicationEvent(getSelectedEbookPropertyItems(), getSelectedMetadataProperty(), e.getSource()));
				}
			}
		});
		
		mainWindow.propertySheet.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()) {
					EventManager.fireEvent(EventManager.EVENT_TYPES.METADATA_SHEET_SELECTION_CHANGE, new ApplicationEvent(getSelectedEbookPropertyItems(), getSelectedMetadataProperty(), e.getSource()));
				}
			}
		});
		
		mainWindow.table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()) {
					EventManager.fireEvent(EventManager.EVENT_TYPES.EBOOK_ITEM_SELECTION_CHANGE, new ApplicationEvent(getSelectedEbookPropertyItems(), getSelectedMetadataProperty(), e.getSource()));
				}
			}
		});				
	}
	
	/**
	 * Refresh the whole table.
	 */
	public void refreshTable(final boolean refreshMetadataSheet) {
		final EbookPropertyDBTableModel model = (EbookPropertyDBTableModel) mainWindow.table.getModel();
		if(model instanceof EbookPropertyDBTableModel) {
			((EbookPropertyDBTableModel)model).setDirty();
		}
		
		if(mainWindow.table.isEditing()) {
			mainWindow.table.editingStopped(null);
		}
			
		mainWindow.table.tableChanged(new TableModelEvent(model));
		if(refreshMetadataSheet) {
			refreshSheetProperties();
		}
	}
	
	public void refreshTableItem(final int[] selectedRows, final boolean refreshMetadataSheet) {
		final EbookPropertyDBTableModel model = (EbookPropertyDBTableModel) mainWindow.table.getModel();
		if(selectedRows==null || selectedRows.length == 0) {
			return;
		} else {
			int editingRow = mainWindow.table.getEditingRow();
			for (int i = 0; i < selectedRows.length; i++) {
				if(editingRow != -1 && editingRow == selectedRows[i]) {
					mainWindow.table.editingStopped(null);
				}
				
				model.reloadEbookPropertyItemAt(selectedRows[i]);
				mainWindow.table.tableChanged(new TableModelEvent(model, selectedRows[i]));					
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
		final EbookPropertyDBTableModel model = (EbookPropertyDBTableModel) mainWindow.table.getModel();
		if(rows==null || rows.length==0) {
			return;
		} else {
			int editingRow = mainWindow.table.getEditingRow();
			int[] selectedEbookPropertyItemRows = getSelectedEbookPropertyItemRows();
			for (int i = 0; i < rows.length; i++) {
				if(editingRow != -1 && editingRow == rows[i]) {
					mainWindow.table.editingStopped(null);
				}
				model.reloadEbookPropertyItemAt(rows[i]);
				mainWindow.table.tableChanged(new TableModelEvent(model, rows[i]));					
				
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
		mainWindow.imageViewer.setImageResource(imageResource);
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
	public BufferedImage getImage() {
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
		final int selectedRow = mainWindow.propertySheet.getTable().getSelectedRow();
		
		if(selectedRow >= 0) {
			final EbookSheetPropertyModel model = getEbookSheetPropertyModel();
			final PropertySheetTableModel.Item item = (Item) model.getObject(selectedRow);
			final Property property = item.getProperty();
			
			return property;
		}
		return null;
	}
	
	/**
	 * Set the given property as selected one in the metadata sheet.
	 * @param property The property to be set as selected.
	 */
	public void setSelectedMetadataProperty(final Property property) {
		if(property != null) {
			final EbookSheetPropertyModel model = getEbookSheetPropertyModel();
			final int rowCount = model.getRowCount();
			
			for (int i = 0; i < rowCount; i++) {
				final PropertySheetTableModel.Item item = (Item) model.getObject(i);
				
				if(item != null && item.getName() != null && item.getName().equals(model.getDisplayName(property))) {
					mainWindow.propertySheet.getTable().getSelectionModel().setSelectionInterval(i, i);
					break;
				} else {
					if(property != null && item != null && item.getProperty() != null && item.getProperty().getName() != null && item.getProperty().getName().equals(property.getName())) {
						mainWindow.propertySheet.getTable().getSelectionModel().setSelectionInterval(i, i);
						break;
					}
				}
			}
		}
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
	public void addEbookPropertyItem(final EbookPropertyItem item, int row) {
		TableModel model = mainWindow.table.getModel();
		if (model instanceof EbookPropertyDBTableModel) {
			this.clearSelection();
			((EbookPropertyDBTableModel) model).addRow(item, row);
			mainWindow.table.setEditingRow(row -1);
		}
	}
	
	/**
	 * Gets all selected items from the main table.
	 * @return The selected items. Never returns <code>null</code>.
	 */
	public List<EbookPropertyItem> getSelectedEbookPropertyItems() {
		final int[] selectedRows = getSelectedEbookPropertyItemRows();
		final ArrayList<EbookPropertyItem> result = new ArrayList<EbookPropertyItem>(selectedRows.length);
		for (int i = 0; i < selectedRows.length; i++) {
			EbookPropertyItem valueAt = (EbookPropertyItem) mainWindow.table.getModel().getValueAt(selectedRows[i], 0);
			result.add(valueAt);
		}
		
		return result;
	}
	
	/**
	 * Gets all selected rows from the main table.
	 * @return all selected rows or an empty array if no row is selected. Never returns <code>null</code>.
	 */
	public int[] getSelectedEbookPropertyItemRows() {
		if (mainWindow != null && mainWindow.table != null) {
			final int[] selectedRows = mainWindow.table.getSelectedRows();
			return selectedRows;
		} else {
			return new int[0];
		}
	}

	/**
	 * Clears the selection on the main table.
	 */
	public void clearSelection() {
		mainWindow.table.clearSelection();
	}
	
	/**
	 * Removes the given {@link EbookPropertyItem} from the model. This
	 * change the view automatically.
	 * @param item The {@link EbookPropertyItem} to be removed.
	 */
	public boolean removeEbookPropertyItem(EbookPropertyItem item) {
		TableModel model = mainWindow.table.getModel();
		if(model instanceof EbookPropertyDBTableModel) {
			mainWindow.table.clearSelection();
			mainWindow.table.editingStopped(new ChangeEvent(this));
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
		TableModel model = mainWindow.table.getModel();
		return (EbookPropertyDBTableModel) model;
	}
	
	/**
	 * Gets the progress indicator.
	 * @return The desired monitor instance of <code>null</code> if the monitor is not ready to use.
	 */
	public MainMonitor getProgressMonitor() {
		if(mainWindow!=null && mainWindow.progressBar!=null) {
			return new MainMonitor(mainWindow.progressBar);
		}
		return null;
	}
	
	/**
	 * Opens a {@link JFileChooser} and returns the selected folder or
	 * <code>null</code> if no folder was selected.
	 * @return The selected folder or <code>null</code>.
	 */
	public List<File> getDirectorySelection() {
		String lastEbookFolder = JeboorkerPreferences.getEntryString("lastEbookFolder");
		IResourceHandler lastEbookFolderResourceLoader = ResourceHandlerFactory.getResourceHandler(lastEbookFolder);
		if(lastEbookFolderResourceLoader == null || !lastEbookFolderResourceLoader.isDirectoryResource()) {
			lastEbookFolder = null;
		}
		
		final JDirectoryChooser chooser = new JDirectoryChooser(true);
		
		//restore directory chooser location
		String location = JeboorkerPreferences.getEntryString("JDirectoryChooserLocation");
		if(location != null && location.indexOf(";") != -1) {
			String[] split = location.split(";");
			chooser.setDialogLocation(new Point(CommonUtils.toNumber(split[0]).intValue(), CommonUtils.toNumber(split[1]).intValue()));
		}
		
		//restore directory chooser size
		String size = JeboorkerPreferences.getEntryString("JDirectoryChooserSize");
		if(size != null && size.indexOf(";") != -1) {
			String[] split = size.split(";");
			chooser.setDialogSize(new Dimension(CommonUtils.toNumber(split[0]).intValue(), CommonUtils.toNumber(split[1]).intValue()));
		}		
		
		List<File> selectedDirectory = chooser.getDirectorySelections(lastEbookFolder, mainWindow);
		if(selectedDirectory!=null && !selectedDirectory.isEmpty()) {
			JeboorkerPreferences.addEntryString("lastEbookFolder", selectedDirectory.get(selectedDirectory.size() - 1).toString());
		}
		
		//store directory chooser size and location
		JeboorkerPreferences.addEntryString("JDirectoryChooserLocation", chooser.getDialogLocation().x + ";" + chooser.getDialogLocation().y);
		JeboorkerPreferences.addEntryString("JDirectoryChooserSize", chooser.getDialogSize().width + ";" + chooser.getDialogSize().height);
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
		if(localized!=null && localized.length() > 0) {
			return StringUtils.capitalize(localized);
		} else {
			return StringUtils.capitalize(name);
		}
	}	
	
	public void dispose() {
		//Writes the application properties to the preference file
		MainControllerUtils.storeApplicationProperties(mainWindow);
		getSortColumnComponentController().dispose();
		getSortOrderComponentController().dispose();
		getFilterPanelController().dispose();
		MainMenuBarController.getController().dispose();
	}

	/**
	 * Gets the controller for the sort column Combobox. Thats these combobox where the column
	 * could be selected which should be used for the ebook item order. 
	 */
	public SortColumnComponentController getSortColumnComponentController() {
		return sortColumnComponentController;
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
	public FilterPanelController getFilterPanelController() {
		return filterPanelController;
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
		try {
			if(mainWindow.table.getSelectedRowCount() >= 1) {
				final int rowCount = mainWindow.table.getRowCount();
				final int[] selectedRows = mainWindow.table.getSelectedRows();
				final int[] modelRowsIndex = new int[selectedRows.length];
				final List<EbookPropertyItem> items = new ArrayList<EbookPropertyItem>(selectedRows.length);
				for (int i = 0; i < selectedRows.length; i++) {
					if(mainWindow.table.getRowSorter() != null) {
						modelRowsIndex[i] = mainWindow.table.getRowSorter().convertRowIndexToModel(selectedRows[i]);
					} else {
						modelRowsIndex[i] = selectedRows[i];
					}	
					if(modelRowsIndex[i] < rowCount) {
						items.add(((EbookPropertyDBTableModel) mainWindow.table.getModel()).getEbookPropertyItemAt(modelRowsIndex[i]));
					}
				}
				
				if(items.size() > 1) {
					//multiple selection 
					final EbookSheetPropertyMultiSelectionModel model = new EbookSheetPropertyMultiSelectionModel();
					mainWindow.propertySheet.setModel(model);
					
					model.loadProperties(items);
					
					setImage(null, null);
					mainWindow.addMetadataButton.setListModel(EmptyListModel.getSharedInstance());
				} else if (items.size() == 1) {
					//single selection
					final EbookSheetPropertyModel model = new EbookSheetPropertyModel();
					mainWindow.propertySheet.setModel(model);
					
					if(items.get(0) != null) {
						EbookPropertyItem ebookPropertyItem = items.get(0);
						model.loadProperties(ebookPropertyItem);
						byte[] cover = model.getCover();
						if(cover != null && cover.length > 0) {
							setImage(cover, ebookPropertyItem);
						} else {
							setImage(null, null);
						}
						
						IMetadataReader reader = model.getMetadataReader();
						if(reader != null) {
							List<MetadataProperty> allMetaData = model.getAllMetaData();
							MetadataAddListModel metadataAddListModel = new MetadataAddListModel(reader.getSupportedMetaData(), allMetaData, ebookPropertyItem);
							mainWindow.addMetadataButton.setListModel(metadataAddListModel);
						}
					}
				}				
			} else {
				//no selection
				mainWindow.propertySheet.setModel(new EbookSheetPropertyMultiSelectionModel());
				setImage(null, null);
				mainWindow.addMetadataButton.setListModel(EmptyListModel.getSharedInstance());						
			}
		} catch (Exception e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Refresh property sheet has failed.", e);
		}
	}
	
	/**
	 * Get the name of the component that currently has the focus
	 * @return The name of the focus component.
	 */
	public boolean isMainTableFocused() {
		JFrame mainWindow = MainController.getController().getMainWindow();
		Component focusOwner = mainWindow.getFocusOwner();
		if(focusOwner != null && focusOwner instanceof JTable && "MainTable".equals(focusOwner.getName())) {
			return true;
		}
		return false;
	}
	
	private void setImage(final byte[] cover, final EbookPropertyItem ebookPropertyItem) {
		if (cover != null && ebookPropertyItem != null) {
			//remove file extension by removing the separation dot because an image file name is expected.  
			final String coverFileName = StringUtils.replace(ebookPropertyItem.getResourceHandler().getResourceString(), new String[] {".", "/", "\\"}, "_");
			setImageViewerResource(ResourceHandlerFactory.getVirtualResourceHandler(coverFileName, new VirtualStaticResourceDataLoader() {
				
			ByteArrayInputStream byteArrayInputStream = null;
				
			@Override
			public InputStream getContentInputStream() {
				if(byteArrayInputStream == null) {
					byteArrayInputStream = new ByteArrayInputStream(cover);
				}
				byteArrayInputStream.reset();
				return byteArrayInputStream;
			}

			@Override
			public long length() {
				return cover.length;
			}
			}));
		} else {
			setImageViewerResource(null);
		}
	}	
	
	/**
	 * Shows a dialog to the user.
	 * @param message The message of the dialog
	 * @param title The dialog title.
	 * @param option The dialog option: JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.YES_NO_OPTION, JOptionPane.OK_CANCEL_OPTION
	 * @param defaultValue The value which is stored if the user do not want to see the dialog again. 
	 * 		-1 for store the result value instead.
	 * @return 0: yes/ok, 1: no, 2:cancel, -1 none
	 */
	public int showMessageBox(String message, String title, int option, String showAgainKey, int defaultValue) {
		return mainWindow.showMessageBox(message, title, option, showAgainKey, defaultValue);
	}
}

