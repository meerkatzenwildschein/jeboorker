package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.rr.commons.utils.compression.truezip.TrueZipUtils;
import org.rr.jeborker.FileRefreshBackgroundThread;

/**
 * {@link ApplicationAction} is an Action delegate which is delivered if
 * an action is requested from the {@link ActionFactory}.
 */
public class ApplicationAction extends AbstractAction {

	/**
	 * Can be used as key for the actions to get marked as singleton.  
	 * Example: <code>putValue(ApplicationAction.SINGLETON_ACTION_KEY, Boolean.TRUE);</code>
	 */
	public static final String SINGLETON_ACTION_KEY = "singletonAction";
	
	/**
	 * Can be used as key for the actions to get marked they're not invoked in a separate thread.  
	 * Example: <code>putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE);</code>
	 */
	public static final String NON_THREADED_ACTION_KEY = "nonThreadedAction"; 
	
	private static final HashMap<Class<?>, ApplicationAction> singletonInstances = new HashMap<Class<?>, ApplicationAction>();
	
	private final Action realAction;
	
	public static ApplicationAction getInstance(Action realAction) {
		Object isSingleton = realAction.getValue(SINGLETON_ACTION_KEY);
		if(isSingleton instanceof Boolean && ((Boolean)isSingleton).booleanValue()) {
			Class<?> realActionClass = realAction.getClass();
			if(singletonInstances.containsKey(realActionClass)) {
				return singletonInstances.get(realActionClass);
			} else {
				ApplicationAction action = new ApplicationAction(realAction);
				singletonInstances.put(realActionClass, action);
				return action;
			}
		}
		return new ApplicationAction(realAction);
	}
	
	private ApplicationAction(Action realAction) {
		this.realAction = realAction;
		this.setEnabled(realAction.isEnabled());
	}
	
	/**
	 * Invoked this {@link ApplicationAction} instance with
	 * <code>SwingUtilities.invokeLater</code> on the AWT thread. 
	 */
	public void invokeLaterAction() {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				invokeAction(null);
			}
		});
	}	

	public void invokeAction() {
		this.invokeAction(null);
	}
	
	public void invokeAction(final ActionEvent e) {
		this.invokeAction(e, null);
	}
	
	public void invokeAction(final ActionEvent e, final Runnable invokeLater) {
		final Object noRealActionThreading = realAction.getValue(NON_THREADED_ACTION_KEY);
		final boolean noRealActionThreadingValue = noRealActionThreading instanceof Boolean && ((Boolean) noRealActionThreading).booleanValue();
		final Object noAppActionThreading = this.getValue(NON_THREADED_ACTION_KEY);
		final boolean noAppActionThreadingValue = noAppActionThreading instanceof Boolean && ((Boolean) noAppActionThreading).booleanValue();
		if(noRealActionThreadingValue || noAppActionThreadingValue) {
			FileRefreshBackgroundThread.setDisabled(true);
			try {
				this.realAction.actionPerformed(e);
				if(this.realAction instanceof IFinalizeAction) {
					((IFinalizeAction)this.realAction).finalizeAction(0);
				}
			} finally {
				FileRefreshBackgroundThread.setDisabled(false);
				TrueZipUtils.unmout();
			}
			invokeLater.run();
		} else {
			ActionEventQueue.addActionEvent(this, e, invokeLater);
		}		
	}
	
	void invokeRealAction(ActionEvent e) {
		FileRefreshBackgroundThread.setDisabled(true);
		try {
			this.realAction.actionPerformed(e);
			if(this.realAction instanceof IFinalizeAction) {
				((IFinalizeAction)this.realAction).finalizeAction(0);
			}
		} finally {
			FileRefreshBackgroundThread.setDisabled(false);
			TrueZipUtils.unmout();
		}		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		FileRefreshBackgroundThread.setDisabled(true);
		try {
			this.invokeAction(e);
		} finally {
			FileRefreshBackgroundThread.setDisabled(false);
			TrueZipUtils.unmout();
		}
	}	
	
	@Override
	public Object getValue(String key) {
		return realAction.getValue(key);
	}
	
	@Override
	public void setEnabled(boolean newValue) {
		super.setEnabled(newValue);
		realAction.setEnabled(newValue);
	}

	@Override
	public void putValue(String key, Object newValue) {
		super.putValue(key, newValue);
		realAction.putValue(key, newValue);
	}
}
