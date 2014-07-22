package org.rr.jeborker.gui;

import javax.swing.ListSelectionModel;

import org.rr.jeborker.gui.model.EbookSheetPropertyModel;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class MainViewPropertySheetHandler {

	private PropertySheetPanel propertySheet;

	public MainViewPropertySheetHandler(PropertySheetPanel propertySheet) {
		this.propertySheet = propertySheet;
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
	
	public EbookSheetPropertyModel getPropertySheetModel() {
		return (EbookSheetPropertyModel) propertySheet.getModel();
	}

}
