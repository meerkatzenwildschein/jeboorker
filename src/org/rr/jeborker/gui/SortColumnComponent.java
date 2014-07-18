package org.rr.jeborker.gui;

import java.awt.BorderLayout;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.swing.components.JRCheckBoxComboBox;
import org.rr.commons.swing.components.event.ListCheckListener;
import org.rr.commons.swing.components.event.ListEvent;
import org.rr.commons.swing.components.model.DefaultJRCheckBoxComboBoxModel;
import org.rr.commons.swing.components.model.JRCheckBoxComboBoxModel;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.ViewField;
import org.rr.jeborker.gui.additional.EbookPropertyItemFieldComperator;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;

class SortColumnComponent extends JPanel {

	private final JRCheckBoxComboBox<Field> comboBox = new JRCheckBoxComboBox<Field>();

	private static EbookPropertyItemFieldComperator ebookPropertyItemFieldComperator = new EbookPropertyItemFieldComperator();

	private ArrayList<Field> internalCheckList = new ArrayList<Field>();

	public SortColumnComponent() {
		this.initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		add(comboBox, BorderLayout.CENTER);
		
		JRCheckBoxComboBoxModel<Field> sortColumnComboBoxModel = initModel();
		initSortAction(sortColumnComboBoxModel);
		initClosedViewValue();
		comboBox.setCheckBoxComboBoxModel(sortColumnComboBoxModel);
	}
	
	/**
	 * Initialize the combobox actions to be triggered on a selection change.
	 * @param sortColumnComboBoxModel The model where the selections are triggered.
	 */
	private void initSortAction(JRCheckBoxComboBoxModel<Field> sortColumnComboBoxModel) {
		sortColumnComboBoxModel.addListCheckListener(new ListCheckListener<Field>() {

			@Override
			public void removeCheck(ListEvent<Field> event) {
				internalCheckList.removeAll(event.getValues());
				this.changed(event);
			}

			@Override
			public void addCheck(ListEvent<Field> event) {
				internalCheckList.addAll(event.getValues());
				this.changed(event);
			}

			private void changed(ListEvent<Field> event) {
				final EbookPropertyDBTableModel tableModel = MainController.getController().getTableModel();
				tableModel.setOrderByColumns(internalCheckList);
				MainController.getController().refreshTable();
			}
		});
	}

	/**
	 * Setup the value which is shown if the combobox is closed.
	 *
	 * @param filterFieldSelection The combobox to be setup.
	 */
	private void initClosedViewValue() {
		comboBox.setTextFor(JRCheckBoxComboBox.CheckState.MULTIPLE, new CharSequence() {

			@Override
			public CharSequence subSequence(int start, int end) {
				return getTextValues().subSequence(start, end);
			}

			@Override
			public int length() {
				return getTextValues().length();
			}

			@Override
			public char charAt(int index) {
				return getTextValues().charAt(index);
			}

			private StringBuilder getTextValues() {
				StringBuilder text = new StringBuilder();
				for (Object field : internalCheckList) {
					if(text.length() > 0) {
						text.append(", ");
					}
					String name = ((Field)field).getAnnotation(ViewField.class).name();
					text.append(name);
				}
				return text;
			}

			@Override
			public String toString() {
				return getTextValues().toString();
			}
		});
	}

	/**
	 * Initializes and set the data model to the combobox.
	 * @return The created model.
	 */
	private JRCheckBoxComboBoxModel<Field> initModel() {
		//get fields to be displayed in the combobox
		final List<Field> listEntries = ReflectionUtils.getFieldsByAnnotation(ViewField.class, EbookPropertyItem.class);

		//sort the fields to the DBViewField.orderPriority()
		Collections.sort(listEntries, ebookPropertyItemFieldComperator);

		final DefaultJRCheckBoxComboBoxModel<Field> sortColumnComboBoxModel = new DefaultJRCheckBoxComboBoxModel<Field>(listEntries, null) {

			@Override
			public String getLabel(int index) {
				Field value = getValueAt(index);
				ViewField annotation = value.getAnnotation(ViewField.class);
				String localizedName = Bundle.getString(StringUtils.replace(annotation.name(), " ", "").toLowerCase());
				if(localizedName != null) {
					return localizedName;
				} else {
					return annotation.name();
				}
			}
		};
		return sortColumnComboBoxModel;
	}

	/**
	 * Gets the fields which are checked.
	 * @return The checked fields. Never returns <code>null</code>.
	 */
	public List<Field> getSelectedFields() {
		final List<Field> checkeds = comboBox.getCheckBoxComboBoxModel().getCheckedValues();
		//sort the fields to the DBViewField.orderPriority()
		Collections.sort(checkeds, ebookPropertyItemFieldComperator);
		final ArrayList<Field> result = new ArrayList<Field>(checkeds.size());
		for (Field field : checkeds) {
			result.add(field);
		}
		return result;
	}

	/**
	 * Reads the order columns from the preferences to the combobox model.
	 */
	private void readOrderColumnsFromPreferences() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final String entryString = preferenceStore.getGenericEntryAsString("sortColumnFields");
		if (entryString != null && entryString.length() > 0) {
			final List<String> splitted = ListUtils.split(entryString, ",", -1);
			final JRCheckBoxComboBoxModel<Field> model = comboBox.getCheckBoxComboBoxModel();
			final int modelSize = model.getSize();

			for (String split : splitted) {
				for (int j = 0; j < modelSize; j++) {
					if(model.getValueAt(j).getName().equals(split)) {
						model.addCheck((Field)model.getValueAt(j));
						break;
					}
				}
			}
		} else {
			//set a default set of sort values
			final EbookPropertyDBTableModel tableModel = MainController.getController().getTableModel();
			List<Field> orderByColumns = tableModel.getOrderByColumns();
	    	try {
				orderByColumns.add(EbookPropertyItem.class.getDeclaredField("authorSort"));
				orderByColumns.add(EbookPropertyItem.class.getDeclaredField("title"));
			} catch (Exception e) {
				LoggerFactory.logWarning(this, "Field named author is not available at " + EbookPropertyItem.class, e);
			}
		}
	}

	void storeApplicationProperties() {
		//store the sort order properties.
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final List<Field> selectedFields = getSelectedFields();
		final StringBuilder value = new StringBuilder();
		for (Field field : selectedFields) {
			if(value.length() > 0) {
				value.append(",");
			}
			value.append(field.getName());
		}
		preferenceStore.addGenericEntryAsString("sortColumnFields", value.toString());
	}

	/**
	 * Restores the order fields and put them to view and model.
	 */
	void restoreApplicationProperties() {
		this.readOrderColumnsFromPreferences();
	}

}
