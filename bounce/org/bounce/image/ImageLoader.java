/*
 * $Id: ImageLoader.java,v 1.6 2008/01/28 21:28:37 edankert Exp $
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

import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * Loads Images from file and stores the images in a list for future reference.
 *
 * @version	$Revision: 1.6 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class ImageLoader {
	private static final boolean DEBUG = false;
	private static ImageLoader loader = null;
	
	private HashMap<String, ImageIcon> images = null;
	
	/**
	 * Constructs the ImageLoader object. 
	 */
	protected ImageLoader() {
		images = new HashMap<String, ImageIcon>();
	}

	/**
	 * Returns the single reference to the ImageLoader.
	 *
	 * @return the ImageLoader singleton.
	 */
	public static ImageLoader get() {
		if ( loader == null) {
			loader = new ImageLoader();
		}

		return loader;
	}

	/**
	 * Gets an image for the string supplied. If the image cannot be found 
	 * in the list of images already loaded, the image is loaded from the 
	 * class path.
	 * 
	 * @param name the name of the image.
	 *
	 * @return the image.
	 */
	public ImageIcon getImage( String name) {
		if (DEBUG) System.out.println("ImageLoader.getImage("+name+")");

		ImageIcon icon = images.get(name);
		
		if (icon == null) {
			icon = new ImageIcon(this.getClass().getResource(name));
			images.put(name, icon);
		}
		
		return icon;
	}

	/**
	 * Gets an image for the url supplied. The image is only loaded from 
	 * file if the image cannot be found in the list of images already loaded.
	 * 
	 * @param url the url to the image.
	 *
	 * @return the image.
	 */
	public ImageIcon getImage(URL url) {
		if (DEBUG) System.out.println( "ImageLoader.getImage( "+url+")");
		ImageIcon icon = images.get(url.toString());
		
		if (icon == null) {
			icon = new ImageIcon(url);
			images.put(url.toString(), icon);
		}
		
		return icon;
	}
} 
