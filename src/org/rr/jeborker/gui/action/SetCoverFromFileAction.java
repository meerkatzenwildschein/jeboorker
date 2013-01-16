package org.rr.jeborker.gui.action;

import java.io.File;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.dialogs.chooser.ChooserDialogFactory;
import org.rr.commons.swing.dialogs.chooser.IFileChooser;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class SetCoverFromFileAction extends SetCoverFrom<IFileChooser> implements IDoOnlyOnceAction<IFileChooser> {

	private static final long serialVersionUID = 4772310971481868593L;

	private IFileChooser fileChooser;

	private static File previousSelectedFile;
	
	SetCoverFromFileAction(IResourceHandler resourceHandler) {
		super(resourceHandler);
		putValue(Action.NAME, Bundle.getString("SetCoverFromFileAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(ImageResourceBundle.getResource("image_16.png")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(ImageResourceBundle.getResource("image_22.png")));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
	}
	
	@Override
	public synchronized IFileChooser doOnce() {
		if(fileChooser == null) {
			fileChooser = ChooserDialogFactory.getFileChooser();
			if(previousSelectedFile != null) {
				fileChooser.setSelectedFile(previousSelectedFile);
			}
			fileChooser.setDialogType(IFileChooser.DIALOG_TPYE.OPEN);
			fileChooser.setTitle(Bundle.getString("SetCoverFromFileAction.fileDialogTitle"));
			fileChooser.showDialog(MainController.getController().getMainWindow());
			previousSelectedFile = fileChooser.getSelectedFile();
			
			setDialogOption(fileChooser.getReturnValue().ordinal());
			if(fileChooser.getSelectedFile() != null) {
				setDialogResult(ResourceHandlerFactory.getResourceLoader(fileChooser.getCurrentDirectory() + File.separator + fileChooser.getSelectedFile()));
			}
		}
		
		return this.fileChooser;
	}

	@Override
	public void setDoOnceResult(IFileChooser result) {
		this.fileChooser = result;
		setDialogOption(fileChooser.getReturnValue().ordinal());
		if(fileChooser.getSelectedFile() != null) {
			setDialogResult(ResourceHandlerFactory.getResourceLoader(fileChooser.getCurrentDirectory() + File.separator + fileChooser.getSelectedFile()));
		}		
	}
}
