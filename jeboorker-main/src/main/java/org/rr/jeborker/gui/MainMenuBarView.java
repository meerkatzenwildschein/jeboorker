package org.rr.jeborker.gui;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.awt.Component;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.commons.lang.ObjectUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.app.BasePathList;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.converter.ConverterFactory;
import org.rr.jeborker.converter.IEBookConverter;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.gui.action.ApplicationAction;
import org.rr.jeborker.gui.resources.ImageResourceBundle;
import org.rr.jeborker.metadata.MetadataHandlerFactory;

class MainMenuBarView extends JMenuBar {

	private static final long serialVersionUID = 5278345064008258478L;

	private static Icon eyesVisible;

	private static Icon eyesInvisible;

	static {
		eyesVisible = ImageResourceBundle.getResourceAsImageIcon("eyes_blue_16.png");

		eyesInvisible = ImageResourceBundle.getResourceAsImageIcon("eyes_gray_16.png");
	}

	private JMenu menueFile;

	private JMenu menuEdit;

	private JMenu menueHelp;

	private JMenu menuRemoveBasePath;

	private JMenu menuRefreshBasePath;

	private JMenu menueBasePathShowHide;


	MainMenuBarView() {
		this.init();
	}

	private void init() {
		add(createFileMenu());
		add(createEditMenu());
		add(createHelpMenu());
	}

	/**
	 * Removes the menu item having the given path in it's name from the refresh and the remove base menu.
	 * @param path The path entry to be removed.
	 */
	void removeBasePathMenuEntry(String path) {
		removeBasePathMenuEntry(menuRemoveBasePath, path);
		removeBasePathMenuEntry(menuRefreshBasePath, path);
	}

	void removeBasePathMenuEntry(JMenu menu, String path) {
		final Component[] menuComponents = menu.getMenuComponents();
		for (int i = 0; i < menuComponents.length; i++) {
			if(menuComponents[i] instanceof JMenuItem) {
				final String name = (String) ((JMenuItem)menuComponents[i]).getAction().getValue(Action.NAME);
				if(StringUtil.replace(name, File.separator, EMPTY).equals(StringUtil.replace(path, File.separator, EMPTY))) {
					menu.remove((JMenuItem)menuComponents[i]);
				}
			}
		}
	}

	/**
	 * Creates the file menu entry with it's menu items.
	 * @return The new created menu entry.
	 */
	private JMenu createFileMenu() {
		String fileMenuBarName = Bundle.getString("EborkerMainView.file");
		this.menueFile = new JMenu(SwingUtils.removeMnemonicMarker(fileMenuBarName));
		this.menueFile.setMnemonic(SwingUtils.getMnemonicKeyCode(fileMenuBarName));

		this.menueFile.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				createDynamicFileMenu();
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}

