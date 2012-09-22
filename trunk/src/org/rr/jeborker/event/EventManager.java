package org.rr.jeborker.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rr.commons.collection.WeakList;


public class EventManager {
	
	public static enum EVENT_TYPES {
		EBOOK_ITEM_SELECTION_CHANGE, METADATA_SHEET_SELECTION_CHANGE
	}
 
	private static WeakList<ApplicationEventListener> weakListenerList = new WeakList<ApplicationEventListener>();
	
	private static List<ApplicationEventListener> listenerList = Collections.synchronizedList(new ArrayList<ApplicationEventListener>());
	
	/**
	 * Adds a weak {@link ApplicationEventListener} listener which will be automatically removed if no hard reference exists anywhere.
	 * @param listener The listener instance to be added.
	 */
	public static synchronized void addWeakListener(ApplicationEventListener listener) {
		weakListenerList.add(listener);
	}
	
	/**
	 * Adds an {@link ApplicationEventListener} to the listener list which is invoked with the {@link #fireEvent(EVENT_TYPES, ApplicationEvent)} method.
	 * @param listener The listener instance to be added.
	 */
	public static synchronized void addListener(ApplicationEventListener listener) {
		listenerList.add(listener);
	}
	
	public static synchronized void removeListener(ApplicationEventListener listener) {
		weakListenerList.remove(listener);
		listenerList.remove(listener);
	}
	
	/**
	 * Fires the event with the specified {@link EVENT_TYPES} to all registered listeners.
	 * @param type The type of event to be fired.
	 * @param evt The event.
	 */
	public static synchronized void fireEvent(EVENT_TYPES type, ApplicationEvent evt) {
		weakListenerList.trimToSize();
		for (ApplicationEventListener listener : weakListenerList) {
			fireEvent(type, evt, listener);
		}
		for (ApplicationEventListener listener : listenerList) {
			fireEvent(type, evt, listener);
		}
	}

	/**
	 * executes the specified event on the given {@link ApplicationEventListener}
	 * @param type The type of event to be fired. 
	 * @param evt The {@link ApplicationEvent} containing the current application values.
	 * @param listener The listener where the event method should be invoked.
	 */
	private static void fireEvent(EVENT_TYPES type, ApplicationEvent evt, ApplicationEventListener listener) {
		switch(type) {
		case EBOOK_ITEM_SELECTION_CHANGE:
			listener.ebookItemSelectionChanged(evt);
			break;
		case METADATA_SHEET_SELECTION_CHANGE:
			listener.metaDataSheetSelectionChanged(evt);
			break;					
		}
	}
}
