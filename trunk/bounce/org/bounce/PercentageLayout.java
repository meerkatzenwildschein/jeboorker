/*
 * $Id: PercentageLayout.java,v 1.5 2008/01/28 21:28:37 edankert Exp $
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.HashMap;

import javax.swing.SwingConstants;

/**
 * A Layout manager that can be used to layout components horizontally or
 * vertically. Every component will be resized relative to the value
 * supplied as a constraint. All the Integer values will be added together 
 * and will be seen as 100% so if the next two components are added:<br/>
 * <code>add( component1, new Integer( 150)); </code><br/>
 * <code>add( component2, new Integer( 50)); </code><br/>
 * In this case 'component1' will occupy 75% of the panel and 'component2' 
 * will occupy 25% of the panel.
 *
 * @version	$Revision: 1.5 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class PercentageLayout implements LayoutManager2 {
    /** Sets the layout orientation to horizontal. */
    public static final int HORIZONTAL 	= SwingConstants.HORIZONTAL;
    /** Sets the layout orientation to vertical. */
    public static final int VERTICAL	= SwingConstants.VERTICAL;

    private int gap;	// gap between the components
    private int orientation;

    private HashMap<Component, Object> constraints;	// The constraints with the components as key

    /**
     * Constructs a default PercentageLayout without a gap and with 
     * a horizontal orientation.
     */
    public PercentageLayout() {
    	this( HORIZONTAL, 0);
    }

    /**
     * Constructs a PercentageLayout with the orientation supplied 
     * and with no gap between the components.
     *
     * @param orientation the orientation of the components.
     */
    public PercentageLayout( int orientation) {
    	this( orientation, 0);
    }

    /**
     * Constructs a PercentageLayout with the gap and orientation 
     * supplied.
     *
     * @param orientation the orientation of the components.
     * @param gap the space between the components.
     */
    public PercentageLayout( int orientation, int gap) {
    	this.orientation = orientation;
    	this.gap = gap;
		
		constraints = new HashMap<Component, Object>();
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
	 * constraint object. For Percentage layouts, the constraint must be
	 * an instance of the Integer class.
	 *
	 * @param component the component added to the parent container.
	 * @param constraint the constraint for the added component.
	 */
    public void setConstraints( Component component, Object constraint) {
		if ( (constraint != null) && (constraint instanceof Integer)) {
			constraints.put( component, constraint);
		} else {
			throw new IllegalArgumentException( "cannot add to layout: constraint must be of type Integer");
		}
    }
    
    /**
     * Removes the specified component and constraints from the layout.
	 *
     * @param component the component to be removed
     */
    public void removeLayoutComponent( Component component) {
        constraints.remove( component);
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
			return new Dimension( 0, 0);
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
		    return new Dimension( 0, 0);
	    }
    }
    
    /** 
     * Lays out the components on the specified container.
	 *
     * @param parent the component which needs to be laid out 
     */
    public void layoutContainer( Container parent) {
        synchronized ( parent.getTreeLock()) {
			Dimension size = parent.getSize();
			Insets insets = parent.getInsets();
			int width = size.width - (insets.left + insets.right);
			int totalGap = gap * (parent.getComponentCount() - 1);
			int height = size.height - (insets.bottom + insets.top);
			int total = getTotal( parent);
			int pos = 0;
			
			if ( orientation == HORIZONTAL) {
				pos = insets.left;
			} else {
				pos = insets.top;
			}
			
			for ( int i = 0; i < parent.getComponentCount(); i++) {
				Component component = parent.getComponent( i);

				if ( component.isVisible()) {
					int value = ((Integer)constraints.get( component)).intValue();
					
					int x = 0;
					int y = 0;
					int w = 0;
					int h = 0;
					
					if ( orientation == HORIZONTAL) {
						x = pos;
						y = insets.top;
						h = height;
						
						// Last component, occupy left over space.
						if ( i == parent.getComponentCount() - 1) {
							w = width - x + insets.left;
						} else {
							w = ((width - totalGap) * value) / total;
						}
						
						if ( w < 0) {
							w = 0;
						}

						component.setBounds( x, y, w, h);
						
						pos += w + gap;
					} else {
						x = insets.left;
						y = pos;
						w = width;

						if ( i == parent.getComponentCount() - 1) {
							h = height - y + insets.top;
						} else {
							h = ((height - totalGap) * value) / total;
						}
						
						if ( h < 0) {
							h = 0;
						}

						component.setBounds( x, y, w, h);
						
						pos += h + gap;
					}
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
     * @param target the component to be laid out
     *
     * @return the maximum size of the layout.
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

    /*
	 * Returns the total added together values of all visible components.
	 */
    private int getTotal( Container parent) {
		int total = 0;
		
		for ( int i = 0; i < parent.getComponentCount(); i++) {
			Component component = parent.getComponent( i);
			
			if ( component.isVisible()) {
				total += ((Integer)constraints.get( component)).intValue();
			}
		}
		
		return total;
	}
}

