package org.rr.jeborker.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import org.rr.common.swing.components.JRCheckBox;
import org.rr.commons.swing.dialogs.PreferenceDialog;

class ConverterPreferenceView extends PreferenceDialog {
	
	static final String LANDSCAPE_FORMAT_PREFERENCE_NAME = "landscape";
	
	static final String IS_MANGA_PREFERENCE_NAME = "manga";
	
	private boolean isInitialized = false;
	
	private boolean showLandscapePageEntries = true;
	
	private ConverterPreferenceController controller;

	public ConverterPreferenceView(ConverterPreferenceController controller, JFrame mainWindow) {
		super(mainWindow);
		this.controller = controller;
		setTitle(Bundle.getString("ConverterPreferenceView.title"));
	}
	
	public void setVisible(boolean visible) {
		this.initialize();
		super.setVisible(visible);
		if(!visible) {
			controller.close();
		}
	}	

	private void initialize() {
		if(!isInitialized) {
			isInitialized = true;
			final String generalCategory = Bundle.getString("ConverterPreferenceView.tab.general");
			createLandscapePageEntries(generalCategory);
		}
	}

	private void createLandscapePageEntries(final String generalCategory) {
		if(isShowLandscapePageEntries()) {
			final JComboBox<String> landscapeFormatCombobox = new JComboBox<String>();
			final JRCheckBox isMangaCheckBox = new JRCheckBox();
			isMangaCheckBox.setEnabled(false);
			
			String landscapeFormatLabel = Bundle.getString("ConverterPreferenceView.pref.landscape");
			landscapeFormatCombobox.setModel(new DefaultComboBoxModel<String>(new String[] {
					Bundle.getString("ConverterPreferenceView.pref.landscape.keep"),
					Bundle.getString("ConverterPreferenceView.pref.landscape.rotate"),
					Bundle.getString("ConverterPreferenceView.pref.landscape.split")
			}));
			landscapeFormatCombobox.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getItem().equals(Bundle.getString("ConverterPreferenceView.pref.landscape.split"))) {
						isMangaCheckBox.setEnabled(true);
					} else {
						isMangaCheckBox.setEnabled(false);
					}
				}
			});
			PreferenceEntry landscapeFormatItem = new PreferenceEntry(LANDSCAPE_FORMAT_PREFERENCE_NAME, landscapeFormatLabel, landscapeFormatCombobox, generalCategory);
			addPreferenceEntry(landscapeFormatItem);
			
			String isManga = Bundle.getString("ConverterPreferenceView.pref.isManga");
			PreferenceEntry isMangaItem = new PreferenceEntry(IS_MANGA_PREFERENCE_NAME, isManga, isMangaCheckBox, generalCategory);
			addPreferenceEntry(isMangaItem);
		}
	}

	public boolean isShowLandscapePageEntries() {
		return showLandscapePageEntries;
	}

	public void setShowLandscapePageEntries(boolean showLandscapePageEntries) {
		this.showLandscapePageEntries = showLandscapePageEntries;
	}
	
}
