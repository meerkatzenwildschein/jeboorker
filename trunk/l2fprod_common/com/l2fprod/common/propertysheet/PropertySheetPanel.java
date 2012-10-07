/**
 * L2FProd.com Common Components 7.3 License.
 *
 * Copyright 2005-2007 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.l2fprod.common.propertysheet;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.l2fprod.common.swing.IconPool;
import com.l2fprod.common.swing.LookAndFeelTweaks;
import com.l2fprod.common.util.ResourceManager;

/**
 * An implementation of a PropertySheet which shows a table to edit/view values, a description pane which updates when the selection changes and buttons to
 * toggle between a flat view and a by-category view of the properties. A button in the toolbar allows to sort the properties and categories by name.
 * <p>
 * Default sorting is by name (case-insensitive). Custom sorting can be implemented through
 * {@link com.l2fprod.common.propertysheet.PropertySheetTableModel#setCategorySortingComparator(Comparator)} and
 * {@link com.l2fprod.common.propertysheet.PropertySheetTableModel#setPropertySortingComparator(Comparator)}
 */
public class PropertySheetPanel extends JPanel implements PropertySheet, PropertyChangeListener {

	private static final long serialVersionUID = 5266942995257597731L;

	private PropertySheetTable table;
	private PropertySheetTableModel model;
	private JScrollPane tableScroll;
	private ListSelectionListener selectionListener = new SelectionListener();

	private JPanel actionPanel;
	private JToggleButton sortButton;
	private JToggleButton asCategoryButton;
	private JToggleButton descriptionButton;

	private JSplitPane split;
	private int lastDescriptionHeight;

	private JEditorPane descriptionPanel;
	private JScrollPane descriptionScrollPane;

	private boolean showCategoryButton = true;
	private boolean showSortButton = true;
	private boolean showDescriptionButton = true;

	boolean isShowCategoryButton() {
		return showCategoryButton;
	}

	public void setShowCategoryButton(boolean showCategoryButton) {
		this.showCategoryButton = showCategoryButton;
		if (showCategoryButton && asCategoryButton == null) {
			this.addCategoryButton();
		} else {
			this.removeCategoryButton();
		}
	}

	public boolean isShowSortButton() {
		return showSortButton;
	}

	public void setShowSortButton(boolean showSortButton) {
		this.showSortButton = showSortButton;
		if (showSortButton && sortButton == null) {
			this.addSortButton();
		} else {
			this.removeSortButton();
		}
	}

	public boolean isShowDescriptionButton() {
		return showDescriptionButton;
	}

	public void setShowDescriptionButton(boolean showDescriptionButton) {
		this.showDescriptionButton = showDescriptionButton;
		if (showDescriptionButton && descriptionButton == null) {
			this.addDescriptionButton();
		} else {
			this.removeDescriptionButton();
		}
	}

	public PropertySheetPanel() {
		this(new PropertySheetTable());
	}

	public PropertySheetPanel(PropertySheetTable table) {
		buildUI();
		setTable(table);
	}
	
	public PropertySheetPanel(PropertySheetTableModel model) {
		this.model = model;
		buildUI();
		setTable(new PropertySheetTable(model));
	}	

	/**
	 * Sets the table used by this panel.
	 * 
	 * Note: listeners previously added with {@link PropertySheetPanel#addPropertySheetChangeListener(PropertyChangeListener)} must be re-added after this call
	 * if the table model is not the same as the previous table.
	 * 
	 * @param table
	 */
	public void setTable(PropertySheetTable table) {
		if (table == null) {
			throw new IllegalArgumentException("table must not be null");
		}

		// remove the property change listener from any previous model
		if (model != null)
			model.removePropertyChangeListener(this);

		// get the model from the table
		model = (PropertySheetTableModel) table.getModel();
		model.addPropertyChangeListener(this);

		// remove the listener from the old table
		if (this.table != null)
			this.table.getSelectionModel().removeListSelectionListener(selectionListener);

		// prepare the new table
		table.getSelectionModel().addListSelectionListener(selectionListener);
		tableScroll.getViewport().setView(table);
		
		// use the new table as our table
		this.table = table;
	}

