package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;

import org.apache.commons.io.FileUtils;
import org.rr.commons.collection.TransformValueList;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.DesktopUtils;
import org.rr.commons.swing.dialogs.JListSelectionDialog;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

import com.shakethat.jpushbullet.PushbulletClient;
import com.shakethat.jpushbullet.net.Devices;
import com.shakethat.jpushbullet.net.Extras;

public class CopyToPushbulletApiFolderAction extends AbstractAction {

	/**
	 * The name where the api key is stored in the database.
	 */
	private static final String PUSHBULLET_API_KEY = "pushBulletApiKey";

	private static final String PUSHBULLET_DEVICE_SELECTION_KEY = "pushBulletDeviceSelection";

	//source file to copy
	String source;

	CopyToPushbulletApiFolderAction(String text) {
		this.source = text;
		putValue(Action.NAME, Bundle.getString("CopyToPushbulletAction.name"));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("pushbullet_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("pushbullet_22.png"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		IResourceHandler resource = ResourceHandlerFactory.getResourceHandler(source);
        try {
        	String message = Bundle.getFormattedString("CopyToPushbulletAction.uploading", resource.getName());
        	MainController.getController().getProgressMonitor().monitorProgressStart(message);

			initUpload(resource);
		} catch (Throwable ex) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Upload failed", ex);
		} finally {
			MainController.getController().getProgressMonitor().monitorProgressStop();
		}
	}

	/**
	 * Uploads the given {@link IResourceHandler} to the desired pushbullet device.
	 * @param resource The resource to be uploaded.
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	private void initUpload(IResourceHandler resource) throws IllegalStateException, IOException {
		String pushBulletApiKey = getApiKey();

		if(StringUtils.isNotEmpty(pushBulletApiKey)) {
			PushbulletClient client = new PushbulletClient(pushBulletApiKey);
			List<String> targetDeviceIdentifiers = askForTargetDeviceIdentifier(client);
			for (String targetDeviceIdentifier : targetDeviceIdentifiers) {
				uploadResource(client, targetDeviceIdentifier, resource);
			}
		}
	}

	private String getApiKey() {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		String pushBulletApiKey = preferenceStore.getGenericEntryAsString(PUSHBULLET_API_KEY);
		if(StringUtils.isEmpty(pushBulletApiKey)) {
			pushBulletApiKey = askForApiKey();
			if(StringUtils.isNotEmpty(pushBulletApiKey)) {
				preferenceStore.addGenericEntryAsString(PUSHBULLET_API_KEY, pushBulletApiKey);
			}
		}
		return pushBulletApiKey;
	}
	
	private void uploadResource(PushbulletClient client, String targetDeviceIdentifier, IResourceHandler resource) throws IOException {
		String name = resource.getName();
		if(containsAnyNonAsciiChars(name)) {
			String newName = createNonAsciiCharFreeString(name, "_");			
			IResourceHandler newResourceHandler = ResourceHandlerFactory.getResourceHandler(new File(FileUtils.getTempDirectory(), newName));
			try {
				resource.copyTo(newResourceHandler, true);
				client.sendFile(targetDeviceIdentifier, newResourceHandler.toFile());
			} finally {
				newResourceHandler.delete();
			}
		}
	}

	private String createNonAsciiCharFreeString(String name, String replacement) {
		String newName = name.replaceAll("[^\\x00-\\x7F]", replacement);
		return newName;
	}

	private boolean containsAnyNonAsciiChars(String name) {
		return name.matches("^.*[^\\x00-\\x7F].*$");
	}

	/**
	 * Opens a message dialog and ask the user to which device the ebook should be send to.
	 * @param client The client which is needed to get the available devices.
	 * @return A list with Identifier for the selected devices.
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	private List<String> askForTargetDeviceIdentifier(PushbulletClient client) throws IllegalStateException, IOException {
		final List<Devices> devices = client.getDevices().getDevices();

		JListSelectionDialog<String> dialog = new JListSelectionDialog<>(MainController.getController().getMainWindow());
		dialog.centerOnScreen();
		dialog.setSelectedIndices(fetchSelectedDeviceIndices());
		dialog.setValues(new JListSelectionDialog.DataModel<String>() {

			@Override
			public String getViewValueAt(int idx) {
				Extras extras = devices.get(idx).getExtras();
				return StringUtils.capitalize(extras.getManufacturer()) + " " + StringUtils.capitalize(extras.getModel());
			}

			@Override
			public String getValueAt(int idx) {
				return devices.get(idx).getIden();
			}

			@Override
			public int getValueCount() {
				return devices.size();
			}
		});
		dialog.setMessage(Bundle.getString("CopyToPushbulletAction.deviceSelection.message"));
		dialog.setTitle(Bundle.getString("CopyToPushbulletAction.deviceSelection.title"));
		if(dialog.ask()) {
			List<Integer> selectedIndices = dialog.getSelectedIndices();
			storeSelectedDeviceIndices(selectedIndices);
			return new TransformValueList<Integer, String>(selectedIndices) {

				@Override
				public String transform(Integer idx) {
					return devices.get(idx.intValue()).getIden();
				}
			};
		}
		return Collections.emptyList();
	}

	private void storeSelectedDeviceIndices(List<Integer> indices) {
		APreferenceStore dbPreferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		String joinedIndices = ListUtils.join(indices, ";");
		dbPreferenceStore.addGenericEntryAsString(PUSHBULLET_DEVICE_SELECTION_KEY, joinedIndices);
	}

	private List<Integer> fetchSelectedDeviceIndices() {
		APreferenceStore dbPreferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		String joinedIndices = dbPreferenceStore.getGenericEntryAsString(PUSHBULLET_DEVICE_SELECTION_KEY);
		if(StringUtils.isNotEmpty(joinedIndices)) {
			List<String> splitIndiceStrings = ListUtils.split(joinedIndices, ";");
			return new TransformValueList<String, Integer>(splitIndiceStrings) {

				@Override
				public Integer transform(String value) {
					return Integer.valueOf(value);
				}
			};
		} else {
			return Collections.emptyList();
		}
	}

	private String askForApiKey() {
		JFrame mainWindow = MainController.getController().getMainWindow();
		return DesktopUtils.showInputDialog(mainWindow, Bundle.getString("CopyToPushbulletAction.apikey.dialog.text"),
				Bundle.getString("CopyToPushbulletAction.apikey.dialog.title"), null);
	}

}
