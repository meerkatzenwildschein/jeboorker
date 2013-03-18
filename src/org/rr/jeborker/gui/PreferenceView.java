package org.rr.jeborker.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.rr.commons.swing.layout.EqualsLayout;
import org.rr.jeborker.JeboorkerPreferences;

class PreferenceView extends JDialog {
	
	private PreferenceController preferenceController;
	
	private JCheckBox checkBoxAutoScrolling;

	public PreferenceView(JFrame mainWindow, PreferenceController preferenceController) {
		super(mainWindow);
		this.preferenceController = preferenceController;
		initialize();
		setModal(true);
		setLocation(mainWindow.getLocation().x, mainWindow.getLocation().y);
		setSize(600, 350);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);				
	}

	private void initialize() {
		setTitle(Bundle.getString("PreferenceView.title"));
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel generalPanel = new JPanel();
		tabbedPane.addTab(Bundle.getString("PreferenceView.tab.general"), null, generalPanel, null);
		GridBagLayout gbl_generalPanel = new GridBagLayout();
		gbl_generalPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_generalPanel.rowHeights = new int[]{0, 0};
		gbl_generalPanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_generalPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		generalPanel.setLayout(gbl_generalPanel);
		
		JLabel lblTreeAutoscrolling = new JLabel(Bundle.getString("PreferenceView.pref.autoscroll"));
		GridBagConstraints gbc_lblTreeAutoscrolling = new GridBagConstraints();
		gbc_lblTreeAutoscrolling.insets = new Insets(0, 3, 0, 5);
		gbc_lblTreeAutoscrolling.gridx = 0;
		gbc_lblTreeAutoscrolling.gridy = 0;
		generalPanel.add(lblTreeAutoscrolling, gbc_lblTreeAutoscrolling);
		
		this.checkBoxAutoScrolling = new JCheckBox("");
		boolean isTreeAutoScrollingEnabled = JeboorkerPreferences.isTreeAutoScrollingEnabled();
		checkBoxAutoScrolling.setSelected(isTreeAutoScrollingEnabled);

		checkBoxAutoScrolling.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				JeboorkerPreferences.setTreeAutoScrollingEnabled(checkBoxAutoScrolling.isSelected());
			}
		});
		GridBagConstraints gbc_checkBoxAutoScrolling = new GridBagConstraints();
		gbc_checkBoxAutoScrolling.gridx = 2;
		gbc_checkBoxAutoScrolling.gridy = 0;
		generalPanel.add(checkBoxAutoScrolling, gbc_checkBoxAutoScrolling);		
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new EqualsLayout(1));
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		JButton btnClose = new JButton(Bundle.getString("PreferenceView.close"));
		bottomPanel.add(btnClose);
		btnClose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				preferenceController.close();
			}
		});
	}
	
}
