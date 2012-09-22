package org.rr.jeborker.gui;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
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
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.UtilConstants;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.db.item.ViewField;
import org.rr.jeborker.gui.additional.EbookPropertyItemFieldComperator;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;

public class SortColumnComponentController {

	private final CheckComboBox<Field> comboBox;
	
	private static EbookPropertyItemFieldComperator ebookPropertyItemFieldComperator = new EbookPropertyItemFieldComperator();
	
	private ArrayList<Field> internalCheckList = new ArrayList<Field>();
	
	public SortColumnComponentController(final CheckComboBox<Field> comboBox) {
		this.comboBox = comboBox;
		this.initialize(comboBox);
	}
	
	private void initialize(final CheckComboBox<Field> comboBox) {
		final DefaultListCheckModel<Field> sortColumnComboBoxModel = this.initModel();
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
	private void initSortAction(DefaultListCheckModel<Field> sortColumnComboBoxModel) {
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
				MainController.getController().refreshTable(true);
			}
		});
	}
	
	/**
	 * Initialize the renderer which is able to show the field names.
	 * @param filterFieldSelection The combobox instance where the renderer should be applied to.
	 */
	private void initRenderer() {
		//renderer setup for showing the name annotation for the fields.
		comboBox.setRenderer(new CheckListRenderer() {
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
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				return c;
			}
		});
	}

	/**
	 * Setup the value which is shown if the combobox is closed. 
	 * 
	 * @param filterFieldSelection The combobox to be setup.
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
	private DefaultListCheckModel<Field> initModel() {
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
	 * Gets the fields which are checked.
	 * @return The checked fields. Never returns <code>null</code>.
	 */
	public List<Field> getSelectedFields() {
		final List<Field> checkeds = comboBox.getModel().getCheckeds();
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
		//preferences to table model
		final String entryString = JeboorkerPreferences.getEntryString("sortColumnFields");
		if(entryString!=null && entryString.length() > 0) {
			final List<String> splitted = ListUtils.split(entryString, ",", -1, UtilConstants.COMPARE_BINARY);
			final ListCheckModel<Field> model = comboBox.getModel();
			final int modelSize = model.getSize();
			
			for (String split : splitted) {
				for (int j = 0; j < modelSize; j++) {
					// the split functions returns always the String.intern() instances.  
					if(model.getElementAt(j).getName().equals(split)) {
						model.addCheck((Field)model.getElementAt(j));
						break;
					}
				}
			}
		} else {
			//set a default set of sort values
			final EbookPropertyDBTableModel tableModel = MainController.getController().getTableModel();
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
		JeboorkerPreferences.addEntryString("sortColumnFields", value.toString());
	}
	
	/**
	 * Restores the order fields and put them to view and model.
	 */
	private void restoreProperties() {
		this.readOrderColumnsFromPreferences();
	}	

}
