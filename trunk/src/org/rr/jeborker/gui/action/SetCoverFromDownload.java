package org.rr.jeborker.gui.action;

import javax.swing.Action;
import javax.swing.JFileChooser;

import org.rr.common.swing.SwingUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.net.imagefetcher.ImageWebSearchFetcherFactory;
import org.rr.commons.swing.dialogs.JImageDownloadDialog;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.IDBObject;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class SetCoverFromDownload extends SetCoverFrom<JImageDownloadDialog> implements IDoOnlyOnceAction<JImageDownloadDialog> {

	private static final long serialVersionUID = -6464113132395695332L;
	
	private JImageDownloadDialog imageDownloadDialog;

	SetCoverFromDownload(IResourceHandler resourceHandler) {
		super(resourceHandler);
		String name = Bundle.getString("SetCoverFromDownloadAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("image_websearch_16.png"));		
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("image_websearch_22.png"));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}

	@Override
	public JImageDownloadDialog doOnce() {
		if(imageDownloadDialog == null) {
			final MainController controller = MainController.getController();
			imageDownloadDialog = new JImageDownloadDialog(controller.getMainWindow(), ImageWebSearchFetcherFactory.getInstance());
			
			//default search phrase
			String searchPhrase = "";
			EbookPropertyItem item = ListUtils.first(EbookPropertyItemUtils.getEbookPropertyItemByResource(resourceHandler));

			//no author in the search term for now. Big Book Search did not like it.
			if(item != null) {
				String authors = item.getAuthor() != null ? item.getAuthor().replace(IDBObject.LIST_SEPARATOR_CHAR, " ") : null;
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
	public void setDoOnceResult(JImageDownloadDialog result) {
		this.imageDownloadDialog = result;
		if(this.imageDownloadDialog != null) {
			IResourceHandler selectedImage = imageDownloadDialog.getSelectedImage();
			if(selectedImage != null) {
				setDialogOption(JFileChooser.APPROVE_OPTION);
				setDialogResult(selectedImage);
			} else {
				setDialogOption(JFileChooser.CANCEL_OPTION);
			}
		}
	}

}
