package org.rr.jeborker.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFrame;

import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.metadata.IMetadataReader;

public class MetadataDownloadController {
	
	private MetadataDownloadView metadataDownloadView;
	
	private JFrame mainWindow;
	
	private MetadataDownloadController(JFrame mainWindow) {
		this.mainWindow = mainWindow;
	}
	
	static MetadataDownloadController getInstance(JFrame mainWindow) {
		MetadataDownloadController controller = new MetadataDownloadController(mainWindow);
		return controller;
	}

	/**
	 * Shows the dialog to the user and wait until the user has confirm or aborted the dialog.
	 */
	public void showDialog() {
		MetadataDownloadView view = createView();
		view.setVisible(true);
	}
	
	private MetadataDownloadView createView() {
		this.metadataDownloadView = new MetadataDownloadView(this, mainWindow);
		this.restorePropeties();
		return this.metadataDownloadView;
	}

	private MetadataDownloadView getView() {
		if(metadataDownloadView == null) {
			return createView();
		}
		return metadataDownloadView;
	}

	public void close() {
		storeProperties();
		metadataDownloadView.setVisible(false);
		metadataDownloadView.dispose();
	}

	private void storeProperties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		preferenceStore.addGenericEntryAsNumber("matadataDownloadDialogSizeWidth", getView().getSize().width);
		preferenceStore.addGenericEntryAsNumber("matadataDownloadDialogSizeHeight", getView().getSize().height);
		preferenceStore.addGenericEntryAsNumber("matadataDownloadDialogLocationX", getView().getLocation().x);
		preferenceStore.addGenericEntryAsNumber("matadataDownloadDialogLocationY", getView().getLocation().y);
		preferenceStore.addGenericEntryAsNumber("matadataDownloadDialogSearchProviderIndex", getView().getSearchProviderIndex());
	}

	private void restorePropeties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		
		//restore the window size from the preferences.
		Number metadataDialogSizeWidth = preferenceStore.getGenericEntryAsNumber("matadataDownloadDialogSizeWidth");
		Number metadataDialogSizeHeight = preferenceStore.getGenericEntryAsNumber("matadataDownloadDialogSizeHeight");
		if(metadataDialogSizeWidth != null && metadataDialogSizeHeight != null) {
			getView().setSize(metadataDialogSizeWidth.intValue(), metadataDialogSizeHeight.intValue());
		}
		
		//restore window location
		Point entryAsScreenLocation = preferenceStore.getGenericEntryAsScreenLocation("matadataDownloadDialogLocationX", "matadataDownloadDialogLocationY");
		if(entryAsScreenLocation != null) {
			getView().setLocation(entryAsScreenLocation);
		}
		
		//restore search provider index.
		Number serachProviderIndex = preferenceStore.getGenericEntryAsNumber("matadataDownloadDialogSearchProviderIndex");
		if(serachProviderIndex != null) {
			getView().setSearchProviderIndex(serachProviderIndex.intValue());
		}
	}
	
	/**
	 * Tells if the user has confirmed the metadata download dialog.
	 * @return <code>true</code> if the user has confirmed the dialog and <code>false</code> if
	 * he hits abort or just closed the dialog.
	 */
	public boolean isConfirmed() {
		return getView().getActionResult() == MetadataDownloadView.ACTION_RESULT_OK;
	}
	
	/**
	 * Get the downloaded metadata values for the given type.
	 * @return A list with the downloaded values for the given type. Each list entry
	 *     is a {@link Entry} with the checkbox boolean value as <code>key</code> and the
	 *     text with the <code>value</code>.
	 */
	public List<Entry<Boolean, String>> getValues(IMetadataReader.METADATA_TYPES type) {
		return getView().getValues(type);
	}
	
	/**
	 * Get the downloaded metadata string values for the given type. Only these values are returned
	 * that are selected by the user.
	 * @return A list with the downloaded string values for the given type.
	 */
	public List<String> getFilteredValues(IMetadataReader.METADATA_TYPES type) {
		List<Entry<Boolean, String>> values = getValues(type);
		ArrayList<String> result = new ArrayList<String>(values.size());
		for(Entry<Boolean, String> value : values) {
			if(value.getKey().booleanValue()) {
				result.add(value.getValue());
			}
		}
		return result;
	}
	
	/**
	 * Get the cover image from the downloader.
	 */
	public byte[] getCoverImage() {
		return getView().getCoverImage();
	}
	
}
