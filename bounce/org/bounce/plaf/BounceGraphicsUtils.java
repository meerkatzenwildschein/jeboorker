/*
 * $Id: BounceGraphicsUtils.java,v 1.4 2008/01/28 21:28:37 edankert Exp $
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

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.FontMetrics;

import javax.swing.JComponent;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;

import javax.swing.text.View;

import javax.swing.plaf.basic.BasicGraphicsUtils;

/**
 * The Graphic Utils for the Bounce package.
 *
 * @version	$Revision: 1.4 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class BounceGraphicsUtils {
    /**
     * Draws one of the lines on a multiline label/button.
	 *
	 * @param g the Graphics context of the component.
	 * @param fm the font metrics.
	 * @param rect the rectangle that the text needs to be painted in.
     * @param text the text that needs to be drawn.
     * @param hAlign the horizontal alignment.
     * @param y the y position of the text.
	 * @param mnemonic the mnemonic.
	 *
	 * @return the x starting position of the text
     */
    public static int drawLine( Graphics g, FontMetrics fm, Rectangle rect, String text, int hAlign, int y, int mnemonic) {
	    int x = rect.x;
	    
	    if ( text != null) {
	    	int width = SwingUtilities.computeStringWidth( fm, text);

	    	if ( hAlign == SwingConstants.CENTER) {
	    		x = rect.x + ((rect.width - width) / 2);
	    	} else if ( hAlign == SwingConstants.RIGHT) {
	    		x = rect.x + (rect.width - width);
	    	}

	    	BasicGraphicsUtils.drawString( g, text, mnemonic, x, y);
	    }
		
		return x;	
    }


    /**
     * Lays out a multiline label/button with icon.
     * 
     * @param c the label component.
     * @param fm the font metrics.
     * @param text the label text.
     * @param icon the label icon.
     * @param verticalAlignment the vertical alignment.
     * @param horizontalAlignment the horizontal alignment.
     * @param verticalTextPosition the vertical text position.
     * @param horizontalTextPosition the horizontal text position.
     * @param viewR the viewing rectangle.
     * @param iconR the icon rectangle.
     * @param textR the text rectangle.
     * @param textIconGap the gap between the text and the icon.
     * @param minLines the minimum amount of lines to display.
     * @param maxLines the maximum amount of lines to display.
     * 
     * @return the newly layed out string.
     */
    public static String[] layoutMultilineCompoundLabel( 
									JComponent c, FontMetrics fm, String text,
									Icon icon, int verticalAlignment, int horizontalAlignment,
									int verticalTextPosition, int horizontalTextPosition, Rectangle viewR,
									Rectangle iconR, Rectangle textR, int textIconGap, int minLines, int maxLines) {
									
		String result[] = new String[ maxLines];
		
        /* 
		 * Initialize the icon bounds rectangle iconR.
         */
        if (icon != null) {
            iconR.width = icon.getIconWidth();
            iconR.height = icon.getIconHeight();
        } else {
            iconR.width = iconR.height = 0;
        }

        /* Initialize the text bounds rectangle textR.  If a null
         * or and empty String was specified we substitute "" here
         * and use 0,0,0,0 for textR.
         */

        boolean textIsEmpty = (text == null) || text.equals("");

		View v = null;
        
		if ( textIsEmpty) {
            textR.width = textR.height = 0;
            text = "";
        } else {
	        v = (c != null) ? (View) c.getClientProperty("html") : null;
    	
		    if (v != null) {
		    	textR.width = (int) v.getPreferredSpan(View.X_AXIS);
		    	textR.height = (int) v.getPreferredSpan(View.Y_AXIS);
			} else {
				textR.width = SwingUtilities.computeStringWidth( fm, text);
				textR.height = fm.getHeight() * minLines;
			}
		}

        /* Unless both text and icon are non-null, we effectively ignore
         * the value of textIconGap.  The code that follows uses the
         * value of gap instead of textIconGap.
         */

        int gap = (textIsEmpty || (icon == null)) ? 0 : textIconGap;

        if ( !textIsEmpty) {

            /* If the label text string is too wide to fit within the available
             * space "..." and as many characters as will fit will be
             * displayed instead.
             */

            int availTextWidth;

            if ( horizontalTextPosition == SwingUtilities.CENTER) {
                availTextWidth = viewR.width;
            } else {
                availTextWidth = viewR.width - (iconR.width + gap);
            }


			if ( textR.width > availTextWidth) {
				if (v != null) {
					textR.width = availTextWidth;
				} else {
					textR.width = 0;
					String clipString = "...";
					int prevChars = 0;
					int nChars = 0;

					for ( int i = 1; i <= maxLines && (nChars < text.length()); i++) {
						int totalWidth = 0;
						int spacePos = 0;
						
						if ( i == maxLines) {
							totalWidth = SwingUtilities.computeStringWidth( fm, clipString);
						} 

						for( nChars = prevChars; nChars < text.length(); nChars++) {
							char ch = text.charAt( nChars);

							if ( ch == ' ') {
								spacePos = nChars;
							}
							
							totalWidth += fm.charWidth( ch);
			
							if ( totalWidth > availTextWidth) {
								if ( (spacePos > 0) && (i < maxLines)) {
									nChars = spacePos;
								}
							
								break;
							}
						}
						
						if ( (i == maxLines) && (nChars < text.length())) {
							result[i-1] = text.substring( prevChars, nChars) + clipString;
						} else {
							result[i-1] = text.substring( prevChars, nChars);
							prevChars = nChars;

							if ( spacePos != 0) { // skip the space...
								prevChars += 1;
							}
						}

						textR.width = Math.max( textR.width, SwingUtilities.computeStringWidth( fm, result[i-1]));
					}
					
					textR.height = fm.getHeight() * minLines;
					
					for ( int i = minLines; (i < maxLines) && (result[i] != null); i++) {
						textR.height += fm.getHeight();
					}
				}
            } else {
				result[0] = text;
            }
        }


        /* Compute textR.x,y given the verticalTextPosition and
         * horizontalTextPosition properties
         */

        if ( verticalTextPosition == SwingUtilities.TOP) {
            if ( horizontalTextPosition != SwingUtilities.CENTER) {
                textR.y = 0;
            } else {
                textR.y = -(textR.height + gap);
            }
        } else if ( verticalTextPosition == SwingUtilities.CENTER) {
            textR.y = (iconR.height / 2) - (textR.height / 2);
        } else { // (verticalTextPosition == BOTTOM)
            if (horizontalTextPosition != SwingUtilities.CENTER) {
                textR.y = iconR.height - textR.height;
            } else {
                textR.y = (iconR.height + gap);
            }
        }

        if (horizontalTextPosition == SwingUtilities.LEFT) {
            textR.x = -(textR.width + gap);
        } else if (horizontalTextPosition == SwingUtilities.CENTER) {
            textR.x = (iconR.width / 2) - (textR.width / 2);
        } else { // (horizontalTextPosition == RIGHT)
            textR.x = (iconR.width + gap);
        }

        /* labelR is the rectangle that contains iconR and textR.
         * Move it to its proper position given the labelAlignment
         * properties.
         *
         * To avoid actually allocating a Rectangle, Rectangle.union
         * has been inlined below.
         */
        int labelR_x = Math.min(iconR.x, textR.x);
        int labelR_width = Math.max(iconR.x + iconR.width,
                                    textR.x + textR.width) - labelR_x;
        int labelR_y = Math.min(iconR.y, textR.y);
        int labelR_height = Math.max(iconR.y + iconR.height,
                                     textR.y + textR.height) - labelR_y;

        int dx, dy;

        if (verticalAlignment == SwingUtilities.TOP) {
            dy = viewR.y - labelR_y;
        }
        else if (verticalAlignment == SwingUtilities.CENTER) {
            dy = (viewR.y + (viewR.height / 2)) - (labelR_y + (labelR_height / 2));
        }
        else { // (verticalAlignment == BOTTOM)
            dy = (viewR.y + viewR.height) - (labelR_y + labelR_height);
        }

        if (horizontalAlignment == SwingUtilities.LEFT) {
            dx = viewR.x - labelR_x;
        }
        else if (horizontalAlignment == SwingUtilities.RIGHT) {
            dx = (viewR.x + viewR.width) - (labelR_x + labelR_width);
        }
        else { // (horizontalAlignment == CENTER)
            dx = (viewR.x + (viewR.width / 2)) -
                 (labelR_x + (labelR_width / 2));
        }

        /* Translate textR and glypyR by dx,dy.
         */

        textR.x += dx;
        textR.y += dy;

        iconR.x += dx;
        iconR.y += dy;
		
		

        return result;
    }
}
