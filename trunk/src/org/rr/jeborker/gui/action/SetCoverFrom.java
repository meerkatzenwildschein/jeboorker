package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.event.ApplicationEvent;
import org.rr.jeborker.event.EventManager;
import org.rr.jeborker.event.RefreshAbstractAction;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.model.EbookSheetPropertyModel;
import org.rr.jeborker.gui.resources.ImageResourceBundle;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

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
		putValue(Action.SMALL_ICON, new ImageIcon(ImageResourceBundle.getResource("image_16.png")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(ImageResourceBundle.getResource("image_22.png")));
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		final MainController controller = MainController.getController();
		try {
			List<EbookPropertyItem> items = EbookPropertyItemUtils.getEbookPropertyItemByResource(resourceHandler);
			
			if(!items.isEmpty()) {
				final EbookPropertyItem item = items.get(0);
				
				this.doOnce();
				if (this.dialogOption == JFileChooser.APPROVE_OPTION) {
					if(dialogResult != null) {
						controller.getProgressMonitor().monitorProgressStart(Bundle.getFormattedString("SetMetadataCoverAction.message", dialogResult.getName(), item.toString()));
						controller.getProgressMonitor().setProgress(index, max);
						
						byte[] content = dialogResult.getContent();
						String imageFileName = dialogResult.getName();
						if(content != null) {
							setupCoverMetadataToModel(content, items);
						}
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
	
	private void setupCoverMetadataToModel(byte[] content, List<EbookPropertyItem> items) {
		final MainController controller = MainController.getController();
		final EbookSheetPropertyModel model = controller.getEbookSheetPropertyModel();
		final IMetadataReader metadataReader = model.getMetadataReader();
		
		boolean isNewCoverProperty = false;
		List<MetadataProperty> allMetaData = model.getAllMetaData();
		List<MetadataProperty> coverMetadata = metadataReader.getMetadataByType(false, allMetaData, IMetadataReader.METADATA_TYPES.COVER);
		if(coverMetadata.isEmpty()) {
			coverMetadata = metadataReader.getMetadataByType(true, model.getAllMetaData(), IMetadataReader.METADATA_TYPES.COVER);
			isNewCoverProperty = true;
		}
		
		if(coverMetadata != null && !coverMetadata.isEmpty()) {
			MetadataProperty coverMetadataProperty = coverMetadata.get(0);
			coverMetadataProperty.setValue(content, 0);
			if(isNewCoverProperty) {
				ActionUtils.addMetadataItem(coverMetadataProperty, items.get(0));
			} else {
				EventManager.fireEvent(EventManager.EVENT_TYPES.METADATA_SHEET_CONTENT_CHANGE, new ApplicationEvent(controller.getSelectedEbookPropertyItems(), null, this));
			}
			model.setChanged(true);
		} else {
			LoggerFactory.getLogger().log(Level.INFO, "Seems tehere is no cover support for " + resourceHandler);
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
