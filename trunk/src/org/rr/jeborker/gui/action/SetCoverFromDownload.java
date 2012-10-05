package org.rr.jeborker.gui.action;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.net.imagefetcher.ImageFetcherFactory;
import org.rr.commons.swing.dialogs.ImageDownloadDialog;
import org.rr.jeborker.gui.MainController;

class SetCoverFromDownload extends SetCoverFrom<ImageDownloadDialog> implements IDoOnlyOnceAction<ImageDownloadDialog> {

	private static final long serialVersionUID = -6464113132395695332L;
	
	private ImageDownloadDialog imageDownloadDialog;

	SetCoverFromDownload(IResourceHandler resourceHandler) {
		super(resourceHandler);
		putValue(Action.NAME, Bundle.getString("SetCoverFromDownloadAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("image_jpeg_16.gif")));		
	}

	@Override
	public ImageDownloadDialog doOnce() {
		if(imageDownloadDialog == null) {
			imageDownloadDialog = new ImageDownloadDialog(MainController.getController().getMainWindow(), ImageFetcherFactory.getInstance());
			
			//default search phrase
			String searchPhrase = this.resourceHandler.getName();
			searchPhrase = searchPhrase.substring(0, searchPhrase.length() - this.resourceHandler.getFileExtension().length()).trim();
			if(searchPhrase.endsWith(".")) {
				searchPhrase = searchPhrase.substring(0, searchPhrase.length()-1);
			}
			imageDownloadDialog.setSearchPhrase(searchPhrase);
			
			imageDownloadDialog.setVisible(true);
			
			IResourceHandler selectedImage = imageDownloadDialog.getSelectedImage();
			if(selectedImage != null) {
				setDialogOption(JFileChooser.APPROVE_OPTION);
				setDialogResult(selectedImage);
			} else {
				setDialogOption(JFileChooser.CANCEL_OPTION);
			}
		}
		
		return this.imageDownloadDialog;
	}

	@Override
	public void setDoOnceResult(ImageDownloadDialog result) {
		this.imageDownloadDialog = result;
		IResourceHandler selectedImage = imageDownloadDialog.getSelectedImage();
		if(selectedImage != null) {
			setDialogOption(JFileChooser.APPROVE_OPTION);
			setDialogResult(selectedImage);
		} else {
			setDialogOption(JFileChooser.CANCEL_OPTION);
		}		
	}

}
