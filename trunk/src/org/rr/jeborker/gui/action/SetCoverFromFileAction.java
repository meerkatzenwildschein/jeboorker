package org.rr.jeborker.gui.action;

import java.io.File;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.dialogs.chooser.ChooserDialogFactory;
import org.rr.commons.swing.dialogs.chooser.IFileChooser;
import org.rr.jeborker.gui.MainController;

class SetCoverFromFileAction extends SetCoverFrom<IFileChooser> implements IDoOnlyOnceAction<IFileChooser> {

	private static final long serialVersionUID = 4772310971481868593L;

	private IFileChooser imageFileChooserT;

	private static File previousSelectedFile;
	
	SetCoverFromFileAction(IResourceHandler resourceHandler) {
		super(resourceHandler);
		putValue(Action.NAME, Bundle.getString("SetCoverFromFileAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("image_16.gif")));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
	}
	
	@Override
	public synchronized IFileChooser doOnce() {
		if(imageFileChooserT == null) {
			imageFileChooserT = ChooserDialogFactory.getFileChooser();
			if(previousSelectedFile != null) {
				imageFileChooserT.setSelectedFile(previousSelectedFile);
			}
			imageFileChooserT.setDialogType(IFileChooser.DIALOG_TPYE.OPEN);
			imageFileChooserT.showDialog(MainController.getController().getMainWindow());
			previousSelectedFile = imageFileChooserT.getSelectedFile();
			
			setDialogOption(imageFileChooserT.getReturnValue().ordinal());
			if(imageFileChooserT.getSelectedFile() != null) {
				setDialogResult(ResourceHandlerFactory.getResourceLoader(imageFileChooserT.getCurrentDirectory() + File.separator + imageFileChooserT.getSelectedFile()));
			}
		}
		
		return this.imageFileChooserT;
	}

	@Override
	public void setDoOnceResult(IFileChooser result) {
		this.imageFileChooserT = result;
		setDialogOption(imageFileChooserT.getReturnValue().ordinal());
		if(imageFileChooserT.getSelectedFile() != null) {
			setDialogResult(ResourceHandlerFactory.getResourceLoader(imageFileChooserT.getCurrentDirectory() + File.separator + imageFileChooserT.getSelectedFile()));
		}		
	}
}
