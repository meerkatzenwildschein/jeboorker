package org.rr.jeborker.gui;

import javax.swing.JCheckBox;
import javax.swing.JFrame;

import org.rr.commons.swing.components.JRCheckBox;
import org.rr.commons.swing.dialogs.JPreferenceDialog;
import org.rr.jeborker.JeboorkerPreferences;

class PreferenceView extends JPreferenceDialog {
	
	static final String AUTO_SCOLL_ITEM_PREFERENCE_NAME = "autoScollItem";
	
	static final String AUTO_SAVE_METADATA_ITEM_PREFERENCE_NAME = "autoSaveMetadataItem";
	
	static final String DELETE_AFTER_IMPORT_PREFERENCE_NAME = "deleteEbookAfterImport";
	
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
				JCheckBox autoScrollCheckBox = new JRCheckBox();
				autoScrollCheckBox.setSelected(JeboorkerPreferences.isTreeAutoScrollingEnabled());
				PreferenceEntry autoScollItem = new PreferenceEntry(AUTO_SCOLL_ITEM_PREFERENCE_NAME, label, autoScrollCheckBox, generalCategory, new Runnable() {
					
					@Override
					public void run() {
						boolean isTreeAutoscrollEnabled = getBooleanValue(PreferenceView.AUTO_SCOLL_ITEM_PREFERENCE_NAME);
						JeboorkerPreferences.setTreeAutoScrollingEnabled(isTreeAutoscrollEnabled);
					}
				});
				addPreferenceEntry(autoScollItem);
			}

			{
				String label = Bundle.getString("PreferenceView.pref.autoSaveMetadata");
				JCheckBox autoSaveMetadataCheckBox = new JRCheckBox();
				autoSaveMetadataCheckBox.setSelected(JeboorkerPreferences.isAutoSaveMetadata());
				PreferenceEntry autoSaveMetadataItem = new PreferenceEntry(AUTO_SAVE_METADATA_ITEM_PREFERENCE_NAME, label, autoSaveMetadataCheckBox, generalCategory, new Runnable() {
					
					@Override
					public void run() {
						boolean isAutoSaveMetadata = getBooleanValue(PreferenceView.AUTO_SAVE_METADATA_ITEM_PREFERENCE_NAME);
						JeboorkerPreferences.addGenericEntryBoolean(JeboorkerPreferences.PREFERENCE_KEYS.MAIN_TABLE_AUTO_SAVE_METADATA_ENABLED, isAutoSaveMetadata);							
					}
				});
				addPreferenceEntry(autoSaveMetadataItem);
			}
			
			{
				String label = Bundle.getString("PreferenceView.pref.deleteAfterImport");
				JCheckBox deleteAfterImportCheckBox = new JRCheckBox();
				deleteAfterImportCheckBox.setSelected(JeboorkerPreferences.getEntryAsBoolean(JeboorkerPreferences.PREFERENCE_KEYS.DELETE_EBOOK_AFTER_IMPORT));
				PreferenceEntry deleteEbookAfterImportItem = new PreferenceEntry(DELETE_AFTER_IMPORT_PREFERENCE_NAME, label, deleteAfterImportCheckBox, generalCategory, new Runnable() {
					
					@Override
					public void run() {
						boolean isEbookDeleteAfterImport = getBooleanValue(PreferenceView.DELETE_AFTER_IMPORT_PREFERENCE_NAME);
						JeboorkerPreferences.addGenericEntryBoolean(JeboorkerPreferences.PREFERENCE_KEYS.DELETE_EBOOK_AFTER_IMPORT, isEbookDeleteAfterImport);						
					}
				});
				addPreferenceEntry(deleteEbookAfterImportItem);
			}
		}
	}
	
}
