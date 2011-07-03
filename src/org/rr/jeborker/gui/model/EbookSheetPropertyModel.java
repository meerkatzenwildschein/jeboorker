package org.rr.jeborker.gui.model;

import java.util.List;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;

public class EbookSheetPropertyModel extends PropertySheetTableModel {

	private static final long serialVersionUID = -4633492433120559387L;

	private boolean changed = false;

	public boolean isChanged() {
		List<Property> properties = this.getProperties();
		for (Property property : properties) {
			if(property instanceof EbookSheetProperty && ((EbookSheetProperty)property).isChanged()) {
				return true;
			}
		}
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public void addProperty(int index, Property property) {
		super.addProperty(index, property);
		changed = true;
	}

	@Override
	public void addProperty(Property property) {
		super.addProperty(property);
		changed = true;
	}

	@Override
	public void removeProperty(Property property) {
//		if(property instanceof EbookSheetProperty) {
//			((EbookSheetProperty)property).firePropertyChanged(null, null);
//		}
		property.setValue("");
		super.removeProperty(property);
		changed = true;
	}

	@Override
	public void setProperties(Property[] newProperties) {
		super.setProperties(newProperties);
		changed = false;
	}
	
	/**
	 * The rating property.
	 * @return The desired rating property or <code>null</code> if no rating property is exists.
	 */
	public Property getRatingProperty() {
		List<Property> properties = getProperties();
		for (Property property : properties) {
			if(property.getName().toLowerCase().indexOf("rating") != -1) {
				return property;
			}
		}
		return null;
	}
}
