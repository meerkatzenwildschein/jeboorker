/*
 * $Id: LinkButton.java,v 1.4 2008/01/28 21:28:37 edankert Exp $
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

package org.bounce;

import org.bounce.plaf.LinkButtonUI;

import java.awt.Cursor;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.Action;

/**
 * A generic link button that has underlined text and
 * a handcursor as the default cursor.
 *
 * @version	$Revision: 1.4 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class LinkButton extends QButton {
	
	private static final long serialVersionUID = 3257003241991320370L;

	/**
	 * Constructor without a set text or icon.
	 */
	public LinkButton() {
		super();
		init();
	}

	/**
	 * Constructor with properties taken from the action supplied.
	 *
	 * @param a the action for the button.
	 */
	public LinkButton( Action a) {
		super( a);
		init();
	}

	/**
	 * Constructor for a button with an Icon.
	 *
	 * @param icon the icon for the button.
	 */
	public LinkButton( Icon icon) {
		super( icon);
		init();
	}

	/**
	 * Constructor for a button with text.
	 *
	 * @param text the text for the button.
	 */
	public LinkButton( String text) {
		super( text);
		init();
	}

	/**
	 * Constructor for a button with an icon and text.
	 *
	 * @param text the text for the button.
	 * @param icon the icon for the button.
	 */
	public LinkButton( String text, Icon icon) {
		super( text, icon);
		init();
	}

	// Initialises the button.
	private void init() {
		setBorder( null);
		setMargin( new Insets( 0, 0, 0, 0));
		setOpaque( false);
		setFocusPainted( false);
	}
	
	/**
	 * Sets the look and feel to the Link Button UI look and feel.
	 * Override this method if you want to install a different UI.
	 */
	public void updateUI() {
	    setUI( LinkButtonUI.createUI( this));
	}

	/**
	 * Returns a HAND_CURSOR if the default cursor has been set 
	 * and the button is enabled.
	 *
	 * @return the cursor.
	 */
    public Cursor getCursor() {
        Cursor cursor = super.getCursor();

		if ( cursor == Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR) && isEnabled()) {
			cursor = Cursor.getPredefinedCursor( Cursor.HAND_CURSOR);
		}
		
		return cursor;
    }
} 

