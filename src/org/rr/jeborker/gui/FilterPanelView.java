package org.rr.jeborker.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.Field;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.japura.gui.CheckComboBox;
import org.rr.jeborker.gui.action.ActionFactory;

class FilterPanelView extends JPanel {

	private static final long serialVersionUID = -6508749893667788695L;

	JComboBox<String> filterField;
	
	CheckComboBox<Field> filterFieldSelection;

	FilterPanelView() {
		this.initialize();

	}

	private void initialize() {
		GridBagLayout gbl_searchPanel = new GridBagLayout();
		gbl_searchPanel.columnWidths = new int[] { 62, 80, 0, 0, 0 };
		gbl_searchPanel.rowHeights = new int[] { 0, 0 };
		gbl_searchPanel.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_searchPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		this.setLayout(gbl_searchPanel);

		JLabel lblSearch = new JLabel(Bundle.getString("FilterPanelView.label.search"));
		GridBagConstraints gbc_lblSearch = new GridBagConstraints();
		gbc_lblSearch.insets = new Insets(0, 0, 0, 5);
		gbc_lblSearch.anchor = GridBagConstraints.EAST;
		gbc_lblSearch.gridx = 0;
		gbc_lblSearch.gridy = 0;
		this.add(lblSearch, gbc_lblSearch);
		
		filterFieldSelection = new CheckComboBox<Field>();
		filterFieldSelection.setPreferredSize(new Dimension(50,filterFieldSelection.getPreferredSize().height));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		add(filterFieldSelection, gbc_comboBox);

		filterField = new JComboBox<String>();
		filterField.setModel(new DefaultComboBoxModel<String>());
		filterField.setEditable(true);
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
}
