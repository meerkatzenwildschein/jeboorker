package org.rr.jeborker.gui.action;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.event.RefreshAbstractAction;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.PlainMetadataEditorController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class EditPlainMetadataAction extends RefreshAbstractAction {
	
	private static final long serialVersionUID = 108236926726922222L;
	
	final IResourceHandler resourceHandler;
	
	EditPlainMetadataAction(final IResourceHandler handler) {
		String additionalName = EMPTY;
		this.resourceHandler = handler;
		if(handler != null) {
			String handlerMimeType = handler.getMimeType(true);
			if(handler != null && handlerMimeType != null && handlerMimeType.equals(JeboorkerConstants.SUPPORTED_MIMES.MIME_PDF.getMime())) {
				additionalName = "XMP";
			} else if(handler != null && handlerMimeType != null && handlerMimeType.equals(JeboorkerConstants.SUPPORTED_MIMES.MIME_HTML.getMime())) {
				additionalName = "HTML";
			} else {
				additionalName = "XML";
			}
		} else {
			setEnabled(false);
		}
		
		String name = Bundle.getFormattedString("EditPlainMetadataAction.name", additionalName);
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("edit_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("edit_22.png"));
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final MainController controller = MainController.getController();
		controller.getProgressMonitor().monitorProgressStart(Bundle.getString("EditPlainMetadataAction.message"));
		
		Exception exception = null;
		try {
			PlainMetadataEditorController instance = PlainMetadataEditorController.getInstance(resourceHandler, getSelectedRowsToRefresh());
			instance.showXMLMetadataDialog();
		} catch(Exception ex) {
			exception = ex;
			controller.getProgressMonitor().monitorProgressStop(ex.getMessage());
		} finally {
			if(exception==null) {
				controller.getProgressMonitor().monitorProgressStop(EMPTY);
			}
		}
		
	}

}
