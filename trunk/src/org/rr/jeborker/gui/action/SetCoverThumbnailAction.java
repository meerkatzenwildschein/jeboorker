package org.rr.jeborker.gui.action;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.dialogs.ImageFileChooser;
import org.rr.commons.utils.ArrayUtils;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.event.RefreshAbstractAction;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.IMetadataWriter;
import org.rr.jeborker.metadata.MetadataHandlerFactory;

class SetCoverThumbnailAction extends RefreshAbstractAction implements IDoOnlyOnceAction<ImageFileChooser> {

	private static final long serialVersionUID = 4772310971481868593L;

	private final IResourceHandler resourceHandler;
	
	private ImageFileChooser imageFileChooserT;

	private int index;

	private int max;
	
	private static File previousSelectedFile;
	
	SetCoverThumbnailAction(IResourceHandler resourceHandler) {
		this.resourceHandler = resourceHandler;
		putValue(Action.NAME, Bundle.getString("AddCoverThumbnailAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("image_jpeg_16.gif")));
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		try {
			final IMetadataWriter writer = MetadataHandlerFactory.getWriter(resourceHandler);
			List<EbookPropertyItem> items = DefaultDBManager.getInstance().getObject(EbookPropertyItem.class, "file", resourceHandler.toString());
			
			if(!items.isEmpty()) {
				final MainController controller = MainController.getController();
				final EbookPropertyItem item = items.get(0);
				
				ImageFileChooser imageFileChooserT = this.doOnce();
				if (imageFileChooserT.getReturnValue() == JFileChooser.APPROVE_OPTION) {
					File selectedFile = imageFileChooserT.getSelectedFile();
					if(selectedFile!=null) {
						controller.getProgressMonitor().monitorProgressStart(Bundle.getFormattedString("SetMetadataCoverAction.message", selectedFile.getName(), item.toString()));
						controller.getProgressMonitor().setProgress(index, max);
						
						IResourceHandler selectedFileResourceHandler = ResourceHandlerFactory.getResourceLoader(selectedFile);
						writer.setCover(selectedFileResourceHandler.getContent());
						RefreshBasePathAction.refreshEbookPropertyItem(item, resourceHandler);
										
						MainController.getController().refreshTableRows(getSelectedRowsToRefresh(), true);
						MainController.getController().setImageViewerResource(selectedFileResourceHandler);
						controller.getProgressMonitor().monitorProgressStop(null);
					}
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
	public synchronized ImageFileChooser doOnce() {
		if(imageFileChooserT == null) {
			imageFileChooserT = new ImageFileChooser();
			imageFileChooserT.setVisible(true);
			if(previousSelectedFile!=null) {
				imageFileChooserT.setSelectedFile(previousSelectedFile);
			}
			imageFileChooserT.showOpenDialog(ArrayUtils.get(Frame.getFrames(), 0));
			previousSelectedFile = imageFileChooserT.getSelectedFile();
		}
		
		return this.imageFileChooserT;
	}

	@Override
	public void setDoOnceResult(ImageFileChooser result) {
		this.imageFileChooserT = result;
	}
	
	@Override
	public void prepareFor(int index, int max) {
		this.index = index;
		this.max = max;
	}	

}
