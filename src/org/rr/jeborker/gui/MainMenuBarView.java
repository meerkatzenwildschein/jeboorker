package org.rr.jeborker.gui;

import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.rr.common.swing.SwingUtils;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.converter.ConverterFactory;
import org.rr.jeborker.converter.IEBookConverter;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class MainMenuBarView extends JMenuBar {

	private static final long serialVersionUID = -8134987169763660105L;
	
	JMenu fileMenuBar;
	
	JMenu metadataMenuBar;

	JMenu editMenuBar;
	
	JMenu helpMenuBar;	

	JMenu mnVerzeichnisEntfernen;

	JMenu mnVerzeichnisRefresh;
	
	JMenu mnVerzeichnisShowHide;

	
	MainMenuBarView() {
		this.init();
	}
	
	private void init() {
		add(createFileMenu());
		add(createEditMenu());
		add(createHelpMenu());
	}
	
	/**
	 * Creates the file menu entry with it's menu items.
	 * @return The new created menu entry.
	 */
	private JMenu createFileMenu() {
		String fileMenuBarName = Bundle.getString("EborkerMainView.file");
		this.fileMenuBar = new JMenu(SwingUtils.removeMnemonicMarker(fileMenuBarName));
		this.fileMenuBar.setMnemonic(SwingUtils.getMnemonicKeyCode(fileMenuBarName));
		
		this.fileMenuBar.addMenuListener(new MenuListener() {
			
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

				fileMenuBar.removeAll();
				
				JMenuItem mntmAddEbooks = new JMenuItem();
				mntmAddEbooks.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.ADD_BASE_PATH_ACTION, null));
				fileMenuBar.add(mntmAddEbooks);
				
				final List<String> basePath = JeboorkerPreferences.getBasePath();
				{
					String name = Bundle.getString("EborkerMainView.removeBasePath");
					mnVerzeichnisEntfernen = new JMenu(SwingUtils.removeMnemonicMarker(name));
					mnVerzeichnisEntfernen.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
					for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
						String path = iterator.next();
						JMenuItem pathItem = new JMenuItem();
						pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_BASE_PATH_ACTION, path));
						mnVerzeichnisEntfernen.add(pathItem);
					}
					fileMenuBar.add(mnVerzeichnisEntfernen);
					if(basePath.isEmpty()) {
						mnVerzeichnisEntfernen.setEnabled(false);
					}	
					
					if(basePath.size() > 1) {
						JMenuItem pathItem = new JMenuItem();
						pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_BASE_PATH_ACTION, "removeAll"));
						mnVerzeichnisEntfernen.add(pathItem);				
					}			
				}

				{
					String name = Bundle.getString("EborkerMainView.refreshBasePath");
					mnVerzeichnisRefresh = new JMenu(SwingUtils.removeMnemonicMarker(name));
					mnVerzeichnisRefresh.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
					for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
						String path = iterator.next();
						JMenuItem pathItem = new JMenuItem();
						pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_BASE_PATH_ACTION, path));
						mnVerzeichnisRefresh.add(pathItem);
					}
					fileMenuBar.add(mnVerzeichnisRefresh);
					if(basePath.isEmpty()) {
						mnVerzeichnisRefresh.setEnabled(false);
					}
					
					if(basePath.size() > 1) {
						JMenuItem pathItem = new JMenuItem();
						pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_BASE_PATH_ACTION, "refreshAll"));
						mnVerzeichnisRefresh.add(pathItem);				
					}
				}
				
				{
					String name = Bundle.getString("EborkerMainView.basePathVisibility");
					mnVerzeichnisShowHide = new JMenu(SwingUtils.removeMnemonicMarker(name));
					mnVerzeichnisShowHide.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
					for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
						String path = iterator.next();
						JCheckBoxMenuItem pathItem = new JCheckBoxMenuItem();
						boolean isShow = MainMenuBarController.getController().isShowHideBasePathStatusShow(path);
						pathItem.setSelected(isShow);
						pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_HIDE_BASE_PATH_ACTION, path));
						mnVerzeichnisShowHide.add(pathItem);
					}
					if(basePath.size() > 1) {
						{
							JMenuItem pathItem = new JMenuItem();
							pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_HIDE_BASE_PATH_ACTION, "showAll"));
							mnVerzeichnisShowHide.add(pathItem);
						}
						{
							JMenuItem pathItem = new JMenuItem();
							pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_HIDE_BASE_PATH_ACTION, "hideAll"));
							mnVerzeichnisShowHide.add(pathItem);
						}		
					}
					
					fileMenuBar.add(mnVerzeichnisShowHide);
					if(basePath.isEmpty()) {
						mnVerzeichnisShowHide.setEnabled(false);
					}		
				}
				
				fileMenuBar.add(new JSeparator());
				
				final JMenuItem saveMetadataMenuEntry = new JMenuItem((ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, "")));
				saveMetadataMenuEntry.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
				fileMenuBar.add(saveMetadataMenuEntry);
				
				fileMenuBar.add(new JSeparator());
				
				//Open folder only for single selection.
				final JMenuItem openFolderMenuEntry;
				final JMenuItem openFileMenuEntry;
				final JMenuItem deleteFileMenuEntry;
				if(selectedItems.size() == 1) {
					openFolderMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, selectedItems.get(0).getFile()));
					openFolderMenuEntry.setEnabled(true);
					
					openFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, selectedItems.get(0).getFile()));
					openFileMenuEntry.setEnabled(true);
				} else {
					openFolderMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, ""));
					openFolderMenuEntry.setEnabled(false);
					
					openFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, ""));
					openFileMenuEntry.setEnabled(false);							
				}
				
				if(selectedItems.size() >= 1) {
					deleteFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.DELETE_FILE_ACTION, selectedItems.get(0).getFile()));
					deleteFileMenuEntry.setEnabled(true);	
				} else {
					deleteFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.DELETE_FILE_ACTION, ""));
					deleteFileMenuEntry.setEnabled(false);				
				}
				
				JMenu copyToSubMenu = MainMenuBarController.createCopyToMenu(selectedItems, selectedEbookPropertyItemRows);
				if(selectedItems.size() >= 1) {
					copyToSubMenu.setEnabled(true);
				}  else {
					copyToSubMenu.setEnabled(false);
				}
				
				fileMenuBar.add(openFileMenuEntry);
				fileMenuBar.add(openFolderMenuEntry);
				fileMenuBar.add(copyToSubMenu);
				fileMenuBar.add(deleteFileMenuEntry);
				
				fileMenuBar.add(new JSeparator());
				
				//quit menu entry
				JMenuItem mntmQuit = new JMenuItem();
				mntmQuit.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.QUIT_ACTION, (String) null));
				mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
				
				fileMenuBar.add(mntmQuit);
			}
		});
		
		return fileMenuBar;
	}
	
	private JMenu createEditMenu() {
		String editMenuBarName = Bundle.getString("EborkerMainView.edit");
		this.editMenuBar = new JMenu(SwingUtils.removeMnemonicMarker(editMenuBarName));
		this.editMenuBar.setMnemonic(SwingUtils.getMnemonicKeyCode(editMenuBarName));
		
		this.editMenuBar.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				final MainController controller = MainController.getController();
				final List<EbookPropertyItem> selectedItems = controller.getSelectedEbookPropertyItems();
				final int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();	
				
				editMenuBar.removeAll();
				createDynamicEditMenu(selectedItems, selectedEbookPropertyItemRows);
				editMenuBar.add(new JSeparator());
				createDynamicMetadataMenuEntries(selectedItems, selectedEbookPropertyItemRows);
				createConvertMenuEntry(selectedItems, selectedEbookPropertyItemRows);
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
			
			private void createDynamicEditMenu(List<EbookPropertyItem> selectedItems, int[] selectedEbookPropertyItemRows) {
				JMenuItem copyClipboard = new JMenuItem(ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.COPY_TO_CLIPBOARD_ACTION, selectedItems, selectedEbookPropertyItemRows));
				copyClipboard.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
				editMenuBar.add(copyClipboard);	
				
				JMenuItem pasteClipboard = new JMenuItem(ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.PASTE_FROM_CLIPBOARD_ACTION, selectedItems, selectedEbookPropertyItemRows));
				pasteClipboard.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
				editMenuBar.add(pasteClipboard);						
							
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
				coverSubMenu.setIcon(new ImageIcon(ImageResourceBundle.getResource("image_16.png")));
				MainMenuBarController.addCoverMenuItems(coverSubMenu, selectedItems, selectedEbookPropertyItemRows);
				editMenuBar.add(coverSubMenu);		
				
				Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.EDIT_PLAIN_METADATA_ACTION, selectedItems, selectedEbookPropertyItemRows);
				editMenuBar.add(new JMenuItem(action));		
				
				action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.REFRESH_ENTRY_ACTION, selectedItems, selectedEbookPropertyItemRows);
				editMenuBar.add(new JMenuItem(action));				
			}
			
			private void createConvertMenuEntry(List<EbookPropertyItem> selectedItems, int[] selectedEbookPropertyItemRows) {
				String name = Bundle.getString("EborkerMainView.convert");
				JMenu convertSubMenu = new JMenu(SwingUtils.removeMnemonicMarker(name));
				convertSubMenu.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
				convertSubMenu.setIcon(new ImageIcon(ImageResourceBundle.getResource("convert_16.png")));
				convertSubMenu.setEnabled(false);
				
				if(!selectedItems.isEmpty() && sameType(selectedItems)) {
					List<IEBookConverter> converter = ConverterFactory.getConverter(selectedItems.get(0).getResourceHandler());
					for(IEBookConverter c : converter) {
						Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.CONVERT_EBOOK_ACTION, selectedItems, selectedEbookPropertyItemRows);
						action.putValue("converterClass", c.getClass());
						JMenuItem converterMenuItem = new JMenuItem(action);
						converterMenuItem.setText(c.getConversionSourceType().getName() + " -> " + c.getConversionTargetType().getName());
						convertSubMenu.add(converterMenuItem);
						convertSubMenu.setEnabled(true);
					}
				} 
				editMenuBar.add(convertSubMenu);
			}
		});		

		
		return this.editMenuBar;
	}
	
	private JMenu createHelpMenu() {
		String helpMenuBarName = Bundle.getString("EborkerMainView.help");
		this.helpMenuBar = new JMenu(SwingUtils.removeMnemonicMarker(helpMenuBarName));
		this.helpMenuBar.setMnemonic(SwingUtils.getMnemonicKeyCode(helpMenuBarName));

		JMenuItem logItem = new JMenuItem();
		logItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.VIEW_LOG_MONITOR_ACTION, null));
		helpMenuBar.add(logItem);
		
		return this.helpMenuBar;
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
