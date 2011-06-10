package org.rr.jeborker.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;

import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.JEBorkerPreferences;
import org.rr.jeborker.gui.action.ActionFactory;

public class FilterPanelController {
	
	private static FilterPanelView view;
	
	FilterPanelController() {
		initialize();
	}

	/**
	 * {@link ActionListener} which starts the filter process if the user press enter in the combobox 
	 * text field.
	 */
	private class FilterFieldActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SEARCH_ACTION, null).actionPerformed(e);
		}
	}
	
	/**
	 * Get the menu view which is a {@link JMenuBar} instance.
	 * @return The menu view.
	 */
	static JPanel getView() {
		if(view==null) {
			view = new FilterPanelView();
		}
		return view;
	}
	
	void initialize() {
		final FilterFieldActionListener filterFieldActionListener = new FilterFieldActionListener();
		final String latestSearch = JEBorkerPreferences.getEntryString("FilterPanelControllerCurrentFilter");
		
		this.restoreFilterHistory();
		view.filterField.getEditor().setItem(latestSearch);
		filterFieldActionListener.actionPerformed(new ActionEvent(this, 0, "initialize"));
		view.filterField.getEditor().addActionListener(filterFieldActionListener);	
	}
	
	/**
	 * Gets the text from the filter/search field.
	 * @return The text from the filter/search field. Never returns <code>null</code>.
	 */
	public String getFilterText() {
		return StringUtils.toString(view.filterField.getEditor().getItem());
	}	
	
	/**
	 * Adds the given search expression to the beginning of the combobox list. 
	 * If the list exceeds more than 10 entries, the last ones will be deleted.
	 * @param searchExpression The search expression to be attached.
	 */
	public void addFilterFieldSearch(final String searchExpression) {
		if(searchExpression!=null && searchExpression.length() > 0) {
			MutableComboBoxModel model = (MutableComboBoxModel)view.filterField.getModel();
			model.insertElementAt(searchExpression, 0);
			if(model.getSize() > 10) {
				model.removeElementAt(model.getSize()-1);
			}
			removeDuplicateElementsFromFilterModel(searchExpression);
		}
	}
	
	/**
	 * If the selected expression or any other one is more than one times in
	 * the filter history list, the last ones will be removed. 
	 * @param selectedExpression The currently selected filter expression.
	 */
	private void removeDuplicateElementsFromFilterModel(final String selectedExpression) {
		MutableComboBoxModel model = (MutableComboBoxModel)view.filterField.getModel();
		HashSet<String> entries = new HashSet<String>(model.getSize());
		for (int i = 0; i < model.getSize(); i++) {
			String elementAt = (String) model.getElementAt(i);
			if(entries.contains(elementAt)) {
				model.removeElementAt(i);
				i--;
			}
			entries.add(elementAt);
		}
		model.setSelectedItem(selectedExpression);
	}
	
	public void dispose() {
		this.storeFilterHistory();
	}
	
	/**
	 * Stores the filter history in a comma separated list to the preference store.
	 * @see #restoreFilterHistory()
	 */
	private void storeFilterHistory() {
		final MutableComboBoxModel model = (MutableComboBoxModel)view.filterField.getModel();
		final StringBuilder modelEntries = new StringBuilder();
		for (int i = 0; i < model.getSize(); i++) {
			String elementAt = StringUtils.replace(StringUtils.toString(model.getElementAt(i)), ",", "");
			if(modelEntries.length() > 0) {
				modelEntries.append(",");
			}
			modelEntries.append(elementAt);
		}
		JEBorkerPreferences.addEntryString("FilterPanelControllerEntries", modelEntries.toString());		
		JEBorkerPreferences.addEntryString("FilterPanelControllerCurrentFilter", getFilterText());
	}
	
	/**
	 * Restore the previously stored filter history.
	 * @see #storeFilterHistory() 
	 */
	private void restoreFilterHistory() {
		final MutableComboBoxModel model = (MutableComboBoxModel)view.filterField.getModel();
		String filterEntries = JEBorkerPreferences.getEntryString("FilterPanelControllerEntries");
		if(filterEntries!=null && filterEntries.length() > 0) {
			List<String> split = ListUtils.split(filterEntries, ",");
			for (String string : split) {
				model.addElement(string);
			}
		}
		view.filterField.updateUI();
	}
}
 