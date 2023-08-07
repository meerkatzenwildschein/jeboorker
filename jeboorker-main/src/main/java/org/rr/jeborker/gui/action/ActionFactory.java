package org.rr.jeborker.gui.action;

import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_EPUB;
import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_MOBI;
import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_AZW;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.JTable;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.converter.ConverterFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

import skt.swing.search.FindAction;
import skt.swing.search.TableFindAction;

public class ActionFactory {

	private static FindAction tableFindAction;

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
		boolean canHandle(final EbookPropertyItem item);

		boolean canHandle(final IResourceHandler resourceHandler);

		/**
		 * Tells if the action is able to handle multiple selections.
		 * @return <code>true</code> if multi select is supported and <code>false</code> otherwise.
		 */
		boolean hasMultipleSelectionSupport();
	}

	public static enum COMMON_ACTION_TYPES {
		ADD_BASE_PATH_ACTION, REMOVE_BASE_PATH_ACTION, REFRESH_BASE_PATH_ACTION, SHOW_HIDE_BASE_PATH_ACTION, REFRESH_ENTRY_ACTION, QUIT_ACTION, SEARCH_ACTION, REMOVE_METADATA_ENTRY_ACTION, SAVE_METADATA_ACTION, SYNC_FOLDER_ACTION, OPEN_FOLDER_ACTION, NEW_FOLDER_ACTION,
		OPEN_FILE_ACTION, RENAME_FILE_ACTION, DELETE_FILE_ACTION, VIEW_LOG_MONITOR_ACTION, VIEW_ABOUT_DIALOG_ACTION, SHOW_METADATA_DOWNLOAD_ACTION, SHOW_PDF_SCISSORS_ACTION, SHOW_PREFERENCE_DIALOG_ACTION, COPY_TO_CLIPBOARD_ACTION, PASTE_FROM_CLIPBOARD_ACTION, FILE_SYSTEM_REFRESH_ACTION, FILE_SYSTEM_COLLAPSE_ALL_ACTION, FILE_SYSTEM_IMPORT_ACTION, MOVE_BETWEEN_BASE_FOLDER_ACTION,
		CHANGE_LOOK_AND_FEEL_ACTION, MERGE_DOCUMENT_ACTION

	}

	public static enum DYNAMIC_ACTION_TYPES implements ActionType {
		SET_COVER_FROM_FILE_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return SetCoverFromFileAction.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				return MetadataHandlerFactory.hasCoverWriterSupport(item.getResourceHandler());
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return false;
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return true;
			}
		},
		SET_COVER_FROM_DOWNLOAD_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return SetCoverFromDownload.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				return MetadataHandlerFactory.hasCoverWriterSupport(item.getResourceHandler());
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return false;
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return true;
			}
		},
		SET_COVER_FROM_CLIPBOARD_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return SetCoverFromClipboardAction.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
				DataFlavor[] availableDataFlavors = c.getAvailableDataFlavors();
				for(DataFlavor flavor : availableDataFlavors) {
					if(flavor.equals(DataFlavor.imageFlavor)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return false;
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return true;
			}
		},
		SET_COVER_FROM_EBOOK_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return SetCoverFromEbook.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				String mime = item.getMimeType();
				if(mime != null) {
					if(mime.equals(MIME_EPUB.getMime()) || mime.equals(MIME_MOBI.getMime()) || mime.equals(MIME_AZW.getMime())) {
						return MetadataHandlerFactory.hasCoverWriterSupport(item.getResourceHandler());
					}
				}
				return false;
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return false;
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return false;
			}
		},
		SAVE_COVER_TO_FILE_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return SaveCoverToFileAction.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				return MainController.getController().getImageViewerResource() != null;
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return false;
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return false;
			}
		}, SAVE_COVER_TO_CLIPBOARD_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return SaveCoverToClipboardAction.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				return MainController.getController().getImageViewerImage() != null;
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return false;
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return false;
			}
		},
		EDIT_PLAIN_METADATA_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return EditPlainMetadataAction.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				return MetadataHandlerFactory.hasPlainMetadataSupport(item.getResourceHandler());
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return false;
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return true;
			}
		},
		REFRESH_ENTRY_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return RefreshEntryAction.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				return true;
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return false;
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return true;
			}
		},
		DELETE_FILE_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return DeleteFileAction.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				return item.getResourceHandler().exists();
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return resourceHandler.exists();
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return true;
			}
		},
		RENAME_FILE_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return RenameFileAction.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				return item.getResourceHandler().exists();
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return resourceHandler.exists();
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return true;
			}
		},
		COPY_TO_DROPBOX_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return CopyToDropboxApiFolderAction.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				return item.getResourceHandler().isFileResource();
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return resourceHandler.isFileResource();
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return true;
			}
		},
		COPY_TO_TARGET_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return CopyToTargetAction.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				return item.getResourceHandler().isFileResource();
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return resourceHandler.isFileResource();
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return true;
			}
		},
		COPY_TO_CLIPBOARD_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return CopyToClipboardAction.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				return true;
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return resourceHandler.isFileResource();
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return true;
			}
		},
		PASTE_FROM_CLIPBOARD_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return PasteFromClipboardAction.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				return PasteFromClipboardAction.hasValidClipboardContent() &&
				item.getResourceHandler().isFileResource();
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return PasteFromClipboardAction.hasValidClipboardContent();
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return false;
			}
		},
		CONVERT_EBOOK_ACTION {

			@Override
			public Class<? extends Action> getActionClass() {
				return ConvertEbookAction.class;
			}

			@Override
			public boolean canHandle(EbookPropertyItem item) {
				return !ConverterFactory.getConverter(item.getResourceHandler()).isEmpty();
			}

			@Override
			public boolean canHandle(IResourceHandler resourceHandler) {
				return false;
			}

			@Override
			public boolean hasMultipleSelectionSupport() {
				return true;
			}
		}
	}

	public static ApplicationAction getAction(COMMON_ACTION_TYPES type, ActionCallback callback, String text) {
		return getAction(type, text, callback);
	}

	public static ApplicationAction getAction(COMMON_ACTION_TYPES type, String text, ActionCallback callback) {
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
				action = SearchAction.getInstance();
				break;
			case REMOVE_METADATA_ENTRY_ACTION:
				action = RemoveMetadataEntryAction.getInstance();
				break;
			case SAVE_METADATA_ACTION:
				action = SaveMetadataAction.getInstance();
				break;
			case SYNC_FOLDER_ACTION:
				action = SyncFolderAction.getInstance();
				break;
			case OPEN_FOLDER_ACTION:
				action = new OpenFolderAction(text);
				break;
			case NEW_FOLDER_ACTION:
				action = new NewFolderAction(text);
				break;
			case OPEN_FILE_ACTION:
				action = new OpenFileAction(text);
				setEnabledForFile(text, action);
				break;
			case RENAME_FILE_ACTION:
				action = new RenameFileAction(text);
				setEnabledForFile(text, action);
				break;
			case DELETE_FILE_ACTION:
				action = new DeleteFileAction(text);
				break;
			case VIEW_LOG_MONITOR_ACTION:
				action = new ShowLogDialogAction(text);
				break;
			case VIEW_ABOUT_DIALOG_ACTION:
				action = new ShowAboutDialogAction();
				break;
			case CHANGE_LOOK_AND_FEEL_ACTION:
				action = new ChangeLookAndFeelAction(text);
				break;
			case SHOW_METADATA_DOWNLOAD_ACTION:
				action = new ShowMetadataDownloadDialogAction(text);
				break;
			case SHOW_PDF_SCISSORS_ACTION:
				action = new ShowPdfScissorsAction(text);
				break;
			case SHOW_PREFERENCE_DIALOG_ACTION:
				action = new ShowPreferenceDialogAction(text);
				break;
			case MERGE_DOCUMENT_ACTION:
				action = new MergeDocumentAction(text);
				break;
			case COPY_TO_CLIPBOARD_ACTION:
				action = new CopyToClipboardAction();
				break;
			case PASTE_FROM_CLIPBOARD_ACTION:
				action = new PasteFromClipboardAction(text);
				if(!PasteFromClipboardAction.hasValidClipboardContent()) {
					action.setEnabled(false);
				}
				break;
			case FILE_SYSTEM_REFRESH_ACTION:
				action = new FileSystemTreeRefreshAction(text);
				break;
			case FILE_SYSTEM_COLLAPSE_ALL_ACTION:
				action = new FileSystemCollapseAllAction(text);
				break;
			case FILE_SYSTEM_IMPORT_ACTION:
				action = new FileSystemImportAction(text);
				break;
			case MOVE_BETWEEN_BASE_FOLDER_ACTION:
				action = new MoveBetweenBaseFolderAction(text);
				break;
		}

		if(action != null) {
			return ApplicationAction.getInstance(action, callback);
		}
		return null;
	}

	public static ApplicationAction getAction(final COMMON_ACTION_TYPES type, final String text) {
		return getAction(type, text, null);
	}

	/**
	 * Set the given action to disabled if the given <code>text</code>did not represents a file.
	 * @param text A resource which can be parsed into a file.
	 * @param action The action which should be disabled if the given <code>text</code> is not an existing file.
	 */
	private static void setEnabledForFile(final String text, Action action) {
		IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(text);
		if(resourceHandler != null && !resourceHandler.isFileResource()) {
			action.setEnabled(false);
		}
	}

	public static ApplicationAction getActionForResource(final DYNAMIC_ACTION_TYPES type, List<IResourceHandler> resourceHandlers) {
		return getActionForResource(type, resourceHandlers, null);
	}

	public static ApplicationAction getActionForResource(final DYNAMIC_ACTION_TYPES type, List<IResourceHandler> resourceHandlers, ActionCallback callback) {
		resourceHandlers = resourceHandlers != null ? resourceHandlers : Collections.<IResourceHandler>emptyList();

		//create the resource handle list.
		boolean canHandle = true;
		for (int i = 0; i< resourceHandlers.size(); i++) {
			IResourceHandler resourceHandler = resourceHandlers.get(i);
			if(!type.canHandle(resourceHandler)) {
				canHandle = false;
			}
		}

		if(!type.hasMultipleSelectionSupport() && resourceHandlers.size() > 1) {
			canHandle = false;
		}

		//test whether the Action is able to handle all given items.
		if(resourceHandlers.isEmpty()) {
			//no selection
			canHandle = false;
		}

		Action action = new MultiActionWrapper(type.getActionClass(), null, resourceHandlers, null);
		if(!canHandle) {
			action.setEnabled(false);
		}

		return ApplicationAction.getInstance(action, callback);
	}

	public static ApplicationAction getActionForItems(final DYNAMIC_ACTION_TYPES type, final List<EbookPropertyItem> items, int[] refreshRowsAfter) {
		return getActionForItems(type, items, refreshRowsAfter, null);
	}

	/**
	 * Get the action of the specific type. If the action could not handle all of the given items
	 * it is disabled.
	 * @param type The type of action.
	 * @param items The items to be handled by the actions.
	 * @return The desired action instance. Never returns <code>null</code>.
	 */
	public static ApplicationAction getActionForItems(final DYNAMIC_ACTION_TYPES type, final List<EbookPropertyItem> items, int[] refreshRowsAfter, ActionCallback callback) {
		final ArrayList<IResourceHandler> resourceHandlers = new ArrayList<>(items.size());

		//create the resource handle list.
		boolean canHandle = true;
		for (EbookPropertyItem item : items) {
			resourceHandlers.add(item.getResourceHandler());
			if(!type.canHandle(item)) {
				canHandle = false;
			}
		}

		if(!type.hasMultipleSelectionSupport() && items.size() > 1) {
			canHandle = false;
		}

		//test whether the Action is able to handle all given items.
		if(items.isEmpty()) {
			//no selection
			canHandle = false;
		}

		Action action = new MultiActionWrapper(type.getActionClass(), items, resourceHandlers, refreshRowsAfter);
		if(!canHandle) {
			action.setEnabled(false);
		}

		return ApplicationAction.getInstance(action, callback);
	}

	/**
	 * Get a list of actions - one for each supported metadata entry - which
	 * allows to add metadata.
	 * @param properties The metadata from the {@link IMetadataReader} where the actions
	 * should be created for.
	 * @return The actions for adding a metadata entry. Never returns <code>null</code>.
	 */
	public static List<Action> getAddMetadataActions(List<MetadataProperty> properties, EbookPropertyItem item, ActionCallback callback) {
		final ArrayList<Action> result = new ArrayList<>();
		for (MetadataProperty property : properties) {
			AddMetadataAction addMetadataAction = new AddMetadataAction(property, item);
			if(!property.isEditable()) {
				addMetadataAction.setEnabled(false);
			}
			result.add(ApplicationAction.getInstance(addMetadataAction, callback));
		}
		return result;
	}

	/**
	 * Get the Action that is used for the find action in the main table.
	 */
	public static FindAction getTableFindAction(JTable table) {
		if(tableFindAction == null) {
			tableFindAction = new TableFindAction(table);
		}
		return tableFindAction;
	}

}
