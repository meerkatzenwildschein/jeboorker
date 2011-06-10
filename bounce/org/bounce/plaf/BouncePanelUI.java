/*
 * $Id: BouncePanelUI.java,v 1.5 2008/01/28 21:28:37 edankert Exp $
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

import java.awt.Image;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;

import javax.swing.plaf.PanelUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ImageIcon;

import org.bounce.QPanel;

/**
 * Extend the BasicPanelUI class, this PanelUI allows for a 
 * background image that can be centered, tiled, anchored in a corner 
 * or stretched. The UI also allows for a diagonal gradient filled 
 * background, for a very subtle 3D effect.
 *
 * @version	$Revision: 1.5 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class BouncePanelUI extends BasicPanelUI {
	private static final String PREVIOUS_GRADIENT_IMAGE_KEY = "BouncePanelUI.previousGradientImage";
	private static final String PREVIOUS_SIZE_KEY = "BouncePanelUI.previousSize";
	
	// Shared UI object
	private static PanelUI panelUI;

	/**
	 * Creates the one version of this UI.
	 *
	 * @param c the component the ui needs to be installed for.
	 *
	 * @return the component's ui!
	 */
	public static ComponentUI createUI( JComponent c) {
		if( panelUI == null) {
			panelUI = new BouncePanelUI();
		}

	    return panelUI;
	}

	/**
	 * Installs the UI for the supplied component.
	 *
	 * @param c the component the ui needs to be installed for.
	 */
	public void installUI( JComponent c) {
	    JPanel p = (JPanel)c;
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
	 * Overrides the paint method in the BasicPanelUI class.
	 *
	 * @param g the graphics.
	 * @param c the button component.
	 */
	public void paint( Graphics g, JComponent c) {
		QPanel panel = (QPanel)c;
		
		if ( panel.isGradientBackground()) {
			// Store variables as ClientProperties on the component since the UI
			// is a singleton and will be reused for every QPanel class.
			Dimension previousSize = (Dimension)c.getClientProperty( PREVIOUS_SIZE_KEY);

			// Only repaint the image if the size has changed, otherwise
			// use the previous image. (a noticable speed improvement!)
			if ( previousSize == null || !panel.getSize().equals( previousSize)) {
				c.putClientProperty( PREVIOUS_SIZE_KEY, panel.getSize());
				c.putClientProperty( PREVIOUS_GRADIENT_IMAGE_KEY, calculateGradientImage( c));
			}

			g.drawImage( (Image)c.getClientProperty( PREVIOUS_GRADIENT_IMAGE_KEY), 0, 0, c);
		}

		ImageIcon icon = panel.getBackgroundImage();

		if ( icon != null) {
			int panelWidth = panel.getSize().width;
			int panelHeight = panel.getSize().height;
			int iconWidth = icon.getIconWidth();
			int iconHeight = icon.getIconHeight();
			int xPos = 0;
			int yPos = 0;
			
			switch ( panel.getBackgroundImageMode()) {

				case QPanel.CENTERED:
					xPos = (panelWidth - iconWidth) / 2;
					yPos = (panelHeight - iconHeight) / 2;
					g.drawImage( icon.getImage(), xPos, yPos, panel);
					break;
					
				case QPanel.TOP_LEFT:
					xPos = 0;
					yPos = 0;
					g.drawImage( icon.getImage(), xPos, yPos, panel);
					break;

				case QPanel.TOP_RIGHT:
					xPos = panelWidth - iconWidth;
					yPos = 0;
					g.drawImage( icon.getImage(), xPos, yPos, panel);
					break;

				case QPanel.BOTTOM_LEFT:
					xPos = 0;
					yPos = panelHeight - iconHeight;
					g.drawImage( icon.getImage(), xPos, yPos, panel);
					break;

				case QPanel.BOTTOM_RIGHT:
					xPos = panelWidth - iconWidth;
					yPos = panelHeight - iconHeight;
					g.drawImage( icon.getImage(), xPos, yPos, panel);
					break;

				case QPanel.STRETCHED:
					g.drawImage( icon.getImage(), 0, 0, panelWidth, panelHeight, panel);
					break;
					
				case QPanel.TILED:
					for ( int j = 0; j < panelHeight; j = j + iconHeight) {
						for ( int i = 0; i < panelWidth; i = i + iconWidth) {
							g.drawImage( icon.getImage(), i, j, panel);
						}
					}
					break;

				default:
					break;
			}
		}
	}

	/**
	 * Calculates and returns the gradient image.
	 */
	private Image calculateGradientImage( JComponent component) {
		QPanel panel = (QPanel)component;
		Color background = component.getBackground();
	    int width = component.getSize().width;
		int height = component.getSize().height;
		int pixels[] = new int[ width * height];
		int index = 0;
		int defaultRGB = 0xFF << 24;
		int color = background.getRGB();
		int startRed = background.getRed();
		int startBlue = background.getBlue();
		int startGreen = background.getGreen();
		
		Color finalColor = panel.getGradientColor();

		if ( finalColor == null) {
			finalColor = background.darker();
		}
		
		int finalRed = finalColor.getRed();
		int finalBlue = finalColor.getBlue();
		int finalGreen = finalColor.getGreen();

		for ( int col = 0; col < height; col++) {
		    int val1 = ((height-col) * 255) / (height - 1);

		    for ( int row = 0; row < width; row++) {
		        int val2 = (row * 255) / (width - 1);
				int value = val2 - val1;
				
				if ( value >= 0)  {
					int red = calculateComponent( startRed, finalRed, value);
					int blue = calculateComponent( startBlue, finalBlue, value);
					int green = calculateComponent( startGreen, finalGreen, value);
					
					pixels[ index] = defaultRGB | (red << 16) | (green << 8) | (blue << 0);
				} else {
					pixels[ index] = color;
				}
				index++;
		    }
		}

	    return component.createImage( new MemoryImageSource( width, height,
		      				ColorModel.getRGBdefault(), pixels, 0, width));
	}
	
	/**
	 * Calculates and red, green or blue component.
	 */
	private int calculateComponent( int start, int end, int value) {
		int component = 0;

		if ( start < end) {
			component = start - ((start - end)*value)/255;
		} else {
			component = start + ((end - start)*value)/255;
		}
		
		if ( component < 0) {
			component = 0;
		} else if ( component > 0xFF) {
			component = 0xFF;
		}
		
		return component;
	}
} 
