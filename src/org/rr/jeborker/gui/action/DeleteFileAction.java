package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.ResourceHandlerUtils;
import org.rr.jeborker.gui.MainController;

class DeleteFileAction extends AbstractAction implements IDoOnlyOnceAction<Integer> {

	private static final long serialVersionUID = -6464113132395695332L;
	
	private Integer result = null;
	
	final IResourceHandler fileToDelete;

	DeleteFileAction(final String text) {
		this(ResourceHandlerFactory.getResourceLoader(text));
	}
	
	public DeleteFileAction(final IResourceHandler resourceLoader) {
		this.fileToDelete = resourceLoader;
		putValue(Action.NAME, Bundle.getString("DeleteFileAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("remove_16.gif")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(Bundle.getResource("remove_22.gif")));		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if(this.doOnce().intValue() == JOptionPane.YES_OPTION) {
				if(!ResourceHandlerUtils.moveToTrash(fileToDelete)) {
					fileToDelete.delete();
					if(fileToDelete.exists()) {
						LoggerFactory.logWarning(this.getClass(), "could not delete file " + fileToDelete);
					} else {
						ActionUtils.refreshEntry(fileToDelete);
					}
				}
			}
		} catch (Exception e1) {
			LoggerFactory.logWarning(this, "could not delete file " + fileToDelete, e1);
		}
	}
	
	@Override
	public Integer doOnce() {
		if(this.result == null) {
			int value = JOptionPane.showConfirmDialog(
					MainController.getController().getMainWindow(), 
					Bundle.getString("DeleteFileAction.delete.message"),
					Bundle.getString("DeleteFileAction.name"),
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
					);
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
