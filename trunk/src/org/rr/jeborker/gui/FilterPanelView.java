package org.rr.jeborker.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.Field;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.JTextComponent;

import org.japura.gui.CheckComboBox;
import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.gui.action.ActionFactory;

class FilterPanelView extends JPanel {

	private static final long serialVersionUID = -6508749893667788695L;

	private static final Color selectedBackgroundColor = SwingUtils.getSelectionBackgroundColor();
	
	private static final Color selectedForegroundColor = SwingUtils.getSelectionForegroundColor();

	private static final Color foregroundColor = SwingUtils.getForegroundColor();
	
	private static final Color backgroundColor = SwingUtils.getBackgroundColor();
	
	JComboBox<String> filterField;
	
	CheckComboBox<Field> filterFieldSelection;
	
	BasicComboBoxEditor comboboxEditor;
	
	FilterPanelView() {
		this.initialize();
	}

	private void initialize() {
		GridBagLayout gbl_searchPanel = new GridBagLayout();
		gbl_searchPanel.columnWidths = new int[] { 0, 80, 0, 0, 0 };
		gbl_searchPanel.rowHeights = new int[] { 0, 0 };
		gbl_searchPanel.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_searchPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		this.setLayout(gbl_searchPanel);

		JLabel lblSearch = new JLabel(Bundle.getString("FilterPanelView.label.search"));
		Dimension lblSearchSize = new Dimension(55, lblSearch.getPreferredSize().height);
		lblSearch.setPreferredSize(lblSearchSize);
		lblSearch.setMinimumSize(lblSearchSize);		
		GridBagConstraints gbc_lblSearch = new GridBagConstraints();
		gbc_lblSearch.anchor = GridBagConstraints.EAST;
		gbc_lblSearch.gridx = 0;
		gbc_lblSearch.gridy = 0;
		this.add(lblSearch, gbc_lblSearch);
		
		filterFieldSelection = new CheckComboBox<Field>();
		Dimension filterFieldSelectionSize = new Dimension(80, filterFieldSelection.getPreferredSize().height);
		filterFieldSelection.setPreferredSize(filterFieldSelectionSize);
		filterFieldSelection.setMinimumSize(filterFieldSelectionSize);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.NONE;
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		add(filterFieldSelection, gbc_comboBox);

		filterField = new JComboBox<String>();
		filterField.setModel(new DefaultComboBoxModel<String>());
		filterField.setEditable(true);
		filterField.setEditor(comboboxEditor = new BasicComboBoxEditor());
		((JComponent)comboboxEditor.getEditorComponent()).setBorder(new EmptyBorder(0, 5, 0, 5));
		((JComponent)comboboxEditor.getEditorComponent()).setOpaque(true);
		((JComponent)comboboxEditor.getEditorComponent()).setForeground(foregroundColor);
		((JComponent)comboboxEditor.getEditorComponent()).setBackground(backgroundColor);		
		GridBagConstraints gbc_searchField = new GridBagConstraints();
		gbc_searchField.insets = new Insets(0, 0, 0, 5);
		gbc_searchField.weightx = 1.0;
		gbc_searchField.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchField.gridx = 2;
		gbc_searchField.gridy = 0;
		this.add(filterField, gbc_searchField);

		JButton searchButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SEARCH_ACTION, ""));
		searchButton.setPreferredSize(new Dimension(27, 27));
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.weightx = 0;
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 3;
		gbc_textField.gridy = 0;
		this.add(searchButton, gbc_textField);
	}
	
	/**
	 * Tells the text filter field to display it self in and active filter color. 
	 */
	public void enableFilterColor(boolean enable) {
		if(enable) {
			((JTextComponent)comboboxEditor.getEditorComponent()).setBackground(selectedBackgroundColor);
			((JTextComponent)comboboxEditor.getEditorComponent()).setForeground(selectedForegroundColor);
			((JTextComponent)comboboxEditor.getEditorComponent()).setSelectionColor(selectedBackgroundColor.brighter());
		} else {
			((JTextComponent)comboboxEditor.getEditorComponent()).setForeground(foregroundColor);
			((JTextComponent)comboboxEditor.getEditorComponent()).setBackground(backgroundColor);
			((JTextComponent)comboboxEditor.getEditorComponent()).setSelectionColor(selectedBackgroundColor);
		}
	}	
}