	/**
	 * React to property changes by repainting.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		repaint();
	}

	/**
	 * @return the table used to edit/view Properties.
	 */
	public PropertySheetTable getTable() {
		return table;
	}

	/**
	 * Toggles the visibility of the description panel.
	 * 
	 * @param visible
	 */
	public void setDescriptionVisible(boolean visible) {
		if (visible) {
			add("Center", split);
			split.setTopComponent(tableScroll);
			split.setBottomComponent(descriptionScrollPane);
			// restore the divider location
			split.setDividerLocation(split.getHeight() - lastDescriptionHeight);
		} else {
			// save the size of the description pane to restore it later
			lastDescriptionHeight = split.getHeight() - split.getDividerLocation();
			remove(split);
			add("Center", tableScroll);
		}
		descriptionButton.setSelected(visible);
		PropertySheetPanel.this.revalidate();
	}
	
	/**
	 * Get the splitpane divider location.
	 * @return The location for the plsitpane divider location.
	 */
	public int getDescriptionDividerLocation() {
		return split.getDividerLocation();
	}
	
	public void setDescriptionDividerLocation(int location) {
		split.setDividerLocation(location);
	}	

	/**
	 * Toggles the visibility of the toolbar panel
	 * 
	 * @param visible
	 */
	public void setToolBarVisible(boolean visible) {
		actionPanel.setVisible(visible);
		PropertySheetPanel.this.revalidate();
	}

	/**
	 * Set the current mode, either {@link PropertySheet#VIEW_AS_CATEGORIES} or {@link PropertySheet#VIEW_AS_FLAT_LIST}.
	 */
	public void setMode(int mode) {
		model.setMode(mode);
		asCategoryButton.setSelected(PropertySheet.VIEW_AS_CATEGORIES == mode);
	}

	public void setProperties(Property[] properties) {
		model.setProperties(properties);
	}

	public List<Property> getProperties() {
		return model.getProperties();
	}

	public void addProperty(Property property) {
		model.addProperty(property);
	}

	public void addProperty(int index, Property property) {
		model.addProperty(index, property);
	}

	public void removeProperty(Property property) {
		model.removeProperty(property);
	}

	public int getPropertyCount() {
		return model.getPropertyCount();
	}

	public Iterator propertyIterator() {
		return model.propertyIterator();
	}

	public void setBeanInfo(BeanInfo beanInfo) {
		setProperties(beanInfo.getPropertyDescriptors());
	}

	public void setProperties(PropertyDescriptor[] descriptors) {
		Property[] properties = new Property[descriptors.length];
		for (int i = 0, c = descriptors.length; i < c; i++) {
			properties[i] = new PropertyDescriptorAdapter(descriptors[i]);
		}
		setProperties(properties);
	}

	/**
	 * Initializes the PropertySheet from the given object. If any, it cancels pending edit before proceeding with properties.
	 * 
	 * @param data
	 */
	public void readFromObject(Object data) {
		// cancel pending edits
		getTable().cancelEditing();

		List<Property> properties = model.getProperties();
		for (int i = 0, c = properties.size(); i < c; i++) {
			properties.get(i).readFromObject(data);
		}
		repaint();
	}

	/**
	 * Writes the PropertySheet to the given object. If any, it commits pending edit before proceeding with properties.
	 * 
	 * @param data
	 */
	public void writeToObject(Object data) {
		// ensure pending edits are committed
		getTable().commitEditing();

		List<Property> properties = getProperties();
		for (int i = 0, c = properties.size(); i < c; i++) {
			properties.get(i).writeToObject(data);
		}
	}

	public void addPropertySheetChangeListener(PropertyChangeListener listener) {
		model.addPropertyChangeListener(listener);
	}

	public void removePropertySheetChangeListener(PropertyChangeListener listener) {
		model.removePropertyChangeListener(listener);
	}

	public void setEditorFactory(PropertyEditorFactory factory) {
		table.setEditorFactory(factory);
	}

	public PropertyEditorFactory getEditorFactory() {
		return table.getEditorFactory();
	}

	public void setRendererFactory(PropertyRendererFactory factory) {
		table.setRendererFactory(factory);
	}

