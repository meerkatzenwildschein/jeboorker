/*
 * $Id: BounceLabelUI.java,v 1.6 2008/01/28 21:28:37 edankert Exp $
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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.FontMetrics;

import javax.swing.plaf.LabelUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import javax.swing.UIManager;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Icon;

import org.bounce.QLabel;

/**
 * Extend the BasicLabelUI class, this LabelUI allows for a multi-line
 * label. 
 *
 * @version	$Revision: 1.6 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class BounceLabelUI extends BasicLabelUI {
	private static Rectangle paintIconR = new Rectangle();
	private static Rectangle paintTextR = new Rectangle();
	private static Rectangle paintViewR = new Rectangle();
	private static Insets paintViewInsets = new Insets(0, 0, 0, 0);

	private static Rectangle iconR = new Rectangle();
	private static Rectangle textR = new Rectangle();
	private static Rectangle viewR = new Rectangle();
	private static Insets viewInsets = new Insets(0, 0, 0, 0);

	
	// Shared UI object
	private static LabelUI labelUI;

	/**
	 * Creates the one version of this UI.
	 *
	 * @param c the component the ui needs to be installed for.
	 *
	 * @return the component's ui!
	 */
	public static ComponentUI createUI( JComponent c) {
		if( labelUI == null) {
			labelUI = new BounceLabelUI();
		}

	    return labelUI;
	}

	/**
	 * Installs the UI for the supplied component.
	 *
	 * @param c the component the ui needs to be installed for.
	 */
	public void installUI( JComponent c) {
	    JLabel p = (JLabel)c;
	    super.installUI( p);
	}

	/**
	 * Un-installs the current UI for the supplied component.
	 *
	 * @param c the component the ui needs to be un-installed for.
	 */
	public void uninstallUI( JComponent c) {
	    super.uninstallUI( c);
	}
	
	/**
	 * Overrides the paint method in the BasicLabelUI class.
	 *
	 * @param g the graphics.
	 * @param c the button component.
	 */
	public void paint( Graphics g, JComponent c) {
	    QLabel label = (QLabel)c;
	    String text = label.getText();
	    Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();

	    if ((icon == null) && (text == null)) {
	        return;
	    }

	    FontMetrics fm = g.getFontMetrics();
	    paintViewInsets = c.getInsets( paintViewInsets);

	    paintViewR.x = paintViewInsets.left;
	    paintViewR.y = paintViewInsets.top;
	    paintViewR.width = c.getWidth() - (paintViewInsets.left + paintViewInsets.right);
	    paintViewR.height = c.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);

	    paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
	    paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

	    String[] clippedText = BounceGraphicsUtils.layoutMultilineCompoundLabel( 
									c, fm, text, icon, 
									label.getVerticalAlignment(),
									label.getHorizontalAlignment(),
									label.getVerticalTextPosition(),
									label.getHorizontalTextPosition(),
									paintViewR, paintIconR, paintTextR, label.getIconTextGap(), 
									label.getMinimumLines(), label.getMaximumLines());

	    if (icon != null) {
	        icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
	    }

	    if (text != null) {
		    View v = (View) c.getClientProperty(BasicHTML.propertyKey);

		    if (v != null) {
				v.paint(g, paintTextR);
		    } else {
				if ( label.isEnabled()) {
				    g.setColor( label.getForeground());
				} else {
					g.setColor( UIManager.getColor("Label.disabledForeground"));
				}
				
			    int y = paintTextR.y + fm.getAscent();
		
			    for ( int i = 0; i < clippedText.length; i++) {
			    	BounceGraphicsUtils.drawLine( g, fm, paintTextR, clippedText[i], label.getHorizontalAlignment(), y, label.getDisplayedMnemonic());

			    	y = y + fm.getHeight();
			    }
		    }
	    }
	}

	/**
	 * Returns the preferred size of the label, this size will return the
	 * full width of the text as the preferred width and the full height of the
	 * lines as the height.
	 * <p>
	 * This method is a revamped version of the method found in:
	 * BasicLabelUI.getPreferredSize()
	 * </p>
	 *
	 * @param c the button component.
	 *
	 * @return the preferred size of the button.
	 */
	public Dimension getPreferredSize(JComponent c) {
	    QLabel label = (QLabel)c;
	    String text = label.getText();
	    Icon icon = label.getIcon();
	    Insets insets = label.getInsets( viewInsets);
	    Font font = label.getFont();

	    int dx = insets.left + insets.right;
	    int dy = insets.top + insets.bottom;

	    if ( (icon == null) && ((text == null) || ((text != null) && (font == null)))) {
	        return new Dimension( dx, dy);
	    } else if ((text == null) || ((icon != null) && (font == null))) {
	        return new Dimension( icon.getIconWidth() + dx, 
	                              icon.getIconHeight() + dy);
	    } else {
			Graphics gc = label.getGraphics();
			
			if ( gc != null) {
		        FontMetrics fm = gc.getFontMetrics();

		        iconR.x = iconR.y = iconR.width = iconR.height = 0;

		        textR.x = textR.y = textR.width = textR.height = 0;
		        Insets i = c.getInsets();
		
		        if ( label.getMaximumLines() == 1) {
		        	viewR.x = i.left + i.right;
		        	viewR.y = i.top + i.bottom;
		        	viewR.width = viewR.height = Short.MAX_VALUE;
		        } else {
		            viewR.x = i.left;
		            viewR.y = i.top;
		            viewR.width = label.getWidth() - (i.right + viewR.x);
		            viewR.height = label.getHeight() - (i.bottom + viewR.y);
		        }

				// Do a dummy layout.
		        BounceGraphicsUtils.layoutMultilineCompoundLabel( 
		        							c, fm, text, icon, 
		        							label.getVerticalAlignment(),
		        							label.getHorizontalAlignment(),
		        							label.getVerticalTextPosition(),
		        							label.getHorizontalTextPosition(),
		        							viewR, iconR, textR, label.getIconTextGap(), 
											label.getMinimumLines(), label.getMaximumLines());

		        int x1 = Math.min(iconR.x, textR.x);
		        int x2 = Math.max(iconR.x + iconR.width, textR.x + textR.width);
		        int y1 = Math.min(iconR.y, textR.y);
		        int y2 = Math.max(iconR.y + iconR.height, textR.y + textR.height);

		        Dimension rv = new Dimension(x2 - x1, y2 - y1);

		        rv.width += dx;
		        rv.height += dy;

		        return rv;
			}
		
            return new Dimension(0, 0);
	    }
	}
} 
