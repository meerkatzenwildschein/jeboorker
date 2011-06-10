/*
 * $Id$
 *
 * Copyright (c) 2002 - 2008, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *   may  be used to endorse or promote products derived from this software 
 *   without specific prior written permission. 
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

import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JViewport;

/**
 * Handles the lay out of the components on the Desktop...
 *
 * @version	$Revision$, $Date$
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class ResizingFlowLayout extends FlowLayout {
	
	private static final long serialVersionUID = -7458075051466274410L;

    public ResizingFlowLayout() {
        super();
    }

    public ResizingFlowLayout(int arg0, int arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    public ResizingFlowLayout(int arg0) {
        super(arg0);
    }

    /**
     * Returns the preferred size for the target.
	 *
	 * @param target the target container to get the size for.
	 *
     * @return the preferred size of the container.
     */
    public Dimension preferredLayoutSize( Container target) {
		Insets insets = target.getInsets();
		int hgap = getHgap();
		int vgap = getVgap();
		
		Dimension targetSize = null;

		if ( target.getParent() instanceof JViewport) {
		    targetSize = target.getParent().getSize();
		} else {
			targetSize = target.getSize();
		}
		
		int maxwidth = targetSize.width - (insets.left + insets.right + hgap*2);
		int nmembers = target.getComponentCount();
		int xPos = 0;
		int maxHeight = 0;
		Dimension dim = new Dimension(0, 0);
		for (int i = 0 ; i < nmembers ; i++) {
			Component m = target.getComponent(i);
			if (m.isVisible()) {
				Dimension d = m.getPreferredSize();
				
				if (xPos + d.width > maxwidth && xPos != 0) {
					xPos = 0;
					dim.height += maxHeight + vgap;
					maxHeight = 0;
				}
				xPos += d.width;
				dim.width = Math.max( dim.width, xPos);
				
				xPos += hgap;
				
				maxHeight = Math.max( maxHeight, d.height);
			}
		}
		dim.height += maxHeight + vgap;
		dim.width += insets.left + insets.right ;
		dim.height += insets.top + insets.bottom + vgap;
		
		return dim;
    }

    /**
     * Returns the minimum size for the target, this is equal to the 
	 * preferred size.
     *
     * @param target the target container to get the size for.
     *
     * @return the preferred size of the container.
     */
//    public Dimension minimuLayoutSize( Container target) {
//		return preferredLayoutSize( target);
//    }
} 
