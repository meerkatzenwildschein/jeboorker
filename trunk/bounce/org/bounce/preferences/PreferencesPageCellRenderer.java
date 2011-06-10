/*
 * $Id$
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
package org.bounce.preferences;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * The cell renderer for a XmlElementNode.
 *
 * @version	$Revision$, $Date$
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class PreferencesPageCellRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = -1320645592895770122L;

    private static final Border BORDER = new EmptyBorder( 0, 2, 0, 0);
	
	/**
	  * Configures the renderer based on the passed in components.
	  * The value is set from messaging the tree with
	  * <code>convertValueToText</code>, which ultimately invokes
	  * <code>toString</code> on <code>value</code>.
	  * The foreground color is set based on the selection and the icon
	  * is set based on on leaf and expanded.
	  */
	public Component getTreeCellRendererComponent( JTree tree, Object value,
						  boolean selected, boolean expanded, boolean leaf, 
						  int row,  boolean focus) {
		JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, focus);
		
		label.setIcon(null);
		label.setBorder(BORDER);
        Object object = ((DefaultMutableTreeNode)value).getUserObject();
        
        if (object instanceof PreferencesPage) {
            label.setText(((PreferencesPage)object).getLabel());
        }
		
		return label;
	}
} 