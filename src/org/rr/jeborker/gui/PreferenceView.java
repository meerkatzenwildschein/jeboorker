package org.rr.jeborker.gui;

import javax.swing.JCheckBox;
import javax.swing.JFrame;

import org.rr.common.swing.components.JRCheckBox;
import org.rr.commons.swing.dialogs.PreferenceDialog;
import org.rr.jeborker.JeboorkerPreferences;

class PreferenceView extends PreferenceDialog {
	
	static final String AUTO_SCOLL_ITEM_PREFERENCE_NAME = "autoScollItem";
	
	private boolean isInitialized = false;
	
	private PreferenceController preferenceController;

	public PreferenceView(JFrame mainWindow, PreferenceController preferenceController) {
		super(mainWindow);
		this.preferenceController = preferenceController;
		setTitle(Bundle.getString("PreferenceView.title"));
	}
	
	public void setVisible(boolean visible) {
		this.initialize();
		super.setVisible(visible);
		if(!visible) {
			preferenceController.close();
		}
	}	

	private void initialize() {
		if(!isInitialized) {
			isInitialized = true;
			final String generalCategory = Bundle.getString("PreferenceView.tab.general");
			
			{
				String label = Bundle.getString("PreferenceView.pref.autoscroll");
				JCheckBox generalCategoryCheckBox = new JRCheckBox();
				generalCategoryCheckBox.setSelected(JeboorkerPreferences.isTreeAutoScrollingEnabled());
				PreferenceEntry autoScollItem = new PreferenceEntry(AUTO_SCOLL_ITEM_PREFERENCE_NAME, label, generalCategoryCheckBox, generalCategory);
				addPreferenceEntry(autoScollItem);
			}
		}
	}
	
}
