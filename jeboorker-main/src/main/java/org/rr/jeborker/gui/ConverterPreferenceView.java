package org.rr.jeborker.gui;

import static org.rr.commons.utils.BooleanUtils.not;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSlider;

import org.rr.commons.collection.Pair;
import org.rr.commons.swing.components.JRCheckBox;
import org.rr.commons.swing.components.JRComboBox;
import org.rr.commons.swing.dialogs.JPreferenceDialog;
import org.rr.commons.utils.BooleanUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.gui.resources.ImageResourceBundle;
import org.rr.jeborker.metadata.MetadataProperty;

class ConverterPreferenceView extends JPreferenceDialog {
	
	private static final long serialVersionUID = 6315443343060853996L;

	static final String LANDSCAPE_FORMAT_PREFERENCE_NAME = "landscape";
	
	static final String IS_MANGA_PREFERENCE_NAME = "manga";

	static final String REDUCE_IMAGE_SIZE_PREFERENCE_NAME = "reduceImageSize";
	
	private boolean isInitialized = false;
	
	private boolean showLandscapePageEntries = false;
	
	private List<Pair<String, ? extends JComponent>> commonComponentEntries = new ArrayList<>();
	
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
			String generalCategory = Bundle.getString("ConverterPreferenceView.tab.general");
			createLandscapePageEntries(generalCategory);
			createCommonComponentEntries(generalCategory);
		}
	}

	/**
	 * Create and add the combobox which provides the components for landscape format images.
	 * The combobox provides options like rotate or split. The manga checkbox for the
	 * split option is also added here.
	 */
	private void createLandscapePageEntries(final String category) {
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
			PreferenceEntry landscapeFormatItem = new PreferenceEntry(LANDSCAPE_FORMAT_PREFERENCE_NAME, landscapeFormatLabel, landscapeFormatCombobox, category);
			addPreferenceEntry(landscapeFormatItem);
			
			String isMangaLabel = Bundle.getString("ConverterPreferenceView.pref.isManga");
			PreferenceEntry isMangaItem = new PreferenceEntry(IS_MANGA_PREFERENCE_NAME, isMangaLabel, isMangaCheckBox, category);
			addPreferenceEntry(isMangaItem);
		}
	}

	private void createCommonComponentEntries(String category) {
		if(ListUtils.isNotEmpty(commonComponentEntries)) {
			for (Pair<String, ? extends JComponent> entry : commonComponentEntries) {
				entry.getF().setName(entry.getE());
				addPreferenceEntry(new PreferenceEntry(entry.getE(), entry.getE(), entry.getF(), category));				
			}
		}
	}
	
	public boolean isShowLandscapePageEntries() {
		return showLandscapePageEntries;
	}

	public void setShowLandscapePageEntries(boolean showLandscapePageEntries) {
		this.showLandscapePageEntries = showLandscapePageEntries;
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

	public void addCommonListSelection(String label, List<String> entries, String selectedEntry) {
		JComboBox<String> combobox = new JRComboBox<>();
		combobox.setModel(new DefaultComboBoxModel<String>(entries.toArray(new String[entries.size()])));
		combobox.setSelectedItem(selectedEntry);
		
		commonComponentEntries.add(new Pair<String, JComboBox<String>>(label, combobox));
	}

	public void addCommonCheckBox(String label, Boolean selected) {
		JCheckBox checkbox = new JRCheckBox();
		checkbox.setSelected(selected);
		
		commonComponentEntries.add(new Pair<String, JCheckBox>(label, checkbox));
	}

	public void addCommonSlider(String label, Integer selected) {
		JSlider slider = new JSlider();
		slider.setMaximum(100);
		slider.setValue(selected);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setSnapToTicks(true);
		slider.setMinimumSize(new Dimension(220, 45));

		commonComponentEntries.add(new Pair<String, JSlider>(label, slider));
	}
	
}
