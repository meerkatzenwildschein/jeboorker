/*
 * $Id: BounceButtonUI.java,v 1.4 2008/01/28 21:28:37 edankert Exp $
 *
 * Copyright (c) 2002 - 2008, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *	 this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 * 	 notice, this list of conditions and the following disclaimer in the 
 *	 documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *	 may  be used to endorse or promote products derived from this software 
 *	 without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
package org.bounce.plaf;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Dimension;

import java.awt.event.MouseEvent;
import java.awt.event.FocusEvent;

import javax.swing.JComponent;
import javax.swing.Icon;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.SwingUtilities;

import javax.swing.text.View;

import javax.swing.plaf.ComponentUI;

import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.plaf.basic.BasicHTML;

import org.bounce.QButton;

/**
 * BounceButtonUI implementation.
 *
 * @version	$Revision: 1.4 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class BounceButtonUI extends BasicButtonUI {

    private final static BounceButtonUI ui = new BounceButtonUI(); 

	/**
	 * Return this button look and feel.
	 *
	 * @param c the component the look and feel is for, normally a QButton.
	 *
	 * @return the ui for the component.
	 */
    public static ComponentUI createUI( JComponent c) {
        return ui;
    }
 
    /**
     * Overrides the paint method in the BasicButtonUI class.
     *
     * @param g the graphics.
     * @param c the button component.
     */
    public void paint( Graphics g, JComponent c) {
        QButton b = (QButton) c;
        ButtonModel model = b.getModel();
		Color foreground = b.getForeground();
		Color tmpForeground = null;
		Color background = b.getBackground();
		Color tmpBackground = null;
		Font font = b.getFont();
		Font tmpFont = null;
		Icon icon = b.getIcon();
		Icon tmpIcon = null;

        FontMetrics fm = g.getFontMetrics();

        Insets i = c.getInsets();

        Rectangle viewRect = new Rectangle();
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();

        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = b.getWidth() - (i.right + viewRect.x);
        viewRect.height = b.getHeight() - (i.bottom + viewRect.y);

        textRect.x = textRect.y = textRect.width = textRect.height = 0;
        iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;

        // Disabled
        if( !model.isEnabled()) {
        	if( model.isSelected()) {
	        	tmpForeground	= b.getDisabledSelectedForeground();
	        	tmpBackground	= b.getDisabledSelectedBackground();
	        	tmpFont 		= b.getDisabledSelectedFont();
	        	tmpIcon 		= b.getDisabledSelectedIcon();
        	} else {
	        	tmpForeground	= b.getDisabledForeground();
	        	tmpBackground	= b.getDisabledBackground();
	        	tmpFont 		= b.getDisabledFont();
	        	tmpIcon 		= b.getDisabledIcon();
        	}

        // Pressed
        } else if( model.isPressed() && model.isArmed()) {
        	tmpForeground	= b.getPressedForeground();
        	tmpBackground	= b.getPressedBackground();
        	tmpFont 		= b.getPressedFont();
        	tmpIcon 		= b.getPressedIcon();

        // In Focus but not pressed.
        } else if( b.hasFocus()) {
        	if( model.isSelected()) {
        		tmpForeground	= b.getFocusedSelectedForeground();
	        	tmpBackground	= b.getFocusedSelectedBackground();
	        	tmpFont 		= b.getFocusedSelectedFont();
	        	tmpIcon 		= b.getFocusedSelectedIcon();
        	} else {
            	tmpForeground	= b.getFocusedForeground();
				tmpBackground	= b.getFocusedBackground();
				tmpFont 		= b.getFocusedFont();
				tmpIcon 		= b.getFocusedIcon();
        	}

        // Rollover but not pressed or in focus.  
        } else if( b.isRolloverEnabled() && model.isRollover()) {
        	if( model.isSelected()) {
        		tmpForeground	= b.getRolloverSelectedForeground();
        		tmpBackground	= b.getRolloverSelectedBackground();
        		tmpFont 		= b.getRolloverSelectedFont();
        		tmpIcon 		= b.getRolloverSelectedIcon();
        	} else {
        		tmpForeground	= b.getRolloverForeground();
        		tmpBackground	= b.getRolloverBackground();
        		tmpFont 		= b.getRolloverFont();
        		tmpIcon 		= b.getRolloverIcon();
        	}

        // Selected but not pressed or in focus rollover.
        } else if( model.isSelected()) {
	        tmpForeground	= b.getSelectedForeground();
	        tmpBackground	= b.getSelectedBackground();
	        tmpFont 		= b.getSelectedFont();
	        tmpIcon 		= b.getSelectedIcon();
        }

		// Set the font to calculate the Icon and Text positions etc.
		if( tmpFont != null) {
			font = tmpFont;
		}

        g.setFont( font);

        // layout the text and icon,
        String[] text = BounceGraphicsUtils.layoutMultilineCompoundLabel( c, fm, b.getText(), b.getIcon(), 
            						b.getVerticalAlignment(), b.getHorizontalAlignment(),
            						b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            						viewRect, iconRect, textRect, 
            						b.getText() == null ? 0 : defaultTextIconGap, b.getMinimumLines(), b.getMaximumLines());

        clearTextShiftOffset();

		// Paint the background.
		if( tmpBackground != null) {
			background = tmpBackground;
		}
 
 		g.setColor( background);

//		Done alreay in the update method in ComponentUI
// 		paintBackground( g, c);
		
        // Paint the icon.
        if( tmpIcon != null) {
        	icon = tmpIcon;
        }

        if( icon  != null) { 
            paintIcon( g, c, iconRect, icon);
        }
		
		// paint text.
		if( tmpForeground != null) {
			foreground = tmpForeground;
		}
		
		g.setColor( foreground);

        if ( text != null && !text.equals("")) {
        	View v = (View) c.getClientProperty( BasicHTML.propertyKey);

        	if ( v != null) {
		    	v.paint( g, textRect);
        	} else {
    			paintText( g, c, textRect, text);
        	}
        }

		if ((b.getFocusedRectangleMode() != QButton.FOCUS_NONE) && 
			b.isFocusPainted() && 
			b.hasFocus() && 
			(b.getFocusedRectangleColor() != null)) {
			// paint UI specific focus
			paintFocus( g, b, viewRect, textRect, iconRect);
		}
    }

    /**
     * Paints the icon for the button.
     *
     * @param g the graphics.
     * @param c the button component.
     * @param iconRect the rectangle the icon has to be painted in.
     * @param icon the icon.
     */
    protected void paintIcon( Graphics g, JComponent c, Rectangle iconRect, Icon icon){
	    QButton b = (QButton) c;			     
	    ButtonModel model = b.getModel();

        if( icon == null) {
			return;
        }

		if( model.isPressed() && model.isArmed()) {
			icon.paintIcon( c, g, iconRect.x + getTextShiftOffset(),
			iconRect.y + getTextShiftOffset());
		} else {
			icon.paintIcon( c, g, iconRect.x, iconRect.y);
		}
    }

    /**
     * Paints button's background.
     *
     * @param g the graphics.
     * @param c the button component.
     */
    protected void paintBackground( Graphics g, JComponent c) {
		Dimension size = c.getSize();
    	g.fillRect( 0, 0, size.width, size.height);
    }

    /**
     * Paints the focus rectangle on the button.
     *
     * @param g the graphics.
     * @param b the button component.
     * @param viewRect the whole button rectangle.
     * @param textRect the text rectangle.
     * @param iconRect the icon rectangle.
     */
    protected void paintFocus(	Graphics g, QButton b, Rectangle viewRect, 
								Rectangle textRect, Rectangle iconRect){

        Rectangle focusRect = new Rectangle();
		String text = b.getText();
		boolean isIcon = b.getIcon() != null;

        // If there is text
        if ( text != null && !text.equals( "" )) {
			// There is no Icon and the focus mode is not around the icon only.
			if ( !isIcon && (b.getFocusedRectangleMode() != QButton.FOCUS_AROUND_ICON)) {
	    	    focusRect.setBounds( textRect );

	    	// There is an Icon and the focus mode is around the icon and the text.
	    	} else if ( b.getFocusedRectangleMode() == QButton.FOCUS_AROUND_TEXT_AND_ICON) {
	        	focusRect.setBounds( iconRect.union( textRect ) );

	    	// There is an Icon and the focus mode is only around the text.
	    	} else if ( b.getFocusedRectangleMode() == QButton.FOCUS_AROUND_TEXT) {
		    	focusRect.setBounds( textRect );

	    	// There is an Icon and the focus mode is only around the icon.
	    	} else if ( b.getFocusedRectangleMode() == QButton.FOCUS_AROUND_ICON) {
		    	focusRect.setBounds( iconRect );
	    	}
        } else if ( isIcon ) { // If there is an icon and no text
	  	    focusRect.setBounds( iconRect );
        }

    	g.setColor( b.getFocusedRectangleColor());
		g.drawRect((focusRect.x-1), (focusRect.y-1), focusRect.width+1, focusRect.height+1);
    }

    /**
     * Paints the text on the button.
     *
     * @param g the graphics.
     * @param c the button component.
     * @param textRect the text rectangle.
     * @param text the text to be painted.
     */
    protected void paintText( Graphics g, JComponent c, Rectangle textRect, String[] text) {
		QButton b = (QButton) c;			     
		ButtonModel model = b.getModel();
		FontMetrics fm = g.getFontMetrics();
		
		int y = textRect.y + fm.getAscent();

		for ( int i = 0; i < text.length; i++) {
			BounceGraphicsUtils.drawLine( g, fm, textRect, text[i], b.getHorizontalAlignment(), y, model.getMnemonic());
			y = y + fm.getHeight();
		}
    }

    /**
     * Overrides the createButtonListener method in the BasicButtonUI class,
	 * to be able to install a new button listener that can listen to double clicks.
     *
     * @param b the button component.
	 *
     * @return the button listener.
     */
    protected BasicButtonListener createButtonListener( AbstractButton b) {
		return new BounceButtonListener( b);
    }

    /**
     * Returns the preferred size of the button, this size will return the
	 * full width of the text as the preferred width and the full height of the
	 * lines as the height.
     * <p>
     * This method is a revamped version of the method:<br/>
     * BasicButtonUI.getPreferredSize()
     * </p>
	 *
     * @param c the button component.
     *
     * @return the preferred size of the button.
     */
    public Dimension getPreferredSize( JComponent c) {
		QButton b = (QButton)c;
		
		// Other components can change the size.
        if ( b.getComponentCount() > 0) {
            return null;
        }

        Insets i = c.getInsets();
        Rectangle viewRect = new Rectangle();
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();

		if ( b.getMaximumLines() == 1) {
			viewRect.x = i.left + i.right;
			viewRect.y = i.top + i.bottom;
			viewRect.width = viewRect.height = Short.MAX_VALUE;
		} else {
	        viewRect.x = i.left;
	        viewRect.y = i.top;
	        viewRect.width = b.getWidth() - (i.right + viewRect.x);
	        viewRect.height = b.getHeight() - (i.bottom + viewRect.y);
		}

		// Perform a dummy layout...
        BounceGraphicsUtils.layoutMultilineCompoundLabel(
            c, b.getGraphics().getFontMetrics(), b.getText(), b.getIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect, b.getText() == null ? 0 : defaultTextIconGap, b.getMinimumLines(), b.getMaximumLines());

        Rectangle r = iconRect.union( textRect);

        Insets insets = b.getInsets();
        r.width += insets.left + insets.right;
        r.height += insets.top + insets.bottom;

        /* Ensure that the width and height of the button is odd,
         * to allow for the focus line.
         */

        if( r.width % 2 == 0) { r.width += 1; }
        if( r.height % 2 == 0) { r.height += 1; }

        return r.getSize();
    }
}   

