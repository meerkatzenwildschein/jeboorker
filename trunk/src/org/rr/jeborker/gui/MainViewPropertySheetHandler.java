package org.rr.jeborker.gui;

import javax.swing.ListSelectionModel;

import org.rr.jeborker.gui.model.EbookSheetPropertyModel;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class MainViewPropertySheetHandler {

	private PropertySheetPanel propertySheet;
	
	private MainView mainView;

	public MainViewPropertySheetHandler(PropertySheetPanel propertySheet, MainView mainView) {
		this.propertySheet = propertySheet;
		this.mainView = mainView;
	}

	/**
	 * Adds the given property to the end of the property sheet.
	 * @param property The property to be added.
	 */
	public void addMetadataProperty(Property property) {
		propertySheet.addProperty(property);
	}
	
	public ListSelectionModel getSelectionModel() {
		return propertySheet.getSelectionModel();
	}
	
	public EbookSheetPropertyModel getModel() {
		return (EbookSheetPropertyModel) propertySheet.getModel();
	}
	
	public void setModel(EbookSheetPropertyModel model) {
		EbookSheetPropertyModel oldModel = getModel();
		if(oldModel != null) {
			oldModel.dispose();
		}
		propertySheet.setModel(model);
	}

	/**
	 * Rereads the metadata properties and set them to the sheet.
	 */
	public void refreshSheetProperties() {
		mainView.refreshSheetProperties();
	}
	
}
