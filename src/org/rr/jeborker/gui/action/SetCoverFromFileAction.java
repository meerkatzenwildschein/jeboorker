package org.rr.jeborker.gui.action;

import java.awt.Frame;
import java.io.File;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.dialogs.ImageFileChooser;
import org.rr.commons.utils.ArrayUtils;

class SetCoverFromFileAction extends SetCoverFrom<ImageFileChooser> implements IDoOnlyOnceAction<ImageFileChooser> {

	private static final long serialVersionUID = 4772310971481868593L;

	private ImageFileChooser imageFileChooserT;

	private static File previousSelectedFile;
	
	SetCoverFromFileAction(IResourceHandler resourceHandler) {
		super(resourceHandler);
		putValue(Action.NAME, Bundle.getString("SetCoverFromFileAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("image_jpeg_16.gif")));
	}
	
	@Override
	public synchronized ImageFileChooser doOnce() {
		if(imageFileChooserT == null) {
			imageFileChooserT = new ImageFileChooser();
			imageFileChooserT.setVisible(true);
			if(previousSelectedFile!=null) {
				imageFileChooserT.setSelectedFile(previousSelectedFile);
			}
			imageFileChooserT.showOpenDialog(ArrayUtils.get(Frame.getFrames(), 0));
			previousSelectedFile = imageFileChooserT.getSelectedFile();
			
			setDialogOption(imageFileChooserT.getReturnValue());
			if(imageFileChooserT.getSelectedFile() != null) {
				setDialogResult(ResourceHandlerFactory.getResourceLoader(imageFileChooserT.getSelectedFile()));
			}
		}
		
		return this.imageFileChooserT;
	}

	@Override
	public void setDoOnceResult(ImageFileChooser result) {
		this.imageFileChooserT = result;
		setDialogOption(imageFileChooserT.getReturnValue());
		if(imageFileChooserT.getSelectedFile() != null) {
			setDialogResult(ResourceHandlerFactory.getResourceLoader(imageFileChooserT.getSelectedFile()));
		}		
	}
}