class BounceButtonListener extends BasicButtonListener {

    /**
     * Constructs a button listener for the button supplied.
     *
     * @param b the button component.
     */
    public BounceButtonListener( AbstractButton b) {
		super(b);  
    }

	/**
	 * Overrides the method in the Basic button listener, to enable the 
	 * double click when needed.
	 *
	 * @param e the mouse event.
	 */
	public void mousePressed( MouseEvent e) {
//		System.out.println("mousePressed( "+e+")");
		QButton b = (QButton) e.getSource();

		// If the double click is not enabled, react as always.
		if ( !b.isDoubleClick()) {
			super.mousePressed( e);
		
		// Double click is enabled...
		} else {
		
			if ( SwingUtilities.isLeftMouseButton( e) && e.getClickCount() == 2) {
				
				if ( b.isDoubleClick()) {
	
					if( b.contains( e.getX(), e.getY())) {
						ButtonModel model = b.getModel();
	
						if ( !model.isEnabled()) {
							// Disabled buttons ignore all input...
							return;
						}
						if ( !model.isArmed()) {
							// button not armed, should be
							model.setArmed( true);
						}
	
						model.setPressed( true);
	
						if( !b.hasFocus()) {
							b.requestFocus();
						}            
					} 
				}
			// No double click.
			} else {
				if( !b.hasFocus()) {
					b.requestFocus();
				}            
			}
		}

	}
	
	/**
	 * Activated when focus gained.
	 * 
	 * @param e the focus gained event.
	 */
    public void focusGained( FocusEvent e) { 
        Component c = (Component)e.getSource();
		c.repaint();
    }
}
