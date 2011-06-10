package org.rr.jeborker.gui.model;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;

public class EbookSheetPropertyModel extends PropertySheetTableModel {

	private static final long serialVersionUID = -4633492433120559387L;

	private boolean changed = false;

	public boolean isChanged() {
		Property[] properties = this.getProperties();
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
		super.removeProperty(property);
		changed = true;
	}

	@Override
	public void setProperties(Property[] newProperties) {
		super.setProperties(newProperties);
		changed = false;
	}
	
}
