package org.rr.jeborker.gui;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;

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
import org.rr.jeborker.db.OrderDirection;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.ViewField;
import org.rr.jeborker.gui.additional.EbookPropertyItemFieldComperator;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class SortColumnComponent extends JPanel {

	private final JRCheckBoxComboBox<Field> orderFieldComboBox = new JRCheckBoxComboBox<Field>();
	
	private JToggleButton sortOrderAscButton;
	
	private JToggleButton sortOrderDescButton;
	
	private JLabel sortLabel;

	private final EbookPropertyItemFieldComperator ebookPropertyItemFieldComperator = new EbookPropertyItemFieldComperator();;

	private final ArrayList<Field> internalCheckList = new ArrayList<Field>();

	public SortColumnComponent() {
		this.initialize();
	}

	private void initialize() {
		setLayout(new MigLayout("insets 0 5 0 0")); // T, L, B, R.
		
		sortLabel = new JLabel(Bundle.getString("EborkerMainView.sortby"));
		add(sortLabel, "");
		
		initSortButtons();
		add(sortOrderAscButton, "w 25!, h 25!");
		add(sortOrderDescButton, "w 25!, h 25!");
		
		JRCheckBoxComboBoxModel<Field> sortColumnComboBoxModel = initModel();
		orderFieldComboBox.setCheckBoxComboBoxModel(sortColumnComboBoxModel);
		add(orderFieldComboBox, "w 100%, h 25!");
		
		initSortAction(sortColumnComboBoxModel);
		initClosedViewValue();
	}
	
	private void initSortButtons() {
		sortOrderAscButton = new JToggleButton();
		sortOrderDescButton = new JToggleButton();
		
		Icon ascOrderIcon = ImageResourceBundle.getResourceAsImageIcon("sort_asc.gif");
		sortOrderAscButton.setIcon(ascOrderIcon);
		Icon descOrderIcon = ImageResourceBundle.getResourceAsImageIcon("sort_desc.gif");
		sortOrderDescButton.setIcon(descOrderIcon);
		
		sortOrderAscButton.setAction(new AbstractAction(null, ascOrderIcon) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				sortOrderDescButton.setSelected(false);
				sortOrderAscButton.setSelected(true);
				MainController.getController().getTableModel().setOrderDirection(new OrderDirection(OrderDirection.DIRECTION_ASC));
				MainController.getController().refreshTable();
			}
		});
		
		sortOrderDescButton.setAction(new AbstractAction(null, descOrderIcon) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				sortOrderAscButton.setSelected(false);
				sortOrderDescButton.setSelected(true);
				MainController.getController().getTableModel().setOrderDirection(new OrderDirection(OrderDirection.DIRECTION_DESC));
				MainController.getController().refreshTable();
			}
		});
		
		if(!sortOrderAscButton.isSelected() && !sortOrderDescButton.isSelected()) {
			//ascending order by default
			sortOrderAscButton.setSelected(true);
		}
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
		orderFieldComboBox.setTextFor(JRCheckBoxComboBox.CheckState.MULTIPLE, new CharSequence() {

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
		final List<Field> checkeds = orderFieldComboBox.getCheckBoxComboBoxModel().getCheckedValues();
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
			final JRCheckBoxComboBoxModel<Field> model = orderFieldComboBox.getCheckBoxComboBoxModel();
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
		
		storeSortButtonProperties();
	}

	/**
	 * Restores the order fields and put them to view and model.
	 */
	void restoreComponentProperties() {
		readOrderColumnsFromPreferences();
		restoreSortButtonProperties();
	}
	
	void storeSortButtonProperties() {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		String value = sortOrderAscButton.isSelected() ? "asc" : "desc";
		preferenceStore.addGenericEntryAsString("sortColumnOrder", value);
	}
	
	/**
	 * Restores the order fields and put them to view and model.
	 */
	private void restoreSortButtonProperties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		String sortColumnOrder = preferenceStore.getGenericEntryAsString("sortColumnOrder");
		if (sortColumnOrder != null) {
			if (sortColumnOrder.equalsIgnoreCase("asc")) {
				ActionEvent e = new ActionEvent(sortOrderAscButton, ActionEvent.ACTION_PERFORMED, null);
				sortOrderAscButton.getAction().actionPerformed(e);
			} else if (sortColumnOrder.equalsIgnoreCase("desc")) {
				ActionEvent e = new ActionEvent(sortOrderDescButton, ActionEvent.ACTION_PERFORMED, null);
				sortOrderDescButton.getAction().actionPerformed(e);
			}
		}
	}

}