			private void createDynamicFileMenu() {
				final MainController controller = MainController.getController();
				final List<EbookPropertyItem> selectedItems = controller.getSelectedEbookPropertyItems();
				final int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();

				menueFile.removeAll();

				JMenuItem mntmAddEbooks = new JMenuItem();
				mntmAddEbooks.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.ADD_BASE_PATH_ACTION, null));
				menueFile.add(mntmAddEbooks);

				final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
				final List<String> basePath = preferenceStore.getBasePath();
				{
					String name = Bundle.getString("EborkerMainView.removeBasePath");
					menuRemoveBasePath = new JMenu(SwingUtils.removeMnemonicMarker(name));
					menuRemoveBasePath.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
					for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
						String path = iterator.next();
						if(ObjectUtils.notEqual(path, BasePathList.getImportBasePath())) {
							JMenuItem pathItem = new JMenuItem();
							pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_BASE_PATH_ACTION, path));
							menuRemoveBasePath.add(pathItem);
						}
					}
					menueFile.add(menuRemoveBasePath);
					if(basePath.isEmpty()) {
						menuRemoveBasePath.setEnabled(false);
					}

					menuRemoveBasePath.add(new JSeparator());

					if(basePath.size() > 1) {
						JMenuItem pathItem = new JMenuItem();
						pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_BASE_PATH_ACTION, "removeAll"));
						menuRemoveBasePath.add(pathItem);
					}
				}

				{
					String name = Bundle.getString("EborkerMainView.refreshBasePath");
					menuRefreshBasePath = new JMenu(SwingUtils.removeMnemonicMarker(name));
					menuRefreshBasePath.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
					for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
						String path = iterator.next();
						JMenuItem pathItem = new JMenuItem();
						pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_BASE_PATH_ACTION, path));
						menuRefreshBasePath.add(pathItem);
					}
					menueFile.add(menuRefreshBasePath);
					if(basePath.isEmpty()) {
						menuRefreshBasePath.setEnabled(false);
					}

					menuRefreshBasePath.add(new JSeparator());

					if(basePath.size() > 1) {
						JMenuItem pathItem = new JMenuItem();
						pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_BASE_PATH_ACTION, "refreshAll"));
						menuRefreshBasePath.add(pathItem);
					}
				}

				{
					String name = Bundle.getString("EborkerMainView.basePathVisibility");
					menueBasePathShowHide = new JMenu(SwingUtils.removeMnemonicMarker(name));
					menueBasePathShowHide.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
					for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
						String path = iterator.next();
						ApplicationAction action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_HIDE_BASE_PATH_ACTION, path);

						boolean isShow = MainMenuBarController.getController().isShowHideBasePathStatusShow(path);
						if(isShow) {
							action.putValue(Action.SMALL_ICON, eyesVisible);
						} else {
							action.putValue(Action.SMALL_ICON, eyesInvisible);
						}
						JMenuItem pathItem = new JMenuItem(action);
						menueBasePathShowHide.add(pathItem);
					}

					menueBasePathShowHide.add(new JSeparator());

					if(basePath.size() > 1) {
						{
							JMenuItem pathItem = new JMenuItem();
							pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_HIDE_BASE_PATH_ACTION, "showAll"));
							menueBasePathShowHide.add(pathItem);
						}
						{
							JMenuItem pathItem = new JMenuItem();
							pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_HIDE_BASE_PATH_ACTION, "hideAll"));
							menueBasePathShowHide.add(pathItem);
						}
					}

					menueFile.add(menueBasePathShowHide);
					if(basePath.isEmpty()) {
						menueBasePathShowHide.setEnabled(false);
					}
				}

				menueFile.add(new JSeparator());

				final JMenuItem saveMetadataMenuEntry = new JMenuItem((ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, EMPTY)));
				saveMetadataMenuEntry.setAccelerator(MainViewMenuUtils.SAVE_KEY);
				menueFile.add(saveMetadataMenuEntry);

				menueFile.add(new JSeparator());

				//Open folder only for single selection.
				final JMenuItem openFolderMenuEntry;
				final JMenuItem openFileMenuEntry;
				final JMenuItem renameFileMenuEntry;
				final JMenuItem deleteFileMenuEntry;
				if(selectedItems.size() == 1) {
					openFolderMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, selectedItems.get(0).getFile()));
					openFolderMenuEntry.setEnabled(true);

					openFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, selectedItems.get(0).getFile()));
					openFileMenuEntry.setEnabled(true);
				} else {
					if(MainViewSelectionUtils.isTreeItemSingleSelection()) {
						List<IResourceHandler> selectedResources = controller.getMainTreeHandler().getSelectedTreeItems();
						openFolderMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, selectedResources.get(0).toString()));
						openFolderMenuEntry.setEnabled(true);

						openFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, selectedResources.get(0).toString()));
						openFileMenuEntry.setEnabled(true);
					} else {
						openFolderMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, EMPTY));
						openFolderMenuEntry.setEnabled(false);

						openFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, EMPTY));
						openFileMenuEntry.setEnabled(false);
					}
				}
				renameFileMenuEntry = MainViewMenuUtils.createRenameFileMenuItem();
				renameFileMenuEntry.setEnabled(false);
				if(!MainViewSelectionUtils.isDirectorySelectionIncluded() &&
						(MainViewSelectionUtils.isTreeItemSelection() || MainViewSelectionUtils.isMainTableSelection())) {
					renameFileMenuEntry.setEnabled(true);
				}

				if(MainViewSelectionUtils.isMainTableSelection()) {
					deleteFileMenuEntry = new JMenuItem(ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.DELETE_FILE_ACTION, selectedItems, selectedEbookPropertyItemRows));
					deleteFileMenuEntry.setEnabled(true);
				} else if(MainViewSelectionUtils.isTreeItemSelection()) {
					List<IResourceHandler> selectedTreeItems = MainController.getController().getMainTreeHandler().getSelectedTreeItems();
					deleteFileMenuEntry = new JMenuItem(ActionFactory.getActionForResource(ActionFactory.DYNAMIC_ACTION_TYPES.DELETE_FILE_ACTION, selectedTreeItems));
					if(PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getBasePath().isOneEntryABasePathMember(selectedTreeItems)) {
						deleteFileMenuEntry.setEnabled(false);
					} else {
						deleteFileMenuEntry.setEnabled(true);	
					}
				} else {
					deleteFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.DELETE_FILE_ACTION, EMPTY));
					deleteFileMenuEntry.setEnabled(false);
				}
				deleteFileMenuEntry.setAccelerator(MainViewMenuUtils.DELETE_KEY);

				JMenu copyToSubMenu = MainViewMenuUtils.createSendToMenu();
				if(selectedItems.size() >= 1) {
					copyToSubMenu.setEnabled(true);
				}  else {
					if(controller.getMainTreeHandler().getSelectedTreeItems().size() > 0) {
						copyToSubMenu.setEnabled(true);
					} else {
						copyToSubMenu.setEnabled(false);
					}
				}

				menueFile.add(openFileMenuEntry);
				menueFile.add(renameFileMenuEntry);
				menueFile.add(openFolderMenuEntry);
				menueFile.add(copyToSubMenu);
				menueFile.add(deleteFileMenuEntry);

				menueFile.add(new JSeparator());

				//quit menu entry
				JMenuItem mntmQuit = new JMenuItem();
				mntmQuit.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.QUIT_ACTION, (String) null));
				mntmQuit.setAccelerator(MainViewMenuUtils.QUIT_KEY);

				menueFile.add(mntmQuit);
			}
		});

		return menueFile;
	}

	private JMenu createEditMenu() {
		String editMenuBarName = Bundle.getString("EborkerMainView.edit");
		this.menuEdit = new JMenu(SwingUtils.removeMnemonicMarker(editMenuBarName));
		this.menuEdit.setMnemonic(SwingUtils.getMnemonicKeyCode(editMenuBarName));

		this.menuEdit.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				final MainController controller = MainController.getController();
				final List<EbookPropertyItem> selectedItems = controller.getSelectedEbookPropertyItems();
				final int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();
				final List<IResourceHandler> selectedItemResources = EbookPropertyItemUtils.createIResourceHandlerList(selectedItems);

				menuEdit.removeAll();
				createDynamicEditMenu(selectedItems, selectedEbookPropertyItemRows);

				JMenuItem find = new JMenuItem(ActionFactory.getTableFindAction(null));
				find.setText(SwingUtils.removeMnemonicMarker(Bundle.getString("MainMenuBarView.find")));
				find.setMnemonic(SwingUtils.getMnemonicKeyCode(Bundle.getString("MainMenuBarView.find")));
				find.setIcon(ImageResourceBundle.getResourceAsImageIcon("find_16.png"));
				find.setAccelerator(MainViewMenuUtils.FIND_KEY);
				menuEdit.add(find);

				menuEdit.add(new JSeparator());

				createDynamicMetadataMenuEntries(selectedItems, selectedEbookPropertyItemRows);

				JMenuItem metadataDownloadItem = new JMenuItem();
				metadataDownloadItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_METADATA_DOWNLOAD_ACTION, null));
				menuEdit.add(metadataDownloadItem);
				if(!MainViewSelectionUtils.isMainTableSelection() || !MetadataHandlerFactory.hasWriterSupport(selectedItemResources)) {
					metadataDownloadItem.setEnabled(false);
				}

				createConvertMenuEntry(selectedItems, selectedEbookPropertyItemRows);

				JMenuItem pdfScissorsItem = new JMenuItem();
				pdfScissorsItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_PDF_SCISSORS_ACTION, null));
				pdfScissorsItem.setEnabled(false);
				if(MainViewSelectionUtils.isMainTableSingleSelection()) {
					if(selectedItems.get(0).getMimeType().equals(JeboorkerConstants.SUPPORTED_MIMES.MIME_PDF.getMime())) {
						pdfScissorsItem.setEnabled(true);
					} else if(selectedItems.get(0).getMimeType().equals(JeboorkerConstants.SUPPORTED_MIMES.MIME_CBZ.getMime())) {
						pdfScissorsItem.setEnabled(true);
					} else if(selectedItems.get(0).getMimeType().equals(JeboorkerConstants.SUPPORTED_MIMES.MIME_CBR.getMime())) {
						pdfScissorsItem.setEnabled(true);
					}
				}
				menuEdit.add(pdfScissorsItem);
				
				JMenuItem mergeDocItem = new JMenuItem();
				mergeDocItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.MERGE_DOCUMENT_ACTION, null));
				mergeDocItem.setEnabled(false);
				if(MainViewSelectionUtils.isMainTableMultiSelection()) {
					Collection<String> mimeTypesFromSelection = MainViewSelectionUtils.getMimeTypesFromSelection();
					if(mimeTypesFromSelection.size() == 1) {
						if(mimeTypesFromSelection.contains(JeboorkerConstants.SUPPORTED_MIMES.MIME_PDF.getMime())) {
							mergeDocItem.setEnabled(true);
						}
					}
				}
