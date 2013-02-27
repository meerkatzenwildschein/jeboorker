package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.rr.common.swing.SwingUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class DeleteFileAction extends AbstractAction implements IDoOnlyOnceAction<Integer> {

	private static final long serialVersionUID = -6464113132395695332L;
	
	private Integer result = null;
	
	final IResourceHandler fileToDelete;

	DeleteFileAction(final String text) {
		this(ResourceHandlerFactory.getResourceHandler(text));
	}
	
	public DeleteFileAction(final IResourceHandler resourceLoader) {
		this.fileToDelete = resourceLoader;
		String name = Bundle.getString("DeleteFileAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("delete_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("delete_22.png"));	
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if(this.doOnce().intValue() == JOptionPane.YES_OPTION) {
				if(fileToDelete == null) {
					//delete selected files if no ones are specified
					List<EbookPropertyItem> selectedEbookPropertyItems = MainController.getController().getSelectedEbookPropertyItems();
					for(EbookPropertyItem item : selectedEbookPropertyItems) {
						IResourceHandler resourceHandler = item.getResourceHandler();
						doDelete(resourceHandler);
					}
				} else {
					doDelete(fileToDelete);
				}
			}
		} catch (Exception e1) {
			LoggerFactory.logWarning(this, "could not delete file " + fileToDelete, e1);
		}
	}
	
	private static void doDelete(IResourceHandler fileToDelete) throws IOException {
		if(!fileToDelete.moveToTrash()) {
			fileToDelete.delete();
			if(fileToDelete.exists()) {
				LoggerFactory.logWarning(DeleteFileAction.class, "could not delete file " + fileToDelete);
			} else {
				ActionUtils.refreshEntry(fileToDelete);
			}
		} else {
			ActionUtils.refreshEntry(fileToDelete);
		}		
	}
	
	@Override
	public Integer doOnce() {
		if(this.result == null) {
			String message = Bundle.getString("DeleteFileAction.delete.message");
			String title = Bundle.getString("DeleteFileAction.name");
			int value = MainController.getController().showMessageBox(message, title, JOptionPane.YES_NO_OPTION, "DeleteFileActionKey", JOptionPane.YES_OPTION);
			this.result = Integer.valueOf(value);
		}
		return result;
	}

	@Override
	public void setDoOnceResult(Integer result) {
		this.result = result;
	}

	@Override
	public void prepareFor(int index, int size) {
		//Not needed
	}
	

}
