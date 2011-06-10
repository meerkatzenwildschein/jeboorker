package org.rr.jeborker.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.rr.jeborker.gui.action.ActionFactory;

public class FilterPanelView extends JPanel {

	private static final long serialVersionUID = -6508749893667788695L;

	JComboBox filterField;

	FilterPanelView() {
		this.initialize();

	}

	private void initialize() {
		GridBagLayout gbl_searchPanel = new GridBagLayout();
		gbl_searchPanel.columnWidths = new int[] { 62, 0, 0, 0 };
		gbl_searchPanel.rowHeights = new int[] { 0, 0 };
		gbl_searchPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_searchPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		this.setLayout(gbl_searchPanel);

		JLabel lblSearch = new JLabel(Bundle.getString("FilterPanelView.label.search"));
		GridBagConstraints gbc_lblSearch = new GridBagConstraints();
		gbc_lblSearch.insets = new Insets(0, 0, 0, 5);
		gbc_lblSearch.anchor = GridBagConstraints.WEST;
		gbc_lblSearch.gridx = 0;
		gbc_lblSearch.gridy = 0;
		this.add(lblSearch, gbc_lblSearch);

		filterField = new JComboBox();
		filterField.setModel(new DefaultComboBoxModel());
		filterField.setEditable(true);
		GridBagConstraints gbc_searchField = new GridBagConstraints();
		gbc_searchField.insets = new Insets(0, 0, 0, 5);
		gbc_searchField.weightx = 1.0;
		gbc_searchField.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchField.gridx = 1;
		gbc_searchField.gridy = 0;
		this.add(filterField, gbc_searchField);

		JButton searchButten = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SEARCH_ACTION, ""));
		searchButten.setPreferredSize(new Dimension(27, 27));
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.weightx = 0;
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 2;
		gbc_textField.gridy = 0;
		this.add(searchButten, gbc_textField);
	}
}
