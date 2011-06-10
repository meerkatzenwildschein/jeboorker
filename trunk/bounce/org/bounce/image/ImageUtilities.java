/*
 * $Id: ImageUtilities.java,v 1.5 2008/01/28 21:28:37 edankert Exp $
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A class with utility methods for the images.
 * 
 * @version $Revision: 1.5 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class ImageUtilities {
	private static final boolean DEBUG = false;
	private static final int DEFAULT_PERCENTAGE = 30;

	/**
	 * Creates a 30% darker version of the image supplied.
	 * 
	 * @param image
	 *            the image to be darkened.
	 * 
	 * @return the darker image.
	 */
	public static ImageIcon createDarkerImage(ImageIcon image) {
		if (DEBUG)
			System.out.println("ImageUtilities.createDarkerImage( " + image + ")");

		return filter(image, new DarkerFilter(DEFAULT_PERCENTAGE));
	}

	/**
	 * Creates a 30% brighter version of the image supplied.
	 * 
	 * @param image
	 *            the image to be brightened.
	 * 
	 * @return the brighter image.
	 */
	public static ImageIcon createBrighterImage(ImageIcon image) {
		if (DEBUG)
			System.out.println("ImageLoader.createBrighterImage( " + image + ")");

		return filter(image, new BrighterFilter(DEFAULT_PERCENTAGE));
	}

	/**
	 * Creates a gray version of the image supplied.
	 * 
	 * @param image
	 *            the image to be filtered.
	 * 
	 * @return the filtered image.
	 */
	public static ImageIcon createGrayImage(ImageIcon image) {
		if (DEBUG)
			System.out.println("ImageLoader.createGrayImage( " + image + ")");

		return filter(image, new GrayFilter(DEFAULT_PERCENTAGE));
	}

	/**
	 * Creates a red version of the image supplied.
	 * 
	 * @param image
	 *            the image to be filtered.
	 * 
	 * @return the filtered image.
	 */
	public static ImageIcon createRedImage(ImageIcon image) {
		if (DEBUG)
			System.out.println("ImageLoader.createRedImage( " + image + ")");

		return filter(image, new RedFilter(DEFAULT_PERCENTAGE));
	}

	/**
	 * Creates a green version of the image supplied.
	 * 
	 * @param image
	 *            the image to be filtered.
	 * 
	 * @return the filtered image.
	 */
	public static ImageIcon createGreenImage(ImageIcon image) {
		if (DEBUG)
			System.out.println("ImageLoader.createGreenImage( " + image + ")");

		return filter(image, new GreenFilter(DEFAULT_PERCENTAGE));
	}

	/**
	 * Creates a blue version of the image supplied.
	 * 
	 * @param image
	 *            the image to be filtered.
	 * 
	 * @return the filtered image.
	 */
	public static ImageIcon createBlueImage(ImageIcon image) {
		if (DEBUG)
			System.out.println("ImageLoader.createBlueImage( " + image + ")");

		return filter(image, new BlueFilter(DEFAULT_PERCENTAGE));
	}

	/**
	 * Creates a cyan version of the image supplied.
	 * 
	 * @param image
	 *            the image to be filtered.
	 * 
	 * @return the filtered image.
	 */
	public static ImageIcon createCyanImage(ImageIcon image) {
		if (DEBUG)
			System.out.println("ImageLoader.createCyanImage( " + image + ")");

		return filter(image, new CyanFilter(DEFAULT_PERCENTAGE));
	}

	/**
	 * Creates a magenta version of the image supplied.
	 * 
	 * @param image
	 *            the image to be filtered.
	 * 
	 * @return the filtered image.
	 */
	public static ImageIcon createMagentaImage(ImageIcon image) {
		if (DEBUG)
			System.out.println("ImageLoader.createMagentaImage( " + image + ")");

		return filter(image, new MagentaFilter(DEFAULT_PERCENTAGE));
	}

	/**
	 * Creates a yellow version of the image supplied.
	 * 
	 * @param image
	 *            the image to be filtered.
	 * 
	 * @return the filtered image.
	 */
	public static ImageIcon createYellowImage(ImageIcon image) {
		if (DEBUG)
			System.out.println("ImageLoader.createYellowImage( " + image + ")");

		return filter(image, new YellowFilter(DEFAULT_PERCENTAGE));
	}

	/**
	 * Creates a silhouette version of the image supplied, in the supplied
	 * color.
	 * 
	 * @param image
	 *            the image to be filtered.
	 * @param color
	 *            the color of the filtered image.
	 * 
	 * @return the filtered image.
	 */
	public static ImageIcon createSilhouetteImage(ImageIcon image, Color color) {
		if (DEBUG)
			System.out.println("ImageLoader.createSilhouetteImage( " + image + ")");

		return filter(image, new SilhouetteFilter(color));
	}

	/**
	 * Utility method that filters the image.
	 * 
	 * @param image
	 *            the image to be filtered.
	 * @param filter
	 *            the filter to be used.
	 * 
	 * @return the filtered image.
	 */
	public static ImageIcon filter(ImageIcon image, RGBImageFilter filter) {
		ImageProducer prod = new FilteredImageSource(image.getImage().getSource(), filter);
		ImageIcon filteredImage = new ImageIcon(Toolkit.getDefaultToolkit().createImage(prod));

		return filteredImage;
	}

	/**
	 * Converts an Icon to an Image.
	 * 
	 * @param icon
	 *            the icon to convert.
	 * @return image the converted icon.
	 */
	public static Image iconToImage(Icon icon) {
		if (icon instanceof ImageIcon) {
			return ((ImageIcon) icon).getImage();
		}

		int w = icon.getIconWidth();
		int h = icon.getIconHeight();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(w, h);

		Graphics2D g = image.createGraphics();
		icon.paintIcon(null, g, 0, 0);
		g.dispose();

		return image;
	}

	/**
	 * Resizes an icon.
	 * 
	 * @param icon
	 *            the icon to resize.
	 * @param width
	 *            the width of the icon or 0 or smaller when the width should be
	 *            relative to the size of the icon and the provided height. Not
	 *            both height and width can be smaller than 0.
	 * @param height
	 *            the height of the icon or 0 or smaller when the height should
	 *            be relative to the size of the icon and the provided width.
	 *            Not both height and width can be smaller than 0.
	 * @return the resized icon.
	 */
	public static Icon resize(Icon icon, int width, int height) {
		if (icon == null) {
			return icon;
		}

		if ((height <= 0 || height == icon.getIconHeight()) && (width <= 0 || width == icon.getIconWidth())) {
			return icon;
		}

		Image image = iconToImage(icon);

		if (height <= 0) {
			height = (int) (icon.getIconHeight() / (float) (icon.getIconWidth() / width));
		}

		if (width <= 0) {
			width = (int) (icon.getIconWidth() / (float) (icon.getIconHeight() / height));
		}

		return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
	}
}
