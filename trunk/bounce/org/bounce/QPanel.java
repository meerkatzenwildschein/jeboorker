/*
 * $Id: QPanel.java,v 1.7 2008/01/28 21:28:37 edankert Exp $
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

import java.awt.Color;
import java.awt.LayoutManager;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.bounce.plaf.BouncePanelUI;

/**
 * Extend the JPanel class, this panel allows for a 
 * background image that can be centered, tiled, anchored in a corner 
 * or stretched. The panel also allows for a diagonal gradient filled 
 * background, for a very subtle 3D effect.
 *
 * @version	$Revision: 1.7 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class QPanel extends JPanel {
	private static final long serialVersionUID = 3258413928311566905L;

	/** The center the background image */
	public static final int CENTERED		= 0;
	/** Place the background image top left */
	public static final int TOP_LEFT		= 1;
	/** Place the background image top right */
	public static final int TOP_RIGHT 		= 2;
	/** Place the background image bottom left*/
	public static final int BOTTOM_LEFT		= 3;
	/** Place the background image bottom right*/
	public static final int BOTTOM_RIGHT	= 4;
	/** Tile the background image */
	public static final int TILED			= 5;
	/** Stretch the background image */
	public static final int STRETCHED		= 6;

	private boolean gradientFilled 		= false;
	private Color finalGradient			= null;
	private ImageIcon backgroundImage	= null;
	private int backgroundImageMode		= CENTERED;
	
	/**
	 * Constructor, calls the super constructor.
	 */
	public QPanel() {
		super();
	}

	/**
	 * Constructor, calls the super constructor.
	 * 
	 * @param isDoubleBuffered is double buffered.
	 */
	public QPanel( boolean isDoubleBuffered) {
		super( isDoubleBuffered);
	}

	/**
	 * Constructor, calls the super constructor.
	 * 
	 * @param layout the layout manager.
	 */
	public QPanel( LayoutManager layout) {
		super( layout);
	}

	/**
	 * Constructor, calls the super constructor.
	 * 
	 * @param layout the layout manager.
	 * @param isDoubleBuffered is double buffered.
	 */
	public QPanel( LayoutManager layout, boolean isDoubleBuffered) {
		super( layout, isDoubleBuffered);
	}
	
	/**
	 * Sets the look and feel to the Bounce Panel UI look and feel.
	 * Override this method if you want to install a different UI.
	 */
	public void updateUI() {
	    setUI( BouncePanelUI.createUI( this));
	}
	
	/**
	 * Enables the filling of the background gradually to achieve a 
	 * 'subtle' 3D effect. It starts in the top left hand corner with the
	 * background color and ends in the bottom right hand corner with the 
	 * gradient color or if this color is null with a darker version of 
	 * the background color.
	 *
	 * @param enable the gradient filling of the background when true.
	 */
	public void setGradientBackground( boolean enable) {
		gradientFilled = enable;
	}
	
	/**
	 * Returns whether the 'subtle' 3D gradient effect is enabled.
	 *
	 * @return true when the gradient filling of the background is enabled.
	 */
	public boolean isGradientBackground() {
		return gradientFilled;
	}

	/**
	 * Returns the color used for the lower right corner of the gradient
	 * 3D effect. When the color is null, a darker version of the 
	 * background color is used instead.
	 *
	 * @return the color for the lower right corner.
	 */
	public Color getGradientColor() {
		return finalGradient;
	}

	/**
	 * Sets the color used for the lower right corner of the gradient
	 * 3D effect. When the color is null, a darker version of the 
	 * background color is used instead.
	 *
	 * @param color the color for the lower right corner.
	 */
	public void setGradientColor( Color color) {
		finalGradient = color;
	}

	/**
	 * Sets the background image for this panel.
	 *
	 * @param image the background image.
	 */
	public void setBackgroundImage( ImageIcon image) {
		backgroundImage = image;
	}

	/**
	 * Returns this panel's background image.
	 *
	 * @return the background image.
	 */
	public ImageIcon getBackgroundImage() {
		return backgroundImage;
	}

	/**
	 * Sets the way the background image is displayed, it can be 
	 * any of the following modes:
	 * CENTERED, The middle of the image is anchored in the middle 
	 * of the panel, when the image is bigger than the panel, the 
	 * image will be clipped.
	 * TOP_LEFT, The top left of the image is anchored in the top
	 * left part of the panel, when the image is bigger than the 
	 * panel, the image will be clipped.
	 * TOP_RIGHT, The top right of the image is anchored in the top
	 * right part of the panel, when the image is bigger than the 
	 * panel, the image will be clipped.
	 * BOTTOM_RIGHT, The bottom right of the image is anchored in the 
	 * bottom right part of the panel, when the image is bigger than 
	 * the panel, the image will be clipped.
	 * BOTTOM_LEFT, The bottom left of the image is anchored in the 
	 * bottom left part of the panel, when the image is bigger than 
	 * the panel, the image will be clipped.
	 * STRETCHED, The image is resized to fit the panel.
	 * TILED, The image is drawn repeatedly until the whole panel is 
	 * filled (like tiles), starting at the top left corner, images on 
	 * the right and bottom edges of the panel are likely to be clipped.
	 *
	 * @param mode	the background image display mode.
	 */
	public void setBackgroundImageMode( int mode) {
		backgroundImageMode = mode;
	}

	/**
	 * Returns the mode in which the background image is displayed.
	 *
	 * @return the background image mode.
	 */
	public int getBackgroundImageMode() {
		return backgroundImageMode;
	}
} 
