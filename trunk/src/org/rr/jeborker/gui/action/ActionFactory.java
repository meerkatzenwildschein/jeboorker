package org.rr.jeborker.gui.action;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

public class ActionFactory {
	
	private static interface ActionType {
		/**
		 * Get the class for the action represented by this enum entry. 
		 */
		Class<? extends Action> getActionClass();
		
		/**
		 * Tells if the action is able to handle the given resource.
		 * @param resourceHandler The resource to be tested.
		 * @return <code>true</code> if the resource can be handled by the action or <code>false</code> if not.
		 */
		boolean canHandle(final IResourceHandler resourceHandler);
	}
	
	public static enum COMMON_ACTION_TYPES {
		ADD_BASE_PATH_ACTION, REMOVE_BASE_PATH_ACTION, REFRESH_BASE_PATH_ACTION, SHOW_HIDE_BASE_PATH_ACTION, REFRESH_ENTRY_ACTION, QUIT_ACTION, SEARCH_ACTION, REMOVE_METADATA_ENTRY_ACTION, SAVE_METADATA_ACTION, OPEN_FOLDER_ACTION,
		OPEN_FILE_ACTION, DELETE_FILE_ACTION
	}
	
	public static enum DYNAMIC_ACTION_TYPES implements ActionType {
		SET_COVER_FROM_FILE_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return SetCoverFromFileAction.class;
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return MetadataHandlerFactory.hasCoverWriterSupport(resourceHandler);
			}
		},
		SET_COVER_FROM_DOWNLOAD_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return SetCoverFromDownload.class;
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return MetadataHandlerFactory.hasCoverWriterSupport(resourceHandler);
			}
		},		
		EDIT_PLAIN_METADATA_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return EditPlainMetadataAction.class;
			}
			
			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return MetadataHandlerFactory.hasPlainMetadataSupport(resourceHandler);
			}
		},
		REFRESH_ENTRY_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return RefreshEntryAction.class;
			}
			
			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return true;
			}
		},
		DELETE_FILE_ACTION {
			
			@Override
			public Class<? extends Action> getActionClass() {
				return DeleteFileAction.class;
			}
			
			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return resourceHandler.isFileResource();
			}			
		}
	}	
	
	public static ApplicationAction getAction(final COMMON_ACTION_TYPES type, final String text) {
		Action action = null;
		switch(type) {
			case ADD_BASE_PATH_ACTION:
				action = new AddBasePathAction(text);
				break;
			case REMOVE_BASE_PATH_ACTION:
				action = new RemoveBasePathAction(text);
				break;
			case REFRESH_BASE_PATH_ACTION:
				action = new RefreshBasePathAction(text);
				break;
			case SHOW_HIDE_BASE_PATH_ACTION:
				action = new ShowHideBasePathAction(text);
				break;
			case REFRESH_ENTRY_ACTION:
				action = new RefreshEntryAction(text);
				break;
			case QUIT_ACTION:
				action = new QuitAction(text);
				break;
			case SEARCH_ACTION:
				action = new SearchAction();
				break;
			case REMOVE_METADATA_ENTRY_ACTION:
				action = RemoveMetadataEntryAction.getInstance();
				break;
			case SAVE_METADATA_ACTION:
				action = SaveMetadataAction.getInstance();
				break;
			case OPEN_FOLDER_ACTION:
				action = new OpenFolderAction(text);
				break;
			case OPEN_FILE_ACTION:
				action = new OpenFileAction(text);
				break;
			case DELETE_FILE_ACTION:
				action = new DeleteFileAction(text);			
				break;
		}
		
		if(action != null) {
			return ApplicationAction.getInstance(action);
		} 
		return null;
	}
	
	/**
	 * Get the action of the specific type. If the action could not handle all of the given items
	 * it is disabled.
	 * @param type The type of action.
	 * @param items The items to be handled by the actions.
	 * @return The desired action instance. Never returns <code>null</code>.
	 */
	public static ApplicationAction getActionForItems(final DYNAMIC_ACTION_TYPES type, final List<EbookPropertyItem> items, int[] refreshRowsAfter) {
		final ArrayList<IResourceHandler> resourceHandlers = new ArrayList<IResourceHandler>(items.size()); 
		
		//create the resource handle list.
		for (EbookPropertyItem item : items) {
			IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceLoader(item.getFile());
			resourceHandlers.add(resourceHandler);
		}
		
		//test whether the Action is able to handle all given items.
		boolean canHandle = true;
		if(!resourceHandlers.isEmpty()) {
			for (IResourceHandler resourceHandler : resourceHandlers) {
				if(!type.canHandle(resourceHandler)) {
					canHandle = false;
					break;
				} 
			}
		} else {
			//no selection
			canHandle = false;
		}
		
		Action action = new MultiActionWrapper(type.getActionClass(), resourceHandlers, refreshRowsAfter);
		if(!canHandle) {
			action.setEnabled(false);
		}
		
		return ApplicationAction.getInstance(action); 
	}
	
	/**
	 * Get a list of actions - one for each supported metadata entry - which
	 * allows to add metadata. 
	 * @param properties The metadata from the {@link IMetadataReader} where the actions 
	 * should be created for.
	 * @return The actions for adding a metadata entry. Never returns <code>null</code>.
	 */
	public static List<Action> getAddMetadataActions(List<MetadataProperty> properties, EbookPropertyItem item) {
		final ArrayList<Action> result = new ArrayList<Action>();
		for (MetadataProperty property : properties) {
			AddMetadataAction addMetadataAction = new AddMetadataAction(property, item);
			if(!property.isEditable()) {
				addMetadataAction.setEnabled(false);
			}
			result.add(ApplicationAction.getInstance(addMetadataAction));
		}
		return result;
	}
}
