package org.rr.jeborker.gui.action;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.model.ReloadableTableModel;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class PasteFromClipboardAction extends AbstractAction implements ClipboardOwner {

	//source file to copy
	String target;

	PasteFromClipboardAction(String text) {
		this.target = text;
		String name = Bundle.getString("PasteFromClipboardAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("paste_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("paste_22.png"));
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(hasValidClipboardContent()) {
			final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			final Transferable contents = clipboard.getContents(null);
			try {
				final MainController controller = MainController.getController();
				if(target == null) {
					final int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();

					if(selectedEbookPropertyItemRows.length > 0) {
						importEbookFromClipboard(contents, selectedEbookPropertyItemRows[0]);
					}
				} else {
					final IResourceHandler targetRecourceDirectory = ResourceHandlerFactory.getResourceHandler(target);
					final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
					final List<String> basePaths = preferenceStore.getBasePath();

					boolean isImported = false;
					for(String basePath : basePaths) {
						if(target.startsWith(basePath)) {
							isImported = true;
							importEbookFromClipboard(contents, basePath, targetRecourceDirectory);
							break;
						}
					}
					if(!isImported) {
						//seems the target is not in the jeboorker area but in the file system. Just copy and refresh
						//the file system tree.
						final List<IResourceHandler> sourceFiles = ResourceHandlerFactory.getResourceHandler(contents);
						for(IResourceHandler source : sourceFiles) {
							IResourceHandler targetFileResource = targetRecourceDirectory.addPathStatement(source.getName());
							source.copyTo(targetFileResource, false);
							controller.getMainTreeHandler().refreshFileSystemTreeEntry(targetRecourceDirectory);
						}
					}
				}
			} catch (Exception ex) {
				LoggerFactory.log(Level.WARNING, this, "Failed to import file from Clipboard", ex);
			}
		}
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

	public static boolean hasValidClipboardContent() {
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		boolean hasResourceHandler = ResourceHandlerFactory.hasResourceHandler(clipboard.getContents(null));
		return hasResourceHandler;
	}

	/**
	 * Imports the file behind the given Transferable into JEboorker.
	 * @param t The transferable containing the data for the file import.
	 * @param row The row for the import target.
	 */
	public static boolean importEbookFromClipboard(Transferable t, int dropRow) {
		ReloadableTableModel listModel = MainController.getController().getModel();

		// Get the current string under the drop.
		EbookPropertyItem value = (EbookPropertyItem) listModel.getValueAt(dropRow, 0);

		// Get the string that is being dropped.
		try {
			IResourceHandler targetRecourceDirectory = value.getResourceHandler().getParentResource();
			importEbookFromClipboard(t, value.getBasePath(), targetRecourceDirectory);
		}
		catch (Exception e) { return false; }

		return true;
	}

	public static void importEbookFromClipboard(Transferable transferable, String basePath, IResourceHandler targetRecourceDirectory)
			throws UnsupportedFlavorException, IOException, ClassNotFoundException {
		boolean deleteSourceFiles = true;
		List<IResourceHandler> sourceFiles = ResourceHandlerFactory.getResourceHandler(transferable);
		List<IResourceHandler> importEbookResources = ActionUtils.importEbookResources(sourceFiles, targetRecourceDirectory, basePath, deleteSourceFiles);
		
		if(!importEbookResources.isEmpty()) {
			// remove the source ebook entries which no longer exists from the database model
			removeNotExistingEbookEntriesFromDatabase(sourceFiles);
			ActionUtils.applyFileNameFilter(importEbookResources, true);
		}
	}

	private static void removeNotExistingEbookEntriesFromDatabase(List<IResourceHandler> resources) {
		for (IResourceHandler importEbookResource : resources) {
			DefaultDBManager dbManager = DefaultDBManager.getInstance();
			List<EbookPropertyItem> ebookPropertyItemByResource = EbookPropertyItemUtils.getEbookPropertyItemByResource(importEbookResource);
			for (EbookPropertyItem ebookPropertyItem : ebookPropertyItemByResource) {
				if(!ebookPropertyItem.getResourceHandler().exists()) {
					dbManager.deleteObject(ebookPropertyItem);
				}
			}
		}
	}

}
