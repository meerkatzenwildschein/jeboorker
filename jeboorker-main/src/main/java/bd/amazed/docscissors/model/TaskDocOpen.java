package bd.amazed.docscissors.model;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.SwingWorker;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;

import bd.amazed.docscissors.doc.DocumentCropper;
import bd.amazed.docscissors.doc.DocumentInfo;

public class TaskDocOpen extends SwingWorker<List<PageGroup>, Void> {

	private DocumentInfo docFile;
	private IResourceHandler originalFile;
	private int groupType;
	private boolean isCancelled;
	private DocumentCropper cropper = null;
	private boolean shouldCreateStackView;

	public TaskDocOpen(IResourceHandler file, int groupType, boolean shouldCreateStackView) {
		this.originalFile = file;
		isCancelled = false;
		this.groupType = groupType;
		this.shouldCreateStackView = shouldCreateStackView;
	}

	@Override
	protected List<PageGroup> doInBackground() throws Exception {
		cropper = DocumentCropper.getCropper(originalFile);
		docFile = cropper.getDocumentInfo();

		List<PageGroup> pageGroups = PageGroup.createGroup(groupType, docFile.getPageCount());

		if (shouldCreateStackView && groupType != PageGroup.GROUP_TYPE_INDIVIDUAL) {
			setProgress(0);
			PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
				}
			};

			for (int i = 0; i < pageGroups.size(); i++) {
				PageGroup pageGroup = pageGroups.get(i);
				BufferedImage image = cropper.getNormalizedImage(docFile, propertyChangeListener, pageGroup);

				if (image == null) {
					debug("Ups.. null image for " + docFile.getOriginalFile());
				} else {
					debug("Document loaded " + pageGroup + " from " + docFile.getOriginalFile());
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
			List<PageGroup> pageGroups = null;
			try {
				pageGroups = this.get();
				if (pageGroups != null && !isCancelled) {
					Model.getInstance().setDoc(docFile, pageGroups);
				} else {
					Model.getInstance().setDocLoadFailed(originalFile, new bd.amazed.docscissors.doc.ScissorsDocumentException("Failed to extract image. Check if PDF is password protected or corrupted."));
				}
			} catch (InterruptedException e) {
				e.printStackTrace(); // ignore
			} catch (ExecutionException e) {
				Model.getInstance().setDocLoadFailed(originalFile, e.getCause());
			}
		}
	}

	private void debug(String string) {
		LoggerFactory.getLogger(TaskDocOpen.class).log(Level.INFO, string);
	}

}
