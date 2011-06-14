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
		ADD_BASE_PATH_ACTION, REMOVE_BASE_PATH_ACTION, REFRESH_BASE_PATH_ACTION, REFRESH_ENTRY_ACTION, QUIT_ACTION, SEARCH_ACTION, REMOVE_METADATA_ENTRY_ACTION
	}
	
	public static enum DYNAMIC_ACTION_TYPES implements ActionType {
		SET_COVER_THUMBNAIL_ACTION {

			public Class<? extends Action> getActionClass() {
				return SetCoverThumbnailAction.class;
			}

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
		}
	}	
	
	public static Action getAction(final COMMON_ACTION_TYPES type, final String text) {
		switch(type) {
			case ADD_BASE_PATH_ACTION:
				return new AddBasePathAction(text);
			case REMOVE_BASE_PATH_ACTION:
				return new RemoveBasePathAction(text);
			case REFRESH_BASE_PATH_ACTION:
				return new RefreshBasePathAction(text);
			case REFRESH_ENTRY_ACTION:
				return new RefreshEntryAction(text);				
			case QUIT_ACTION:
				return new QuitAction(text);
			case SEARCH_ACTION:
				return new SearchAction();
			case REMOVE_METADATA_ENTRY_ACTION:
				return RemoveMetadataEntryAction.getInstance();			
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
	public static Action getActionForItems(final DYNAMIC_ACTION_TYPES type, final List<EbookPropertyItem> items, int[] refreshRowsAfter) {
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
		
		Action result = new MultiActionWrapper(type.getActionClass(), resourceHandlers, refreshRowsAfter);
		if(!canHandle) {
			result.setEnabled(false);
		}
		
		return result; 
	}
	
	/**
	 * Get a list of actions - one for each supported metadata entry - which
	 * allows to add a metadata. 
	 * @param properties The metadata from the {@link IMetadataReader} where the actions 
	 * should be created for.
	 * @return The actions for adding a metadata entry. Never returns <code>null</code>.
	 */
	public static List<Action> getAddMetadataActions(List<MetadataProperty> properties) {
		final ArrayList<Action> result = new ArrayList<Action>();
		for (MetadataProperty property : properties) {
			AddMetadataAction addMetadataAction = new AddMetadataAction(property);
			if(!property.isEditable()) {
				addMetadataAction.setEnabled(false);
			}
			result.add(addMetadataAction);
		}
		return result;
	}
}
