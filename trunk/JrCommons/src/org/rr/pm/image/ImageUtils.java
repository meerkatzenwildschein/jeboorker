package org.rr.pm.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.ReflectionUtils;

import com.sun.image.codec.jpeg.JPEGImageDecoder;



/**
 * provides some static methods to deal with images.
 */
public class ImageUtils {
	
	private static final Class<?> jpegCodecClass = ReflectionUtils.getClassForName("com.sun.image.codec.jpeg.JPEGCodec");
	
	/**
	 * Creates the image bytes from the given image.
	 * @param image the image to be converted into bytes.
	 * @param formatName The format of the returned bytes. For example "jpeg", "png" or "gif".
	 * @return The converted bytes or <code>null</code> if something went wrong with the conversion.
	 */
	public static byte[] getImageBytes(BufferedImage image, String mime) {
		if(image==null) {
			return null;
		}
		Iterator<ImageWriter> imageWritersByFormatName = ImageIO.getImageWritersByMIMEType(mime);
		if(imageWritersByFormatName.hasNext()) {
			ImageWriter writer = imageWritersByFormatName.next();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			MemoryCacheImageOutputStream mem = new MemoryCacheImageOutputStream(output);
			writer.setOutput(mem);
			try {
				writer.write(image);
			} catch (IOException e) {
				//OpenJDK is not able to encode Jpeg. This is just a fallback for this case.
				if(mime.equals("image/jpeg") || mime.equals("image/jpg")) {
					return encodeJpeg(image, 75);
				}
				
				LoggerFactory.logInfo(ImageUtils.class, "could not create thumbnail", e);
			} finally {
				try {mem.flush();} catch (Exception e) {}
				try {mem.close();} catch (Exception e) {}
			}
			return output.toByteArray();
		}
		return null;
	}

