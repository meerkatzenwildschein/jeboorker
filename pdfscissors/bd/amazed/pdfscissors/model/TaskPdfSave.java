package bd.amazed.pdfscissors.model;

import java.awt.Component;
import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.io.FilenameUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.app.FileRefreshBackground;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.action.ActionUtils;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;

import bd.amazed.pdfscissors.pdf.DocumentCropper;
import bd.amazed.pdfscissors.pdf.DocumentInfo;

import com.j256.ormlite.stmt.Where;

public class TaskPdfSave extends SwingWorker<Boolean, Void> {

	private static final String QUERY_IDENTIFER = TaskPdfSave.class.getName();
	private DocumentInfo pdfFile;
	private File targetFile;
	PageRectsMap pageRectsMap;
	private int viewWidth;
	private int viewHeight;
	private Component owner;
	private boolean targetFileExists;

	ProgressMonitor progressMonitor;

	public TaskPdfSave(DocumentInfo pdfFile, File targetFile, PageRectsMap pageRectsMap, int viewWidth, int viewHeight,
			Component owner) {
		this.pdfFile = pdfFile;
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
			DocumentCropper.getCropper(pdfFile.getOriginalFile()).cropPdf(pdfFile, targetFile, pageRectsMap, viewWidth, viewHeight, progressMonitor);
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
					IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(targetFile);
					String baseFolder = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getBasePathFor(resourceHandler);
					if(StringUtils.isNotEmpty(baseFolder)) {
						if(!this.targetFileExists) { //Do not add again
							addToDatabase(resourceHandler, ResourceHandlerFactory.getResourceHandler(baseFolder));
						}
						applyFilter(resourceHandler);
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

	private void applyFilter(IResourceHandler targetResourceHandler) {
		final MainController controller = MainController.getController();
		final String sourcePdfFileName = FilenameUtils.removeExtension(pdfFile.getOriginalFile().getName());

		MainController.getController().getTableModel().addWhereCondition(new EbookPropertyDBTableModel.EbookPropertyDBTableModelQuery() {

			@Override
			public String getIdentifier() {
				return QUERY_IDENTIFER;
			}

			@Override
			public void appendQuery(Where<EbookPropertyItem, EbookPropertyItem> where) throws SQLException {
				where.like("fileName", sourcePdfFileName + "%");
			}

			@Override
			public boolean isVolatile() {
				return true;
			}
		});

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				controller.refreshTable();
			}
		});

	}

	private void addToDatabase(IResourceHandler resource, IResourceHandler baseFolder) {
		final EbookPropertyItem item = EbookPropertyItemUtils.createEbookPropertyItem(resource, baseFolder);
		DefaultDBManager.getInstance().storeObject(item);
		ActionUtils.addEbookPropertyItem(item);
	}

	private void debug(String string) {
		LoggerFactory.getLogger(TaskPdfSave.class).log(Level.INFO, string);
	}
}
