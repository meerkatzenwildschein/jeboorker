package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.app.BasePathList;
import org.rr.jeborker.app.FileRefreshBackground;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.RenameFileController;


class RenameFileAction extends AbstractAction {

	private String file;

	private RenameFileController renameFileController = MainController.getController().getRenameFileController();

	RenameFileAction(String text) {
		this.file = text;
		String name = Bundle.getString("RenameFileAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MainController controller = MainController.getController();
		List<Entry<EbookPropertyItem, IResourceHandler>> renameFiles = Collections.emptyList();
		if (StringUtil.isEmpty(file)) {
			List<EbookPropertyItem> selectedEbookPropertyItems = controller.getSelectedEbookPropertyItems();
			if (!selectedEbookPropertyItems.isEmpty()) {
				renameFiles = openRenameFileDialogWithEbookPropertyItems(selectedEbookPropertyItems);
			} else {
				List<IResourceHandler> selectedEbookPropertyResources = controller.getMainTreeHandler().getSelectedTreeItems();
				renameFiles = openRenameFileDialogWithIResources(selectedEbookPropertyResources);
			}
		} else {
			List<IResourceHandler> selectedEbookPropertyResources = Arrays.asList(ResourceHandlerFactory.getResourceHandler(file));
			renameFiles = openRenameFileDialogWithIResources(selectedEbookPropertyResources);
		}
		doRenameFiles(renameFiles);
	}

	private void doRenameFiles(final List<Entry<EbookPropertyItem, IResourceHandler>> renameFiles) {
		FileRefreshBackground.runWithDisabledRefresh(new Runnable() {

			@Override
			public void run() {
				BasePathList basePathList = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getBasePath();
				LinkedList<IResourceHandler> toRefreshInFileSystemTree = new LinkedList<>();
				for (Entry<EbookPropertyItem, IResourceHandler> toRename : renameFiles) {
					EbookPropertyItem source = toRename.getKey();
					IResourceHandler sourceResourceHandler = source.getResourceHandler();
					IResourceHandler targetResourceHandler = toRename.getValue();

					try {
						if(sourceResourceHandler.equals(targetResourceHandler)) {
							continue;
						}

						if (targetResourceHandler.exists() && !renameFileController.isOverwriteExistingFiles()) {
							String message = Bundle.getFormattedString("RenameFileAction.overwrite.message", source.getFileName(), targetResourceHandler.getName());
							String title = Bundle.getString("RenameFileAction.overwrite.title");
							int value = MainController.getController().showMessageBox(message, title, JOptionPane.YES_NO_OPTION, "RenameFileActionKey",
									JOptionPane.YES_OPTION, true);
							if (value != 0) {
								continue;
							}
						}
						sourceResourceHandler.moveTo(targetResourceHandler, true);

						if(basePathList.containsBasePathFor(source.getBasePath())) {
							EbookPropertyItemUtils.renameCoverThumbnail(sourceResourceHandler, targetResourceHandler);
							DefaultDBManager.getInstance().deleteObject(source);
							source.setFile(targetResourceHandler.getResourceString());
							DefaultDBManager.getInstance().storeObject(source);
						} else {
							source.setFile(targetResourceHandler.getResourceString());
							toRefreshInFileSystemTree.add(sourceResourceHandler.getParentResource());
							toRefreshInFileSystemTree.add(targetResourceHandler.getParentResource());
						}
					} catch (IOException e) {
						LoggerFactory.getLogger().log(Level.SEVERE, "Rename of file " + source + " to " + targetResourceHandler + " has failed.", e);
					}
				}
				MainController.getController().refreshTableSelectedItem(true);
				for(IResourceHandler toRefresh : toRefreshInFileSystemTree) {
					MainController.getController().getMainTreeHandler().refreshFileSystemTreeEntry(toRefresh);
				}
			}
		});
	}

	private List<Entry<EbookPropertyItem, IResourceHandler>> openRenameFileDialogWithEbookPropertyItems(List<EbookPropertyItem> toRename) {
		renameFileController.showDialog(toRename);
		if (renameFileController.isConfirmed()) {
			return renameFileController.getValues();
		}
		return Collections.emptyList();
	}

	private List<Entry<EbookPropertyItem, IResourceHandler>> openRenameFileDialogWithIResources(List<IResourceHandler> toRename) {
		List<EbookPropertyItem> ebookPropertyItems = getEbookPropertyItems(toRename);
		return openRenameFileDialogWithEbookPropertyItems(ebookPropertyItems);
	}

	private List<EbookPropertyItem> getEbookPropertyItems(final List<IResourceHandler> toRename) {
		MainController.getController().getProgressMonitor().monitorProgressStart(Bundle.getString("RenameFileAction.readingMetadata"));
		try {
			return new ArrayList<EbookPropertyItem>() {{
				for(IResourceHandler resourceLoader : toRename) {
					List<EbookPropertyItem> ebookPropertyItemByResource = EbookPropertyItemUtils.getEbookPropertyItemByResource(resourceLoader);
					if (ebookPropertyItemByResource.isEmpty()) {
						MainController.getController().getProgressMonitor().setMessage(Bundle.getFormattedString("RenameFileAction.readingMetadataEntry", resourceLoader.getName()));
						add(EbookPropertyItemUtils.createEbookPropertyItem(resourceLoader, null));
					} else {
						add(ebookPropertyItemByResource.get(0));
					}
				}
			}};
		} finally {
			MainController.getController().getProgressMonitor().monitorProgressStop();
		}
	}

}
