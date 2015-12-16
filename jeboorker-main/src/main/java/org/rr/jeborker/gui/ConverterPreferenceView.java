package org.rr.jeborker.gui;

import static org.rr.commons.utils.BooleanUtils.not;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JSlider;

import org.rr.commons.swing.components.JRCheckBox;
import org.rr.commons.swing.dialogs.JPreferenceDialog;
import org.rr.commons.utils.BooleanUtils;
import org.rr.jeborker.gui.resources.ImageResourceBundle;
import org.rr.jeborker.metadata.MetadataProperty;

class ConverterPreferenceView extends JPreferenceDialog {
	
	private static final long serialVersionUID = 6315443343060853996L;

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
		if(not(visible)) {
			controller.close();
		}
	}

	private void initialize() {
		if(not(isInitialized)) {
			isInitialized = true;
			setIconImage(ImageResourceBundle.getResourceAsImageIcon("preferences_16.png").getImage());
			final String generalCategory = Bundle.getString("ConverterPreferenceView.tab.general");
			createLandscapePageEntries(generalCategory);
			createImageSizeEntries(generalCategory);
		}
	}
	
	/**
	 * Create and add the image resize slider.
	 */
	private void createImageSizeEntries(final String generalCategory) {
		if(isShowImageSizeEntry()) {
			final JSlider reduceValue = new JSlider(JSlider.HORIZONTAL, 10, 100, 100);
			reduceValue.setMajorTickSpacing(10);
			reduceValue.setMinorTickSpacing(5);
			reduceValue.setPaintTicks(true);
			reduceValue.setPaintLabels(true);
			reduceValue.setSnapToTicks(true);
			reduceValue.setMinimumSize(new Dimension(220, 45));
			String reduceImageQualityLabel = Bundle.getString("ConverterPreferenceView.pref.reduceImageQuality");
			PreferenceEntry reduceImageQualityItem = new PreferenceEntry(REDUCE_IMAGE_SIZE_PREFERENCE_NAME, reduceImageQualityLabel, reduceValue, generalCategory);
			addPreferenceEntry(reduceImageQualityItem);
		}
	}

	/**
	 * Create and add the combobox which provides the components for landscape format images.
	 * The combobox provides options like rotate or split. The manga checkbox for the
	 * split option is also added here.
	 */
	private void createLandscapePageEntries(final String generalCategory) {
		if(isShowLandscapePageEntries()) {
			final JComboBox<String> landscapeFormatCombobox = new JComboBox<>();
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
						if(isManga()) {
							//enable the manga checkbox per default if there is any hint the selection is a manga.
							isMangaCheckBox.setSelected(true);
						}
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
	
	/**
	 * Fast check at the loaded metadata for a manga mode property. No metadata loading happens here!
	 * @return <code>true</code> if a manga metadata property could be found and is <code>true</code> and <code>false</code> otherwise.
	 */
	private static boolean isManga() {
		boolean result = false;
		List<MetadataProperty> allMetadata = MainController.getController().getPropertySheetHandler().getModel().getAllMetadata();
		for(MetadataProperty metadata : allMetadata) {
			String name = metadata.getName().toLowerCase();
			if(name.contains("manga")) {
				String mangaValue = metadata.getValueAsString();
				return BooleanUtils.toBoolean(mangaValue);
			}
		}
		return result;
	}
	
}
