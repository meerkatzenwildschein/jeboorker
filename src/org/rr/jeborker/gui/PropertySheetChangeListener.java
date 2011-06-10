package org.rr.jeborker.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PropertySheetChangeListener implements PropertyChangeListener {

	public PropertySheetChangeListener() {
		super();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println("PropertySheetChangeListener: " + evt.getNewValue());
		
	}

}
