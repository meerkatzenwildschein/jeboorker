package bd.amazed.pdfscissors.model;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.SwingWorker;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;

public class TaskPdfOpen extends SwingWorker<Vector<PageGroup>, Void> {

	private PdfFile pdfFile;
	private IResourceHandler originalFile;
	private int groupType;
	private boolean isCancelled;
	private PdfCropper cropper = null;
	private boolean shouldCreateStackView;

	public TaskPdfOpen(IResourceHandler file, int groupType, boolean shouldCreateStackView) {
		this.originalFile = file;
		isCancelled = false;
		this.groupType = groupType;
		this.shouldCreateStackView = shouldCreateStackView;
	}

	@Override
	protected Vector<PageGroup> doInBackground() throws Exception {
		pdfFile = PdfCropper.getNormalizedPdf(originalFile);
		pdfFile.getNormalizedFile().deleteOnExit();

		cropper = new PdfCropper(pdfFile.getNormalizedFile());

		Vector<PageGroup> pageGroups = PageGroup.createGroup(groupType, pdfFile.getPageCount());

		if (shouldCreateStackView && groupType != PageGroup.GROUP_TYPE_INDIVIDUAL) {
			setProgress(0);
			PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
				}
			};

			for (int i = 0; i < pageGroups.size(); i++) {
				PageGroup pageGroup = pageGroups.elementAt(i);
				BufferedImage image = cropper.getImage(propertyChangeListener, pageGroup);

				if (image == null) {
					debug("Ups.. null image for " + pdfFile.getNormalizedFile());
				} else {
					debug("PDF loaded " + pageGroup + " from " + pdfFile.getNormalizedFile());
				}
				pageGroup.setStackImage(image);

			}
			setProgress(100);
		}
		return pageGroups;
	}

	public void cancel() {
		isCancelled = true;
		if (this.cropper != null) {
			cropper.cancel();
		}
	}

	@Override
	protected void done() {
		super.done();
		setProgress(100);
		firePropertyChange("done", false, true);
		if (!isCancelled) {
			Vector<PageGroup> pageGroups = null;
			try {
				pageGroups = this.get();
				if (pageGroups != null && !isCancelled) {
					Model.getInstance().setPdf(pdfFile, pageGroups);
				} else {
					Model.getInstance().setPdfLoadFailed(originalFile, new bd.amazed.pdfscissors.pdf.PdfException("Failed to extract image. Check if PDF is password protected or corrupted."));
				}
			} catch (InterruptedException e) {
				e.printStackTrace(); // ignore
			} catch (ExecutionException e) {
				Model.getInstance().setPdfLoadFailed(originalFile, e.getCause());
			}
		}
	}

	private void debug(String string) {
		LoggerFactory.getLogger(TaskPdfOpen.class).log(Level.INFO, string);
	}

}
