package org.rr.jeborker.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.apache.commons.lang.StringUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.ResourceHandlerUtils;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class MainMenuBarController {

	private static MainMenuBarController controller;
	
	private MainMenuBarView view;
	
	private HashMap<String, Boolean> showHideBasePathToggleStatus = new HashMap<String, Boolean>();

	private MainMenuBarController() {
	}
	
	/**
	 * Gets the controller instance. because we have only one main window
	 * We have a singleton here.
	 * @return The desired EBorkerMainController.
	 */
	public static MainMenuBarController getController() {
		if(controller==null) {
			controller = new MainMenuBarController();
		}
		return controller;
	}
	
	public void dispose() {
		this.storeProperties();
	}	
	
	/**
	 * Get the menu view which is a {@link JMenuBar} instance.
	 * @return The menu view.
	 */
	MainMenuBarView getView() {
		if(view == null) {
			view = new MainMenuBarView();
		}
		return view;
	}
	
	/**
	 * Removes the menu item having the given path in it's name.
	 * @param path The path entry to be removed.
	 */
	public void removeBasePathMenuEntry(String path) {
		{
			final Component[] menuComponents = view.mnVerzeichnisEntfernen.getMenuComponents();
			for (int i = 0; i < menuComponents.length; i++) {
				if(menuComponents[i] instanceof JMenuItem) {
					final String name = (String) ((JMenuItem)menuComponents[i]).getAction().getValue(Action.NAME);
					if(StringUtils.replace(name, File.separator, "").equals(StringUtils.replace(path, File.separator, ""))) {
						view.mnVerzeichnisEntfernen.remove((JMenuItem)menuComponents[i]);
					}
				}
			}
		}
		
		{
			final Component[] menuComponents = view.mnVerzeichnisRefresh.getMenuComponents();
			for (int i = 0; i < menuComponents.length; i++) {
				if(menuComponents[i] instanceof JMenuItem) {
					final String name = (String) ((JMenuItem)menuComponents[i]).getAction().getValue(Action.NAME);
					if(StringUtils.replace(name, File.separator, "").equals(StringUtils.replace(path, File.separator, ""))) {
						view.mnVerzeichnisRefresh.remove((JMenuItem)menuComponents[i]);
					}
				}
			}	
		}
	}
	
	/**
	 * Shows the popup menu for the selected entries.
	 * @param location The locaten where the popup should appears.
	 * @param invoker The invoker for the popup menu.
	 */
	void showMainPopupMenu(Point location, Component invoker) {
		JPopupMenu menu = createMainTablePopupMenu();
		
		//setup and show popup
		if(menu.getComponentCount() > 0) {
			menu.setLocation(location);
			menu.show(invoker, location.x, location.y);
		}
	}
	
	/**
	 * Shows the popup menu for the selected entries.
	 * @param location The locaten where the popup should appears.
	 * @param invoker The invoker for the popup menu.
	 */
	void showFileSystemTreePopupMenu(Point location, Component invoker) {
		JPopupMenu menu = createFileSystemTreePopupMenu();
		
		//setup and show popup
		if(menu.getComponentCount() > 0) {
			menu.setLocation(location);
			menu.show(invoker, location.x, location.y);
		}
	}
	
	private static JPopupMenu createFileSystemTreePopupMenu() {
		final MainController controller = MainController.getController();
		final List<IResourceHandler> items = controller.getMainTreeController().getSelectedTreeItems();
		final JPopupMenu menu = new JPopupMenu();
		
		Action action;
		if(items.size() == 1) {
			//only visible to single selections
			if(items.get(0).isDirectoryResource()) {
				action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.FILE_SYSTEM_REFRESH_ACTION, items.get(0).toString());
				JMenuItem item = new JMenuItem(action);
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false));
				menu.add(item);	
			}
			
			action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, items.get(0).toString());
			menu.add(action);
			
			action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, items.get(0).toString());
			menu.add(action);
		} 
		if(items.size() >= 1) {
			final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
			final List<String> basePath = preferenceStore.getBasePath();
			final String name = Bundle.getString("MainMenuBarController.import");
			final JMenu mnImport = new JMenu(SwingUtils.removeMnemonicMarker(name));
			
			mnImport.setIcon(ImageResourceBundle.getResourceAsImageIcon("import_16.png"));
			mnImport.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
			for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
				String path = iterator.next();
				JMenuItem pathItem = new JMenuItem();
				pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.FILE_SYSTEM_IMPORT_ACTION, path));
				mnImport.add(pathItem);
			}
			menu.add(mnImport);
			if(!ResourceHandlerUtils.containFilesOnly(items)) {
				mnImport.setEnabled(false);
			}
		}
		
		JMenu copyToSubMenu = createCopyToMenu();
		menu.add(copyToSubMenu);		
		
		action = ActionFactory.getActionForResource(ActionFactory.DYNAMIC_ACTION_TYPES.DELETE_FILE_ACTION, items);
		JMenuItem item = new JMenuItem(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
		menu.add(item);		
		
		return menu;
	}
	
	/**
	 * Creates the popup menu for the main table having only these entries inside
	 * that can be processed with the given {@link EbookPropertyItem} list.
	 * @param items The items to be tested if they're matching against the menu entries.
	 * @return The desired {@link JPopupMenu}. Never returns <code>null</code>.
	 */
	private static JPopupMenu createMainTablePopupMenu() {
		final MainController controller = MainController.getController();
		final List<EbookPropertyItem> items = MainController.getController().getSelectedEbookPropertyItems();
		final int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();
		final JPopupMenu menu = new JPopupMenu();
		
		{
			Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.EDIT_PLAIN_METADATA_ACTION, items, selectedEbookPropertyItemRows);
			if(action.isEnabled()) {
				menu.add(action);
			}
		}
		
		{
			Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.REFRESH_ENTRY_ACTION, items, selectedEbookPropertyItemRows);
			JMenuItem item = new JMenuItem(action);
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false));
			if(action.isEnabled()) {
				menu.add(item);
			}
		}	
		
		Action action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.RENAME_FILE_ACTION, "");
		JMenuItem item = new JMenuItem(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false));
		menu.add(item);
		
		if(items.size() == 1) {
			//only visible to single selections
			action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, items.get(0).getFile());
			menu.add(action);
			
			action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, items.get(0).getFile());
			menu.add(action);
		}

		JMenu copyToSubMenu = createCopyToMenu();
		menu.add(copyToSubMenu);

		action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.DELETE_FILE_ACTION, items, selectedEbookPropertyItemRows);
		item = new JMenuItem(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
		menu.add(item);
		
		return menu;
	}	
	
	/**
	 * Creates the <code>copy to</code> submenu.
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
	
	/**
	 * Tells if the ebook items with the given basePath are shown or not. 
	 * @param basePath The pase path for the items.
	 * @return <code>true</code> if the ebook items are shown and <code>false</code> if not.
	 */
	public boolean isShowHideBasePathStatusShow(final String basePath) {
		final Boolean status = showHideBasePathToggleStatus.get(basePath);
		if(status == null) {
			return true; //show per default
		}
		return status.booleanValue();
	}
	
	public void setShowHideBasePathStatusShow(final String basePath, final boolean show) {
		showHideBasePathToggleStatus.put(basePath, Boolean.valueOf(show));
	}
	
	/**
	 * get all base path entries which are marked as hidden in the file menu.
	 * @return A list of all hidden base path entries.
	 */
	public List<String> getHiddenBasePathEntries() {
		if(showHideBasePathToggleStatus.isEmpty()) {
			return Collections.emptyList();
		}
		
		final ArrayList<String> result = new ArrayList<String>();
	    for (Map.Entry<String, Boolean> entry : showHideBasePathToggleStatus.entrySet()) {
	        String basePath = entry.getKey();
	        Boolean isSHow = entry.getValue();
	        if(isSHow!=null && isSHow.booleanValue() == false) {
	        	result.add(basePath);
	        }
	    }
	    return result;
	}
	
	private void storeProperties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final List<String> hiddenBasePathEntries = MainMenuBarController.getController().getHiddenBasePathEntries();
		if(hiddenBasePathEntries.isEmpty()) {
			preferenceStore.addGenericEntryAsString("mainMenuBasePathHide", "");
		} else {
			preferenceStore.addGenericEntryAsString("mainMenuBasePathHide", ListUtils.join(hiddenBasePathEntries, String.valueOf(File.pathSeparatorChar)));
		}		
	}
	
	void restoreProperties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final List<String> path = preferenceStore.getBasePath();
		final String basePathPropString = preferenceStore.getGenericEntryAsString("mainMenuBasePathHide");
		
		if(basePathPropString != null && !basePathPropString.isEmpty()) {
			List<String> split = ListUtils.split(basePathPropString, String.valueOf(File.pathSeparatorChar));
			for(String basePath : split) {	
				if(!path.contains(basePath)) {
					//there is no base path for the hidden path
					ArrayList<String> s = new ArrayList<String>(split);
					boolean remove = s.remove(basePath);
					if(remove) {
						preferenceStore.addGenericEntryAsString("mainMenuBasePathHide", ListUtils.join(s, String.valueOf(File.pathSeparatorChar)));
					}
				} else {
					ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_HIDE_BASE_PATH_ACTION, basePath).invokeAction(new ActionEvent(this, 0, "initialize"));
				}
			}
		}
	}
}
