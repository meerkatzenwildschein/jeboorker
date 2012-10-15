package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.jeborker.event.RefreshAbstractAction;

class MultiActionWrapper extends AbstractAction {

	private static final long serialVersionUID = -2128569488506763407L;

	private final Action firstActionInstance;
	
	private List<IResourceHandler> handlers;
	
	private  int[] selectedRowsToRefresh;
	
	public MultiActionWrapper(final Class<? extends Action> actionClass, final List<IResourceHandler> handlers, int[] selectedRowsToRefresh) {
		this.handlers = handlers;
		this.selectedRowsToRefresh = selectedRowsToRefresh;
		this.firstActionInstance = createInstance(actionClass, (handlers.isEmpty() ? null : handlers.get(0)), selectedRowsToRefresh);
		
		putValue(Action.NAME, String.valueOf(firstActionInstance.getValue(Action.NAME)));
		putValue(Action.SMALL_ICON, firstActionInstance.getValue(Action.SMALL_ICON));
		putValue(Action.LARGE_ICON_KEY, firstActionInstance.getValue(Action.LARGE_ICON_KEY));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, firstActionInstance.getValue(ApplicationAction.NON_THREADED_ACTION_KEY));
		putValue(ApplicationAction.SINGLETON_ACTION_KEY, firstActionInstance.getValue(ApplicationAction.SINGLETON_ACTION_KEY));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final Iterator<IResourceHandler> iterator = handlers.iterator();
		final int size = handlers.size();
		
		//skip the first and use the firstActionInstance action we have already created with the constructor
		iterator.next();
		
		Object doOnce;
		if(firstActionInstance instanceof IDoOnlyOnceAction<?>) {
			doOnce = ((IDoOnlyOnceAction<?>)firstActionInstance).doOnce();
		} else {
			doOnce = null;
		}
		
		this.doActionAt(firstActionInstance, e, 0, size);
		
		//create an action instance for all the other handlers. 
		for (int i=1; iterator.hasNext(); i++) {
			IResourceHandler handler = iterator.next();
			Action action = createInstance(firstActionInstance.getClass(), handler, selectedRowsToRefresh);
			
			if(action!=null) {
				if(doOnce!=null && action instanceof IDoOnlyOnceAction<?>) {
					((IDoOnlyOnceAction)action).setDoOnceResult(doOnce);
				}				
				this.doActionAt(action, e, i, size);			
			} else {
				LoggerFactory.logWarning(this, "could not create action for " + handler, null);
			}
		}
	}

	/**
	 * Invokes the action method to the given action.
	 * @param action The action to be executed.
	 */
	public void doActionAt(Action action, ActionEvent e, int entryIndex, int entrySize) {
		if(firstActionInstance instanceof IDoOnlyOnceAction<?>) {
			((IDoOnlyOnceAction<?>)action).prepareFor(entryIndex, entrySize);
		}
		
		action.actionPerformed(e);
	}
	
	/**
	 * Creates the action instance from the given class and {@link IResourceHandler}.
	 * @param clazz The class to be instantiated.
	 * @param handler The handler for the first constructor parameter.
	 * @return The desired action.
	 */
	private Action createInstance(final Class<? extends Action> clazz, final IResourceHandler handler, int[] selectedRowsToRefresh) {
		Action objectInstance = (Action) ReflectionUtils.getObjectInstance(clazz, new Object[] {handler});
		if(objectInstance instanceof RefreshAbstractAction) {
			((RefreshAbstractAction)objectInstance).setSelectedRowsToRefresh(selectedRowsToRefresh);
		}
		return objectInstance;
	}
}
