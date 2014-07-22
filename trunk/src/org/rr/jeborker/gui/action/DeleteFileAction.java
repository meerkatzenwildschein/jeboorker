package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.app.FileWatchService;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class DeleteFileAction extends AbstractAction implements IDoOnlyOnceAction<Integer>, IFinalizeAction {

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
					if(!selectedEbookPropertyItems.isEmpty()) {
						for(EbookPropertyItem item : selectedEbookPropertyItems) {
							IResourceHandler resourceHandler = item.getResourceHandler();
							doDelete(resourceHandler);
						}
					} else {
						List<IResourceHandler> selectedTreeItems = MainController.getController().getMainTreeHandler().getSelectedTreeItems();
						for(IResourceHandler resourceHandler : selectedTreeItems) {
							doDelete(resourceHandler);
						}
					}
				} else {
					doDelete(fileToDelete);
				}
			}
		} catch (Exception e1) {
			LoggerFactory.logWarning(this, "could not delete file " + fileToDelete, e1);
		}
	}

	private void doDelete(IResourceHandler fileToDelete) throws IOException {
		try {
			if(fileToDelete.isDirectoryResource()) {
				if(!deleteFolderIfItIsNotEmpty(fileToDelete)) {
					return;
				}
				FileWatchService.removeWatchPath(fileToDelete.toString());
			}

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
		} catch(java.io.FileNotFoundException e) {
			LoggerFactory.logWarning(DeleteFileAction.class, "File is already deleted " + fileToDelete, e);
		}
	}

	/**
	 * Asks the user if he really want to delete the folder if it's not empty.
	 * @param folderToDelete The folder which should be tested if it's empty.
	 * @return <code>true</code> if the folder is empty and <code>false</code> otherwise.
	 */
	private boolean deleteFolderIfItIsNotEmpty(IResourceHandler folderToDelete) {
		if(folderToDelete != null && !folderToDelete.isEmpty()) {
			String title = Bundle.getString("DeleteFileAction.dirNotEmpty.title");
			String message = Bundle.getFormattedString("DeleteFileAction.dirNotEmpty.message", folderToDelete.toString());
			int deleteFolder = MainController.getController().showMessageBox(message, title, JOptionPane.YES_NO_OPTION, "DeleteFolderActionKey", JOptionPane.YES_OPTION, true);
			if(deleteFolder == JOptionPane.YES_OPTION) {
				return true;
			}
			return false;
		}
		return true;
	}

	@Override
	public Integer doOnce() {
		if(this.result == null) {
			String message = Bundle.getString("DeleteFileAction.delete.message");
			String title = Bundle.getString("DeleteFileAction.name");
			int value = MainController.getController().showMessageBox(message, title, JOptionPane.YES_NO_OPTION, "DeleteFileActionKey", JOptionPane.YES_OPTION, true);
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

	@Override
	public void finalizeAction(int count) {
	}


}
