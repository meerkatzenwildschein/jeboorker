package org.rr.jeborker.gui.action;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.ResourceHandlerUtils;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.dialogs.chooser.FileChooserDialogFactory;
import org.rr.commons.swing.dialogs.chooser.IFileChooser;
import org.rr.commons.swing.dialogs.chooser.IFileChooser.RETURN_OPTION;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class SaveCoverToFileAction extends AbstractAction {

	private String ebook;
	
	private static final String PATH_PREF_KEY =  SaveCoverToFileAction.class.getSimpleName() + "LatestPath";

	SaveCoverToFileAction(String text) {
		this.ebook = text;
		String name = Bundle.getString("SaveCoverToFileAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("file_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("file_22.png"));		
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final MainController controller = MainController.getController();
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final IResourceHandler imageViewerResource = controller.getImageViewerResource();
		final String fileExtension = ResourceHandlerUtils.getFileExtension(imageViewerResource);
		
		String filename = "cover" + (fileExtension != null ? fileExtension : EMPTY);
		String dir = preferenceStore.getGenericEntryAsString(PATH_PREF_KEY);
		IFileChooser c = FileChooserDialogFactory.getFileChooser();
		if(dir != null) {
			c.setCurrentDirectory(new File(dir));
		}
		if(filename != null) {
			c.setSelectedFile(new File(filename));
		}
		c.setDialogType(IFileChooser.DIALOG_TPYE.SAVE);
		c.setTitle(Bundle.getString("SaveCoverToFileAction.saveDialogTitle"));
		
		RETURN_OPTION rVal = c.showDialog(controller.getMainWindow());

		if (rVal == IFileChooser.RETURN_OPTION.APPROVE) {
			filename = c.getSelectedFile().getName();
			dir = c.getCurrentDirectory().toString();
			preferenceStore.addGenericEntryAsString(PATH_PREF_KEY, dir);
			
			String targetString = dir + File.separator + filename;
			IResourceHandler targetRecource = ResourceHandlerFactory.getResourceHandler(targetString);
			try {
				imageViewerResource.copyTo(targetRecource, true);
				MainController.getController().getProgressMonitor().setMessage(Bundle.getFormattedString("SaveCoverToFileAction.finished", new String[] {targetString}));
			} catch (IOException e1) {
				LoggerFactory.getLogger().log(Level.WARNING, "Failed to save cover for " + ebook);
			}
		} 
	}

}
