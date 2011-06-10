/*
 * $Id: BaseFilter.java,v 1.4 2008/01/28 21:28:37 edankert Exp $
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

import java.awt.image.RGBImageFilter;

/**
 * The base filter for all the image filters in the bounce package. 
 * Contains a couple of handy Utility methods.
 *
 * @version	$Revision: 1.4 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public abstract class BaseFilter extends RGBImageFilter {

	/**
	 * Gets the red value out of the rgb value.
	 *
	 * @param rgb the rgb value of the pixel.
	 *
	 * @return the red component value.
	 */
	protected int getRed( int rgb) {
		return (rgb >> 16) & 0xff;
	}

	/**
	 * Gets the blue value out of the rgb value.
	 *
	 * @param rgb the rgb value of the pixel.
	 *
	 * @return the blue component value.
	 */
	protected int getBlue( int rgb) {
		return rgb & 0xff;
	}

	/**
	 * Gets the green value out of the rgb value.
	 *
	 * @param rgb the rgb value of the pixel.
	 *
	 * @return the green component value.
	 */
	protected int getGreen( int rgb) {
		return (rgb >> 8) & 0xff;
	}

	/**
	 * Returns the rgb value for the blue, red and green values.
	 *
	 * @param rgb the previous rgb value of the pixel, with alpha value.
	 * @param red the red component value of the pixel.
	 * @param green the green component value of the pixel.
	 * @param blue the blue component value of the pixel.
	 *
	 * @return the rgb value.
	 */
	protected int getRGB( int rgb, int red, int green, int blue) {
		return (rgb & 0xff000000) | (red << 16) | (green << 8) | (blue << 0);
	}
	
	/**
	 * Converts the red green and blue values to one value.
	 *
	 * @param rgb the rgb value of the pixel.
	 *
	 * @return a component value (between 0-255).
	 */
	protected int getNTSCValue( int rgb) {
		int component = (int)((0.30 * getRed( rgb) + 
		                     0.59 * getGreen( rgb) + 
		                     0.11 * getBlue( rgb)) / 3);
							 
		return component;
	}
} 
