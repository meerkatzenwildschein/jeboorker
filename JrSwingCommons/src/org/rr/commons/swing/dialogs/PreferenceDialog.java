package org.rr.commons.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import org.rr.commons.swing.layout.EqualsLayout;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.StringUtils;

public class PreferenceDialog extends JDialog {

	public static final int ACTION_RESULT_OK = 0;
	
	public static final int ACTION_RESULT_ABORT = 1;
	
	private final LinkedHashMap<String, PreferenceEntry> preferenceEntries = new LinkedHashMap<String, PreferenceEntry>();

	private final ActionListener okAction = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			actionResult = ACTION_RESULT_OK;
			setVisible(false);
			dispose();
		}
	};	
	
	private final ActionListener abortAction = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			actionResult = ACTION_RESULT_ABORT;
			setVisible(false);
			dispose();
		}
	};	
	
	private int actionResult = -1;
	
	private boolean isInitialized = false;
	
	public PreferenceDialog(JFrame owner) {
		super(owner);
	}
	
	public void addPreferenceEntry(PreferenceEntry entry) {
		preferenceEntries.put(entry.getName(), entry);
	}
	
	/**
	 * Gets the action result of this {@link PreferenceDialog} instance.
	 * @return The action result. This is ACTION_RESULT_OK or ACTION_RESULT_ABORT.
	 */
	public int getActionResult() {
		return this.actionResult;
	}
	
	/**
	 * Get the result value for the {@link PreferenceEntry} with the given name. 
	 */
	public boolean getBooleanValue(final String name) {
		final PreferenceEntry preferenceEntry = preferenceEntries.get(name);
		final Component component = preferenceEntry.getCustomComponent();
		
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
		final Component component = preferenceEntry.getCustomComponent();
		
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
		if(!isInitialized) {
			isInitialized = true;
			
			final List<String> categories = getPreferenceCategories();
			final ArrayList<JPanel> generalPanels = new ArrayList<JPanel>(categories.size());
			getContentPane().setLayout(new BorderLayout());
			((JComponent)getContentPane()).registerKeyboardAction(abortAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
			
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
				int preferenceEntriesSize = preferenceEntriesByCategory.size();
				for(int i = 0; i < preferenceEntriesSize; i++) {
					final PreferenceEntry preferenceEntry = preferenceEntriesByCategory.get(i);
					
					JLabel label = new JLabel(preferenceEntry.getLabel());
					GridBagConstraints gbc_label = new GridBagConstraints();
					gbc_label.insets = new Insets(0, 3, 0, 5);
					gbc_label.gridx = 0;
					gbc_label.gridy = i;
					generalPanel.add(label, gbc_label);
					
					Component c = preferenceEntry.getCustomComponent();
					GridBagConstraints gbc_component = new GridBagConstraints();
					gbc_component.gridx = 2;
					gbc_component.gridy = i;
					generalPanel.add(c, gbc_component);				
				}
				
				//add a panel at the end which fills the rest of the space so the
				//other components are located at the top of the dialog panel.
				Component fillPanel = new JPanel();
				GridBagConstraints gbc_fillPanel = new GridBagConstraints();
				gbc_fillPanel.gridx = 2;
				gbc_fillPanel.gridwidth = 3;
				gbc_fillPanel.gridy = preferenceEntriesSize;
				gbc_fillPanel.fill = GridBagConstraints.BOTH;
				gbc_fillPanel.weighty = 1f;
				generalPanel.add(fillPanel, gbc_fillPanel);						
				
			}
			
			//add the category panels to the dialog root pane.
			if(generalPanels.size() > 1) {
				JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
				getContentPane().add(tabbedPane, BorderLayout.CENTER);
				
				for(JPanel generalPanel : generalPanels) {
					tabbedPane.addTab(generalPanel.getName(), null, generalPanel, null);
				}
			} else if(!generalPanels.isEmpty()) {
				JPanel panel = new JPanel(new BorderLayout());
				getContentPane().add(panel, BorderLayout.CENTER);
				
				JPanel generalPanel = generalPanels.get(0);
				panel.add(generalPanel, BorderLayout.CENTER);
				
				if(!generalPanel.getName().isEmpty()) {
					TitledBorder border = new TitledBorder(generalPanel.getName());
					generalPanel.setBorder(border);
				}
			}
			
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new EqualsLayout(1));
			getContentPane().add(bottomPanel, BorderLayout.SOUTH);
			
			JButton btnAbort = new JButton(Bundle.getString("PreferenceDialog.Action.Cancel"));
			bottomPanel.add(btnAbort);
			btnAbort.addActionListener(abortAction);	
			
			JButton btnOK = new JButton(Bundle.getString("PreferenceDialog.Action.OK"));
			bottomPanel.add(btnOK);
			btnOK.addActionListener(okAction);		
		}
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
		
		private String category;
		
		private String name;
		
		private String label;
		
		private Component customComponent;
		
		private Object value;
		
		public PreferenceEntry(String name, String label, Component customComponent) {
			this.name = name;
			this.label = label;
			this.customComponent = customComponent;
		}
		
		public PreferenceEntry(String name, String label, Component customComponent, String category) {
			this.name = name;
			this.label = label;
			this.category = category;
			this.customComponent = customComponent;
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
		
		public Component getCustomComponent() {
			return customComponent;
		}

		public void setCustomComponent(Component customComponent) {
			this.customComponent = customComponent;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public Object getValue() {
			return value;
		}
		
	}
}
