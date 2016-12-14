package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.converter.ConverterFactory;
import org.rr.jeborker.converter.IEBookConverter;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.ConverterPreferenceController;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class ConvertEbookAction extends AbstractAction implements IFinalizeAction, IDoOnlyOnceAction<ConverterPreferenceController> {

	private static final long serialVersionUID = -6464113132395695332L;
	
	private String book;
	
	private IResourceHandler bookResourceHandler;
	
	private EbookPropertyItem newEbookPropertyItem;
	
	private int row = 0;
	
	private ConverterPreferenceController converterPreferenceController;

	ConvertEbookAction(String text) {
		this.book = text;
		this.bookResourceHandler = ResourceHandlerFactory.getResourceHandler(book);
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("convert_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("convert_16.png"));		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final MainController controller = MainController.getController();
		final Class<?> converterClass = (Class<?>) getValue("converterClass");
		final IEBookConverter converter = ConverterFactory.getConverterbyClass(converterClass, bookResourceHandler);
		final int[] selectedRowsToRefresh = (int[]) getValue(MultiActionWrapper.SELECTED_ROWS_TO_REFRESH_KEY);
		
		try {
			if(!this.converterPreferenceController.isConfirmed()) {
				return;
			}
			controller.getProgressMonitor().monitorProgressStart(Bundle.getFormattedString("ConvertEbookAction.message", bookResourceHandler.getName()), false);
			
			converter.setConverterPreferenceController(this.converterPreferenceController);
			IResourceHandler targetResourceHandler = converter.convert();
			if(targetResourceHandler != null) {
				EbookPropertyItem sourceItem = null;
				for(int rowIndex : selectedRowsToRefresh) {
					EbookPropertyItem ebookPropertyItemAt = controller.getModel().getEbookPropertyItemAt(rowIndex);
					if(ebookPropertyItemAt.getResourceHandler().equals(bookResourceHandler)) {
						sourceItem = ebookPropertyItemAt;
						row = rowIndex;
						break;
					}
				}
				
				if(sourceItem != null) {
					IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(sourceItem.getBasePath());
					this.newEbookPropertyItem = EbookPropertyItemUtils.createEbookPropertyItem(targetResourceHandler, resourceHandler);
					controller.getMainTreeHandler().refreshFileSystemTreeEntry(resourceHandler.getParentResource());
				} else {
					LoggerFactory.getLogger(this).log(Level.SEVERE, "Failed to find " + bookResourceHandler + " in table.");
				}
			} else {
				LoggerFactory.getLogger(this).log(Level.INFO, "Converting " + bookResourceHandler + " aborted.");
			}
		} catch (Exception ex) {
			LoggerFactory.logWarning((Object) this, "Converting " + bookResourceHandler + " has failed.", ex);
		} finally {
			controller.getProgressMonitor().monitorProgressStop();
		}
	}

	@Override
	public void finalizeAction(int count) {
		if(newEbookPropertyItem != null) {
			ActionUtils.addAndStoreEbookPropertyItem(newEbookPropertyItem, row + 1 + count);
		}
	}

	@Override
	public ConverterPreferenceController doOnce() {
		final Class<?> converterClass = (Class<?>) getValue("converterClass");
		final IEBookConverter converter = ConverterFactory.getConverterbyClass(converterClass, bookResourceHandler);
		this.converterPreferenceController = converter.createConverterPreferenceController();
		if(converterPreferenceController != null && !this.converterPreferenceController.hasShown()) {
			this.converterPreferenceController.showPreferenceDialog();
		}
		return this.converterPreferenceController;
	}

	@Override
	public void setDoOnceResult(ConverterPreferenceController controller) {
		this.converterPreferenceController = controller;
	}

	@Override
	public void prepareFor(int index, int size) {
	}

}
