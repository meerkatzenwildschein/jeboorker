/*
 * $Id: FormLayout.java,v 1.5 2008/01/28 21:28:37 edankert Exp $
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
import java.awt.Rectangle;
import java.util.HashMap;

/**
 * A Layout manager that can be used to layout 2 columns of components.
 * The alignment and fill type of the columns can be set by using the 
 * FormConstraint class.<br/>
 * A couple of predefined FormConstraint objects have been defined:
 * <ul>
 * <li>LEFT, Places the component in the left column of the form. 
 * <li>RIGHT, Places the component in the right column of the form.
 * <li>RIGHT_FILL, Places the component in the right column of the form and fills 
 * the component so it takes up the maximum horizontal space.
 * <li>FULL, The component takes up both the left and right column, and is aligned 
 * to the left of the row.
 * <li>FULL_FILL, The component takes up both the left and right column, and is set
 * to fill the whole width of the row.
 * </ul>
 *
 * Notes:<br/>
 * - Alignment of components that fill the height or width completely already will
 *   not result in a visible change. Ie. the full positioned component will never
 *   react to a Vertical alignment setting.<br/>
 * - The fill method will only change the horizontal appearance.<br/>
 * - Components that are bigger than the available width/height will be LEFT aligned.<br/>
 *
 * @version	$Revision: 1.5 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class FormLayout implements LayoutManager2 {
	private static final int MINIMUM	= 0;
	private static final int PREFERRED	= 1;
	private static final int MAXIMUM	= 2;

	/** A constraint that adds the component to the left side of the form. */
	public static final FormConstraints LEFT		= new FormConstraints( FormConstraints.LEFT);
    /** A constraint that adds the component to the right side of the form. */
    public static final FormConstraints RIGHT		= new FormConstraints( FormConstraints.RIGHT);
    /** 
     * A constraint that adds the component to the right side of the form 
     * and resizes the component to fill the whole available width. 
     */
    public static final FormConstraints RIGHT_FILL	= new FormConstraints( FormConstraints.RIGHT, true);
    /** A constraint that allows the component to use the total width of the form. */
    public static final FormConstraints FULL		= new FormConstraints( FormConstraints.FULL);
    /**
     * A constraint that allows the component to use the total width of 
     * the form and resizes the component to fill the whole width.
     */ 
    public static final FormConstraints FULL_FILL	= new FormConstraints( FormConstraints.FULL, true);;

    private int horizontalGap;		// horizontal gap
    private int verticalGap;		// vertical gap
    private HashMap<Component, FormConstraints> constraints;	// The constraints with the components as key

    /**
	 * Constructs a default FormLayout without any horizontal or
	 * vertical gaps.
     */
    public FormLayout() {
		this( 0, 0);
    }

    /**
     * Constructs a FormLayout with the specified horizontal
	 * and vertical gap.
     * 
     * @param hGap the horizontal gap between the left and right components.
     * @param vGap the vertical gap between the rows.
     */
    public FormLayout( int hGap, int vGap) {
		horizontalGap = hGap;
	    verticalGap = vGap;
	    constraints = new HashMap<Component, FormConstraints>();
    }
    
    /**
     * Returns the horizontal gap between the left column 
	 * and the right column.
	 *
	 * @return the horizontal gap.
     */
    public int getHgap() {
	    return horizontalGap;
    }

	/**
	 * Sets the horizontal gap between the left column 
	 * and the right column.
	 *
     * @param hGap the horizontal gap between the columns
     */
    public void setHgap( int hGap) {
    	horizontalGap = hGap;
    }

    /**
     * Returns the vertical gap between the rows.
	 *
	 * @return the vertical gap.
     */
    public int getVgap() {
	    return verticalGap;
    }

    /**
	 * Returns the vertical gap between the rows.
	 *
     * @param vGap the vertical gap between the rows
     */
    public void setVgap( int vGap) {
	    this.verticalGap = vGap;
    }
    
	/**
	 * Adds the specified component to the layout, using the specified
	 * constraint object. For Form layouts, the constraint must be
	 * an instance of the FormConstraints class.
	 *
	 * @param component the component added to the parent container.
	 * @param constraint the constraint for the added component.
	 */
    public void setConstraints( Component component, Object constraint) {
		if ( (constraint != null) && (constraint instanceof FormConstraints)) {
			constraints.put( component, new FormConstraints( (FormConstraints)constraint));
		} else {
			throw new IllegalArgumentException( "cannot add to layout: constraint must be of type FormConstraints");
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
            int totalComponents = parent.getComponentCount();

            // Total parent dimensions
            Dimension size = parent.getSize();
			int leftWidth = getWidth( FormConstraints.LEFT, PREFERRED, parent);
            int totalWidth = size.width - (insets.left + insets.right);
			int top = insets.top;
			int componentCounter = 0;

            while ( componentCounter < totalComponents) {
                Component component = parent.getComponent( componentCounter);

                if ( component.isVisible()) {
                	FormConstraints constraint = constraints.get( component);
                	int height = component.getPreferredSize().height;

                	if ( constraint.getPosition() == FormConstraints.LEFT) {
						
						componentCounter++;

	                	Component rightComponent = parent.getComponent( componentCounter);
	                	FormConstraints rightConstraint = constraints.get( rightComponent);
						
	                	if ( component.isVisible()) {
							if ( rightComponent.getPreferredSize().height > component.getPreferredSize().height) {
								height = rightComponent.getPreferredSize().height;
							} else {
								height = component.getPreferredSize().height;
							}
							
							if ( rightConstraint.getPosition() == FormConstraints.RIGHT) {
								// Right component
								align(	rightConstraint, 
										new Rectangle(	insets.left + leftWidth + horizontalGap, 
														top, 
														totalWidth - leftWidth - horizontalGap, 
														height), 
										rightComponent);
							} else {
								componentCounter--;
							}
							
	                	} else {
		                	componentCounter--;
	                	}
							
                		// Left component
                		align(	constraint, 
                				new Rectangle(	insets.left, 
                								top, 
                								leftWidth, 
                								height), 
                				component);
					
					// When an Component is added to the right without a left component.
                	} else if ( constraint.getPosition() == FormConstraints.RIGHT) { 
						align(	constraint, 
								new Rectangle(	insets.left + leftWidth + horizontalGap, 
												top, 
												totalWidth - leftWidth - horizontalGap,
												height), 
								component);

                	} else if ( constraint.getPosition() == FormConstraints.FULL) { 
						align(	constraint, 
								new Rectangle(	insets.left, 
												top, 
												totalWidth,
												height), 
								component);
                	} 
                	
                	top += verticalGap+height;
                }

                componentCounter++;
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

    /**
     * Returns the height of the parent, preferred, minimum or maximum.
     */
    private int getHeight( int type, Container parent) {
		boolean rowComplete = true;
		int height = 0;
	    int leftHeight = 0;
		int rowHeight = 0;
	    int rowCounter = 0;
		
	    for ( int i = 0; i < parent.getComponentCount(); i++) {
	    	Component component = parent.getComponent( i);

	    	if ( component.isVisible()) {
	    		rowHeight = getSize( type, component).height;
	    		FormConstraints constraint = constraints.get(component);

	    		// Check if the component is positioned on the left.
	    		if ( constraint.getPosition() == FormConstraints.LEFT) {
	    			// previous row was not complete
	    			if ( !rowComplete) { 
	    				height += leftHeight;
	    				rowCounter++;
	    			}

	    			leftHeight = rowHeight;
	    			rowComplete = false;

	    		// Check if the component is positioned on the left.
	    		} else if ( constraint.getPosition() == FormConstraints.RIGHT) {
	    			if ( !rowComplete) {
	    				if ( rowHeight < leftHeight) {
	    					rowHeight = leftHeight;
	    				}

		    			rowComplete = true;
	    			}

	    		// Check if the component is positioned over the full width.
	    		} else if ( constraint.getPosition() == FormConstraints.FULL) { 
	    			if ( !rowComplete) {
	    				rowHeight += leftHeight;
		    			rowComplete = true;
	    			}
	    		} 
				
				// If the row has finished.	    		
	    		if ( rowComplete) {
	    			height += rowHeight;
	    			rowCounter++;
	    		}
	    	}
	    }
	    
	    // The last component was positioned on the left.
	    if ( !rowComplete) {
	    	height += rowHeight;
	    	rowCounter++;
	    }
		
		return height + (verticalGap * (rowCounter - 1));
    }

	/**
	 * Returns the specified size for the component.
	 */
	private Dimension getSize( int type, Component component) {
		Dimension size = null;
		
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
		
		return size;
	}

    /**
     * Returns the specified size for the parent.
     */
    private Dimension getSize( int type, Container parent) {
	    Dimension size = new Dimension( 0, 0);

	    int height = getHeight( type, parent);

		Insets insets = parent.getInsets();

		int leftWidth = getWidth( FormConstraints.LEFT, type, parent);
		int rightWidth = getWidth( FormConstraints.RIGHT, type, parent);
		int fullWidth = getWidth( FormConstraints.FULL, type, parent);

	    // Add the container's insets!

	    if ( fullWidth < (leftWidth + rightWidth + horizontalGap)) {
	    	size.width = leftWidth + rightWidth + horizontalGap + insets.left + insets.right;
	    } else {
	    	size.width = fullWidth + insets.left + insets.right;
	    }
	    size.height = height + insets.top + insets.bottom;

	    return size;
    }
	
	/**
	 * Returns the width of the parent, preferred, minimum or maximum.
	 */
	private int getWidth( int position, int sizeType, Container parent) {
		int width = 0;

		for ( int i = 0; i < parent.getComponentCount(); i++) {
			Component component = parent.getComponent( i);

			if ( component.isVisible()) {
				FormConstraints constraint = constraints.get(component);
				
				if ( constraint.getPosition() == position && !constraint.isFilled()) {
					Dimension size = getSize(sizeType, component);
					
					if ( size.width > width) {
						width = size.width;
					}
				}
			}
		}
		
		return width;
	}

	/**
	 * Sets the bounds on the component and calculates the alignment.
	 */
	private void align( FormConstraints constraint, Rectangle bounds, Component component) {
		if ( !constraint.isFilled()) {
			if ( constraint.getHorizontalAlignment() == FormConstraints.CENTER) {
				int x = bounds.x + ((bounds.width - component.getPreferredSize().width) / 2);

				if ( x > bounds.x) {
					bounds.x = x;
				}
			} else if ( constraint.getHorizontalAlignment() == FormConstraints.RIGHT) {
				int x = (bounds.width + bounds.x) - component.getPreferredSize().width;

				if ( x > bounds.x) {
					bounds.x = x;
				}
			}

			if ( component.getPreferredSize().height != bounds.height) {
				if ( constraint.getVerticalAlignment() == FormConstraints.CENTER) {
					int y = bounds.y + ((bounds.height - component.getPreferredSize().height) / 2);

					if ( y > bounds.y) {
						bounds.y = y;
					}
					
				} else if ( constraint.getVerticalAlignment() == FormConstraints.BOTTOM) {
					int y = (bounds.height + bounds.y) - component.getPreferredSize().height;

					if ( y > bounds.y) {
						bounds.y = y;
					}
				}
				
				bounds.height = component.getPreferredSize().height;
			}

			bounds.width = component.getPreferredSize().width;
		}

		component.setBounds( bounds);
	}
}
