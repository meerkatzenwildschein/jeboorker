package org.rr.jeborker.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.ResourceHandlerUtils;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.components.tree.JRTree;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.action.ActionCallback;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.gui.model.BasePathTreeModel;
import org.rr.jeborker.gui.model.FileSystemNode;
import org.rr.jeborker.gui.model.FileSystemTreeModel;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

/**
 * This Utils class contains all static methods to create menu items and their accelerators and keyboard actions.
 */
class MainViewMenuUtils {

	static final KeyStroke DELETE_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);

	static final KeyStroke COPY_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);

	static final KeyStroke PASTE_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);

	static final KeyStroke REFRESH_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false);

	static final KeyStroke RENAME_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false);

	static final KeyStroke SAVE_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK);

	static final KeyStroke QUIT_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK);

	static final KeyStroke FIND_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK);

	static JMenuItem createDeleteMenuItem(List<IResourceHandler> items) {
		Action action = ActionFactory.getActionForResource(ActionFactory.DYNAMIC_ACTION_TYPES.DELETE_FILE_ACTION, items);
		JMenuItem item = new JMenuItem(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
		return item;
	}

	static JMenuItem createNewFolderMenuItem(final JRTree basePathTree, final JRTree fileSystemTree, FileSystemNode pathNode) {
		final IResourceHandler pathNodeResource = pathNode.getResource();
		Action action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.NEW_FOLDER_ACTION, pathNodeResource.toString(), new ActionCallback() {

			@Override
			public void afterAction() {
				((BasePathTreeModel) basePathTree.getModel()).reload(pathNodeResource);
				((FileSystemTreeModel) fileSystemTree.getModel()).reload(pathNodeResource);
			}

		});
		return new JMenuItem(action);
	}

	static JMenuItem createOpenFolderMenuItem(List<IResourceHandler> items) {
		Action action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, items.get(0).toString());
		return new JMenuItem(action);
	}

	static JMenuItem createOpenFileMenuItem(List<IResourceHandler> items) {
		Action action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, items.get(0).toString());
		return new JMenuItem(action);
	}

	static JMenuItem createRenameFileMenuItem() {
		Action action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.RENAME_FILE_ACTION, "");
		JMenuItem menuItem = new JMenuItem(action);
		menuItem.setAccelerator(MainViewMenuUtils.RENAME_KEY);
		return menuItem;
	}

	static JMenuItem createFileSystemRefreshMenuItem(List<IResourceHandler> items) {
		Action action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.FILE_SYSTEM_REFRESH_ACTION, items.get(0).toString());
		JMenuItem item = new JMenuItem(action);
		item.setAccelerator(MainViewMenuUtils.REFRESH_KEY);
		return new JMenuItem(action);
	}

	/**
	 * Creates the <code>copy to</code> menu with all sub menu targets.
	 */
	static JMenu createCopyToMenu() {
		final MainController controller = MainController.getController();
		final List<EbookPropertyItem> items = MainController.getController().getSelectedEbookPropertyItems();
		final int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();
		final List<IResourceHandler> selectedTreeItems = controller.getMainTreeController().getSelectedTreeItems();
		final String name = Bundle.getString("MainMenuBarController.copyToSubMenu");
		final JMenu copyToSubMenu = new JMenu(SwingUtils.removeMnemonicMarker(name));

		copyToSubMenu.setIcon(ImageResourceBundle.getResourceAsImageIcon("copy_16.png"));
		copyToSubMenu.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
		Action action;

		if(!items.isEmpty()) {
			action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.COPY_TO_DROPBOX_ACTION, items, selectedEbookPropertyItemRows);
		} else {
			action = ActionFactory.getActionForResource(ActionFactory.DYNAMIC_ACTION_TYPES.COPY_TO_DROPBOX_ACTION, selectedTreeItems);
		}
		copyToSubMenu.add(action);

		if(!items.isEmpty()) {
			action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.COPY_TO_TARGET_ACTION, items, selectedEbookPropertyItemRows);
		} else {
			action = ActionFactory.getActionForResource(ActionFactory.DYNAMIC_ACTION_TYPES.COPY_TO_TARGET_ACTION, selectedTreeItems);
		}
		IResourceHandler homeFolder = ResourceHandlerFactory.getResourceHandler(System.getProperty("user.home"));
		action.putValue(Action.NAME, Bundle.getString("MainMenuBarController.userhome"));
		action.putValue("TARGET", homeFolder);
		action.putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("home_16.png"));
		action.putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("home_22.png"));
		copyToSubMenu.add(action);

		List<IResourceHandler> externalDriveResources = ResourceHandlerUtils.getExternalDriveResources();
		for(IResourceHandler externalResource : externalDriveResources) {
			if(!items.isEmpty()) {
				action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.COPY_TO_TARGET_ACTION, items, selectedEbookPropertyItemRows);
			} else {
				action = ActionFactory.getActionForResource(ActionFactory.DYNAMIC_ACTION_TYPES.COPY_TO_TARGET_ACTION, selectedTreeItems);
			}
			action.putValue(Action.NAME, externalResource.toString());
			action.putValue("TARGET", externalResource);
			action.putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("removable_drive_16.png"));
			action.putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("removable_drive_22.png"));
			copyToSubMenu.add(action);

			//add also the ebook folders in the external drive
			List<IResourceHandler> childFolderByRegExp = ResourceHandlerUtils.getChildFolderByRegExp(externalResource, "[eE]{0,1}-{0,1}[bB][oO][oO][kK]");
			if(!childFolderByRegExp.isEmpty()) {
				for(IResourceHandler ebookFolder : childFolderByRegExp) {
					if(!items.isEmpty()) {
						action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.COPY_TO_TARGET_ACTION, items, selectedEbookPropertyItemRows);
					} else {
						action = ActionFactory.getActionForResource(ActionFactory.DYNAMIC_ACTION_TYPES.COPY_TO_TARGET_ACTION, selectedTreeItems);
					}
					action.putValue(Action.NAME, ebookFolder.toString());
					action.putValue("TARGET", ebookFolder);
					action.putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("removable_drive_16.png"));
					action.putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("removable_drive_22.png"));
					copyToSubMenu.add(action);
				}
			}
		}
		return copyToSubMenu;
	}

	static JMenuItem crateFileSystemImportTargetMenuItem(String path) {
		Action action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.FILE_SYSTEM_IMPORT_ACTION, path);
		return new JMenuItem(action);
	}

	static void registerDeleteKeyAction(JComponent component) {
		component.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.DELETE_FILE_ACTION, null), "DeleteFile", DELETE_KEY, JComponent.WHEN_FOCUSED);
	}

	static void registerPasteFromClipboardKeyAction(JComponent component) {
		component.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.PASTE_FROM_CLIPBOARD_ACTION, null), "Paste", PASTE_KEY, JComponent.WHEN_FOCUSED);
	}

	static void registerCopyToClipboardKeyAction(JComponent component) {
		component.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.COPY_TO_CLIPBOARD_ACTION, null), "Copy", COPY_KEY, JComponent.WHEN_FOCUSED);
	}

	static void registerRefreshEntryKeyAction(JComponent component) {
		component.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_ENTRY_ACTION, null), "RefreshEntry", REFRESH_KEY, JComponent.WHEN_FOCUSED);
	}

	static void registerRenameFileKeyAction(JComponent component) {
		component.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.RENAME_FILE_ACTION, null), "RenameFile", RENAME_KEY, JComponent.WHEN_FOCUSED);
	}

	static void registerFileSystemRefreshKeyAction(JComponent component) {
		component.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.FILE_SYSTEM_REFRESH_ACTION, null), "Refresh", REFRESH_KEY, JComponent.WHEN_FOCUSED);
	}
}
