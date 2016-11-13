package org.rr.jeborker.gui.action;

import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_CBR;
import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_CBZ;
import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_EPUB;
import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_MOBI;
import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_AZW;

import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.JFileChooser;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.net.imagefetcher.IImageFetcherFactory;
import org.rr.commons.net.imagefetcher.ImageMobiFileFetcherFactory;
import org.rr.commons.net.imagefetcher.ImageZipFileFetcherFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.dialogs.JImageDownloadDialog;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class SetCoverFromEbook extends SetCoverFrom<JImageDownloadDialog> implements IDoOnlyOnceAction<JImageDownloadDialog> {

	private static final long serialVersionUID = -6464113132395695332L;
	
	private JImageDownloadDialog imageDownloadDialog;

	SetCoverFromEbook(IResourceHandler resourceHandler) {
		super(resourceHandler);
		String name = Bundle.getString("SetCoverFromEbookAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("image_ebook_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("image_ebook_22.png"));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}

	@Override
	public JImageDownloadDialog doOnce() {
		if(imageDownloadDialog == null) {
			final MainController controller = MainController.getController();
			final EbookPropertyItem item = ListUtils.first(EbookPropertyItemUtils.getEbookPropertyItemByResource(resourceHandler));
			
			IImageFetcherFactory fetcher;
			if(item != null) {
				if(MIME_CBZ.getMime().equals(item.getMimeType()) || MIME_CBR.getMime().equals(item.getMimeType()) 
						|| MIME_EPUB.getMime().equals(item.getMimeType())) {
					fetcher = new ImageZipFileFetcherFactory(item.getResourceHandler());
				} else if(MIME_MOBI.getMime().equals(item.getMimeType()) || MIME_AZW.getMime().equals(item.getMimeType())) {
					fetcher = new ImageMobiFileFetcherFactory(item.getResourceHandler());
				} else {
					LoggerFactory.log(Level.WARNING, this, "No image fetcher instance for " + item.getResourceHandler());
					return null;
				}
				imageDownloadDialog = new JImageDownloadDialog(controller.getMainWindow(), fetcher);
				imageDownloadDialog.setVisible(true);
				
				IResourceHandler selectedImage = imageDownloadDialog.getSelectedImage();
				if(selectedImage != null) {
					setDialogOption(JFileChooser.APPROVE_OPTION);
					setDialogResult(selectedImage);
				} else {
					setDialogOption(JFileChooser.CANCEL_OPTION);
				}
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
