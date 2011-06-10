/*
 * $Id: LinkButtonUI.java,v 1.5 2008/01/28 21:28:37 edankert Exp $
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

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;

import org.bounce.LinkButton;

/**
 * LinkButtonUI implementation.
 *
 * @version	$Revision: 1.5 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class LinkButtonUI extends BounceButtonUI {

    private final static LinkButtonUI ui = new LinkButtonUI(); 

	/**
	 * Return this button look and feel.
	 *
	 * @param c the component the look and feel is for, normally a LinkButton.
	 *
	 * @return the ui for the component.
	 */
    public static ComponentUI createUI( JComponent c) {
        return ui;
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
		LinkButton b = (LinkButton) c;			     
		FontMetrics fm = g.getFontMetrics();

		int y = textRect.y + fm.getAscent();

	    for ( int i = 0; i < text.length; i++) {
			if ( text[i] != null) {
		    	int x = BounceGraphicsUtils.drawLine( g, fm, textRect, text[i], b.getHorizontalAlignment(), y, b.getMnemonic());

		    	if ( b.isEnabled()) {
		    		int width = SwingUtilities.computeStringWidth( fm, text[i]);

		    		g.drawLine(	x + getTextShiftOffset(), y + getTextShiftOffset() + 1, 
		    					x + width + getTextShiftOffset(), y + getTextShiftOffset() + 1);
		    	}		
			}

	    	y = y + fm.getHeight();
	    }
    }
}
