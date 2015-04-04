package skt.swing.search;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.JButton;

public class FindActionPanel extends JPanel {
	private JTextField textField;

	private JCheckBox chckbxNewCheckBox;
	
	JButton closeButton;
	
	public FindActionPanel() {
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setSize(200, 30);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{54, 50, 30, 0};
		gridBagLayout.rowHeights = new int[]{21, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(3, 3, 3, 5);
		gbc_textField.fill = GridBagConstraints.BOTH;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 0;
		add(textField, gbc_textField);
		textField.setColumns(10);
		
		chckbxNewCheckBox = new JCheckBox(Bundle.getString("FindActionPanel.case"));
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.insets = new Insets(3, 0, 3, 5);
		gbc_chckbxNewCheckBox.gridx = 1;
		gbc_chckbxNewCheckBox.gridy = 0;
		add(chckbxNewCheckBox, gbc_chckbxNewCheckBox);

		ImageIcon closeIcon = new ImageIcon(this.getClass().getResource("close_16.png"));
		closeButton = new JButton(closeIcon);
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.insets = new Insets(5, 0, 5, 5);
		gbc_button.fill = GridBagConstraints.BOTH;
		gbc_button.gridx = 2;
		gbc_button.gridy = 0;
		add(closeButton, gbc_button);
		
		textField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				textField.requestFocus();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				
			}
		});
	}
	
	public JTextField getSearchField() {
		return textField;
	}
	
	public JButton getCloseButton() {
		return closeButton;
	}
	
	public boolean isCaseSensitiveSearch() {
		return chckbxNewCheckBox.isSelected();
	}

}
