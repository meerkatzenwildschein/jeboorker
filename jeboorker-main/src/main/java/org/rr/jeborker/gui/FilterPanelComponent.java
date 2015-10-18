package org.rr.jeborker.gui;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;

import org.rr.commons.swing.components.JRCheckBoxComboBox;
import org.rr.commons.swing.components.JREditableHistoryComboBox;
import org.rr.commons.swing.components.model.DefaultJRCheckBoxComboBoxModel;
import org.rr.commons.swing.components.model.JRCheckBoxComboBoxModel;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.ViewField;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.gui.action.ApplicationAction;
import org.rr.jeborker.gui.additional.EbookPropertyItemFieldComperator;

import net.miginfocom.swing.MigLayout;

public class FilterPanelComponent extends JPanel {

	private static EbookPropertyItemFieldComperator ebookPropertyItemFieldComperator = new EbookPropertyItemFieldComperator();
	
	private final JRCheckBoxComboBox<Field> filterFieldSelection = new JRCheckBoxComboBox<>();
	
	private final JREditableHistoryComboBox filterField = new JREditableHistoryComboBox();
	
	private final FilterFieldActionListener filterFieldActionListener = new FilterFieldActionListener();

	FilterPanelComponent() {
		initialize();
	}

	/**
	 * {@link ActionListener} which starts the filter process if the user press enter in the combobox text field.
	 */
	private class FilterFieldActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			getSearchAction().invokeAction(e);
		}
		
		public ApplicationAction getNonThreadingSearchAction() {
			ApplicationAction action = getSearchAction();
			action.putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE);
			return action;
		}
		
		public ApplicationAction getSearchAction() {
			return ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SEARCH_ACTION, null);
		}
	}

	private void initialize() {
		setLayout(new MigLayout("insets 0 5 0 0"));
		
		JLabel lblSearch = new JLabel(Bundle.getString("FilterPanelView.label.search"));
		add(lblSearch, "w 55!");
		
		filterFieldSelection.setMinPopupWidth(200);
		
		Dimension filterFieldSelectionSize = new Dimension(80, filterFieldSelection.getPreferredSize().height);
		filterFieldSelection.setPreferredSize(filterFieldSelectionSize);
		filterFieldSelection.setMinimumSize(filterFieldSelectionSize);
		
		JRCheckBoxComboBoxModel<Field> sortColumnComboBoxModel = this.initFieldSelectionModel();
		filterFieldSelection.setCheckBoxComboBoxModel(sortColumnComboBoxModel);
		
		add(filterFieldSelection, EMPTY);
		add(filterField, "w 100%");
		
		JButton searchButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SEARCH_ACTION, EMPTY));
		add(searchButton);
	}
	
	public void initListeners() {
		filterField.getEditor().addActionListener(filterFieldActionListener);
	}

	/**
	 * Gets the text from the filter/search field.
	 *
	 * @return The text from the filter/search field. Never returns <code>null</code>.
	 */
	public String getFilterText() {
		return filterField.getEditorValue();
	}

	/**
	 * get all {@link EbookPropertyItem} fields which should be included by the filter.
	 *
	 * @return The fields to be filtered. Never returns null.
	 */
	public List<Field> getSelectedFilterFields() {
		JRCheckBoxComboBoxModel<Field> model = filterFieldSelection.getCheckBoxComboBoxModel();
		List<Field> checkeds = model.getCheckedValues();
		return checkeds;
	}

	/**
	 * get all {@link EbookPropertyItem} field names which are selected by the filter selection combobox.
	 *
	 * @return The field names to be filtered. Never returns null.
	 */
	public List<String> getSelectedFilterFieldNames() {
		List<Field> selectedFilterFields = getSelectedFilterFields();
		ArrayList<String> result = new ArrayList<>(selectedFilterFields.size());
		for (Field field : selectedFilterFields) {
			result.add(field.getName());
		}

		return result;
	}

	/**
	 * Initializes and set the data model to the combobox.
	 *
	 * @return The created model.
	 */
	private JRCheckBoxComboBoxModel<Field> initFieldSelectionModel() {
		// get fields to be displayed in the combobox
		final List<Field> listEntries = ReflectionUtils.getFieldsByAnnotation(ViewField.class, EbookPropertyItem.class);

		// sort the fields to the DBViewField.orderPriority()
		Collections.sort(listEntries, ebookPropertyItemFieldComperator);

		final JRCheckBoxComboBoxModel<Field> filterFieldColumnComboBoxModel = new DefaultJRCheckBoxComboBoxModel<Field>(listEntries, null) {

			@Override
			public String getLabel(int index) {
				Field value = getValueAt(index);
				ViewField annotation = ((Field) value).getAnnotation(ViewField.class);
				String localizedName = Bundle.getString(StringUtil.replace(annotation.name(), " ", EMPTY).toLowerCase());
				if (localizedName != null) {
					return localizedName;
				} else {
					return annotation.name();
				}
			}
		};
		return filterFieldColumnComboBoxModel;
	}

	/**
	 * Adds the given search expression to the beginning of the combobox list. If the list exceeds more than 10 entries, the last ones will be deleted.
	 *
	 * @param searchExpression
	 *            The search expression to be attached.
	 */
	public void addFilterFieldSearch(final String searchExpression) {
		filterField.addHistoryValue(searchExpression);
	}

	
	/**
	 * Tells the text filter field to display it self in and active filter color.
	 */
	public void enableFilterColor(boolean enable) {
		filterField.enableFilterColor(enable);
	}

	/**
	 * Stores the filter history in a comma separated list to the preference store.
	 *
	 * @see #restoreComponentProperties()
	 */
	void storeApplicationHistory() {
		storeFileHistory();
		
		storeFilterHistory();
		storeFilterSelection();
	}

	private void storeFilterSelection() {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		preferenceStore.addGenericEntryAsString("FilterPanelControllerCurrentFilterFieldSelection", ListUtils.join(getSelectedFilterFieldNames(), ","));
	}
	
	private void storeFilterHistory() {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		String historyValues = filterField.getHistoryValues();
		preferenceStore.addGenericEntryAsString("FilterPanelControllerEntries", historyValues);
	}

	private void storeFileHistory() {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		preferenceStore.addGenericEntryAsString("FilterPanelControllerCurrentFilter", getFilterText());
	}

	/**
	 * Restore the previously stored filter history.
	 *
	 * @see #storeApplicationHistory()
	 */
	void restoreComponentProperties() {
		restoreFilterHistory();
		restoreFilterSelection();

		restoreFileHistory();
	}

	private void restoreFilterHistory() {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		String filterEntries = preferenceStore.getGenericEntryAsString("FilterPanelControllerEntries");
		filterField.setHistoryValues(filterEntries);
	}

	private void restoreFilterSelection() {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		String filterFieldSelectionEntries = preferenceStore.getGenericEntryAsString("FilterPanelControllerCurrentFilterFieldSelection");
		List<String> splitted = ListUtils.split(filterFieldSelectionEntries, ",");
		final JRCheckBoxComboBoxModel<Field> model = filterFieldSelection.getCheckBoxComboBoxModel();
		final int modelSize = model.getSize();

		for (String split : splitted) {
			for (int j = 0; j < modelSize; j++) {
				if (model.getValueAt(j).getName().equals(split)) {
					model.addCheck((Field) model.getValueAt(j));
					break;
				}
			}
		}
	}
	
	private void restoreFileHistory() {
		String latestSearch = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getGenericEntryAsString(
				"FilterPanelControllerCurrentFilter");
		filterField.setItem(latestSearch);
		filterFieldActionListener.getNonThreadingSearchAction().invokeAction(new ActionEvent(this, 0, "initialize"));
	}
}