	public PropertyRendererFactory getRendererFactory() {
		return table.getRendererFactory();
	}

	/**
	 * Sets sorting of categories enabled or disabled.
	 * 
	 * @param value
	 *            true to enable sorting
	 */
	public void setSortingCategories(boolean value) {
		model.setSortingCategories(value);
		sortButton.setSelected(isSorting());
	}

	/**
	 * Is sorting of categories enabled.
	 * 
	 * @return true if category sorting is enabled
	 */
	public boolean isSortingCategories() {
		return model.isSortingCategories();
	}

	/**
	 * Sets sorting of properties enabled or disabled.
	 * 
	 * @param value
	 *            true to enable sorting
	 */
	public void setSortingProperties(boolean value) {
		model.setSortingProperties(value);
		sortButton.setSelected(isSorting());
	}

	/**
	 * Is sorting of properties enabled.
	 * 
	 * @return true if property sorting is enabled
	 */
	public boolean isSortingProperties() {
		return model.isSortingProperties();
	}

	/**
	 * Sets sorting properties and categories enabled or disabled.
	 * 
	 * @param value
	 *            true to enable sorting
	 */
	public void setSorting(boolean value) {
		model.setSortingCategories(value);
		model.setSortingProperties(value);
		sortButton.setSelected(value);
	}

	/**
	 * @return true if properties or categories are sorted.
	 */
	public boolean isSorting() {
		return model.isSortingCategories() || model.isSortingProperties();
	}

	/**
	 * Sets the Comparator to be used with categories. Categories are treated as String-objects.
	 * 
	 * @param comp
	 *            java.util.Comparator used to compare categories
	 */
	public void setCategorySortingComparator(Comparator comp) {
		model.setCategorySortingComparator(comp);
	}

	/**
	 * Sets the Comparator to be used with Property-objects.
	 * 
	 * @param comp
	 *            java.util.Comparator used to compare Property-objects
	 */
	public void setPropertySortingComparator(Comparator comp) {
		model.setPropertySortingComparator(comp);
	}

	/**
	 * Set wether or not toggle states are restored when new properties are applied.
	 * 
	 * @param value
	 *            true to enable
	 */
	public void setRestoreToggleStates(boolean value) {
		model.setRestoreToggleStates(value);
	}

	/**
	 * @return true is toggle state restore is enabled
	 */
	public boolean isRestoreToggleStates() {
		return model.isRestoreToggleStates();
	}

	/**
	 * @return the category view toggle states.
	 */
	public Map getToggleStates() {
		return model.getToggleStates();
	}

	/**
	 * Sets the toggle states for the category views. Note this <b>MUST</b> be called <b>BEFORE</b> setting any properties.
	 * 
	 * @param toggleStates
	 *            the toggle states as returned by getToggleStates
	 */
	public void setToggleStates(Map toggleStates) {
		model.setToggleStates(toggleStates);
	}

	private void addCategoryButton() {
		asCategoryButton = new JToggleButton(new ToggleModeAction());
//		asCategoryButton.setUI(new BlueishButtonUI());
		asCategoryButton.setText(null);
		asCategoryButton.setOpaque(false);
		actionPanel.add(asCategoryButton);
	}

	private void removeCategoryButton() {
		if (asCategoryButton != null) {
			actionPanel.remove(asCategoryButton);
			asCategoryButton = null;
		}
	}

	private void addDescriptionButton() {
		descriptionButton = new JToggleButton(new ToggleDescriptionAction());
//		descriptionButton.setUI(new BlueishButtonUI());
		descriptionButton.setText(null);
		descriptionButton.setOpaque(false);
		actionPanel.add(descriptionButton);
	}
	
	/**
	 * Adds a button or something else behind the description and sort button
	 * to the toolbar.
	 * @param c The component which should be added.
	 */
	public void addToolbarComponent(Component c) {
		actionPanel.add(c);
	}

	private void removeDescriptionButton() {
		if (descriptionButton != null) {
			actionPanel.remove(descriptionButton);
			descriptionButton = null;
		}
	}

