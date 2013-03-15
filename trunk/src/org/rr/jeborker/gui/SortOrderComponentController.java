package org.rr.jeborker.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JToggleButton;

import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.db.OrderDirection;

public class SortOrderComponentController {
	
	private JToggleButton ascButton;
	private JToggleButton descButton;

	public SortOrderComponentController(JToggleButton ascButton, JToggleButton descButton) {
		this.ascButton = ascButton;
		this.descButton = descButton;
		this.init();
	}
	
	private void init() {
		Icon ascIcon = ascButton.getIcon();
		ascButton.setAction(new AbstractAction(null, ascIcon) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				descButton.setSelected(false);
				ascButton.setSelected(true);
				MainController.getController().getTableModel().setOrderDirection(new OrderDirection(OrderDirection.DIRECTION_ASC));
				MainController.getController().refreshTable();
			}
		});
		
		Icon descIcon = descButton.getIcon();
		descButton.setAction(new AbstractAction(null, descIcon) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				ascButton.setSelected(false);
				descButton.setSelected(true);
				MainController.getController().getTableModel().setOrderDirection(new OrderDirection(OrderDirection.DIRECTION_DESC));
				MainController.getController().refreshTable();
			}
		});	
		
		this.restoreProperties();
		if(!ascButton.isSelected() && !descButton.isSelected()) {
			//ascending order by default
			ascButton.setSelected(true);
		}
		
	}
	
	public void dispose() {
		this.storeProperties();
	}
	
	private void storeProperties() {
		String value = ascButton.isSelected() ? "asc" : "desc";
		JeboorkerPreferences.addGenericEntryAsString("sortColumnOrder", value);
	}
	
	
	/**
	 * Restores the order fields and put them to view and model.
	 */
	private void restoreProperties() {
		String sortColumnOrder = JeboorkerPreferences.getGenericEntryAsString("sortColumnOrder");
		if(sortColumnOrder != null) {
		if(sortColumnOrder.equalsIgnoreCase("asc")) {
			ActionEvent e = new ActionEvent(ascButton, ActionEvent.ACTION_PERFORMED, null);
			ascButton.getAction().actionPerformed(e);
		} else if(sortColumnOrder.equalsIgnoreCase("desc")) {
			ActionEvent e = new ActionEvent(descButton, ActionEvent.ACTION_PERFORMED, null);
			descButton.getAction().actionPerformed(e);
		}
		}
	}	
}
