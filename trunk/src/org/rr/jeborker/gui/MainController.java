package org.rr.jeborker.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.VirtualStaticResourceDataLoader;
import org.rr.commons.swing.dialogs.JDirectoryChooser;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.UtilConstants;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.event.ApplicationEvent;
import org.rr.jeborker.event.EventManager;
import org.rr.jeborker.gui.action.AddMetadataAction;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;
import org.rr.jeborker.gui.model.EbookSheetProperty;
import org.rr.jeborker.gui.model.EbookSheetPropertyModel;
import org.rr.jeborker.gui.model.MetadataAddListModel;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

import com.l2fprod.common.propertysheet.DefaultProperty;
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
				saveProperties();
				refreshSheetProperties(); 
			}
		}

		/**
		 * Saves/Writes the metadata properties if something has changed. 
		 */
		private void saveProperties() {
			//save the previous properties 
			final List<Property> previousProperties = mainWindow.propertySheet.getProperties();
			if(mainWindow.propertySheet.getTable().getModel() instanceof EbookSheetPropertyModel && ((EbookSheetPropertyModel)mainWindow.propertySheet.getTable().getModel()).isChanged()) {
				//write properties
				MainControllerUtils.writeProperties(previousProperties);
				
				//a repaint does a refresh to all visible table rows.
				mainWindow.table.repaint();
			}
		}
	}
	
	/**
	 * PropertySheetChangeListener is always invoked if a values in the property sheet has changed.
	 */
	private class PropertySheetChangeListener implements PropertyChangeListener {
		
		/**
		 * On each change in the metadata sheet the sheet properties are transfered into metadata properties
		 * and the metadata reader refreshes the selected {@link EbookPropertyItem}. 
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			final List<EbookPropertyItem> selectedEbookPropertyItems = getSelectedEbookPropertyItems();
			for (EbookPropertyItem ebookPropertyItem : selectedEbookPropertyItems) {
				IMetadataReader reader = MetadataHandlerFactory.getReader(ebookPropertyItem.getResourceHandler());
				ArrayList<MetadataProperty> metadataProperties = MainControllerUtils.createMetadataProperties(mainWindow.propertySheet.getProperties());
				reader.fillEbookPropertyItem(metadataProperties, ebookPropertyItem);
				refreshTableSelectedItem(false);
			}
		}
	}
	
	/**
	 * Mouse listener which handles the right click / popup menu on the main table.
	 */
	private class MainTablePopupMouseAdapter extends MouseAdapter {

		public void mouseReleased(MouseEvent event) {
			if (event.getButton() == MouseEvent.BUTTON3) {
				final int rowAtPoint = mainWindow.table.rowAtPoint(event.getPoint());
				
				//set selection for the right click
				if(mainWindow.table.getSelectedRowCount() <= 1 ) {
					mainWindow.table.getSelectionModel().setSelectionInterval(rowAtPoint, rowAtPoint);
				}
				
				MainMenuController.getController().showMainPopupMenu(event.getPoint(), mainWindow.table);
			}
		}
	}
	
	/**
	 * Gets the controller instance. because we have only one main window
	 * We have a singleton here.
	 * @return The desired EBorkerMainController.
	 */
	public static MainController getController() {
		if(controller==null) {
			controller = new MainController();
			controller.initialize();
		}
		return controller;
	}
	
	private void initialize() {
		mainWindow = new MainView();
		initListeners();
		initSubController();
		
		MainControllerUtils.restoreApplicationProperties(mainWindow);
		mainWindow.setVisible(true);
	}

	private void initSubController() {
		sortColumnComponentController = new SortColumnComponentController(mainWindow.sortColumnComboBox);
		sortOrderComponentController = new SortOrderComponentController(mainWindow.sortOrderAscButton, mainWindow.sortOrderDescButton);
		filterPanelController = new FilterPanelController();
	}
	
	private void initListeners() {
		mainWindow.table.getSelectionModel().addListSelectionListener(new PropertySheetListSelectionListener());
		mainWindow.table.addMouseListener(new MainTablePopupMouseAdapter());
		
		mainWindow.propertySheet.addPropertySheetChangeListener(new PropertySheetChangeListener());
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
		final TableModel model = mainWindow.table.getModel();
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

	/**
	 * Refresh the selected table row.
	 */
	public void refreshTableSelectedItem(final boolean refreshMetadataSheet) {
		final TableModel model = mainWindow.table.getModel();
		int[] selectedRows = getSelectedEbookPropertyItemRows() ;
		if(selectedRows==null || selectedRows.length==0) {
			return;
		} else {
			int editingRow = mainWindow.table.getEditingRow();
			for (int i = 0; i < selectedRows.length; i++) {
				if(editingRow != -1 && editingRow == selectedRows[i]) {
					mainWindow.table.editingStopped(null);
				} 
				mainWindow.table.tableChanged(new TableModelEvent(model, selectedRows[i]));					
			}
		}
		
		if(refreshMetadataSheet) {
			refreshSheetProperties();
		}
	}
	
	/**
	 * Refresh the given table rows.
	 * @param rows The rows to be refreshed.
	 * @param refreshMetadataSheet TODO
	 * @param refreshMetadataSheet do also refresh the metadata sheet. 
	 */
	public void refreshTableRows(final int[] rows, boolean refreshMetadataSheet) {
		final TableModel model = mainWindow.table.getModel();
		if(rows==null || rows.length==0) {
			return;
		} else {
			int editingRow = mainWindow.table.getEditingRow();
			int[] selectedEbookPropertyItemRows = getSelectedEbookPropertyItemRows();
			for (int i = 0; i < rows.length; i++) {
				if(editingRow != -1 && editingRow == rows[i]) {
					mainWindow.table.editingStopped(null);
				}
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
	 * Removes the metadata property which is currently selected in the property sheet.
	 */
	public void removeSelectedMetadataProperty() {
		Property selectedMetadataProperty = getSelectedMetadataProperty();
		if(selectedMetadataProperty!=null) {
			if(selectedMetadataProperty.isEditable()) {
				TableModel model = mainWindow.propertySheet.getTable().getModel();
				((PropertySheetTableModel)model).removeProperty(selectedMetadataProperty);
			}
		}
	}	
	
	/**
	 * Adds the given property to the end of the property sheet.
	 * @param property The property to be added.
	 */
	public void addMetadataProperty(Property property) {
		mainWindow.propertySheet.addProperty(property);
	}	
	
	/**
	 * Get the currently selected metadata property from the metadata sheet.
	 * @return The desired {@link Property} instance or <code>null</code> if no selection is made.
	 */
	public Property getSelectedMetadataProperty() {
		int selectedRow = mainWindow.propertySheet.getTable().getSelectedRow();
		if(selectedRow >= 0) {
			EbookSheetPropertyModel model = (EbookSheetPropertyModel) mainWindow.propertySheet.getTable().getModel();
			PropertySheetTableModel.Item item = (Item) model.getObject(selectedRow);
			Property property = item.getProperty();
			return property;
		}
		return null;
	}
	
	/**
	 * Sets the rating value (between 1 and 10).
	 * @param rating The rating value.
	 */
	public void setRatingToSelectedEntry(int rating) {
		EbookSheetPropertyModel model = (EbookSheetPropertyModel) mainWindow.propertySheet.getTable().getModel();
		Property ratingProperty = model.getRatingProperty();
		if(ratingProperty != null) {
			ratingProperty.setValue(rating);
		} else {
			List<EbookPropertyItem> selectedEbookPropertyItems = getSelectedEbookPropertyItems();
			for (EbookPropertyItem item : selectedEbookPropertyItems) {
				IMetadataReader reader = MetadataHandlerFactory.getReader(ResourceHandlerFactory.getResourceLoader(item.getFile()));
				MetadataProperty ratingMetaData = reader.getRatingMetaData();
				Property addMetadataItem = AddMetadataAction.addMetadataItem(ratingMetaData);
				addMetadataItem.setValue(rating);
			}
		}
	}
	
	/**
	 * Adds the given {@link EbookPropertyItem} to the model. The added
	 * {@link EbookPropertyItem} will be shown to the ui.
	 * @param item The item to be added.
	 */
	public void addEbookPropertyItem(EbookPropertyItem item) {
		TableModel model = mainWindow.table.getModel();
		if(model instanceof EbookPropertyDBTableModel) {
			((EbookPropertyDBTableModel)model).addRow(item);
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
	 * @return all selected rows or an empty array if no row is selected.
	 */
	public int[] getSelectedEbookPropertyItemRows() {
		if(mainWindow!=null && mainWindow.table != null) {
			final int[] selectedRows = mainWindow.table.getSelectedRows();
			return selectedRows;
		} else {
			return new int[0];
		}
	}
	
	/**
	 * Removes the given {@link EbookPropertyItem} from the model. This
	 * change the view automatically.
	 * @param item The {@link EbookPropertyItem} to be removed.
	 */
	public boolean removeEbookPropertyItem(EbookPropertyItem item) {
		TableModel model = mainWindow.table.getModel();
		if(model instanceof EbookPropertyDBTableModel) {
			return ((EbookPropertyDBTableModel)model).removeRow(item);
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
	public File getDirectorySelection() {
		final String lastEbookFolder = JeboorkerPreferences.getEntryString("lastEbookFolder");
		File selectedDirectory = JDirectoryChooser.getDirectorySelection(lastEbookFolder, mainWindow);
		if(selectedDirectory!=null) {
			JeboorkerPreferences.addEntryString("lastEbookFolder", selectedDirectory.toString());
		}
		return selectedDirectory;
	}
	
	/**
	 * Tries to translate the given property name.
	 *  
	 * @param name The name to be translated.
	 * @return The translated name.
	 */
	public String getLocalizedString(final String name) {
		if(name == null) {
			return null;
		}
		String localized = StringUtils.replace(name.toLowerCase(), ":", "");
		localized = StringUtils.replace(localized, "/", "");
		localized = StringUtils.replace(localized, " ", "");
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
	JFrame getMainWindow() {
		return mainWindow;
	}

	/**
	 * Rereads the metadata properties and set them to the sheet.
	 */
	public void refreshSheetProperties() {
		if(mainWindow.table.getSelectedRowCount() > 1 || mainWindow.table.getSelectedRowCount() == 0) {
			//clear on multiple selection 
			mainWindow.propertySheet.setProperties(new Property[] {new DefaultProperty()});
			setImage(null);
			mainWindow.addMetadataButton.setListModel(new DefaultListModel());
		} else if (mainWindow.table.getSelectedRowCount() == 1) {
			final int selectedRow = mainWindow.table.getSelectedRow();
			final int modelRowIndex;
			if(mainWindow.table.getRowSorter()!=null) {
				modelRowIndex = mainWindow.table.getRowSorter().convertRowIndexToModel(selectedRow);
			} else {
				modelRowIndex = selectedRow;
			}
			final EbookPropertyItem item = ((EbookPropertyDBTableModel)mainWindow.table.getModel()).getEbookPropertyItemAt(modelRowIndex);
			if(item==null) {
				//clear
				mainWindow.propertySheet.setProperties(new Property[] {new DefaultProperty()});
			}
			
			final IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceLoader(item.getFile());
			final IMetadataReader reader = MetadataHandlerFactory.getReader(resourceHandler);
			mainWindow.propertySheet.setProperties(EbookSheetProperty.createProperties(resourceHandler, reader));
			if(item.getCoverThumbnail()!=null && item.getCoverThumbnail().length > 0) {
				setImage(reader);
			} else {
				setImage(null);
			}
			
			mainWindow.addMetadataButton.setListModel(new MetadataAddListModel(reader));
		}
	}
	
	private void setImage(final IMetadataReader reader) {
		final IResourceHandler ebookResource = reader!=null ? reader.getEbookResource() : null;
		if (ebookResource != null) {
			String coverFileName = StringUtils.substringBefore(ebookResource.getName(), ".", false, UtilConstants.COMPARE_BINARY);
			setImageViewerResource(ResourceHandlerFactory.getVirtualResourceLoader(coverFileName, new VirtualStaticResourceDataLoader() {
				
				ByteArrayInputStream byteArrayInputStream = null;
				
				@Override
				public InputStream getContentInputStream() {
					if(byteArrayInputStream==null) {
						byteArrayInputStream = new ByteArrayInputStream(reader.getCover());
					}
					byteArrayInputStream.reset();
					return byteArrayInputStream;
				}
			}));
		} else {
			setImageViewerResource(null);
		}
	}	
}