	private void addSortButton() {
		sortButton = new JToggleButton(new ToggleSortingAction());
//		sortButton.setUI(new BlueishButtonUI());
		sortButton.setText(null);
		sortButton.setOpaque(false);
		actionPanel.add(sortButton);
	}

	private void removeSortButton() {
		if (sortButton != null) {
			actionPanel.remove(sortButton);
			sortButton = null;
		}
	}

	private void buildUI() {
		LookAndFeelTweaks.setBorderLayout(this);
		LookAndFeelTweaks.setBorder(this);

		actionPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
		actionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
		actionPanel.setOpaque(false);
		add("North", actionPanel);

		this.addSortButton();
		this.addCategoryButton();
		this.addDescriptionButton();

		split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setBorder(null);
		split.setResizeWeight(1.0);
		split.setContinuousLayout(true);
		add("Center", split);

		tableScroll = new JScrollPane();
		tableScroll.setBorder(BorderFactory.createEmptyBorder());
		split.setTopComponent(tableScroll);

		descriptionPanel = new JEditorPane("text/html", "<html>");
		descriptionPanel.setBorder(BorderFactory.createEmptyBorder());
		descriptionPanel.setEditable(false);
		descriptionPanel.setBackground(UIManager.getColor("Panel.background"));
		LookAndFeelTweaks.htmlize(descriptionPanel);

		selectionListener = new SelectionListener();

		descriptionScrollPane = new JScrollPane(descriptionPanel);
		descriptionScrollPane.setBorder(LookAndFeelTweaks.addMargin(BorderFactory.createLineBorder(UIManager.getColor("controlDkShadow"))));
		descriptionScrollPane.getViewport().setBackground(descriptionPanel.getBackground());
		descriptionScrollPane.setMinimumSize(new Dimension(50, 50));
		split.setBottomComponent(descriptionScrollPane);

		// by default description is not visible, toolbar is visible.
		setDescriptionVisible(false);
		setToolBarVisible(true);
	}

	class SelectionListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			int row = table.getSelectedRow();
			Property prop = null;
			if (row >= 0 && table.getRowCount() > row)
				prop = model.getPropertySheetElement(row).getProperty();
			if (prop != null) {
				descriptionPanel.setText("<html>" + "<b>" + (prop.getDisplayDescriptionName() == null ? "" : prop.getDisplayDescriptionName()) + "</b><br>"
						+ (prop.getShortDescription() == null ? "" : prop.getShortDescription()));
			} else {
				descriptionPanel.setText("<html>");
			}

			// position it at the top
			descriptionPanel.setCaretPosition(0);
		}
	}

	class ToggleModeAction extends AbstractAction {

		public ToggleModeAction() {
			super("toggle", IconPool.shared().get(PropertySheet.class.getResource("icons/category.gif")));
			putValue(Action.SHORT_DESCRIPTION, ResourceManager.get(PropertySheet.class).getString("PropertySheetPanel.category.shortDescription"));
		}

		public void actionPerformed(ActionEvent e) {
			if (asCategoryButton.isSelected()) {
				model.setMode(PropertySheet.VIEW_AS_CATEGORIES);
			} else {
				model.setMode(PropertySheet.VIEW_AS_FLAT_LIST);
			}
		}
	}

	class ToggleDescriptionAction extends AbstractAction {

		public ToggleDescriptionAction() {
			super("toggleDescription", IconPool.shared().get(PropertySheet.class.getResource("icons/description.gif")));
			putValue(Action.SHORT_DESCRIPTION, ResourceManager.get(PropertySheet.class).getString("PropertySheetPanel.description.shortDescription"));
		}

		public void actionPerformed(ActionEvent e) {
			setDescriptionVisible(descriptionButton.isSelected());
		}
	}

	class ToggleSortingAction extends AbstractAction {

		public ToggleSortingAction() {
			super("toggleSorting", IconPool.shared().get(PropertySheet.class.getResource("icons/sort.gif")));
			putValue(Action.SHORT_DESCRIPTION, ResourceManager.get(PropertySheet.class).getString("PropertySheetPanel.sort.shortDescription"));
		}

		public void actionPerformed(ActionEvent e) {
			setSorting(sortButton.isSelected());
		}
	}

}