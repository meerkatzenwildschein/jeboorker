package org.rr.jeborker.gui.controllers;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JList;

import org.japura.gui.CheckComboBox;
import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;
import org.japura.gui.model.DefaultListCheckModel;
import org.japura.gui.model.ListCheckModel;
import org.japura.gui.renderer.CheckListRenderer;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.UtilConstants;
import org.rr.jeborker.JEBorkerPreferences;
import org.rr.jeborker.db.item.DBViewField;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.JEBorkerMainController;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;

public class SortColumnComponentController {

	private final CheckComboBox comboBox;
	
	private static EbookPropertyItemFieldComperator ebookPropertyItemFieldComperator = new EbookPropertyItemFieldComperator();
	
	@SuppressWarnings("unchecked")
	private ArrayList internalCheckList = new ArrayList();
	
	/**
	 * Can be used for sorting the DBViewField fields into the right order.
	 */
	private static class EbookPropertyItemFieldComperator implements Comparator<Field> {

		@Override
		public int compare(Field o1, Field o2) {
			DBViewField annotation1 = o1.getAnnotation(DBViewField.class);
			DBViewField annotation2 = o2.getAnnotation(DBViewField.class);
			if(annotation1!=null && annotation2!=null) {
				return Integer.valueOf(annotation1.orderPriority()).compareTo(Integer.valueOf(annotation2.orderPriority())) * -1;
			}
			return 0;
		}
	}
	
	public SortColumnComponentController(final CheckComboBox comboBox) {
		this.comboBox = comboBox;
		this.initialize(comboBox);
	}
	
	private void initialize(final CheckComboBox comboBox) {
		final DefaultListCheckModel sortColumnComboBoxModel = this.initModel();
		initSortAction(sortColumnComboBoxModel);
		initClosedViewValue();
		initRenderer();
		comboBox.setModel(sortColumnComboBoxModel);
		restoreProperties();
	}

	/**
	 * Initialize the combobox actions to be triggered on a selection change.  
	 * @param sortColumnComboBoxModel The model where the selections are triggered.
	 */
	private void initSortAction(DefaultListCheckModel sortColumnComboBoxModel) {
		sortColumnComboBoxModel.addListCheckListener(new ListCheckListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void removeCheck(ListEvent event) {
				internalCheckList.removeAll(event.getValues());
				this.changed(event);
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public void addCheck(ListEvent event) {
				internalCheckList.addAll(event.getValues());
				this.changed(event);
			}
			
			@SuppressWarnings("unchecked")
			private void changed(ListEvent event) {
				final EbookPropertyDBTableModel tableModel = JEBorkerMainController.getController().getTableModel();
				tableModel.getOrderByColumns().clear();
				tableModel.getOrderByColumns().addAll(internalCheckList);
				JEBorkerMainController.getController().refreshTable();
			}
		});
	}
	
	/**
	 * Initialize the renderer which is able to show the field names.
	 * @param comboBox The combobox instance where the renderer should be applied to.
	 */
	private void initRenderer() {
		//renderer setup for showing the name annotation for the fields.
		comboBox.setRenderer(new CheckListRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(Object value) {
				DBViewField annotation = ((Field) value).getAnnotation(DBViewField.class);
				return annotation.name();
			}

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				return c;
			}
		});
	}

	/**
	 * Setup the value which is shown if the combobox is closed. 
	 * 
	 * @param comboBox The combobox to be setup.
	 */
	private void initClosedViewValue() {
		comboBox.setTextFor(CheckComboBox.MULTIPLE, new CharSequence() {
			
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
					String name = ((Field)field).getAnnotation(DBViewField.class).name();
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
	private DefaultListCheckModel initModel() {
		//get fields to be displayed in the combobox
		final List<Field> fields = ReflectionUtils.getFields(EbookPropertyItem.class, ReflectionUtils.VISIBILITY_VISIBLE_ALL);
		final ArrayList<Field> listEntries = new ArrayList<Field>(fields.size());
		for (Field field : fields) {
			DBViewField dbViewFieldAannotation = field.getAnnotation(DBViewField.class);
			if(dbViewFieldAannotation!=null) {
				listEntries.add(field);
			}
		}
		
		//sort the fields to the DBViewField.orderPriority()
		Collections.sort(listEntries, ebookPropertyItemFieldComperator);
		
		final DefaultListCheckModel sortColumnComboBoxModel = new DefaultListCheckModel();
		for (Field field : listEntries) {
			sortColumnComboBoxModel.addElement(field);
		}
		return sortColumnComboBoxModel;
	}
	
	/**
	 * Gets the fields which are checked.
	 * @return The checked fields. Never returns <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public List<Field> getSelectedFields() {
		final List checkeds = comboBox.getModel().getCheckeds();
		//sort the fields to the DBViewField.orderPriority()
		Collections.sort(checkeds, ebookPropertyItemFieldComperator);		
		final ArrayList<Field> result = new ArrayList<Field>(checkeds.size());
		for (Object field : checkeds) {
			result.add(((Field)field));
		}
		return result;
	}
	
	/**
	 * Reads the order columns from the preferences to the combobox model. 
	 */
	private void readOrderColumnsFromPreferences() {
		//preferences to table model
		final String entryString = JEBorkerPreferences.getEntryString("sortColumnFields");
		if(entryString!=null && entryString.length() > 0) {
			final List<String> splitted = ListUtils.split(entryString, ",", -1, UtilConstants.COMPARE_BINARY);
			final ListCheckModel model = comboBox.getModel();
			final int modelSize = model.getSize();
			
			for (String split : splitted) {
				for (int j = 0; j < modelSize; j++) {
					// the split functions returns always the String.intern() instances.  
					if(((Field)model.getElementAt(j)).getName().equals(split)) {
						model.addCheck((Field)model.getElementAt(j));
						break;
					}
				}
			}
		} else {
			//set a default set of sort values
			final EbookPropertyDBTableModel tableModel = JEBorkerMainController.getController().getTableModel();
			List<Field> orderByColumns = tableModel.getOrderByColumns();
	    	try {
				orderByColumns.add(EbookPropertyItem.class.getDeclaredField("author"));
				orderByColumns.add(EbookPropertyItem.class.getDeclaredField("title"));
			} catch (Exception e) {
				LoggerFactory.logWarning(this, "Field named author is not available at " + EbookPropertyItem.class, e);
			} 
		}
	}
	
	public void dispose() {
		this.storeProperties();
	}	
	
	private void storeProperties() {
		//store the sort order properties.
		final List<Field> selectedFields = getSelectedFields();
		final StringBuilder value = new StringBuilder();
		for (Field field : selectedFields) {
			if(value.length() > 0) {
				value.append(",");
			}
			value.append(field.getName());
		}
		JEBorkerPreferences.addEntryString("sortColumnFields", value.toString());
	}
	
	/**
	 * Restores the order fields and put them to view and model.
	 */
	private void restoreProperties() {
		this.readOrderColumnsFromPreferences();
	}	

}
