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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Menu Utilities
 *
 * @version $Revision$, $Date$
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class MenuUtilities {

	/**
	 * Aligns sub items in a menu.
	 * 
	 * @param menu the menu to align
	 */
	public static void alignMenu( JMenu menu) {
		alignComponents( menu.getMenuComponents());
	}

	/**
	 * Aligns sub items in a popup menu.
	 * 
	 * @param menu the popmenu to align
	 */
	public static void alignMenu( JPopupMenu menu) {
		alignComponents( menu.getComponents());
	}

	private static void alignComponents( Component[] components) {
		Dimension size = new Dimension( 0, 0);

		// Non windows Look and Feel
		if ( UIManager.getLookAndFeel().getName().toLowerCase().indexOf( "windows") == -1) {
			Insets margin = null;
			Icon check = null;
			EmptyBorder border = null;

			// Find the icon size ...
			for ( Component component : components) {
				if ( component instanceof JMenuItem) {
					if ( component instanceof JCheckBoxMenuItem || component instanceof JRadioButtonMenuItem && check == null) {
						margin = ((JMenuItem)component).getMargin(); 
						check = UIManager.getIcon( "CheckBoxMenuItem.checkIcon");
					}
					
					updateSize( ((JMenuItem)component), size);
				} else if ( component instanceof JMenu) {
					updateSize( ((JMenuItem)component), size);
				}
			}
						
			if ( check != null && margin != null) {
				border = new EmptyBorder( margin.top, margin.left + check.getIconWidth(), margin.bottom, margin.right);
			}
	
			for ( Component component : components) {

				if ( component instanceof JMenuItem) {
				
					if ( component instanceof JCheckBoxMenuItem || component instanceof JRadioButtonMenuItem) {
						alignCheck( (JMenuItem)component, size, null);
					} else {
						align( (JMenuItem)component, size, border);
					}

				} else if ( component instanceof JMenu) {
					align( (JMenu)component, size, border);
				}
			}
		} else { // Windows Look and Feel
			// Find the icon size ...
			for ( Component component : components) {
				
				if ( component instanceof JMenuItem) {
					updateSize( (JMenuItem)component, size);
				} else if ( component instanceof JMenu) {
					updateSize( (JMenu)component, size);
				}
			}
			
			for ( Component component : components) {
				
				if ( component instanceof JMenuItem) {
					
					if ( component instanceof JRadioButtonMenuItem) {
						alignCheck( (JMenuItem)component, size, new EmptyBorder( 2, 5, 2, 2));
					} else if ( component instanceof JCheckBoxMenuItem) {
						alignCheck( (JMenuItem)component, size, null);
					} else {
						align( (JMenuItem)component, size, null);
					}

				} else if ( component instanceof JMenu) {
					align( (JMenu)component, size, null);
				}
			}
		}
	}
	
	private static void alignCheck( JMenuItem item, Dimension size, Border border) {
		Icon icon = item.getIcon();
		
		if ( border != null) {
			item.setBorder( border);
		}

		if ( icon != null && icon.getIconWidth() < size.width) {
			item.setIconTextGap( item.getIconTextGap() + ((size.width - icon.getIconWidth())/2));
		} else if ( icon == null) {
			item.setIcon( new DummyIcon( size));
		}
	}
	
	private static void align( AbstractButton button, Dimension size, Border border) {
		Icon icon = button.getIcon();
		
		if ( border != null) {
			button.setBorder( border);
		}

		if ( icon != null && icon.getIconWidth() < size.width) {
			Insets insets = button.getMargin();
			button.setBorder( new EmptyBorder( insets.top, insets.left+ (size.width - icon.getIconWidth()), insets.bottom, insets.right));
		} else if ( icon == null) {
			button.setIcon( new DummyIcon( size));
		}
	}
	
	private static void updateSize( AbstractButton button, Dimension size) {
		Icon icon = button.getIcon();
		
		if ( icon != null) {
			size.width = Math.max( size.width, icon.getIconWidth());
			size.height = Math.max( size.height, icon.getIconHeight());
		}
	}
} 
