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

public class SetCoverThumbnailAction extends RefreshAbstractAction {

	private static final long serialVersionUID = 4772310971481868593L;

	private final IResourceHandler resourceHandler;
	
	private static File selectedFile = null;
	
	SetCoverThumbnailAction(IResourceHandler resourceHandler) {
		this.resourceHandler = resourceHandler;
		putValue(Action.NAME, Bundle.getString("AddCoverThumbnailAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("image_jpeg_16.gif")));
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		try {
			final IMetadataWriter writer = MetadataHandlerFactory.getWriter(resourceHandler);
			final ImageFileChooser imageFileChooser = new ImageFileChooser();
			List<EbookPropertyItem> items = DefaultDBManager.getInstance().getObject(EbookPropertyItem.class, "file", resourceHandler.toString());
			
			if(!items.isEmpty()) {
				EbookPropertyItem item = items.get(0);
				imageFileChooser.setVisible(true);
				if(selectedFile!=null) {
					imageFileChooser.setSelectedFile(selectedFile);
				}
				int returnVal = imageFileChooser.showOpenDialog(ArrayUtils.get(Frame.getFrames(), 0));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					selectedFile = imageFileChooser.getSelectedFile();
					if(selectedFile!=null) {
						IResourceHandler selectedFileResourceHandler = ResourceHandlerFactory.getResourceLoader(selectedFile);
						writer.setCover(selectedFileResourceHandler.getContent());
						RefreshBasePathAction.refreshEbookPropertyItem(item, resourceHandler);
										
						MainController.getController().refreshTableRows(getSelectedRowsToRefresh());
						MainController.getController().setImageViewerResource(selectedFileResourceHandler);
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

}