//				menuEdit.add(mergeDocItem);
				
				
				menuEdit.add(new JSeparator());

				JMenuItem editPreferencesItem = new JMenuItem();
				editPreferencesItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_PREFERENCE_DIALOG_ACTION, null));
				menuEdit.add(editPreferencesItem);
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}

			private void createDynamicEditMenu(List<EbookPropertyItem> selectedItems, int[] selectedEbookPropertyItemRows) {
				List<IResourceHandler> selectedTreeItems = MainController.getController().getMainTreeHandler().getSelectedTreeItems();

				JMenuItem copyClipboard = new JMenuItem(ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.COPY_TO_CLIPBOARD_ACTION, selectedItems, selectedEbookPropertyItemRows));
				if(!copyClipboard.isEnabled()) {
					copyClipboard = new JMenuItem(ActionFactory.getActionForResource(ActionFactory.DYNAMIC_ACTION_TYPES.COPY_TO_CLIPBOARD_ACTION, selectedTreeItems));
				}
				copyClipboard.setAccelerator(MainViewMenuUtils.COPY_KEY);
				menuEdit.add(copyClipboard);


				JMenuItem pasteClipboard = new JMenuItem(ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.PASTE_FROM_CLIPBOARD_ACTION, selectedItems, selectedEbookPropertyItemRows));
				if(!pasteClipboard.isEnabled()) {
					pasteClipboard = new JMenuItem(ActionFactory.getActionForResource(ActionFactory.DYNAMIC_ACTION_TYPES.PASTE_FROM_CLIPBOARD_ACTION, selectedTreeItems));
				}
				pasteClipboard.setAccelerator(MainViewMenuUtils.PASTE_KEY);
				menuEdit.add(pasteClipboard);
			}

			/**
			 * Creates the menu entries for the metadata menu entry.
			 * @param items Items for the menu items.
			 * @return The list of menu entries.
			 */
			private void createDynamicMetadataMenuEntries(List<EbookPropertyItem> selectedItems, int[] selectedEbookPropertyItemRows) {
				String name = Bundle.getString("EborkerMainView.cover");
				JMenu coverSubMenu = new JMenu(SwingUtils.removeMnemonicMarker(name));
				coverSubMenu.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
				coverSubMenu.setIcon(ImageResourceBundle.getResourceAsImageIcon("image_16.png"));
				MainView.addCoverMenuItems(coverSubMenu, selectedItems, selectedEbookPropertyItemRows);
				menuEdit.add(coverSubMenu);
				if(coverSubMenu.getMenuComponentCount() == 0) {
					coverSubMenu.setEnabled(false);
				}

				Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.EDIT_PLAIN_METADATA_ACTION, selectedItems, selectedEbookPropertyItemRows);
				menuEdit.add(new JMenuItem(action));

				action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.REFRESH_ENTRY_ACTION, selectedItems, selectedEbookPropertyItemRows);
				menuEdit.add(new JMenuItem(action));
			}

			private void createConvertMenuEntry(List<EbookPropertyItem> selectedItems, int[] selectedEbookPropertyItemRows) {
				String name = Bundle.getString("EborkerMainView.convert");
				JMenu convertSubMenu = new JMenu(SwingUtils.removeMnemonicMarker(name));
				convertSubMenu.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
				convertSubMenu.setIcon(ImageResourceBundle.getResourceAsImageIcon("convert_16.png"));
				convertSubMenu.setEnabled(false);

				if(!selectedItems.isEmpty() && sameType(selectedItems)) {
					List<IEBookConverter> converter = ConverterFactory.getConverter(selectedItems.get(0).getResourceHandler());
					for(IEBookConverter c : converter) {
						Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.CONVERT_EBOOK_ACTION, selectedItems, selectedEbookPropertyItemRows);
						action.putValue("converterClass", c.getClass());
						JMenuItem converterMenuItem = new JMenuItem(action);

						converterMenuItem.setText(StringUtil.capitalize(c.getConversionSourceType().getName()) + " " + Bundle.getString("MainMenuBarView.conversion.connector") + " " + StringUtil.capitalize(c.getConversionTargetType().getName()));
						convertSubMenu.add(converterMenuItem);
						convertSubMenu.setEnabled(true);
					}
				}
				menuEdit.add(convertSubMenu);
			}
		});


		return this.menuEdit;
	}

	private JMenu createHelpMenu() {
		final String helpMenuBarName = Bundle.getString("EborkerMainView.help");

		menueHelp = new JMenu(SwingUtils.removeMnemonicMarker(helpMenuBarName));
		menueHelp.setMnemonic(SwingUtils.getMnemonicKeyCode(helpMenuBarName));
		
		JMenuItem logItem = new JMenuItem();
		logItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.VIEW_LOG_MONITOR_ACTION, null));
		menueHelp.add(logItem);

		{ // look and feel menu
			String lookAndFeelName = Bundle.getString("EborkerMainView.laf");
			JMenu lookAndFeelMenu = new JMenu(SwingUtils.removeMnemonicMarker(lookAndFeelName));
			lookAndFeelMenu.setMnemonic(SwingUtils.getMnemonicKeyCode(lookAndFeelName));
			final String currentLaf = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.PREFERENCE_KEYS.LOOK_AND_FEEL)
					.getEntryAsString(PreferenceStoreFactory.PREFERENCE_KEYS.LOOK_AND_FEEL);
			final ButtonGroup grp = new ButtonGroup();
			final HashMap<String, JMenu> subMenus = new HashMap<String, JMenu>();
			for(String lafName : JeboorkerConstants.LOOK_AND_FEELS.keySet()) {
				JMenu parentMenu = lookAndFeelMenu;
				String lafViewName = lafName;
				if(lafName.contains(";")) {
					List<String> split = ListUtils.split(lafName, ";");
					parentMenu = subMenus.containsKey(split.get(0)) ? subMenus.get(split.get(0)) : new JMenu(split.get(0));
					subMenus.put(split.get(0), parentMenu);
					lookAndFeelMenu.add(parentMenu);
					lafViewName = split.get(1);
				}
				ApplicationAction action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.CHANGE_LOOK_AND_FEEL_ACTION, lafName);
				JRadioButtonMenuItem radioMenuItem = new JRadioButtonMenuItem(action);
				radioMenuItem.setText(lafViewName);
				grp.add(radioMenuItem);

				if(JeboorkerConstants.LOOK_AND_FEELS.get(lafName).equals(currentLaf)) {
					radioMenuItem.setSelected(true);
				} else {
					radioMenuItem.setSelected(false);
				}
				parentMenu.add(radioMenuItem);
			}
			menueHelp.add(lookAndFeelMenu);
		}
		
		menueHelp.add(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.VIEW_ABOUT_DIALOG_ACTION, null));

		return menueHelp;
	}

	/**
	 * Tests of the selected {@link EbookPropertyItem} are from the same mime type.
	 */
	private static boolean sameType(List<EbookPropertyItem> selectedItems) {
		String type = null;
		for(EbookPropertyItem item : selectedItems) {
			if(type == null) {
				type = item.getMimeType();
			} else {
				if(item.getMimeType().equals(type)) {
					continue;
				} else {
					return false;
				}
			}
		}
		return true;
	}

}
