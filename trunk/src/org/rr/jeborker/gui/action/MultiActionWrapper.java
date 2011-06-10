package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.jeborker.event.QueueableAction;
import org.rr.jeborker.event.RefreshAbstractAction;

public class MultiActionWrapper extends QueueableAction {

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
	}
	
	@Override
	public void doAction(ActionEvent e) {
		Iterator<IResourceHandler> iterator = handlers.iterator();
		
		//skip the first and use the action we have already created with the constructor
		iterator.next();
		if(firstActionInstance instanceof QueueableAction) {
			((QueueableAction)firstActionInstance).doAction(e);
		} else {
			firstActionInstance.actionPerformed(e);
		}
		
		//create an action instance for all the other handlers. 
		while (iterator.hasNext()) {
			IResourceHandler handler = iterator.next();
			Action action = createInstance(firstActionInstance.getClass(), handler, selectedRowsToRefresh);
			
			if(action!=null) {
				if(firstActionInstance instanceof QueueableAction) {
					((QueueableAction)action).doAction(e);
				} else {
					action.actionPerformed(e);
				}				
			} else {
				LoggerFactory.logWarning(this, "could not create action for " + handler, null);
			}
		}
	}
	
	/**
	 * Creates the action instance from the given class and {@link IResourceHandler}.
	 * @param clazz The class to be instanciated.
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
