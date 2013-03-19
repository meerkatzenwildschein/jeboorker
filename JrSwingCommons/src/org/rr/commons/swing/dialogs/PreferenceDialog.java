package org.rr.commons.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import org.rr.common.swing.components.JRCheckBox;
import org.rr.commons.swing.layout.EqualsLayout;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.StringUtils;

public class PreferenceDialog extends JDialog {

	private final LinkedHashMap<String, PreferenceEntry> preferenceEntries = new LinkedHashMap<String, PreferenceEntry>();

	private final ActionListener closeAction = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
			dispose();
		}
	};	
	
	public PreferenceDialog(JFrame owner) {
		super(owner);
	}
	
	public void addPreferenceEntry(PreferenceEntry entry) {
		preferenceEntries.put(entry.getName(), entry);
	}
	
	/**
	 * Get the result value for the {@link PreferenceEntry} with the given name. 
	 */
	public boolean getBooleanValue(final String name) {
		final PreferenceEntry preferenceEntry = preferenceEntries.get(name);
		final Component component = preferenceEntry.getComponent();
		
		if(component instanceof JCheckBox) {
			return ((JCheckBox)component).isSelected();
		} else if(component instanceof JTextComponent) {
			String text = ((JTextComponent)component).getText();
			Boolean booleanValue = CommonUtils.toBoolean(text);
			return booleanValue.booleanValue();
		}
		return false;
	}
	
	/**
	 * Get the result value for the {@link PreferenceEntry} with the given name. 
	 */
	public String getStringValue(final String name) {
		final PreferenceEntry preferenceEntry = preferenceEntries.get(name);
		final Component component = preferenceEntry.getComponent();
		
		if(component instanceof JCheckBox) {
			return Boolean.valueOf(((JCheckBox)component).isSelected()).toString();
		} else if(component instanceof JTextComponent) {
			String text = ((JTextComponent)component).getText();
			return text;
		}
		return "";
	}
	
	public void setVisible(boolean visible) {
		this.initialize();
		super.setVisible(visible);
	}
	
	private void initialize() {
		final List<String> categories = getPreferenceCategories();
		final ArrayList<JPanel> generalPanels = new ArrayList<JPanel>(categories.size());
		for(final String category : categories) {
			final JPanel generalPanel = new JPanel();
			
			GridBagLayout gbl_generalPanel = new GridBagLayout();
			gbl_generalPanel.columnWidths = new int[]{0, 0, 0, 0};
			gbl_generalPanel.rowHeights = new int[]{0, 0};
			gbl_generalPanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_generalPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			generalPanel.setLayout(gbl_generalPanel);
			generalPanel.setName(category);
			generalPanels.add(generalPanel);
			
			List<PreferenceEntry> preferenceEntriesByCategory = getPreferenceEntriesByCategory(category);
			for(int i = 0; i < preferenceEntriesByCategory.size(); i++) {
				final PreferenceEntry preferenceEntry = preferenceEntriesByCategory.get(i);
				
				JLabel lblTreeAutoscrolling = new JLabel(preferenceEntry.getLabel());
				GridBagConstraints gbc_lblTreeAutoscrolling = new GridBagConstraints();
				gbc_lblTreeAutoscrolling.insets = new Insets(0, 3, 0, 5);
				gbc_lblTreeAutoscrolling.gridx = 0;
				gbc_lblTreeAutoscrolling.gridy = i;
				generalPanel.add(lblTreeAutoscrolling, gbc_lblTreeAutoscrolling);
				
				Component c = preferenceEntry.getComponent();
				GridBagConstraints gbc_checkBoxAutoScrolling = new GridBagConstraints();
				gbc_checkBoxAutoScrolling.gridx = 2;
				gbc_checkBoxAutoScrolling.gridy = i;
				generalPanel.add(c, gbc_checkBoxAutoScrolling);				
			}
		}
		
		//add the category panels to the dialog root pane.
		if(generalPanels.size() > 1) {
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			getContentPane().add(tabbedPane, BorderLayout.CENTER);
			
			for(JPanel generalPanel : generalPanels) {
				tabbedPane.addTab(generalPanel.getName(), null, generalPanel, null);
			}
		} else if(!generalPanels.isEmpty()) {
			JPanel panel = new JPanel();
			getContentPane().add(panel, BorderLayout.CENTER);
			
			JPanel generalPanel = generalPanels.get(0);
			panel.add(generalPanel);
			
			if(!generalPanel.getName().isEmpty()) {
				TitledBorder border = new TitledBorder(generalPanel.getName());
				generalPanel.setBorder(border);
			}
		}
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new EqualsLayout(1));
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		JButton btnClose = new JButton(Bundle.getString("PreferenceView.close"));
		bottomPanel.add(btnClose);
		btnClose.addActionListener(closeAction);		
		
	}
	
	/**
	 * Get all {@link PreferenceEntry} instances for the given category.
	 * @return The desired {@link PreferenceEntry} instances. Never returns <code>null</code>.
	 */
	private List<PreferenceEntry> getPreferenceEntriesByCategory(String category) {
		ArrayList<PreferenceEntry> result = new ArrayList<PreferenceDialog.PreferenceEntry>();
		for(PreferenceEntry preferenceEntry : preferenceEntries.values()) {
			if(StringUtils.toString(category).equals(StringUtils.toString(preferenceEntry.getCategory()))) {
				result.add(preferenceEntry);
			}
		}
		return result;
	}
	
	/**
	 * Get a list of existing categories. Empty/null categories are contained as empty {@link String} category.
	 */
	private List<String> getPreferenceCategories() {
		ArrayList<String> result = new ArrayList<String>();
		for(PreferenceEntry preferenceEntry : preferenceEntries.values()) {
			String category = StringUtils.toString(preferenceEntry.getCategory());
			if(!result.contains(category)) {
				result.add(category);
			}
		}
		return result;		
	}
	
	public static class PreferenceEntry {
		
		public static final int BOOLEAN_TYPE = 0;
		
		public static final int TEXT_TYPE = 1;
		
		public static final int CUSTOM_TYPE = 2;
		
		private String category;
		
		private String name;
		
		private String label;
		
		private int type;
		
		private Component customComponent;
		
		public PreferenceEntry(String name, String label, int type) {
			this.name = name;
			this.type = type;
			this.label = label;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public Component getComponent() {
			switch(type) {
			case BOOLEAN_TYPE:
				return new JRCheckBox();
			case TEXT_TYPE:
				return new JTextField();
			case CUSTOM_TYPE:
			default:
				return customComponent;
			}
		}
		
		public Component getCustomComponent() {
			return customComponent;
		}

		public void setCustomComponent(Component customComponent) {
			this.customComponent = customComponent;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}
		
	}
}
