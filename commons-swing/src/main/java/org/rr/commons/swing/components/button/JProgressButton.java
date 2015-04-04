package org.rr.commons.swing.components.button;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.rr.commons.swing.components.resources.ImageResourceBundle;
import org.rr.commons.swing.icon.AnimatedIcon;

/**
 * The progress button shows a progress indicator icon during the
 * action is performed.
 * <BR><BR>
 * Hint: Use {@link #setHorizontalTextPosition(int)} for control the icon / progress icon 
 * icon location.
 * <BR><BR>
 * Use {@link #setIconTextGap(int)} to control the distance between the text and the icon / progress icon
 */
public class JProgressButton extends JButton {

	private static final long serialVersionUID = -7625670036906963026L;
	
	private String progressString = "Wait...";
	
	private final ProgressAction progressAction = new ProgressAction();
	
	public String getProgressString() {
		return progressString;
	}

	public void setProgressString(String progressString) {
		this.progressString = progressString;
	}

	protected void fireActionPerformed(final ActionEvent event) {
		final SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
		
			@Override
			protected Void doInBackground() throws Exception {
				final Dimension oldSize = getPreferredSize();
				
				//change to progress mode.
				progressAction.setWrapp(false);
						
				//update the button properties
				configurePropertiesFromAction(progressAction);
				setPreferredSize(oldSize);
				
				//fire the action 
				internalFireActionPerformed(event);

				return null;
			}

			@Override
			protected void done() {
				//change to wrapping mode.
				progressAction.setWrapp(true);
				
				//update the button properties
				configurePropertiesFromAction(progressAction);
			}
		};
		sw.execute();
	}
	
	protected void internalFireActionPerformed(ActionEvent event) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		ActionEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ActionListener.class) {
				// Lazily create the event:
				if (e == null) {
					String actionCommand = event.getActionCommand();
					if (actionCommand == null) {
						actionCommand = getActionCommand();
					}
					e = new ActionEvent(JProgressButton.this, ActionEvent.ACTION_PERFORMED, actionCommand, event.getWhen(), event.getModifiers());
				}
				if (((ActionListener) listeners[i + 1]) instanceof ProgressAction) {
					((ProgressAction) listeners[i + 1]).wrappedAction.actionPerformed(e);
				} else {
					((ActionListener) listeners[i + 1]).actionPerformed(e);
				}
			}
		}
	}
	
	@Override
	public void setAction(Action a) {
		this.progressAction.setText(progressString);
		this.progressAction.setWrappedAction(a);
		super.setAction(this.progressAction);
	}
	
	private static class ProgressAction extends AbstractAction {

		private static final long serialVersionUID = -3981953770330568086L;
		
		Action wrappedAction;
		
		private boolean wrapp = true;
		
		ProgressAction() {
			super();
			ImageIcon progressIcon = ImageResourceBundle.getResourceAsImageIcon("progress.gif");
			final AnimatedIcon icon = new AnimatedIcon(progressIcon); 
			setLargeIcon(icon);
		}
		
		/**
		 * Tells if the Action is in wrapping or progress mode.
		 * @param wrapp <code>true</code> for wrappign mode and <code>false</code> for progress mode.
		 */
		void setWrapp(boolean wrapp) {
			this.wrapp = wrapp;
		}

		/**
		 * Sets the indicator text
		 * @param text The indicator text.
		 */
		public void setText(String text) {
			super.putValue(Action.NAME, text);
		}
		
		/**
		 * Sets the indicator icon.
		 * @param icon indicator icon
		 */
		public void setLargeIcon(Icon icon) {
			super.putValue(Action.LARGE_ICON_KEY, icon);
		}
		
		public void setWrappedAction(Action a) {
			this.wrappedAction = a;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(wrapp) {
				wrappedAction.actionPerformed(e);
			}
		}

		@Override
		public boolean isEnabled() {
			if(wrapp) {
				return wrappedAction.isEnabled();
			} else {
				//false in progress mode
				return false;
			}
		}

		@Override
		public void putValue(String key, Object newValue) {
			wrappedAction.putValue(key, newValue);
		}

		@Override
		public Object getValue(String key) {
			if(wrapp) {
				return wrappedAction.getValue(key);
			} else {
				return super.getValue(key);
			}
		}
		
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		JProgressButton b = new JProgressButton();
		b.setSize(100, 25);
		b.setLocation(10, 10);
		AbstractAction abstractAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("actionPerformed");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("actionPerformed end");
			}
		};
		
		abstractAction.putValue(AbstractAction.NAME, "test");
		
		b.setAction(abstractAction);
		b.setHorizontalTextPosition(SwingConstants.LEADING);
		b.setIconTextGap(16);
		
		frame.setSize(800, 600);
		frame.getContentPane().setLayout(null);
		frame.getContentPane().add(b);
		frame.setVisible(true);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
	}
}
