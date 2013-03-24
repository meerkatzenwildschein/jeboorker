package org.rr.jeborker.gui;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JSlider;

import org.rr.common.swing.components.JRCheckBox;
import org.rr.commons.swing.dialogs.PreferenceDialog;
import org.rr.commons.utils.ReflectionUtils;

class ConverterPreferenceView extends PreferenceDialog {
	
	static final String LANDSCAPE_FORMAT_PREFERENCE_NAME = "landscape";
	
	static final String IS_MANGA_PREFERENCE_NAME = "manga";

	static final String REDUCE_IMAGE_SIZE_PREFERENCE_NAME = "reduceImageSize";
	
	private boolean isInitialized = false;
	
	private boolean showLandscapePageEntries = true;
	
	private boolean showImageSizeEntry = true;
	
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
			createImageSizeEntries(generalCategory);
		}
	}
	
	private void createImageSizeEntries(final String generalCategory) {
		if(isShowImageSizeEntry()) {
			final JSlider reduceValue = new JSlider(10, 100);
			reduceValue.setMajorTickSpacing(10);
			reduceValue.setMinorTickSpacing(5);
			if(ReflectionUtils.getOS() == ReflectionUtils.OS_WINDOWS) {
				reduceValue.setPaintTicks(true);
				reduceValue.setPaintLabels(true);
				reduceValue.setSnapToTicks(true);				
			} 
			reduceValue.setValue(100);
			reduceValue.setMinimumSize(new Dimension(220, 45));
			String reduceImageQualityLabel = Bundle.getString("ConverterPreferenceView.pref.reduceImageQuality");
			PreferenceEntry reduceImageQualityItem = new PreferenceEntry(REDUCE_IMAGE_SIZE_PREFERENCE_NAME, reduceImageQualityLabel, reduceValue, generalCategory);
			addPreferenceEntry(reduceImageQualityItem);			
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
					Bundle.getString("ConverterPreferenceView.pref.landscape.rotate_clockwise"),
					Bundle.getString("ConverterPreferenceView.pref.landscape.rotate_counterclockwise"),
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
			
			String isMangaLabel = Bundle.getString("ConverterPreferenceView.pref.isManga");
			PreferenceEntry isMangaItem = new PreferenceEntry(IS_MANGA_PREFERENCE_NAME, isMangaLabel, isMangaCheckBox, generalCategory);
			addPreferenceEntry(isMangaItem);
		}
	}

	public boolean isShowLandscapePageEntries() {
		return showLandscapePageEntries;
	}

	public void setShowLandscapePageEntries(boolean showLandscapePageEntries) {
		this.showLandscapePageEntries = showLandscapePageEntries;
	}

	/**
	 * Tells if the slider for reducing the image quality should be shown.
	 */
	public boolean isShowImageSizeEntry() {
		return showImageSizeEntry;
	}

	public void setShowImageSizeEntry(boolean showImageSizeEntry) {
		this.showImageSizeEntry = showImageSizeEntry;
	}
	
}
