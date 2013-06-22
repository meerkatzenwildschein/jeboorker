package org.rr.common.swing.components;

import java.awt.Color;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class JRButton extends JButton {

	Color enabledHighlightColor;
	
	private HighlightPropertyChangeListener highlightPropertyChangeListener;
	
	public JRButton(Action action) {
		super(action);
	}

	public Color getEnabledHighlightColor() {
		return enabledHighlightColor;
	}

	/**
	 * Enabled the highlight color function with the given color. The button is highlighted
	 * with the given color for a short time if this {@link JRButton} instance gets enabled.
	 *  
	 * @param enabledHighlightColor The color for the highlight or <code>null</code>
	 * for disable the highlight function.
	 */
	public void setEnabledHighlightColor(Color enabledHighlightColor) {
		this.enabledHighlightColor = enabledHighlightColor;
		removePropertyChangeListener(getHighlightPropertyChangeListener());
		if(enabledHighlightColor != null) {
			addPropertyChangeListener(getHighlightPropertyChangeListener());
		}
	}
	
	private HighlightPropertyChangeListener getHighlightPropertyChangeListener() {
		if(this.highlightPropertyChangeListener == null) {
			this.highlightPropertyChangeListener = new HighlightPropertyChangeListener();
		}
		return this.highlightPropertyChangeListener;
	}
	
	private class HighlightPropertyChangeListener implements PropertyChangeListener  {
		
		private int lineWith = 3;
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getPropertyName().equals("enabled") && JRButton.this.isEnabled()) {
				new Thread() {
					public void run() {
						final Border border = JRButton.this.getBorder();
						
						//set the highlight border
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								Insets borderInsets = border.getBorderInsets(JRButton.this);
								setupInset(borderInsets, lineWith);
								JRButton.this.setBorder(new CompoundBorder(new LineBorder(Color.RED, lineWith), new EmptyBorder(borderInsets)));
							}
						});
						
						try {Thread.sleep(500); } catch (InterruptedException e) {}
						
						//reset the border
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								JRButton.this.setBorder(border);
							}
						});									
					}
					
					private void setupInset(Insets borderInsets, int subtract) {
						borderInsets.top = borderInsets.top - lineWith >= 0 ? borderInsets.top - lineWith : 0;
						borderInsets.bottom = borderInsets.bottom - lineWith >= 0 ? borderInsets.bottom - lineWith : 0;
						borderInsets.left = borderInsets.left - lineWith >= 0 ? borderInsets.left - lineWith : 0;
						borderInsets.right = borderInsets.right - lineWith >= 0 ? borderInsets.right - lineWith : 0;
					}
				}.start();
			}
		}

	}

}
