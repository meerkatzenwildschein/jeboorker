package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.converter.ConverterFactory;
import org.rr.jeborker.converter.IEBookConverter;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class ConvertEbookAction extends AbstractAction implements IFinalizeAction {

	private static final long serialVersionUID = -6464113132395695332L;
	
	private String book;
	
	private EbookPropertyItem newEbookPropertyItem;
	
	private int row = 0;

	ConvertEbookAction(String text) {
		this.book = text;
//		String name = Bundle.getString("OpenFileAction.name");
//		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("convert_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("convert_16.png"));		
//		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final MainController controller = MainController.getController();
		final IResourceHandler resource = ResourceHandlerFactory.getResourceHandler(book);
		final Class<?> converterClass = (Class<?>) getValue("converterClass");
		final IEBookConverter converter = ConverterFactory.getConverterbyClass(converterClass, resource);
		final int[] selectedRowsToRefresh = (int[]) getValue(MultiActionWrapper.SELECTED_ROWS_TO_REFRESH_KEY);
		
		try {
			controller.getProgressMonitor().monitorProgressStart(Bundle.getFormattedString("ConvertEbookAction.message", resource.getName()));
			
			IResourceHandler targetResourceLoader = converter.convert();
			EbookPropertyItem sourceItem = null;
			for(int rowIndex : selectedRowsToRefresh) {
				EbookPropertyItem ebookPropertyItemAt = controller.getTableModel().getEbookPropertyItemAt(rowIndex);
				if(ebookPropertyItemAt.getResourceHandler().equals(resource)) {
					sourceItem = ebookPropertyItemAt;
					row = rowIndex;
					break;
				}
			}
			
			this.newEbookPropertyItem = EbookPropertyItemUtils.createEbookPropertyItem(targetResourceLoader, ResourceHandlerFactory.getResourceHandler(sourceItem.getBasePath()));
		} catch (Exception ex) {
			LoggerFactory.logWarning((Object) this, "", ex);
		} finally {
			controller.getProgressMonitor().monitorProgressStop();
		}
	}

	@Override
	public void finalizeAction(int count) {
		if(newEbookPropertyItem != null) {
			ActionUtils.addEbookPropertyItem(newEbookPropertyItem, row + 1 + count);
		}
	}

}
