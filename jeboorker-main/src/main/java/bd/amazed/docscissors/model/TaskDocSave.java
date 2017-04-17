package bd.amazed.docscissors.model;

import java.awt.Component;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.app.FileRefreshBackground;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMonitor;
import org.rr.jeborker.gui.action.ActionUtils;

import bd.amazed.docscissors.doc.DocumentCropper;
import bd.amazed.docscissors.doc.DocumentInfo;

public class TaskDocSave extends SwingWorker<Boolean, Void> {

	private DocumentInfo docFile;
	private File targetFile;
	PageRectsMap pageRectsMap;
	private int viewWidth;
	private int viewHeight;
	private Component owner;
	private boolean targetFileExists;

	ProgressMonitor progressMonitor;

	public TaskDocSave(DocumentInfo docFile, File targetFile, PageRectsMap pageRectsMap, int viewWidth, int viewHeight,
			Component owner) {
		this.docFile = docFile;
		this.targetFile = targetFile;
		this.pageRectsMap = pageRectsMap;
		this.owner = owner;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.targetFileExists = targetFile.exists();
		progressMonitor = new ProgressMonitor(owner, "Saving " + targetFile.getName() + "...", "", 0, 100);
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		debug("Cropping to " + targetFile + "...");
		try {
			FileRefreshBackground.setDisabled(true);
			DocumentCropper.getCropper(docFile.getOriginalFile()).crop(docFile, targetFile, pageRectsMap, viewWidth, viewHeight, progressMonitor);
		} finally {
			FileRefreshBackground.setDisabled(false);
		}
		debug("Cropping success : " + targetFile);
		return true;
	}

	@Override
	protected void done() {
		super.done();
		progressMonitor.close();
		if (!progressMonitor.isCanceled()) {
			try {
				if (this.get()) {
					MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
					try {
						progressMonitor.blockMainFrame(true).setMessage("Saving file " + targetFile.getName());

						IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(targetFile);
						String baseFolder = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getBasePathFor(resourceHandler);
						if (StringUtil.isNotEmpty(baseFolder)) {
							if (!this.targetFileExists) { // Do not add again
								addToDatabase(resourceHandler, ResourceHandlerFactory.getResourceHandler(baseFolder));
							}
							applyFilter(Arrays.asList(resourceHandler, docFile.getOriginalFile()));
						} else {
							MainController.getController().changeToFileModel(Collections.singletonList(resourceHandler));
						}
						ActionUtils.refreshFileSystemResourceParent(resourceHandler.getParentResource());
					} finally {
						progressMonitor.blockMainFrame(false);
					}
				}
			} catch (InterruptedException e) {
				LoggerFactory.getLogger().log(Level.WARNING, "", e);
			} catch (ExecutionException e) {
				JOptionPane.showMessageDialog(owner, "Failed to save image ...\nDetails:" + e.getCause());
				LoggerFactory.getLogger().log(Level.WARNING, "Failed to save image ...\nDetails:" + e.getCause(), e);
			}
		}
	}

	private void applyFilter(List<IResourceHandler> targetResourceHandler) {
		ActionUtils.applyFileNameFilter(targetResourceHandler, true);
	}

	private void addToDatabase(IResourceHandler resource, IResourceHandler baseFolder) {
		EbookPropertyItem item = EbookPropertyItemUtils.createEbookPropertyItem(resource, baseFolder);
		ActionUtils.addAndStoreEbookPropertyItem(item);
	}

	private void debug(String string) {
		LoggerFactory.getLogger(TaskDocSave.class).log(Level.INFO, string);
	}
}
