package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.swing.dialogs.SimpleInputDialog;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.event.RefreshAbstractAction;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.IMetadataWriter;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;
import org.rr.jeborker.metadata.MetadataUtils;

public class SetAuthorAction extends RefreshAbstractAction implements IDoOnlyOnceAction<SimpleInputDialog> {

	private static final long serialVersionUID = 4772310971481868593L;

	private final IResourceHandler resourceHandler;
	
	private SimpleInputDialog inputDialog;
	
	SetAuthorAction(IResourceHandler resourceHandler) {
		this.resourceHandler = resourceHandler;
		putValue(Action.NAME, Bundle.getString("SetAuthorAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("set_author_16.gif")));
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		try {
			List<EbookPropertyItem> items = DefaultDBManager.getInstance().getObject(EbookPropertyItem.class, "file", resourceHandler.toString());
			
			if(!items.isEmpty()) {
				final EbookPropertyItem item = items.get(0);
				SimpleInputDialog inputDialog = doOnce();
				if (inputDialog.getReturnValue() == JFileChooser.APPROVE_OPTION) {
					final IMetadataWriter writer = MetadataHandlerFactory.getWriter(resourceHandler);
					final IMetadataReader reader = MetadataHandlerFactory.getReader(resourceHandler);
					final String author = inputDialog.getInput();
					
					//get author metadata an set the entered author.
					List<MetadataProperty> readMetaData = reader.readMetaData();
					List<MetadataProperty> authorMetaData = reader.getAuthorMetaData(true, readMetaData);
					for (MetadataProperty authorMetadataProperty : authorMetaData) {
						List<String> authors = ListUtils.split(author, ',');
						for (int i = 0; i < authors.size(); i++) {
							authorMetadataProperty.setValue(authors.get(i), i);
						}
					}
					

					//Merge and write the metadata
					List<MetadataProperty> mergeMetadata = MetadataUtils.mergeMetadata(readMetaData, authorMetaData);
					writer.writeMetadata(mergeMetadata.iterator());
					
					//do some refresh to the changed entry.
					RefreshBasePathAction.refreshEbookPropertyItem(item, resourceHandler);
					MainController.getController().refreshTableRows(getSelectedRowsToRefresh(), true);
				}
			} else {
				LoggerFactory.logInfo(this, "No database item found for " + resourceHandler, null);
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "could not set cover for " + resourceHandler, e);
		}
	}
	
	/**
	 * Tells the factory if this writer is able to work with the 
	 * {@link IResourceHandler} given with the constructor.
	 * @return <code>true</code> if this action is able to do something or <code>false</code> otherwise.
	 */
	public static boolean canHandle(final IResourceHandler resourceHandler) {
		return MetadataHandlerFactory.hasCoverWriterSupport(resourceHandler);
	}

	@Override
	public synchronized SimpleInputDialog doOnce() {
		if(inputDialog == null) {
			inputDialog = new SimpleInputDialog(MainController.getController().getMainWindow(), "Authors name", true);
			inputDialog.setDialogMode(SimpleInputDialog.OK_CANCEL_DIALOG);
			inputDialog.setQuestionTest("Please enter the authors name");
			inputDialog.setSize(300,150);
			inputDialog.setVisible(true);
		}
		
		return inputDialog;
	}

	@Override
	public void setResult(SimpleInputDialog result) {
		this.inputDialog = result;
	}

}
