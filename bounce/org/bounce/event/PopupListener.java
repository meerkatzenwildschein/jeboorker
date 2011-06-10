/*
 * $Id: PopupListener.java,v 1.4 2008/01/28 21:28:37 edankert Exp $
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

package org.bounce.event;

import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

/**
 * A MouseListener that informs about events that trigger a popup.
 *
 * @version	$Revision: 1.4 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public abstract class PopupListener extends MouseAdapter {
	/**
	 * Invoked when a mouse event has occured that triggers a popup.
	 *
	 * @param e the mouse event.
	 */
	public abstract void popupTriggered( MouseEvent e);
	
	/**
	 * @inheritDoc java.awt.event.MouseAdapter
	 */ 
	public void mouseClicked( MouseEvent e) {
		if ( e.isPopupTrigger()) {
			popupTriggered( e);
		}
	}

	/**
	 * @inheritDoc java.awt.event.MouseAdapter
	 */ 
	public void mousePressed( MouseEvent e) {
		if ( e.isPopupTrigger()) {
			popupTriggered( e);
		}
	}
	
	/**
	 * @inheritDoc java.awt.event.MouseAdapter
	 */ 
	public void mouseReleased( MouseEvent e) {
		if ( e.isPopupTrigger()) {
			popupTriggered( e);
		}
	}
} 

