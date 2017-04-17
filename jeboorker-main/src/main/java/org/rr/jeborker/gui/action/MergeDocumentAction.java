package org.rr.jeborker.gui.action;

import static org.rr.commons.utils.ListUtils.join;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.collection.TransformValueList;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.MimeUtils;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.app.FileRefreshBackground;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.pdf.PDFUtils;

class MergeDocumentAction extends AbstractAction {

	MergeDocumentAction(String text) {
		String name = Bundle.getString("MergeDocumentAction.name");
		if (text == null) {
			putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		} else {
			putValue(Action.NAME, text);
		}
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final MainController controller = MainController.getController();
		final List<IResourceHandler> selectedEbookResources = new TransformValueList<EbookPropertyItem, IResourceHandler>(
				controller.getSelectedEbookPropertyItems()) {

			@Override
			public IResourceHandler transform(EbookPropertyItem source) {
				return source.getResourceHandler();
			}
		};

		MainController.getController().getProgressMonitor().monitorProgressStart("Merging files");
		if (!selectedEbookResources.isEmpty()) {
			final IResourceHandler uniqueResourceHandler = ResourceHandlerFactory.getUniqueResourceHandler(selectedEbookResources.get(0), "merged", null);
			try {
				FileRefreshBackground.setDisabled(true);
				if(uniqueResourceHandler.getMimeType(false).equals(MimeUtils.MIME_PDF)) {
					PDFUtils.merge(selectedEbookResources, uniqueResourceHandler);
				} else if(uniqueResourceHandler.getMimeType(false).equals(MimeUtils.MIME_CBZ)) {
					PDFUtils.merge(selectedEbookResources, uniqueResourceHandler);
				}
				ActionUtils.refreshFileSystemResourceParent(uniqueResourceHandler.getParentResource());
				
				String baseFolder = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getBasePathFor(uniqueResourceHandler);
				if(StringUtil.isNotEmpty(baseFolder)) {
					addToDatabase(uniqueResourceHandler, ResourceHandlerFactory.getResourceHandler(baseFolder));
					applyFilter(uniqueResourceHandler);
				} else {
					controller.changeToFileModel(Collections.singletonList(uniqueResourceHandler));
				}
			} catch (Exception ex) {
				LoggerFactory.log(Level.WARNING, this, String.format("Failed to merge files %s", join(selectedEbookResources, ", ")), ex);
			} finally {
				MainController.getController().getProgressMonitor().monitorProgressStop("Files merged to " + uniqueResourceHandler);
				FileRefreshBackground.setDisabled(false);
			}
		}
	}

	private void addToDatabase(IResourceHandler resource, IResourceHandler baseFolder) {
		final EbookPropertyItem item = EbookPropertyItemUtils.createEbookPropertyItem(resource, baseFolder);
		ActionUtils.addAndStoreEbookPropertyItem(item);
	}
	
	private void applyFilter(IResourceHandler uniqueResourceHandler) {
		ActionUtils.applyFileNameFilter(Collections.singletonList(uniqueResourceHandler), true);
	}
}
