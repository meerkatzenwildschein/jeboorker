package org.rr.jeborker.gui;

import java.awt.Component;
import java.awt.Point;
import java.io.File;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import org.apache.commons.lang.StringUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.action.ActionFactory;

public class JEborkerMainMenuController {

	private static JEborkerMainMenuController controller;
	
	private JEborkerMainMenuView view;

	private JEborkerMainMenuController() {
	}
	
	/**
	 * Gets the controller instance. because we have only one main window
	 * We have a singleton here.
	 * @return The desired EBorkerMainController.
	 */
	public static JEborkerMainMenuController getController() {
		if(controller==null) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				LoggerFactory.logWarning(JEBorkerMainController.class, "Could not set system look and feel");
			}
			
			controller = new JEborkerMainMenuController();
		}
		return controller;
	}
	
	/**
	 * Get the menu view which is a {@link JMenuBar} instance.
	 * @return The menu view.
	 */
	JEborkerMainMenuView getView() {
		if(view==null) {
			view = new JEborkerMainMenuView();
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
		List<EbookPropertyItem> selectedItems = JEBorkerMainController.getController().getSelectedEbookPropertyItems();
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
		final JEBorkerMainController controller = JEBorkerMainController.getController();
		int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();
		final JPopupMenu menu = new JPopupMenu();
		
		{
			Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.SET_COVER_THUMBNAIL_ACTION, items, selectedEbookPropertyItemRows);
			if(action.isEnabled()) {
				menu.add(action);
			}
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
		return menu;
	}	
}
