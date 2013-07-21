package org.rr.jeborker.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.JList;
import javax.swing.MutableComboBoxModel;

import org.japura.gui.CheckComboBox;
import org.japura.gui.model.DefaultListCheckModel;
import org.japura.gui.model.ListCheckModel;
import org.japura.gui.renderer.CheckListRenderer;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.db.item.ViewField;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.gui.additional.EbookPropertyItemFieldComperator;

public class FilterPanelController {
	
	private static MainView view;
	
	private static EbookPropertyItemFieldComperator ebookPropertyItemFieldComperator = new EbookPropertyItemFieldComperator();
	
	FilterPanelController() {
		view = MainController.getController().mainWindow;
		initialize();
	}

	/**
	 * {@link ActionListener} which starts the filter process if the user press enter in the combobox 
	 * text field.
	 */
	private class FilterFieldActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SEARCH_ACTION, null).invokeAction(e);
		}
	}
	
//	/**
//	 * Get the menu view which is a {@link JMenuBar} instance.
//	 * @return The menu view.
//	 */
//	static JPanel getView() {
//		if(view==null) {
//			view = new FilterPanelView();
//		}
//		return view;
//	}
	
	void initialize() {
		final FilterFieldActionListener filterFieldActionListener = new FilterFieldActionListener();
		final String latestSearch = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getGenericEntryAsString("FilterPanelControllerCurrentFilter");
		
		this.initFieldSelectionRenderer();
		this.initFilterSelectionClosedViewValue();
		
		DefaultListCheckModel<Field> sortColumnComboBoxModel = this.initFieldSelectionModel();
		view.filterFieldSelection.setModel(sortColumnComboBoxModel);
		
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
	 * Tells the text filter field to display it self in and active filter color. 
	 */
	public void enableFilterColor(boolean enable) {
		view.enableFilterColor(enable);
	}
	
	/**
	 * get all {@link EbookPropertyItem} fields which should be included by the filter.
	 * @return The fields to be filtered. Never returns null.
	 */
	public List<Field> getSelectedFilterFields() {
		ListCheckModel<Field> model = view.filterFieldSelection.getModel();
		List<Field> checkeds = model.getCheckeds();
		return checkeds;
	}
	
	/**
	 * get all {@link EbookPropertyItem} field names which are selected by the filter selection combobox.
	 * @return The field names to be filtered. Never returns null.
	 */
	public List<String> getSelectedFilterFieldNames() {
		List<Field> selectedFilterFields = getSelectedFilterFields();
		ArrayList<String> result = new ArrayList<String>(selectedFilterFields.size());
		for (Field field : selectedFilterFields) {
			result.add(field.getName());
		}
		
		return result;
	}	
	
	/**
	 * Initialize the renderer which is able to show the field names.
	 * @param filterFieldSelection The combobox instance where the renderer should be applied to.
	 */
	private void initFieldSelectionRenderer() {
		//renderer setup for showing the name annotation for the fields.
		view.filterFieldSelection.setRenderer(new CheckListRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(Object value) {
				ViewField annotation = ((Field) value).getAnnotation(ViewField.class);
				String localizedName = Bundle.getString(StringUtils.replace(annotation.name(), " ", "").toLowerCase());
				if(localizedName != null) {
					return localizedName;
				} else {
					return annotation.name();					
				}
			}

			@Override
			public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				return c;
			}
		});
	}

	/**
	 * Initializes and set the data model to the combobox.
	 * @return The created model.
	 */
	private DefaultListCheckModel<Field> initFieldSelectionModel() {
		//get fields to be displayed in the combobox
		final List<Field> listEntries = EbookPropertyItemUtils.getFieldsByAnnotation(ViewField.class, EbookPropertyItem.class);
		
		//sort the fields to the DBViewField.orderPriority()
		Collections.sort(listEntries, ebookPropertyItemFieldComperator);
		
		final DefaultListCheckModel<Field> sortColumnComboBoxModel = new DefaultListCheckModel<Field>();
		for (Field field : listEntries) {
			sortColumnComboBoxModel.addElement(field);
		}
		return sortColumnComboBoxModel;
	}	
	
	/**
	 * Setup the value which is shown if the combobox is closed. 
	 * 
	 * @param filterFieldSelection The combobox to be setup.
	 */
	private void initFilterSelectionClosedViewValue() {
		view.filterFieldSelection.setTextFor(CheckComboBox.MULTIPLE, "***");
	}	
	
	/**
	 * Adds the given search expression to the beginning of the combobox list. 
	 * If the list exceeds more than 10 entries, the last ones will be deleted.
	 * @param searchExpression The search expression to be attached.
	 */
	public void addFilterFieldSearch(final String searchExpression) {
		if(searchExpression!=null && searchExpression.length() > 0) {
			MutableComboBoxModel<String> model = (MutableComboBoxModel<String>)view.filterField.getModel();
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
		MutableComboBoxModel<String> model = (MutableComboBoxModel<String>)view.filterField.getModel();
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
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final MutableComboBoxModel<String> model = (MutableComboBoxModel<String>)view.filterField.getModel();
		final StringBuilder modelEntries = new StringBuilder();
		for (int i = 0; i < model.getSize(); i++) {
			String elementAt = StringUtils.replace(StringUtils.toString(model.getElementAt(i)), ",", "");
			if(modelEntries.length() > 0) {
				modelEntries.append(",");
			}
			modelEntries.append(elementAt);
		}
		preferenceStore.addGenericEntryAsString("FilterPanelControllerEntries", modelEntries.toString());		
		preferenceStore.addGenericEntryAsString("FilterPanelControllerCurrentFilter", getFilterText());
		preferenceStore.addGenericEntryAsString("FilterPanelControllerCurrentFilterFieldSelection", ListUtils.join(getSelectedFilterFieldNames(), ","));
	}
	
	/**
	 * Restore the previously stored filter history.
	 * @see #storeFilterHistory() 
	 */
	private void restoreFilterHistory() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		{
			final MutableComboBoxModel<String> model = (MutableComboBoxModel<String>)view.filterField.getModel();
			String filterEntries = preferenceStore.getGenericEntryAsString("FilterPanelControllerEntries");
			if(filterEntries!=null && filterEntries.length() > 0) {
				List<String> split = ListUtils.split(filterEntries, ",");
				for (String string : split) {
					model.addElement(string);
				}
			}
			view.filterField.updateUI();
		}
		
		
		{ //restore the filter field selection
			String filterFieldSelectionEntries = preferenceStore.getGenericEntryAsString("FilterPanelControllerCurrentFilterFieldSelection");
			List<String> splitted = ListUtils.split(filterFieldSelectionEntries, ",");
			final ListCheckModel<Field> model = view.filterFieldSelection.getModel();
			final int modelSize = model.getSize();
			
			for (String split : splitted) {
				for (int j = 0; j < modelSize; j++) {
					if(model.getElementAt(j).getName().equals(split)) {
						model.addCheck((Field)model.getElementAt(j));
						break;
					}
				}
			}
		}
	}
}
 