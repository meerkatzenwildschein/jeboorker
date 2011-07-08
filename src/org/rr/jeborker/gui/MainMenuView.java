package org.rr.jeborker.gui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.action.ActionFactory;

class MainMenuView extends JMenuBar {

	private static final long serialVersionUID = -8134987169763660105L;
	
	JMenu fileMenuBar;

	JMenu mnVerzeichnisEntfernen;

	JMenu mnVerzeichnisRefresh;
	
	JMenu metadataMenuBar;

	MainMenuView() {
		this.init();
	}
	
	private void init() {
		add(createFileMenu());
		add(createMetadataMenuEntry());
	}
	
	/**
	 * Creates the metadata menu entry.
	 * @return The new created menu entry.
	 */
	private JMenu createMetadataMenuEntry() {
		metadataMenuBar = new JMenu(Bundle.getString("EborkerMainView.metadata"));
		metadataMenuBar.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				final MainController controller = MainController.getController();
				final List<EbookPropertyItem> selectedItems = controller.getSelectedEbookPropertyItems();
				int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();
				final List<Action> menuActions = createDynamicMetadataMenuEntries(selectedItems, selectedEbookPropertyItemRows);
				
				metadataMenuBar.removeAll();
				for (Action menuAction : menuActions) {
					metadataMenuBar.add(menuAction);
				}
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		return metadataMenuBar;
	}
	
	/**
	 * Creates the menu entries for the metadata menu entry. 
	 * @param items Items for the menu items.
	 * @return The list of menu entries.
	 */
	private List<Action> createDynamicMetadataMenuEntries(List<EbookPropertyItem> items, int[] rowsToRefreshAfter) {
		ArrayList<Action> result = new ArrayList<Action>();
		Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.SET_COVER_THUMBNAIL_ACTION, items, rowsToRefreshAfter);
		result.add(action);
		
		action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.EDIT_PLAIN_METADATA_ACTION, items, rowsToRefreshAfter);
		result.add(action);		
		
		action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.REFRESH_ENTRY_ACTION, items, rowsToRefreshAfter);
		result.add(action);				
		return result;
	}
	
	/**
	 * Creates the file menu entry with it's menu items.
	 * @return The new created menu entry.
	 */
	private JMenu createFileMenu() {
		this.fileMenuBar = new JMenu(Bundle.getString("EborkerMainView.file"));
		
		this.fileMenuBar.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				final MainController controller = MainController.getController();
				final List<EbookPropertyItem> selectedItems = controller.getSelectedEbookPropertyItems();
				final List<Component> menuActions = createDynamicFileMenu(selectedItems);
				
				fileMenuBar.removeAll();
				for (Component menuAction : menuActions) {
					fileMenuBar.add(menuAction);
				}
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		

		
		return fileMenuBar;
	}

	/**
	 * Creates all menu entries for the file menu. This method is always invoked
	 * if the file menu is opened.
	 * @param items Currently selected items.
	 * @return The menu entries for the file menu.
	 */
	private List<Component> createDynamicFileMenu(List<EbookPropertyItem> selectedEbookPropertyItems) {
		ArrayList<Component> fileMenuBar = new ArrayList<Component>();
		
		JMenuItem mntmAddEbooks = new JMenuItem();
		mntmAddEbooks.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.ADD_BASE_PATH_ACTION, null));
		fileMenuBar.add(mntmAddEbooks);
		
		final List<String> basePath = JeboorkerPreferences.getBasePath();
		mnVerzeichnisEntfernen = new JMenu(Bundle.getString("EborkerMainView.removeBasePath"));
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

		mnVerzeichnisRefresh = new JMenu(Bundle.getString("EborkerMainView.refreshBasePath"));
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
		
		fileMenuBar.add(new JSeparator());
		
		//Open folder only for single selection.
		final JMenuItem openFolderMenuEntry;
		final JMenuItem openFileMenuEntry;
		if(selectedEbookPropertyItems.size() == 1) {
			openFolderMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, selectedEbookPropertyItems.get(0).getFile()));
			openFolderMenuEntry.setEnabled(true);
			
			openFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, selectedEbookPropertyItems.get(0).getFile()));
			openFileMenuEntry.setEnabled(true);
		} else {
			openFolderMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, ""));
			openFolderMenuEntry.setEnabled(false);
			
			openFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, ""));
			openFileMenuEntry.setEnabled(false);			
		}
		fileMenuBar.add(openFileMenuEntry);
		fileMenuBar.add(openFolderMenuEntry);
		
		fileMenuBar.add(new JSeparator());
		
		//quit menu entry
		JMenuItem mntmQuit = new JMenuItem();
		mntmQuit.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.QUIT_ACTION, null));
		fileMenuBar.add(mntmQuit);
		
		return fileMenuBar;
	}

}
