package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.event.RefreshAbstractAction;
import org.rr.jeborker.gui.MainController;

class MultiActionWrapper extends AbstractAction {

	private static final long serialVersionUID = -2128569488506763407L;
	
	public static final String SELECTED_ROWS_TO_REFRESH_KEY = "SELECTED_ROWS_TO_REFRESH_KEY";
	
	public static final String SELECTED_ITEMS_TO_REFRESH_KEY = "SELECTED_ITEMS_TO_REFRESH_KEY";

	private final Action firstActionInstance;
	
	private List<IResourceHandler> handlers;
	
	private  int[] selectedRowsToRefresh;
	
	private  List<EbookPropertyItem> selectedItemsToRefresh;
	
	private HashMap<String, Object> values = new HashMap<String, Object>();

	public MultiActionWrapper(final Class<? extends Action> actionClass, List<EbookPropertyItem> items, final List<IResourceHandler> handlers, int[] selectedRowsToRefresh) {
		this.handlers = handlers;
		this.selectedRowsToRefresh = selectedRowsToRefresh;
		this.firstActionInstance = createInstance(actionClass, (handlers.isEmpty() ? null : handlers.get(0)), selectedRowsToRefresh);
		this.selectedItemsToRefresh = items;
		init();
	}
	
	private void init() {
		putValue(Action.NAME, String.valueOf(firstActionInstance.getValue(Action.NAME)));
		putValue(Action.SMALL_ICON, firstActionInstance.getValue(Action.SMALL_ICON));
		putValue(Action.LARGE_ICON_KEY, firstActionInstance.getValue(Action.LARGE_ICON_KEY));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, firstActionInstance.getValue(ApplicationAction.NON_THREADED_ACTION_KEY));
		putValue(ApplicationAction.SINGLETON_ACTION_KEY, firstActionInstance.getValue(ApplicationAction.SINGLETON_ACTION_KEY));		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void actionPerformed(ActionEvent e) {
		final Iterator<IResourceHandler> iterator = handlers.iterator();
		final int size = handlers.size();
		
		//skip the first and use the firstActionInstance action we have already created with the constructor
		if(iterator.hasNext()) {
			iterator.next();
		}
		
		Object doOnce;
		transferValues(firstActionInstance);
		if(firstActionInstance instanceof IDoOnlyOnceAction<?>) {
			doOnce = ((IDoOnlyOnceAction<?>)firstActionInstance).doOnce();
		} else {
			doOnce = null;
		}
		
		List<Action> actions = new ArrayList<>(size);
		actions.add(this.doActionAt(firstActionInstance, e, 0, size));
		
		//create an action instance for all the other handlers. 
		for (int i = 1; iterator.hasNext(); i++) {
			MainController.getController().getProgressMonitor().setProgress(i, handlers.size());
			IResourceHandler handler = iterator.next();
			Action action = createInstance(firstActionInstance.getClass(), handler, selectedRowsToRefresh);
			
			if(action != null) {
				if(doOnce != null && action instanceof IDoOnlyOnceAction<?>) {
					((IDoOnlyOnceAction)action).setDoOnceResult(doOnce);
				}				
				actions.add(this.doActionAt(action, e, i, size));	
			} else {
				LoggerFactory.logWarning(this, "could not create action for " + handler, null);
			}
		}
		MainController.getController().getProgressMonitor().resetProgress();
		
		//do the finalize for all actions
		if(firstActionInstance instanceof IFinalizeAction) {
			for(int i = 0; i < actions.size() ; i++) {
				Action action = actions.get(i);
				((IFinalizeAction) action).finalizeAction(i);
			}
		}
		
		//clean the results for all action instances
		if(firstActionInstance instanceof IDoOnlyOnceAction) {
			for(int i = 0; i < actions.size() ; i++) {
				Action action = actions.get(i);
				((IDoOnlyOnceAction) action).setDoOnceResult(null);
			}
		}
	}

	/**
	 * Invokes the action method to the given action.
	 * @param action The action to be executed.
	 */
	public Action doActionAt(Action action, ActionEvent e, int entryIndex, int entrySize) {
		if(firstActionInstance instanceof IDoOnlyOnceAction<?>) {
			((IDoOnlyOnceAction<?>)action).prepareFor(entryIndex, entrySize);
		}
		
		action.actionPerformed(e);
		return action;
	}
	
	/**
	 * Creates the action instance from the given class and {@link IResourceHandler}.
	 * @param clazz The class to be instantiated.
	 * @param handler The handler for the first constructor parameter.
	 * @return The desired action.
	 */
	private Action createInstance(final Class<? extends Action> clazz, final IResourceHandler handler, int[] selectedRowsToRefresh) {
		Action objectInstance = (Action) ReflectionUtils.getObjectInstance(clazz, new Object[] {handler});
		if(objectInstance == null) {
			throw new RuntimeException("Could not create instance for " + clazz);
		} else if(objectInstance instanceof RefreshAbstractAction) {
			((RefreshAbstractAction)objectInstance).setSelectedRowsToRefresh(selectedRowsToRefresh);
		}
		transferValues(objectInstance);
		return objectInstance;
	}

	@Override
	public void putValue(String key, Object newValue) {
		super.putValue(key, newValue);
		values.put(key, newValue);
	}
	
	@Override
	public Object getValue(String key) {
		if(super.getValue(key) != null) {
			return super.getValue(key);
		}
		return firstActionInstance.getValue(key);
	}	
	
	private void transferValues(Action objectInstance) {
		objectInstance.putValue(SELECTED_ROWS_TO_REFRESH_KEY, this.selectedRowsToRefresh);
		objectInstance.putValue(SELECTED_ITEMS_TO_REFRESH_KEY, this.selectedItemsToRefresh);
		for (Entry<String, Object> e : values.entrySet()) {
			objectInstance.putValue(e.getKey(), e.getValue());
		}	
	}

}