	/**
	 * Uses a poor and old jpeg encoder to encode the given image. It's possibly
	 * the last was if jpeg encoding is not supported with the jre per default.
	 * @param image The image to be encoded.
	 * @param quality Encoding quality value.
	 * @return The encoded image
	 */
	public static byte[] encodeJpeg(BufferedImage image, int quality) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JpegEncoder jpegEncoder = new JpegEncoder(image, quality, out);
		jpegEncoder.Compress();
		return out.toByteArray();
	}
	
	/**
	 * Loaded the given jpg file and decodes it.
	 * @param resourceLoader The jpeg resource to be loaded.
	 * @return The {@link BufferedImage} for the given file or <code>null</code> if the image could not be loaded.
	 */
	public static BufferedImage decodeJpeg(IResourceHandler resourceLoader) {
		//something about 200 ms faster than ImageIO.read and jai
		BufferedImage bi = null;
		InputStream bin = null;
		try {
			try {
				bin = resourceLoader.getContentInputStream();
				bi = ImageIO.read(bin);
			} catch (Exception e) {
			}
			
			if(bi == null && bin != null) {
				try {
					bin.reset();
					bi = decodeJpegInternal(bin, resourceLoader);
				} catch (IOException e) {
					try {
						bi = decodeJpegInternal(resourceLoader.getContentInputStream(), resourceLoader);
					} catch (IOException e1) {
					}
				}
			}
			
			if(bi == null && bin != null) {
				try {
					if(jpegCodecClass != null) {
						bin.reset();
						final JPEGImageDecoder decoder = (JPEGImageDecoder) ReflectionUtils.invokeMethod(jpegCodecClass, "createJPEGDecoder", bin);
						bi = decoder.decodeAsBufferedImage();
					}
				} catch(Exception e) {}
			}			
		} finally {
			IOUtils.closeQuietly(bin);
		}
		return bi;
	}
	
	/**
	 * Use a simple jpeg decoder as fallback.
	 * @param resourceLoader The jpeg resource to be loaded.
	 * @return The {@link BufferedImage} for the given file or <code>null</code> if the image could not be loaded.
	 */
	private static BufferedImage decodeJpegInternal(InputStream in, IResourceHandler resourceLoader) {
		try {
			JpegDecoder decoder = new JpegDecoder();
			return toBufferedImage(decoder.decode(resourceLoader.getContentInputStream()));
		} catch (Exception e1) {
			LoggerFactory.log(Level.INFO, ImageUtils.class, "Image " + resourceLoader.getResourceString() + " with mime " + resourceLoader.getMimeType() + " could not be loaded", e1);	
		}
		return null;
	}
	
	/**
	 * Scales the given image to the maximum fitting into the given frame
	 * dimension without loosing the proportionals.
	 * 
	 * @param frame The dimension for the target image
	 * @param image The image to be resized. 
	 * 
	 * @return a new {@link BufferedImage} instance with the scaled image data.
	 */
	public static BufferedImage scaleToMatch(final BufferedImage image, final Dimension frame, boolean proportional) {
		if(image==null) {
			return null;
		}
		
		if(proportional) {
			double heightFactor = ((double)frame.height) / ((double)image.getHeight());
			double widthFactor = ((double)frame.width) / ((double)image.getWidth());
	
			BufferedImage scaledImage = scalePercent(image, Math.min(heightFactor, widthFactor));
			return scaledImage;
		} else {
			BufferedImage scaledImage = new BufferedImage(frame.width, frame.height, image.getType() > 0 ? image.getType() : BufferedImage.TYPE_INT_RGB);
			Graphics scaledImageGraphics = scaledImage.getGraphics();
			scaledImageGraphics.drawImage(image, 0, 0, frame.width, frame.height, 0, 0, image.getWidth(), image.getHeight(), null);
			scaledImageGraphics.dispose();
			return scaledImage;
		}
	}
	
	/**
	 * Scales the given image so it matches to the given width
	 * without loosing it's proportionals.
	 * 
	 * @param width The width where the image should be scaled to.
	 * @param image The image to be resized. 
	 * 
	 * @return a new {@link BufferedImage} instance with the scaled image data.
	 */
	public static BufferedImage scaleToWidth(BufferedImage image, int width) {
		if(image==null) {
			return null;
		}
		
		double widthFactor = ((double)width) / ((double)image.getWidth());

		BufferedImage scaledImage = scalePercent(image, widthFactor);
		return scaledImage;
	}
	
	/**
	 * Scales the given image so it matches to the given height
	 * without loosing it's proportionals.
	 * 
	 * @param width The width where the image should be scaled to.
	 * @param image The image to be resized. 
	 * 
	 * @return a new {@link BufferedImage} instance with the scaled image data.
	 */
	public static BufferedImage scaleToHeight(BufferedImage image, int height) {
		if(image==null) {
			return null;
		}
		
		double heightFactor = ((double)height) / ((double)image.getHeight());

		BufferedImage scaledImage = scalePercent(image, heightFactor);
		return scaledImage;
	}	
	
	/**
	 * Scales the image handled by this {@link IImageProvider} instance so it shall match into the given frame
	 * dimension.
	 * 
	 * @param frame The dimension for the target image
	 * @param image The image to be resized. 
	 * 
	 * @return a new {@link BufferedImage} instance with the scaled image data.
	 */
	public static BufferedImage cutToMatch(BufferedImage image, Dimension frame) {
		BufferedImage cuttedImage = new BufferedImage(frame.width, frame.height, image.getType() != 0 ? image.getType() : BufferedImage.TYPE_INT_RGB);
		Graphics graphics = cuttedImage.getGraphics();
		graphics.drawImage(image, 0, 0, cuttedImage.getWidth(), cuttedImage.getHeight(), 0, 0, cuttedImage.getWidth(), cuttedImage.getHeight(), null);
		graphics.dispose();
		
		return cuttedImage;
	}	
	
	/**
	 * Shrinks or enlarges the current JpgImage object by the given scale
	 * factor, with a scale of 1 being 100% (or no change).<p>
	 * For example, if you need to reduce the image to 75% of the current size, 
	 * you should use a scale of 0.75. If you want to double the size of the
	 * image, you should use a scale of 2. If you attempt to scale using a
	 * negative number, the image will not be modified.
	 *
	 * @param  scale    the amount that this image should be scaled (1 = no change)
	 * @return a new {@link BufferedImage} instance with the scaled image data.
	 */
	public static BufferedImage scalePercent(BufferedImage image, double scale) {
		if ((scale > 0) && (scale != 1)) {
			AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
			AffineTransformOp op = new AffineTransformOp(scaleInstance, null);
			
			return op.filter(image, null);
		}
		return image;
	}
	
	/**
	 * Crops the frame having the given crop color around the given image.  
	 * 
	 * @param image The image to be croped.
	 * @param cropColor The color around the image.
	 * @return The croped image or the given image instance if no crop is needed.
	 */
	public static BufferedImage cropByColor(BufferedImage image, Color cropColor) {
		
		return null;
	}

	/**
	 * Gets a {@link AffineTransform} instance which scales the image
	 * so it shall fit into the given {@link Dimension} frame.
	 * @param d The {@link Dimension} frame where the image should be fit in.
	 * @return The desired {@link AffineTransform} instance.
	 */
	public static AffineTransform getTransformToMatchDimension(BufferedImage image, Dimension frame, double rotatenDegree) {
		//create a AffineTransform to scale the image so it matches into the given Dimension
		double heightFactor;
		double widthFactor;
		if(rotatenDegree==90d || rotatenDegree==270d) {
			heightFactor = ((double)frame.height) / ((double)image.getWidth());
			widthFactor = ((double)frame.width) / ((double)image.getHeight());
		} else {
			heightFactor = ((double)frame.height) / ((double)image.getHeight());
			widthFactor = ((double)frame.width) / ((double)image.getWidth());
		}
		double scale = Math.min(heightFactor, widthFactor);
		
		AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);

		//apply a transform to the AffineTransform so the image is located at the middle/center
		//of the given Dimension.
		double scaleX = scaleInstance.getScaleX();
		double scaleY = scaleInstance.getScaleY();
		double emptyX = ((double)frame.width) - (((double)image.getWidth()) * scaleX);
		double emptyY = ((double)frame.height) - (((double)image.getHeight()) * scaleY);
		scaleInstance.translate(emptyX/2/scaleX, emptyY/2/scaleY);
		
		//apply the rotation
		scaleInstance.rotate(Math.toRadians(rotatenDegree), (double)image.getWidth()/2 , (double)image.getHeight()/2);
		
		return scaleInstance;
	}	
	
	/**
	 * Detect and crop a white / light gray frame around the given image. 
	 * @param image The image to be croped.
	 * @return The croped image or the given one if no crop is needed.
	 */
	public static BufferedImage crop(BufferedImage image) {
		if(image==null) {
			return null;
		}
		
		try {
			int minRow = -1;
			int maxRow = -1;
			
			int[] img_pixels = new int[image.getHeight() * image.getWidth()];
			// ...grab this images's pixels
			PixelGrabber pg = new PixelGrabber(image, 0, 0, image.getWidth(), image.getHeight(), img_pixels, 0, image.getWidth());
			pg.grabPixels();
			for (int row = 0; row < image.getHeight(); ++row) {
				if (!isRowHomogeneous(img_pixels, image.getWidth() * row, image.getWidth())) {
					if (minRow < 0) {
						minRow = row;
					} else if (row > maxRow) {
						maxRow = row;
					}
				}
			}
	
			// ...how about column-based cropping?
			int minCol = -1;
			int maxCol = -1;
			for (int col = 0; col < image.getWidth(); ++col) {
				if (!isColumnHomogeneous(img_pixels, col, image.getWidth(), minRow, maxRow - minRow)) {
					if (minCol < 0) {
						minCol = col;
					} else if (col > maxCol) {
						maxCol = col;
					}
				}
			}
			
			if(minCol > 1 && maxRow > 1) {
				minCol+=1;
				minRow+=1;
				maxCol-=1;
				maxRow-=1;
				int cropedWidth = maxCol - minCol;
				int cropedHeight = maxRow - minRow;
				
				BufferedImage scaledImage = new BufferedImage(cropedWidth, cropedHeight, image.getType() != -1 ? image.getType() : BufferedImage.TYPE_INT_RGB);
				Graphics scaledImageGraphics = scaledImage.getGraphics();
				scaledImageGraphics.drawImage(image, 0, 0, scaledImage.getWidth(), scaledImage.getHeight(), minCol, minRow, maxCol, maxRow, null);
				scaledImageGraphics.dispose();
				
//				System.out.println(" org rect:  (" + image.getWidth() + " / " + image.getHeight());
//				System.out.println(" crop rect: (" + minCol + "," + minRow + ") to (" + maxCol + "," + maxRow + ")");
				
				return scaledImage;
			} else {
				return image;
			}
		} catch(Exception e) {
			LoggerFactory.logWarning(ImageUtils.class, "image crop has failed", e);
		}
		return null;
	}
	
	/**
	 * Tests if the given pixel is in the color area for cropping.  
	 * @param pixel The pixel to be tested.
	 * @return <code>true</code> if the pixel is in the crop color area als <code>false</code> otherwise.
	 */
	private static boolean isHomogeneousPixel(int pixel) {
		//int alpha = (pixel >> 24) & 0xff;
		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;

		return (red >=240 && green >=240 && blue>=200);
	}

	/**
	 * Tests if the given pixels in the specified range are in the cropping color area.  
	 * @param pixels The pixels contains the row to be tested.
	 * @param off The start offset for the pixels to be tested.
	 * @param len The amount of pixels to be tested starting with the off parameter.
	 * @return <code>true</code> if the row should be cropped and <code>false</code> otherwise.
	 */
	private static boolean isRowHomogeneous(int[] pixels, int off, int len) {
		//5% of pixels must noch match
		int failcount = (int) (((double)len)/100*5);
		for (int pixel = off; pixel < off + len; ++pixel) {
			if (!isHomogeneousPixel(pixels[pixel])) {
				if(failcount-- <= 0) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean isColumnHomogeneous(int[] pixels, int col, int row_len, int row_offset, int rows) {
		//5% of pixels must noch match
		int failcount = (int) (((double)row_len)/100*5);
		for (int row = row_offset; row < row_offset + rows; ++row) {
			int pixel = row * row_len + col;
			if (!isHomogeneousPixel(pixels[pixel])) {
				if(failcount-- <= 0) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static BufferedImage toBufferedImage(Image image) {
	    if (image instanceof BufferedImage) {
	        return (BufferedImage)image;
	    }

	    // This code ensures that all the pixels in the image are loaded
	    image = new ImageIcon(image).getImage();

	    // Determine if the image has transparent pixels; for this method's
	    // implementation, see Determining If an Image Has Transparent Pixels
	    boolean hasAlpha = hasAlpha(image);

	    // Create a buffered image with a format that's compatible with the screen
	    BufferedImage bimage = null;
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    try {
	        // Determine the type of transparency of the new buffered image
	        int transparency = Transparency.OPAQUE;
	        if (hasAlpha) {
	            transparency = Transparency.BITMASK;
	        }

	        // Create the buffered image
	        GraphicsDevice gs = ge.getDefaultScreenDevice();
	        GraphicsConfiguration gc = gs.getDefaultConfiguration();
	        bimage = gc.createCompatibleImage(
	            image.getWidth(null), image.getHeight(null), transparency);
	    } catch (HeadlessException e) {
	        // The system does not have a screen
	    }

	    if (bimage == null) {
	        // Create a buffered image using the default color model
	        int type = BufferedImage.TYPE_INT_RGB;
	        if (hasAlpha) {
	            type = BufferedImage.TYPE_INT_ARGB;
	        }
	        bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
	    }

	    // Copy image to buffered image
	    Graphics g = bimage.createGraphics();

	    // Paint the image onto the buffered image
	    g.drawImage(image, 0, 0, null);
	    g.dispose();

	    return bimage;
	}	
	
	public static boolean hasAlpha(Image image) {
	    // If buffered image, the color model is readily available
	    if (image instanceof BufferedImage) {
	        BufferedImage bimage = (BufferedImage)image;
	        return bimage.getColorModel().hasAlpha();
	    }

	    // Use a pixel grabber to retrieve the image's color model;
	    // grabbing a single pixel is usually sufficient
	     PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
	    try {
	        pg.grabPixels();
	    } catch (InterruptedException e) {
	    }

	    // Get the image's color model
	    ColorModel cm = pg.getColorModel();
	    return cm.hasAlpha();
	}
	
}
