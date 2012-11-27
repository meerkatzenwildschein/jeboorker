package org.rr.jeborker.gui.action;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.net.imagefetcher.ImageFetcherFactory;
import org.rr.commons.swing.dialogs.ImageDownloadDialog;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;

class SetCoverFromDownload extends SetCoverFrom<ImageDownloadDialog> implements IDoOnlyOnceAction<ImageDownloadDialog> {

	private static final long serialVersionUID = -6464113132395695332L;
	
	private ImageDownloadDialog imageDownloadDialog;

	SetCoverFromDownload(IResourceHandler resourceHandler) {
		super(resourceHandler);
		putValue(Action.NAME, Bundle.getString("SetCoverFromDownloadAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("image_16.gif")));		
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
	}

	@Override
	public ImageDownloadDialog doOnce() {
		if(imageDownloadDialog == null) {
			final MainController controller = MainController.getController();
			imageDownloadDialog = new ImageDownloadDialog(controller.getMainWindow(), ImageFetcherFactory.getInstance());
			
			//default search phrase
			String searchPhrase = "";
			EbookPropertyItem item = ListUtils.first(DefaultDBManager.getInstance().getObject(EbookPropertyItem.class, "file", resourceHandler.toString()));

			//no author in the search term for now. Big Book Search did not like it.
			if(item != null) {
				String authors = ListUtils.join(item.getAuthors(), " ").trim();
				if(StringUtils.isNotEmpty(authors)) {
					searchPhrase += authors + " ";
				}
			} 
			
			if(item != null && StringUtils.isNotEmpty(item.getTitle())) {
				searchPhrase += item.getTitle();
			} else {
				searchPhrase += this.resourceHandler.getName();
				searchPhrase = searchPhrase.substring(0, searchPhrase.length() - this.resourceHandler.getFileExtension().length()).trim();
				if(searchPhrase.endsWith(".")) {
					searchPhrase = searchPhrase.substring(0, searchPhrase.length()-1);
				}				
			}
			searchPhrase = StringUtils.replace(searchPhrase, "-", " ");
			
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
