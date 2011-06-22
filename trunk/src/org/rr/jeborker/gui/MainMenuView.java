package org.rr.jeborker.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.rr.jeborker.JEBorkerPreferences;
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
		add(createFileMenuEntry());
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
	private JMenu createFileMenuEntry() {
		this.fileMenuBar = new JMenu(Bundle.getString("EborkerMainView.file"));
		
		JMenuItem mntmAddEbooks = new JMenuItem();
		mntmAddEbooks.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.ADD_BASE_PATH_ACTION, null));
		fileMenuBar.add(mntmAddEbooks);
		
		final List<String> basePath = JEBorkerPreferences.getBasePath();
		mnVerzeichnisEntfernen = new JMenu(Bundle.getString("EborkerMainView.removeBasePath"));
		for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
			String path = iterator.next();
			JMenuItem pathItem = new JMenuItem();
			pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_BASE_PATH_ACTION, path));
			mnVerzeichnisEntfernen.add(pathItem);
		}
		fileMenuBar.add(mnVerzeichnisEntfernen);
		
		mnVerzeichnisRefresh = new JMenu(Bundle.getString("EborkerMainView.refreshBasePath"));
		for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
			String path = iterator.next();
			JMenuItem pathItem = new JMenuItem();
			pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_BASE_PATH_ACTION, path));
			mnVerzeichnisRefresh.add(pathItem);
		}
		fileMenuBar.add(mnVerzeichnisRefresh);		
		
		JMenuItem mntmQuit = new JMenuItem();
		mntmQuit.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.QUIT_ACTION, null));
		fileMenuBar.add(mntmQuit);
		
		return fileMenuBar;
	}

}
