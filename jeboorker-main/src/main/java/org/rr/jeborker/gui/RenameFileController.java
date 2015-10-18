package org.rr.jeborker.gui;

import java.awt.Point;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFrame;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;

public class RenameFileController {

	private JFrame mainWindow;

	private RenameFileView renameFileView;

	private boolean confirmState;

	private boolean overwrite;

	private List<Entry<EbookPropertyItem, IResourceHandler>> values;

	private RenameFileController(JFrame mainWindow) {
		this.mainWindow = mainWindow;
	}

	static RenameFileController getInstance(JFrame mainWindow) {
		RenameFileController controller = new RenameFileController(mainWindow);
		return controller;
	}

	/**
	 * Shows the dialog to the user and wait until the user has confirm or aborted the dialog.
	 */
	public void showDialog(List<EbookPropertyItem> list) {
		RenameFileView view = getView(list);
		view.setVisible(true);
	}

	private RenameFileView getView() {
		return getView(null);
	}

	private RenameFileView getView(List<EbookPropertyItem> list) {
		if(renameFileView == null) {
			this.renameFileView = new RenameFileView(this, list, mainWindow);
			this.restoreProperties();
		}
		return renameFileView;
	}

	public void close() {
		confirmState = getView().getActionResult() == RenameFileView.ACTION_RESULT_OK;
		overwrite = getView().isOverwriteExistingFiles();
		values = getView().getValues();
		storeProperties();
		renameFileView.setVisible(false);
		renameFileView.dispose();
		renameFileView = null;
	}

	/**
	 * Tells if the user has confirmed the rename file dialog.
	 * @return <code>true</code> if the user has confirmed the dialog and <code>false</code> if
	 * he hits abort or just closed the dialog.
	 */
	public boolean isConfirmed() {
		return confirmState;
	}

	/**
	 * Tells if existing target files should be overwritten.
	 * @return <code>true</code> if existing files should be overwritten and <code>false</code> otherwise.
	 */
	public boolean isOverwriteExistingFiles() {
		return overwrite;
	}

	/**
	 * Get the rename file values.
	 * @return A list with the the original {@link EbookPropertyItem} and their rename target {@link IResourceHandler}.
	 */
	public List<Entry<EbookPropertyItem, IResourceHandler>> getValues() {
		return values;
	}

	private void storeProperties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		preferenceStore.addGenericEntryAsNumber("renameFileDialogSizeWidth", getView().getSize().width);
		preferenceStore.addGenericEntryAsNumber("renameFileDialogSizeHeight", getView().getSize().height);
		preferenceStore.addGenericEntryAsNumber("renameFileDialogLocationX", getView().getLocation().x);
		preferenceStore.addGenericEntryAsNumber("renameFileDialogLocationY", getView().getLocation().y);
		preferenceStore.addGenericEntryAsString("renameFileDialogPattern", getView().getFileNamePattern());
		preferenceStore.addGenericEntryAsString("renameFileDialogPatternHistory", getView().getFileNameHistory());
	}

	private void restoreProperties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);

		//restore the window size from the preferences.
		Number renameDialogSizeWidth = preferenceStore.getGenericEntryAsNumber("renameFileDialogSizeWidth");
		Number renameDialogSizeHeight = preferenceStore.getGenericEntryAsNumber("renameFileDialogSizeHeight");
		if(renameDialogSizeWidth != null && renameDialogSizeHeight != null) {
			getView().setSize(renameDialogSizeWidth.intValue(), renameDialogSizeHeight.intValue());
		}

		//restore window location
		Point entryAsScreenLocation = preferenceStore.getGenericEntryAsScreenLocation("renameFileDialogLocationX", "renameFileDialogLocationY");
		if(entryAsScreenLocation != null) {
			getView().setLocation(entryAsScreenLocation);
		}

		getView().setFileNamePattern(preferenceStore.getGenericEntryAsString("renameFileDialogPattern"));
		getView().setFileNameHistory(preferenceStore.getGenericEntryAsString("renameFileDialogPatternHistory"));
	}
}
