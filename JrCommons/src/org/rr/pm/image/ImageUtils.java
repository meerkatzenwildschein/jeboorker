package org.rr.pm.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.LookupOp;
import java.awt.image.PixelGrabber;
import java.awt.image.ShortLookupTable;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;

/**
 * provides some static methods to deal with images.
 */
public class ImageUtils {
	
	private static final short[] invertTable;
	static {
		invertTable = new short[256];
		for (int i = 0; i < 256; i++) {
			invertTable[i] = (short) (255 - i);
		}
	}	
	
	/**
	 * Creates the image bytes from the given image.
	 * @param image the image to be converted into bytes.
	 * @param formatName The format of the returned bytes. For example "jpeg", "png" or "gif".
	 * @return The converted bytes or <code>null</code> if something went wrong with the conversion.
	 */
	public static byte[] getImageBytes(final BufferedImage image, String mime) {
		if (image == null) {
			return null;
		}
		
		//image/jpg did not always work with ImageIO.getImageWritersByMIMEType
		if(mime.equals("image/jpg")) {
			mime = "image/jpeg";
		}
		
		ImageWriter writer = null;
        Iterator<ImageWriter> imageWritersByFormatName = ImageIO.getImageWritersByMIMEType(mime);
        while(imageWritersByFormatName.hasNext()) {
        	ImageWriter next = imageWritersByFormatName.next();
        	if(writer == null && next.getClass().getName().equals("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriter")) {
        		writer = next;
        		break;
        	} else {
        		writer = next;
        	}
        }
		
        if(writer != null) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			MemoryCacheImageOutputStream mem = new MemoryCacheImageOutputStream(output);
			writer.setOutput(mem);
			try {
				writer.write(image);
			} catch (IOException e) {
				LoggerFactory.logInfo(ImageUtils.class, "could not create thumbnail", e);
			} finally {				
				try {mem.flush();} catch (Exception e) {}
				try {mem.close();} catch (Exception e) {}
				try {writer.setOutput(null);} catch(Exception e) {}
				try {writer.dispose();} catch(Exception e) {}
			}
			return output.toByteArray();
        }
        return null;
	}
	
	/**
	 * Loaded the given jpg file and decodes it.
	 * @param resourceLoader The jpeg resource to be loaded.
	 * @return The {@link BufferedImage} for the given file or <code>null</code> if the image could not be loaded.
	 */
	static BufferedImage decodeImage(IResourceHandler resourceLoader, String mime) {
		BufferedImage bi = null;
		InputStream bin = null;
		try {
			bin = resourceLoader.getContentInputStream();
			bi = ImageIO.read(bin);
		} catch (Exception e) {
			//LoggerFactory.getLogger().log(Level.WARNING, "Could not decode image " + resourceLoader, e);
		} finally {
			IOUtils.closeQuietly(bin);
			bin = null;				
		}
		
		return bi;
	}
	
	/**
	 * Scales the given image to the maximum fitting into the given frame
	 * dimension without loosing the proportions.
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
	 * without loosing it's proportions.
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
	 * without loosing it's proportions.
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
	 * Scales the image so it shall match into the given frame dimension.
	 * 
	 * @param frame The dimension for the target image
	 * @param image The image to be resized. 
	 * 
	 * @return a new {@link BufferedImage} instance with the scaled image data.
	 */
	public static BufferedImage cut(BufferedImage image, Rectangle frame) {
	    BufferedImage dest = image.getSubimage(frame.x, frame.y, frame.width, frame.height);
	    return dest;
	}

	/**
	 * Splits the given image into the given amount of parts.
	 */
	public static List<BufferedImage> splitHorizontal(BufferedImage image, int parts) {
		if(parts > 1) {
			ArrayList<BufferedImage> result = new ArrayList<BufferedImage>(parts);
			int width = image.getWidth();
			int height = image.getHeight();
			int each = width / parts;
			for(int i = 0; i < parts; i++) {
				BufferedImage cut = cut(image, new Rectangle(i * each, 0, each, height));
				result.add(cut);
			}
			return result;
		}
		return Collections.singletonList(image);
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
		if(rotatenDegree == 90d || rotatenDegree == 270d) {
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
		if(image == null) {
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
				
				if(cropedWidth < 10 || cropedHeight < 10) {
					return image;
				}
				BufferedImage scaledImage = new BufferedImage(cropedWidth, cropedHeight, image.getType() > 0 ? image.getType() : BufferedImage.TYPE_INT_RGB);
				Graphics scaledImageGraphics = scaledImage.getGraphics();
				scaledImageGraphics.drawImage(image, 0, 0, scaledImage.getWidth(), scaledImage.getHeight(), minCol, minRow, maxCol, maxRow, null);
				scaledImageGraphics.dispose();
				
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
	
	/**
	 * Inverts the colors of the given image.
	 * @param src The image to be invert.
	 * @return The inverted image.
	 */
	public static BufferedImage invertImage(final BufferedImage src) {
		final int w = src.getWidth();
		final int h = src.getHeight();
		final BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		 
		final BufferedImageOp invertOp = new LookupOp(new ShortLookupTable(0, invertTable), null);			
		if(src.getType() == BufferedImage.TYPE_BYTE_INDEXED || src.getType() == 12) {
			BufferedImage newSrc = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			newSrc.getGraphics().drawImage(src, 0, 0, null);
			return invertOp.filter(newSrc, dst);
		} else {
			return invertOp.filter(src, dst);	
		}
	}	
	

    /** 
     * @author flubshi 
     */
    public static BufferedImage convertToGrayScale(final BufferedImage bufferedImage) {
        final BufferedImage dest = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Color tmp;
        int val, alpha;
        for (int y = 0; y < dest.getHeight(); y++) {
            for (int x = 0; x < dest.getWidth(); x++) {
                alpha = bufferedImage.getRGB(x, y) & 0xFF000000;
                tmp = new Color(bufferedImage.getRGB(x, y));
                // val = (int) (tmp.getRed()+tmp.getGreen()+tmp.getBlue())/3;
                // val =
                // Math.max(tmp.getRed(),Math.max(tmp.getGreen(),tmp.getBlue()));
                val = (int) (tmp.getRed() * 0.3 + tmp.getGreen() * 0.59 + tmp.getBlue() * 0.11);
                dest.setRGB(x, y, alpha | val | val << 8 & 0x0000FF00 | val << 16 & 0x00FF0000);
            }
        }
        return dest;
    }
    
	/**
	 * Create a black and white image from a gray scale image
	 */
	public static BufferedImage grayToBlackWhite(BufferedImage inputImage, boolean dither) {
		int w = inputImage.getWidth();
		int h = inputImage.getHeight();
		BufferedImage outputImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);

		// Work on a copy of input image because it is modified by diffusion
		WritableRaster input = inputImage.copyData(null);
		WritableRaster output = outputImage.getRaster();

		final int threshold = 128;
		float value, qerror;

		for (int y = 0; y < h; ++y) {
			for (int x = 0; x < w; ++x) {
				value = input.getSample(x, y, 0);

				// Threshold value and compute quantization error
				if (value < threshold) {
					output.setSample(x, y, 0, 0);
					qerror = value;
				} else {
					output.setSample(x, y, 0, 1);
					qerror = value - 255;
				}

				// Spread error amongst neighboring pixels
				// Based on Floyd-Steinberg Dithering
				// http://en.wikipedia.org/wiki/Floyd-Steinberg_dithering
				if (dither) {
					if((x > 0) && (y > 0) && (x < (w-1)) && (y < (h-1))) {
						// 7/16
						value = input.getSample(x+1, y, 0);
						input.setSample(x+1, y, 0, clamp(value + 0.4375f * qerror));
						// 3/16
						value = input.getSample(x-1, y+1, 0);
						input.setSample(x-1, y+1, 0, clamp(value + 0.1875f * qerror));
						// 5/16
						value = input.getSample(x, y+1, 0);
						input.setSample(x, y+1, 0, clamp(value + 0.3125f * qerror));
						// 1/16
						value = input.getSample(x+1, y+1, 0);
						input.setSample(x+1, y+1, 0, clamp(value + 0.0625f * qerror));
					}
				}
			}
		}
		return outputImage;
	}    
	
	/**
	 * Forces a value to a 0-255 integer range
	 */
	private static int clamp(float value) {
		return Math.min(Math.max(Math.round(value), 0), 255);
	}	
    
    /**
     * Rotates the given image by the given degree and returns the transformed image.
     * If the degree value is 0 the given image is returned. 
     */
    public static BufferedImage rotate90Degree(BufferedImage image, boolean clockwise) {
		    int w = image.getWidth();
		    int h = image.getHeight();
		    double theta = Math.toRadians(clockwise ? 90 : -90);
		    
		    BufferedImage rotatedImage = new BufferedImage(h, w, image.getType() > 0 ? image.getType() : BufferedImage.TYPE_INT_RGB);
		    Graphics2D g2d = (Graphics2D) rotatedImage.getGraphics();
		    
            double x = (h - w) / 2.0;
            double y = (w - h) / 2.0;
            
            AffineTransform at = AffineTransform.getTranslateInstance(x, y);
            at.rotate(theta, w / 2.0, h / 2.0);
            g2d.drawImage(image, at, null);            
		    
            g2d.dispose();
	    	return rotatedImage;
    }
	
}
