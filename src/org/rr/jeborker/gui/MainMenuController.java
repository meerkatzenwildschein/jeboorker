package org.rr.jeborker.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import org.apache.commons.lang.StringUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.action.ActionFactory;

public class MainMenuController {

	private static MainMenuController controller;
	
	private MainMenuView view;
	
	HashMap<String, Boolean> showHideBasePathToggleStatus = new HashMap<String, Boolean>();

	private MainMenuController() {
	}
	
	/**
	 * Gets the controller instance. because we have only one main window
	 * We have a singleton here.
	 * @return The desired EBorkerMainController.
	 */
	public static MainMenuController getController() {
		if(controller==null) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				LoggerFactory.logWarning(MainController.class, "Could not set system look and feel");
			}
			
			controller = new MainMenuController();
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
	MainMenuView getView() {
		if(view==null) {
			view = new MainMenuView();
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
	 * Adds a menu item having the given path to the base path sub menu.
	 * @param path The path to be added.
	 */
	public void addBasePathMenuEntry(String path) {
		{
			JMenuItem pathItem = new JMenuItem();
			pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_BASE_PATH_ACTION, path));
			view.mnVerzeichnisEntfernen.add(pathItem);
		}
		
		{
			JMenuItem pathItem = new JMenuItem();
			pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_BASE_PATH_ACTION, path));
			view.mnVerzeichnisRefresh.add(pathItem);
		}
	}
	
	/**
	 * Shows the popup menu for the selected entries.
	 * @param location The locaten where the popup should appears.
	 * @param invoker The invoker for the popup menu.
	 */
	void showMainPopupMenu(Point location, Component invoker) {
		List<EbookPropertyItem> selectedItems = MainController.getController().getSelectedEbookPropertyItems();
		JPopupMenu menu = createMainTablePopupMenu(selectedItems);
		
		//setup and show popup
		if(menu.getComponentCount() > 0) {
			menu.setLocation(location);
			menu.show(invoker, location.x, location.y);
		}
	}
	
	/**
	 * Creates the popup menu for the main table having only these entries inside
	 * that can be processed with the given {@link EbookPropertyItem} list.
	 * @param items The items to be tested if they're matching against the menu entries.
	 * @return The desired {@link JPopupMenu}. Never returns <code>null</code>.
	 */
	private static JPopupMenu createMainTablePopupMenu(List<EbookPropertyItem> items) {
		//create and fill popup menu
		final MainController controller = MainController.getController();
		int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();
		final JPopupMenu menu = new JPopupMenu();
		
		JMenu submenu = new JMenu(Bundle.getString("MainMenuController.setMetadataAction"));
		{
			Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.SET_COVER_THUMBNAIL_ACTION, items, selectedEbookPropertyItemRows);
			if(action.isEnabled()) {
				submenu.add(action);
			}
		}
		
		{
			Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.SET_METADATA_AUTHOR_ACTION, items, selectedEbookPropertyItemRows);
			if(action.isEnabled()) {
				submenu.add(new JMenuItem(action));
			}
		}
		
		{
			Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.SET_METADATA_GENRE_ACTION, items, selectedEbookPropertyItemRows);
			if(action.isEnabled()) {
				submenu.add(new JMenuItem(action));
			}
		}	
		
		{
			Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.SET_METADATA_TITLE_ACTION, items, selectedEbookPropertyItemRows);
			if(action.isEnabled()) {
				submenu.add(new JMenuItem(action));
			}
		}	
		
		if(submenu.getMenuComponentCount() > 0) {
			menu.add(submenu);
		}
		
		{
			Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.EDIT_PLAIN_METADATA_ACTION, items, selectedEbookPropertyItemRows);
			if(action.isEnabled()) {
				menu.add(action);
			}
		}
		
		{
			Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.REFRESH_ENTRY_ACTION, items, selectedEbookPropertyItemRows);
			if(action.isEnabled()) {
				menu.add(action);
			}
		}	
		
		if(items.size() == 1) {
			//only visible to single selections
			Action action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, items.get(0).getFile());
			menu.add(action);			
			
			action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, items.get(0).getFile());
			menu.add(action);
		}
		return menu;
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
		List<String> hiddenBasePathEntries = MainMenuController.getController().getHiddenBasePathEntries();
		if(hiddenBasePathEntries.isEmpty()) {
			JeboorkerPreferences.addEntryString("mainMenuBasePathHide", "");
		} else {
			JeboorkerPreferences.addEntryString("mainMenuBasePathHide", ListUtils.join(hiddenBasePathEntries, String.valueOf(File.pathSeparatorChar)));
		}		
	}
	
	void restoreProperties() {
		String basePathPropString = JeboorkerPreferences.getEntryString("mainMenuBasePathHide");
		if(basePathPropString!=null && !basePathPropString.isEmpty()) {
			List<String> split = ListUtils.split(basePathPropString, String.valueOf(File.pathSeparatorChar));
			for(String basePath : split) {
				//setShowHideBasePathStatusShow(basePath, false); //set checkbox value to the view
				ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_HIDE_BASE_PATH_ACTION, basePath).actionPerformed(new ActionEvent(this, 0, "initialize"));
			}
		}
	}
}
