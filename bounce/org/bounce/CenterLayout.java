/*
 * $Id: CenterLayout.java,v 1.5 2008/01/28 21:28:37 edankert Exp $
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

import java.awt.LayoutManager2;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Dimension;

import javax.swing.SwingConstants;

/**
 * A Layout manager that can be used to layout 3 components, 
 * one component in the center that does not resize, and a 
 * component on either side that take up the rest of the 
 * available space. The orientation can be Vertical or 
 * Horizontal.
 *
 * @version	$Revision: 1.5 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class CenterLayout implements LayoutManager2 {
	private static final int MINIMUM	= 0;
	private static final int PREFERRED	= 1;
	private static final int MAXIMUM	= 2;

	/** A constraint that adds the component to the center. */
	public static final String CENTER	= "Center";
    /** A constraint that adds the component to the left/top. */
    public static final String TOP		= "Top/Left";
    /** A constraint that adds the component to the left/top. */
    public static final String LEFT		= TOP;
    /** A constraint that adds the component to the right/bottom. */
    public static final String BOTTOM 	= "Bottom/Right";
    /** A constraint that adds the component to the right/bottom. */
    public static final String RIGHT 	= BOTTOM;

    /** Sets the layout orientation to horizontal. */
    public static final int HORIZONTAL 	= SwingConstants.HORIZONTAL;
    /** Sets the layout orientation to vertical. **/
    public static final int VERTICAL	= SwingConstants.VERTICAL;

    private int gap;	// gap between the components
	private int orientation;
	
	private Component topLeftComponent = null;
	private Component bottomRightComponent = null;
	private Component centerComponent = null;

    /**
	 * Constructs a default CenterLayout without a gap and with 
	 * a horizontal orientation.
     */
    public CenterLayout() {
		this( HORIZONTAL, 0);
    }

    /**
     * Constructs a CenterLayout with the orientation supplied 
	 * and with no gap between the components.
	 *
     * @param orientation the orientation of the components.
     */
    public CenterLayout( int orientation) {
    	this( orientation, 0);
    }

    /**
     * Constructs a CenterLayout with the gap and orientation 
	 * supplied.
     *
     * @param orientation the orientation of the components.
     * @param gap the space between the components.
     */
    public CenterLayout( int orientation, int gap) {
		this.orientation = orientation;
		this.gap = gap;
    }

    /**
     * Returns the gap between the components.
	 *
	 * @return the gap.
     */
    public int getGap() {
	    return gap;
    }

    /**
     * Sets the gap between the components.
     *
     * @param gap the gap.
     */
    public void setGap( int gap) {
        this.gap = gap;
    }

    /**
     * Returns the orientation of the layout manager.
     *
     * @return the orientation.
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * Sets the orientation of the layout manager.
     *
     * @param orientation the orientation.
     */
    public void setOrientation( int orientation) {
        this.orientation = orientation;
    }

	/**
	 * Adds the specified component to the layout, using the specified
	 * constraint object. For a Center layout, the constraint must be
	 * an instance of a String.
	 *
	 * @param component the component added to the parent container.
	 * @param constraint the constraint for the added component.
	 */
    public void setConstraints( Component component, Object constraint) {
		if ( (constraint != null) && (constraint instanceof String)) {
			String value = (String) constraint;
		
			// Remove the component if used as other component.
			removeLayoutComponent( component);
			
			if ( value == CENTER) {
				centerComponent = component;
			} else if ( value == TOP) { // TOP and LEFT are already the same.
				topLeftComponent = component;
			} else if ( value == BOTTOM) { // BOTTOM and RIGHT are already the same.
				bottomRightComponent = component;
			} else {
				throw new IllegalArgumentException( "cannot add to layout: constraint must be of a defined type");
			}
		} else {
			throw new IllegalArgumentException( "cannot add to layout: constraint must be of type String");
		}
    }
    
    /**
     * Removes the specified component and constraints from the layout.
	 *
     * @param component the component to be removed
     */
    public void removeLayoutComponent( Component component) {
		if ( component != null) {
			if ( centerComponent != null && centerComponent.equals( component)) {
				centerComponent = null;
			} else if ( topLeftComponent != null && topLeftComponent.equals( component)) {
				topLeftComponent = null;
			} else if ( bottomRightComponent != null && bottomRightComponent.equals( component)) {
				bottomRightComponent = null;
			}
		}
    }

    /**
     * Calculates the preferred size dimensions for the specified 
     * container given the components in the specified parent container.
	 * 
     * @param parent the component to be laid out.
	 *
     * @return the preferred size of the layout.
     */
    public Dimension preferredLayoutSize( Container parent) {
        synchronized ( parent.getTreeLock()) {
			return getSize( PREFERRED, parent);
		}
    }

    /** 
     * Calculates the minimum size dimensions for the specified 
     * panel given the components in the specified parent container.
	 *
     * @param parent the component to be laid out
     *
     * @return the minimum size of the layout.
     */
    public Dimension minimumLayoutSize(Container parent) {
	    synchronized ( parent.getTreeLock()) {
		    return getSize( MINIMUM, parent);
	    }
    }
    
    /** 
     * Lays out the components on the specified container.
	 *
     * @param parent the component which needs to be laid out 
     */
    public void layoutContainer( Container parent) {
        synchronized ( parent.getTreeLock()) {
            Insets insets = parent.getInsets();
			Dimension size = parent.getSize();
			int width = size.width - (insets.left + insets.right);
			int height = size.height - (insets.top + insets.bottom);
			Dimension centerSize = getSize( PREFERRED, centerComponent);
			int totalGap = getTotalGap();
			
			// VERTICAL Alignment
			if ( orientation == VERTICAL) {
				int x = insets.left;
				int y = insets.top;
				int w = width;
				int h = 0;

 				// TOP Component
				h = (height - centerSize.height - totalGap) / 2;
					
				if ( h < 0) {
					h = 0;
				}
					
				if ( topLeftComponent != null && topLeftComponent.isVisible()) {
					topLeftComponent.setBounds( x, y, w, h);
				}
	
				y += h + gap;
				
				// CENTER Component
				if ( centerComponent != null && centerComponent.isVisible()) {
					h = centerSize.height;
					centerComponent.setBounds( x, y, w, h);
					y += h + gap;
				}

				// BOTTOM Component
				h = (height - y) + insets.top;
					
				if ( h < 0) {
					h = 0;
				}
					
				if ( bottomRightComponent != null && bottomRightComponent.isVisible()) {
					bottomRightComponent.setBounds( x, y, w, h);
				}

			// HORIZONTAL Alignment
			} else {
				int x = insets.left;
				int y = insets.top;
				int w = 0;
				int h = height;

				// LEFT Component
				w = (width - centerSize.width - totalGap) / 2;

				if ( w < 0) {
					w = 0;
				}

				if ( topLeftComponent != null && topLeftComponent.isVisible()) {
					topLeftComponent.setBounds( x, y, w, h);
				}

				x += w + gap;

				// CENTER Component
				if ( centerComponent != null && centerComponent.isVisible()) {
					w = centerSize.width;
					
					centerComponent.setBounds( x, y, w, h);
					
					x += w + gap;
				}
				
				// RIGHT Component
				w = (width - x) + insets.left;
					
				if ( w < 0) {
					w = 0;
				}
					
				if ( bottomRightComponent != null && bottomRightComponent.isVisible()) {
					bottomRightComponent.setBounds( x, y, w, h);
				}
			}
        }
    }
    
    /**
     * Adds the specified component to the layout, using the specified
     * constraint object.
	 *
     * @param component the component to be added
     * @param constraints  where/how the component is added to the layout.
     */
    public void addLayoutComponent(Component component, Object constraints) {
		setConstraints( component, constraints);
    }

    /** 
     * Returns the maximum size of this component.
	 *
	 * @param target the container to calculate the maximum size for.
	 * 
	 * @return the maximum size.
	 * 
     * @see java.awt.Component#getMinimumSize()
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Returns the alignment along the x axis.  This specifies how
     * the component would like to be aligned relative to other 
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     * 
	 * @param target the container to calculate X alignment for.
	 * 
	 * @return the X alignment.
     */
    public float getLayoutAlignmentX( Container target) {
        return 0.5f;
    }

    /**
     * Returns the alignment along the y axis.  This specifies how
     * the component would like to be aligned relative to other 
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     * 
	 * @param target the container to calculate Y alignment for.
	 * 
	 * @return the Y alignment.
     */
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }

    /**
     * Invalidates the layout, indicating that if the layout manager
     * has cached information it should be discarded.
     * 
	 * @param target the container.
     */
    public void invalidateLayout(Container target) {
        // Do nothing
    }

    /**
	 * Not used, components cannot be added without a constraint.
     *
	 * @param name the component name.
	 * @param comp the component.
	 */
    public void addLayoutComponent( String name, Component comp) {}

	/**
	 * Returns the the total gap.
	 */
	private int getTotalGap() {
		int totalGap = 0;
		
		if ( topLeftComponent != null && topLeftComponent.isVisible()) {
			totalGap += gap;
		}
		
		if ( bottomRightComponent != null && bottomRightComponent.isVisible()) {
			totalGap += gap;
		}
		
		if ( centerComponent == null || !centerComponent.isVisible()) {
			totalGap -= gap;
		}
		
		if ( totalGap < 0) {
			totalGap = 0;
		}

		return totalGap;
	}

	/**
	 * Returns the specified size for the component.
	 */
	private Dimension getSize( int type, Component component) {
		Dimension size = new Dimension();
		
		if ( component != null && component.isVisible()) {
			switch ( type) {
				case MINIMUM:
					size = component.getMinimumSize();
					break;

				case PREFERRED:
					size = component.getPreferredSize();
					break;

				case MAXIMUM:
					size = component.getMaximumSize();
					break;
			}
		}
		
		return size;
	}

    /**
     * Returns the specified size for the parent.
     */
    private Dimension getSize( int type, Container parent) {
	    Dimension size = new Dimension();
	    Dimension centerSize = getSize( type, centerComponent);
	    
	    Insets insets = parent.getInsets();
	    
	    int totalGap = getTotalGap();
	    
	    if ( orientation == VERTICAL) {
	    	size.width = centerSize.width + insets.left + insets.right;
	    	size.height = centerSize.height + totalGap + insets.top + insets.bottom;
	    } else {
	    	size.width = centerSize.width + totalGap + insets.left + insets.right;
	    	size.height = centerSize.height + insets.top + insets.bottom;
	    }

	    return size;
    }
}
