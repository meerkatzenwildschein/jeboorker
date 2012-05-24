package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFileChooser;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.swing.dialogs.SimpleInputDialog;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.IMetadataWriter;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

public class SetMetadataGenreAction extends ASetCommonMetadataAction implements IDoOnlyOnceAction<SimpleInputDialog> {

	private static final long serialVersionUID = 4772310971481868593L;

	private final IResourceHandler resourceHandler;
	
	private SimpleInputDialog inputDialog;
	
	SetMetadataGenreAction(IResourceHandler resourceHandler) {
		this.resourceHandler = resourceHandler;
		putValue(Action.NAME, Bundle.getString("SetMetadataGenreAction.name"));
//		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("set_author_16.gif")));
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
					final String genre = inputDialog.getInput();
					
					//get author metadata an set the entered author.
					List<MetadataProperty> readMetaData = reader.readMetaData();
					List<MetadataProperty> genreMetaData = reader.getGenreMetaData(true, readMetaData);
					
					transferValueToMetadata(genre, genreMetaData);

					mergeAndWrite(writer, readMetaData, genreMetaData);
					
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
			inputDialog = new SimpleInputDialog(MainController.getController().getMainWindow(), "Genre", true);
			inputDialog.setDialogMode(SimpleInputDialog.OK_CANCEL_DIALOG);
			inputDialog.setQuestionTest(Bundle.getString("SetMetadataGenreAction.input.text"));
			inputDialog.setSize(305,160);
			inputDialog.setVisible(true);
		}
		
		return inputDialog;
	}

	@Override
	public void setResult(SimpleInputDialog result) {
		this.inputDialog = result;
	}

}
