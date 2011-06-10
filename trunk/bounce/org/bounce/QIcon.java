/*
 * $Id: QIcon.java,v 1.6 2008/01/28 21:28:37 edankert Exp $
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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.SwingConstants;

/** 
  * Icon implementation that allows for overlaying a number of icons on top 
  * of the base icon.
  *
  * Important: Icons are overlayed in the order they are supplied.
  *
  * Note: This implementation is based on the CompositeIcon 
  * Version 1.00 Aug 15 2001, implementation by Eric Schultz:
  * http://forum.java.sun.com/thread.jsp?forum=57&thread=157029
  *
  * @version	$Revision: 1.6 $, $Date: 2008/01/28 21:28:37 $
  * @author Edwin Dankert <edankert@gmail.com>
  */
public class QIcon implements Icon {
	/** The overlay icon location NORTH */
	public static final int NORTH		= SwingConstants.NORTH;
	/** The overlay icon location NORTH_EAST */
	public static final int NORTH_EAST	= SwingConstants.NORTH_EAST;
	/** The overlay icon location NORTH_WEST */
	public static final int NORTH_WEST	= SwingConstants.NORTH_WEST;

	/** The overlay icon location WEST */
	public static final int WEST		= SwingConstants.WEST;
	/** The overlay icon location EAST */
	public static final int EAST		= SwingConstants.EAST;
	/** The overlay icon location CENTER */
	public static final int CENTER		= SwingConstants.CENTER;

	/** The overlay icon location SOUTH */
	public static final int SOUTH		= SwingConstants.SOUTH;
	/** The overlay icon location SOUTH_EAST */
	public static final int SOUTH_EAST	= SwingConstants.SOUTH_EAST;
	/** The overlay icon location SOUTH_WEST */
	public static final int SOUTH_WEST	= SwingConstants.SOUTH_WEST;

	private Icon baseIcon = null;
	private List<Object[]> overlays = null;

	/**
	 * Creates a QIcon for the base icon.
	 *
	 * @param baseIcon the base icon.
	 */
	public QIcon(Icon baseIcon) {
        this(baseIcon, null, -1);
	}
	
    /**
     * Creates a new composite icon from two existing icons and places the overlay in the
     * specified position
     * @param baseIcon the base icon
     * @param overlay the icon to draw on top of the base
     * @param position the location of the overlay relative to the base
     */
    public QIcon(Icon baseIcon, Icon overlay, int position) {
        this.baseIcon = baseIcon;
        overlays = new ArrayList<Object[]>();
        
        if (overlay != null) {
            addOverlayIcon(overlay, position);
        }
    }

    /**
	 * Add an overlay icon, using the abstract position.
	 *
	 * @param icon the overlay icon.
	 * @param pos the abstract position of the icon.
	 */
	public void addOverlayIcon( Icon icon, int pos) {
		Point position = getPosition( icon, pos);
		
		addOverlayIcon( icon, position);
	}
	
    /**
	 * Add an overlay icon, using the position supplied.
	 *
	 * @param icon the overlay icon.
	 * @param xpos the X position of the icon.
	 * @param ypos the Y position of the icon.
	 */
	public void addOverlayIcon( Icon icon, int xpos, int ypos) {
		Point position = new Point( xpos, ypos);

		addOverlayIcon( icon, position);
	}

	/**
	 * Add an overlay icon, using the position supplied.
	 *
	 * @param icon the overlay icon.
	 * @param position the position of the icon.
	 */
	public void addOverlayIcon( Icon icon, Point position) {
		Object[] objects = new Object[2];
		
		objects[0] = icon;
		objects[1] = position;
		
		overlays.add( objects);
	}    

	// calculate the icons abstract-position.
	private Point getPosition( Icon overlayIcon, int position) {
		int x = 0;
		int y = 0;
	
		switch ( position) {                
			case NORTH_EAST:                     
				x = baseIcon.getIconWidth() - overlayIcon.getIconWidth();
	            y = 0;                    
				break;                
			
			case NORTH:
				x =  (baseIcon.getIconWidth() - overlayIcon.getIconWidth()) / 2;
				y = 0;
				break;
			
			case NORTH_WEST:
				x = 0;
				y = 0;
				break;
				
			case WEST:
				x = 0;
				y = (baseIcon.getIconHeight() - overlayIcon.getIconHeight()) / 2;
				break;
				
			case CENTER:
				x = (baseIcon.getIconWidth() - overlayIcon.getIconWidth()) / 2;
				y = (baseIcon.getIconHeight() - overlayIcon.getIconHeight()) / 2;
				break;                
			
			case EAST:
				x = baseIcon.getIconWidth() - overlayIcon.getIconWidth();
				y = (baseIcon.getIconHeight() - overlayIcon.getIconHeight()) / 2;
				break;
				
			case SOUTH_EAST: 
				x = baseIcon.getIconWidth() - overlayIcon.getIconWidth();
				y = baseIcon.getIconHeight() - overlayIcon.getIconHeight();
				break;                
			
			case SOUTH:
				x = (baseIcon.getIconWidth() - overlayIcon.getIconWidth()) / 2;
				y = baseIcon.getIconHeight() - overlayIcon.getIconHeight();
				break;
			
			case SOUTH_WEST:
				x = 0;
				y = baseIcon.getIconHeight() - overlayIcon.getIconHeight();
				break;                
			
			default :
				x = (baseIcon.getIconWidth() - overlayIcon.getIconWidth()) / 2;
				y = (baseIcon.getIconHeight() - overlayIcon.getIconHeight()) / 2;
				break;            
		}
		
		return new Point( x, y);
	}        
	
	/**
	 * Return the height of the icon.
	 * 
	 * @return the icon height.
	 */
	public int getIconHeight() {
		return baseIcon.getIconHeight();
	}
	
	/**
	 * Return the width of the icon.
	 * 
	 * @return the icon width.
	 */
	public int getIconWidth() {
		return baseIcon.getIconWidth();
	}
	
	/**
	 * Paint the icon.
	 * 
	 * @param c the component to paint the icon on.
	 * @param g the graphics.
	 * @param x the x location.
	 * @param y the y location.
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) {
		baseIcon.paintIcon( c, g, x, y);
		
		for ( int i = 0; i < overlays.size(); i++) {
			Object[] objects = overlays.get(i);
			Icon overlayIcon = (Icon)objects[0];
			Point position = (Point)objects[1];

			overlayIcon.paintIcon( c, g, x+((int)position.getX()), y+((int)position.getY()));
		}
	}
}