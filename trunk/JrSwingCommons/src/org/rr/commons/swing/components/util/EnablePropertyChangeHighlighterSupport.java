package org.rr.commons.swing.components.util;

import java.awt.Color;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Adds highlight border color functionality to the given component.
 */
public class EnablePropertyChangeHighlighterSupport {

	private final JComponent target; 
	private Color color; 
	int lineWidth;
	
	
	/**
	 * @param target Component which should get the highlight support. 
	 * @param color The highlight color. If it is null, highlight support will be removed from the component. 
	 * @param lineWidth The with of the highlight border.
	 */
	public EnablePropertyChangeHighlighterSupport(JComponent target, Color color, int lineWidth) {
		this.target = target;
		this.color = color;
		this.lineWidth = lineWidth;
		
		removeHighlightPropertyChangeListener(target);
		if(color != null) {
			target.addPropertyChangeListener(new HighlightPropertyChangeListener());
		}
	}
	
	/**
	 * Removes all registered {@link HighlightPropertyChangeListener} from the given component. 
	 */
	private void removeHighlightPropertyChangeListener(JComponent c) {
		final PropertyChangeListener[] propertyChangeListeners = c.getPropertyChangeListeners();
		for (PropertyChangeListener listener : propertyChangeListeners) {
			if(listener instanceof HighlightPropertyChangeListener) {
				c.removePropertyChangeListener(listener);
			}
		}
	}
	
	private class HighlightPropertyChangeListener implements PropertyChangeListener  {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getPropertyName().equals("enabled") && target.isEnabled()) {
				new Thread() {
					public void run() {
						final Border oldBorder = target.getBorder();
						final Insets borderInsets = setupInset(oldBorder.getBorderInsets(target), lineWidth);
						final Border highlight = new CompoundBorder(new LineBorder(color, lineWidth), new EmptyBorder(borderInsets));
						
						for(int i = 0; i < 3; i++) {
							//set the highlight border
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									target.setBorder(highlight);
								}
							});
							
							try {Thread.sleep(200); } catch (InterruptedException e) {}
							
							//reset the border
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									target.setBorder(oldBorder);
								}
							});		
							
							try {Thread.sleep(200); } catch (InterruptedException e) {}
						}
					}
					
					private Insets setupInset(Insets borderInsets, int subtract) {
						borderInsets.top = borderInsets.top - lineWidth >= 0 ? borderInsets.top - lineWidth : 0;
						borderInsets.bottom = borderInsets.bottom - lineWidth >= 0 ? borderInsets.bottom - lineWidth : 0;
						borderInsets.left = borderInsets.left - lineWidth >= 0 ? borderInsets.left - lineWidth : 0;
						borderInsets.right = borderInsets.right - lineWidth >= 0 ? borderInsets.right - lineWidth : 0;
						return borderInsets;
					}
				}.start();
			}
		}

	}	
}
