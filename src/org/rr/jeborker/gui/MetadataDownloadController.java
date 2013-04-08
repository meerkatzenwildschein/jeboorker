package org.rr.jeborker.gui;

import java.awt.Point;

import javax.swing.JFrame;

import org.rr.jeborker.JeboorkerPreferences;

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

	public void showDialog() {
		MetadataDownloadView view = getView();
		view.setVisible(true);
	}

	private MetadataDownloadView getView() {
		if(metadataDownloadView == null) {
			this.metadataDownloadView = new MetadataDownloadView(this, mainWindow);
			this.initialize();
		}
		return metadataDownloadView;
	}

	private void initialize() {
		restorePropeties();		
	}
	
	public void close() {
		storeProperties();
		metadataDownloadView.setVisible(false);
		metadataDownloadView.dispose();
	}		

	private void storeProperties() {
		JeboorkerPreferences.addGenericEntryAsNumber("matadataDownloadDialogSizeWidth", getView().getSize().width);
		JeboorkerPreferences.addGenericEntryAsNumber("matadataDownloadDialogSizeHeight", getView().getSize().height);
		JeboorkerPreferences.addGenericEntryAsNumber("matadataDownloadDialogLocationX", getView().getLocation().x);
		JeboorkerPreferences.addGenericEntryAsNumber("matadataDownloadDialogLocationY", getView().getLocation().y);
		JeboorkerPreferences.addGenericEntryAsNumber("matadataDownloadDialogSearchProviderIndex", getView().getSearchProviderIndex());
	}

	private void restorePropeties() {
		//restore the window size from the preferences.
		Number metadataDialogSizeWidth = JeboorkerPreferences.getGenericEntryAsNumber("matadataDownloadDialogSizeWidth");
		Number metadataDialogSizeHeight = JeboorkerPreferences.getGenericEntryAsNumber("matadataDownloadDialogSizeHeight");
		if(metadataDialogSizeWidth != null && metadataDialogSizeHeight != null) {
			getView().setSize(metadataDialogSizeWidth.intValue(), metadataDialogSizeHeight.intValue());
		}
		
		//restore window location
		Point entryAsScreenLocation = JeboorkerPreferences.getGenericEntryAsScreenLocation("matadataDownloadDialogLocationX", "matadataDownloadDialogLocationY");
		if(entryAsScreenLocation != null) {
			getView().setLocation(entryAsScreenLocation);
		}		
		
		//restore search provider index.
		Number serachProviderIndex = JeboorkerPreferences.getGenericEntryAsNumber("matadataDownloadDialogSearchProviderIndex");
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
	
}
