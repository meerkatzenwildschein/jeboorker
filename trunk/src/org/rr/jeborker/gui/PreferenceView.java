package org.rr.jeborker.gui;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.rr.common.swing.components.JRCheckBox;
import org.rr.commons.swing.dialogs.PreferenceDialog;
import org.rr.jeborker.JeboorkerPreferences;

class PreferenceView extends PreferenceDialog {
	
	static final String AUTO_SCOLL_ITEM_NAME = "autoScollItem";
	
	private boolean isInitialized = false;

	public PreferenceView(JFrame mainWindow, PreferenceController preferenceController) {
		super(mainWindow);
		setModal(true);
		setLocation(mainWindow.getLocation().x, mainWindow.getLocation().y);
		setSize(600, 350);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);		
		setTitle(Bundle.getString("PreferenceView.title"));
	}
	
	public void setVisible(boolean visible) {
		this.initialize();
		super.setVisible(visible);
	}	

	private void initialize() {
		if(!isInitialized) {
			isInitialized = true;
			final String generalCategory = Bundle.getString("PreferenceView.tab.general");
			
			{
				String label = Bundle.getString("PreferenceView.pref.autoscroll");
				JCheckBox generalCategoryCheckBox = new JRCheckBox();
				generalCategoryCheckBox.setSelected(JeboorkerPreferences.isTreeAutoScrollingEnabled());
				PreferenceEntry autoScollItem = new PreferenceEntry(AUTO_SCOLL_ITEM_NAME, label, generalCategoryCheckBox, generalCategory);
				addPreferenceEntry(autoScollItem);
			}
		}
	}
	
}
