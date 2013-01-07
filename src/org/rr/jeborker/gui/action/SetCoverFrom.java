package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.event.RefreshAbstractAction;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.IMetadataWriter;
import org.rr.jeborker.metadata.MetadataHandlerFactory;

abstract class SetCoverFrom<T> extends RefreshAbstractAction implements IDoOnlyOnceAction<T> {

	private static final long serialVersionUID = 4772310971481868593L;

	protected final IResourceHandler resourceHandler;
	
	private int dialogOption; //JFileChooser.APPROVE
	
	private IResourceHandler dialogResult;

	private int index;

	private int max;
	
	SetCoverFrom(IResourceHandler resourceHandler) {
		this.resourceHandler = resourceHandler;
		putValue(Action.NAME, Bundle.getString("SetCoverFromFileAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("image_16.gif")));
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		final MainController controller = MainController.getController();
		try {
			savePendingMetadata(evt);
			
			final IMetadataWriter writer = MetadataHandlerFactory.getWriter(resourceHandler);
			List<EbookPropertyItem> items = EbookPropertyItemUtils.getEbookPropertyItemByResource(resourceHandler);
			
			if(!items.isEmpty()) {
				final EbookPropertyItem item = items.get(0);
				
				this.doOnce();
				if (this.dialogOption == JFileChooser.APPROVE_OPTION) {
					if(dialogResult!=null) {
						controller.getProgressMonitor().monitorProgressStart(Bundle.getFormattedString("SetMetadataCoverAction.message", dialogResult.getName(), item.toString()));
						controller.getProgressMonitor().setProgress(index, max);
						
						byte[] content = dialogResult.getContent();
						if(content != null) {
							writer.setCover(dialogResult.getContent());
							ActionUtils.refreshEbookPropertyItem(item, resourceHandler);
						}
										
						MainController.getController().refreshTableRows(getSelectedRowsToRefresh(), true);
						MainController.getController().setImageViewerResource(dialogResult);
					}
				}
			} else {
				LoggerFactory.logInfo(this, "No database item found for " + resourceHandler.getName(), null);
			}
		} catch (FileNotFoundException e) {
			LoggerFactory.logWarning(this, "Cover file for " + resourceHandler.getName() + " couldn't be loaded.", e);
		}  catch (Exception e) {
			LoggerFactory.logWarning(this, "Could not set cover for " + resourceHandler.getName(), e);
		} finally {
			controller.getProgressMonitor().monitorProgressStop(null);
		}
	}
	
	/**
	 * If the user has changed something in the metadata sheet, store the changes before continue
	 * with the cover. Otherwise the entered data get lost.
	 */
	private void savePendingMetadata(ActionEvent evt) {
		final ApplicationAction action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null);
		action.actionPerformed(evt);		
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
	public void prepareFor(int index, int max) {
		this.index = index;
		this.max = max;
	}

	public int getDialogOption() {
		return dialogOption;
	}

	public void setDialogOption(int dialogOption) {
		this.dialogOption = dialogOption;
	}

	public IResourceHandler getDialogResult() {
		return dialogResult;
	}

	public void setDialogResult(IResourceHandler dialogResult) {
		this.dialogResult = dialogResult;
	}	

}
