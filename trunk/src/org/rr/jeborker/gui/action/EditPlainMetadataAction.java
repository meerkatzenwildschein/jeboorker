package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.event.RefreshAbstractAction;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.PlainMetadataEditorController;

public class EditPlainMetadataAction extends RefreshAbstractAction {
	
	private static final long serialVersionUID = 108236926726922222L;
	
	final IResourceHandler resourceHandler;
	
	EditPlainMetadataAction(final IResourceHandler handler) {
		this.resourceHandler = handler;
		String additionalName = "";
		if(handler.getMimeType().equals("application/pdf")) {
			additionalName = "XMP";
		}
		
		putValue(Action.NAME, Bundle.getFormattedString("EditPlainMetadataAction.name", additionalName));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("edit_16.gif")));
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
				controller.getProgressMonitor().monitorProgressStop("");
			}
		}
		
	}

}
