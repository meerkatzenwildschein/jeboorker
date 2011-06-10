/*
 * $Id: FormConstraints.java,v 1.4 2008/01/28 21:28:37 edankert Exp $
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

/**
 * A constraints object, used for the FormLayout, the constraints allow 
 * for setting the components position, alignments and fill type.
 *
 * @version	$Revision: 1.4 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class FormConstraints {
	/** LEFT position */
	public static final int LEFT 	= 0;
	/** BOTTOM position */
	public static final int BOTTOM	= 1;
	/** RIGHT position */
	public static final int RIGHT	= 2;
	/** TOP position */
	public static final int TOP		= 3;
	/** CENTER position */
	public static final int CENTER	= 4;
	/** FULL position */
	public static final int FULL	= 5;

	/*
	 * The position of the component, can be any of the following values:
	 * LEFT, RIGHT, FULL.
	 */
	private int position;

	/**
	 * horizontal alignment, can be any of the following values:
	 * LEFT, CENTER, RIGHT.
	 */
    private int horizontalAlignment;

    /**
     * Vertical alignment, can be any of the following values:
     * TOP, CENTER, BOTTOM.
     */
    private int verticalAlignment;

    /**
     * If true, the component fills the whole available horizontal space.
     */
    private boolean filled;

    /**
	 * Constructs a default FormConstraint which positions the 
	 * component over both columns and aligns the component 
	 * horizontally to the LEFT and vertically at the CENTER.
     */
    public FormConstraints() {
		this( FULL, LEFT, CENTER);
    }

    /**
     * Constructs a default FormConstraint which aligns the 
	 * component horizontally to the LEFT and vertically at 
	 * the CENTER and positions the component at the position 
	 * supplied.
	 *
     * @param position the column position of the component.
     */
    public FormConstraints( int position) {
    	this( position, LEFT, CENTER);
    }

    /**
     * Constructs a FormConstraint with the filled value set to 
	 * the value supplied. If the value is set to true, the alignment
	 * is no longer important.
	 *
	 * @param position the column position of the component.
	 * @param filled whether the component should fill the available 
	 * horizontal space.
     */
    public FormConstraints( int position, boolean filled) {
		this( position, LEFT, CENTER);

	    this.filled = filled;
    }

    /**
     * Constructs a FormConstraint which aligns the component 
	 * horizontally with the value supplied and vertically at 
	 * the CENTER.
	 *
	 * @param position the column position of the component.
	 * @param horizontalAlignment the horizontal alignment type.
     */
    public FormConstraints( int position, int horizontalAlignment) {
		this( position, horizontalAlignment, CENTER);
    }

    /**
     * Constructs a FormConstraint which aligns the component 
     * horizontally and vertically with the values supplied.
     *
     * @param position the column position of the component.
     * @param horizontalAlignment the horizontal alignment type.
     * @param verticalAlignment the vertical alignment type.
     */
    public FormConstraints( int position, int horizontalAlignment, int verticalAlignment) {
		this.position = position;
		this.horizontalAlignment = horizontalAlignment;
	    this.verticalAlignment = verticalAlignment;
		this.filled = false;
    }
    
    /**
     * Constructs a FormConstraint which copies its values 
     * from the constraint supplied.
     *
     * @param constraint the form constraint to copy the values from.
     */
    public FormConstraints( FormConstraints constraint) {
	    this.horizontalAlignment = constraint.getHorizontalAlignment();
	    this.verticalAlignment = constraint.getVerticalAlignment();
	    this.filled = constraint.isFilled();
		this.position = constraint.getPosition();
    }

    /**
     * Sets wether the component should occupy the full available 
	 * horizontal space. The values for Horizontal and vertical alignment 
	 * are discarded when the component is filled.
	 *
     * @param enabled enables/disables the filling of the full space.
     */
    public void setFilled( boolean enabled) {
    	filled = enabled;
    }

    /**
     * Returns wether the component occupies the full available horizontal 
	 * space. The values for Horizontal and vertical alignment are discarded 
	 * when the component is filled.
     *
     * @return the filling of the available space.
     */
    public boolean isFilled() {
    	return filled;
    }

    /**
     * Sets the horizontal alignment for the component.
	 * The alignment can be any of the following values:
	 * LEFT, for a left aligned component.
	 * CENTER, for a center aligned component.
	 * RIGHT, for a right aligned component.
     *
     * @param alignment the horizontal alignment.
     */
    public void setHorizontalAlignment( int alignment) {
		horizontalAlignment = alignment;
    }

    /**
     * Returns the horizontal alignment for the component.
	 * @see #setHorizontalAlignment
     *
     * @return the horizontal alignment.
     */
    public int getHorizontalAlignment() {
		return horizontalAlignment;
    }

    /**
     * Sets the vertical alignment for the component.
	 * The alignment can be any of the following values:
	 * TOP, for a top aligned component.
	 * CENTER, for a center aligned component.
	 * BOTTOM, for a bottom aligned component.
     *
     * @param alignment the vertical alignment.
     */
    public void setVerticalAlignment( int alignment) {
		verticalAlignment = alignment;
    }

    /**
     * Returns the vertical alignment for the component.
	 * @see #setVerticalAlignment
     *
     * @return the vertical alignment.
     */
    public int getVerticalAlignment() {
		return verticalAlignment;
    }

    /**
     * Sets the column position of the component;
	 * LEFT, for a component in the left column.
	 * RIGHT, for a component in the right column.
	 * FULL, for a component that occupies both columns.
     *
     * @param position the column position of the component.
     */
    public void setPosition( int position) {
		this.position = position;
    }

    /**
     * Returns the column position of the component.
	 * @see #setPosition
     *
     * @return the column position.
     */
    public int getPosition() {
		return position;
    }

    /**
     * Finds out if the values in this FormConstraint are equal to 
	 * the supplied object.
     *
     * @param object the object to test.
     *
     * @return true when equal.
     */
    public boolean equals( Object object) {
		boolean equal = true;
		
		if ( this != object) {
			if ( object instanceof FormConstraints) {
				FormConstraints constraint = (FormConstraints) object;

				if ( constraint.isFilled() != filled) {
					equal = false;
				} else if ( constraint.getPosition() != position) {
					equal = false;
				} else if ( constraint.getHorizontalAlignment() != horizontalAlignment) {
					equal = false;
				} else if ( constraint.getVerticalAlignment() != verticalAlignment) {
					equal = false;
				} 
			} else {
				equal = false;
			}
		}

		return equal;
    }
}
