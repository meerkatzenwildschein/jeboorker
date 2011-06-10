/*
 * $Id: DarkerFilter.java,v 1.4 2008/01/28 21:28:37 edankert Exp $
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

package org.bounce.image;

/**
 * A filter to create an X% darker image.
 *
 * @version	$Revision: 1.4 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class DarkerFilter extends BaseFilter {
	private int percent;

	/**
	 * Constructs a Filter that can be used to construct a darker image.
	 *
	 * @param p the percentage of darkness in the range 0..100, where 100 is 
	 *        the darkest, and 0 is the lightest.
	 */
	public DarkerFilter( int p) {
	    percent = p;

	    canFilterIndexColorModel = true;
	}
	
	/**
	 * Overrides <code>RGBImageFilter.filterRGB</code>.
	 *
	 * @param x the horizontal position of the pixel.
	 * @param y the vertical position of the pixel.
	 * @param rgb the color of the pixel.
	 *
	 * @return the new rgb value
	 */
	public int filterRGB( int x, int y, int rgb) {
		int red 	= getRed( rgb);
		int green	= getGreen( rgb);
		int blue	= getBlue( rgb);

		red = red * (100 - percent) / 100;
		blue =  blue * (100 - percent) / 100;
		green = green *(100 - percent) / 100;
		
	    return getRGB( rgb, red, green, blue);
	}
} 
