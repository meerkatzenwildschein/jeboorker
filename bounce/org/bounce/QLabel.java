/*
 * $Id: QLabel.java,v 1.6 2008/01/28 21:28:37 edankert Exp $
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

import javax.swing.Icon;
import javax.swing.JLabel;

import org.bounce.plaf.BounceLabelUI;

/**
 * Extend the JLabel class, this label allows for multiple
 * lines.
 *
 * @version	$Revision: 1.6 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class QLabel extends JLabel {
	private static final long serialVersionUID = 3978422533542130994L;

	private int maxLines	= 1;
	private int minLines	= 1;
	
	/**
	 * Constructor, calls the super constructor.
	 */
	public QLabel() {
		super();
	}

	/**
	 * Constructor, calls the super constructor.
	 * 
	 * @param image the icon image.
	 */
	public QLabel( Icon image) {
		super( image);
	}

	/**
	 * Constructor, calls the super constructor.
	 * 
	 * @param image the icon image.
	 * @param horizontalAlignment the horizontal alignment.
	 */
	public QLabel( Icon image, int horizontalAlignment) {
		super( image, horizontalAlignment);
	}

	/**
	 * Constructor, calls the super constructor.
	 * 
	 * @param text the label text.
	 */
	public QLabel( String text) {
		super( text);
	}

	/**
	 * Constructor, calls the super constructor.
	 * 
	 * @param text the label text.
	 * @param image the icon image.
	 * @param horizontalAlignment the horizontal alignment.
	 */
	public QLabel( String text, Icon image, int horizontalAlignment) {
		super( text, image, horizontalAlignment);
	}

	/**
	 * Constructor, calls the super constructor.
	 * 
	 * @param text the label text.
	 * @param horizontalAlignment the horizontal alignment.
	 */
	public QLabel( String text, int horizontalAlignment) {
		super( text, horizontalAlignment);
	}

	/**
	 * Sets the look and feel to the Bounce Label UI look and feel.
	 * Override this method if you want to install a different UI.
	 */
	public void updateUI() {
	    setUI( BounceLabelUI.createUI( this));
	}

	/**
	 * Sets the maximum number of possible lines on this label.
	 * If there is text in the lines the lines will be painted and
	 * included in the preferred-height calculations.
	 * 
	 * @param lines the number of lines.
	 *
	 * @exception java.lang.IllegalArgumentException if <code>lines</code>
	 *            is smaller than the minimum number of lines.
	 */
	public void setMaximumLines( int lines) {
		if ( lines > 0) {
			if ( lines >= minLines) {
				this.maxLines = lines;
			} else {
				this.maxLines = minLines;

				throw new IllegalArgumentException( "Maximum number of lines < Minimum : "+lines+" < "+minLines);
			}
		} else {
			this.maxLines = 1;
		}
	}

	/**
	 * Get the maximum number of lines used for the label text.
	 * 
	 * @return the maximum number of lines.
	 */
	public int getMaximumLines() {
		return maxLines;
	}
	
	/**
	 * Sets the minimum number of possible lines on this label.
	 * The lines will always be included in the preferred-height 
	 * calculations, even if they don't have text.
	 * 
	 * @param lines the number of lines.
	 *
	 * @exception java.lang.IllegalArgumentException if <code>lines</code>
	 *            is bigger than the maximum number of lines.
	 */
	public void setMinimumLines( int lines) {
		if ( lines > 0) {
			if ( lines <= maxLines) {
				this.minLines = lines;
			} else {
				this.minLines = maxLines;

				throw new IllegalArgumentException( "Minimum number of lines > Maximum : "+lines+" > "+maxLines);
			}
		} else {
			this.minLines = 1;
		}
	}

	/**
	 * Get the minimum number of lines used for the label text.
	 * 
	 * @return the minimum number of lines.
	 */
	public int getMinimumLines() {
		return minLines;
	}

	/**
	 * Set the number of lines used for the label text.
	 * This forces the preferred height of the label to be 
	 * as high as the height of the lines of text.
	 *
	 * This is equal to setting the maximum and minimum number of 
	 * lines to the same value.
	 * 
	 * @param lines the number of lines.
	 */
	public void setLines( int lines) {
		if ( lines > 0) {
			this.minLines = lines;
			this.maxLines = lines;
		} else {
			this.minLines = 1;
			this.maxLines = 1;
		}
	}
} 
