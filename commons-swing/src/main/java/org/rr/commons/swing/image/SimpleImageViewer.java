package org.rr.commons.swing.image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;


public class SimpleImageViewer extends JComponent {

    /** Creates new form BeanForm */
    public SimpleImageViewer() {
    	this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				transformToView = null;
				repaint();
			}
		});
    }

	private static final long serialVersionUID = 8055865896136562197L;

    /**
     * The Resourcehandler points to the image resource to be displayed.
     */
	private IResourceHandler currentDisplayedResource = null;

    /**
     * The image transform to be used for scaling and rotating the image
     */
    private AffineTransform transformToView = null;

    /**
     * Stores the image to be displayed.
     */
    private BufferedImage currentDisplayImage = null;
    
    /**
     * Runnable to be invoked after a repaint has been done.
     */
    private Runnable repaintRunnable;
    
    /**
     * Thel SwingWorker which are currently in progress loading a picture.
     */
    private SwingWorker<Void, Void> swingWorker = null;

    /**
     * Gets the {@link IResourceHandler} for the image currently displayed
     * with this {@link SimpleImageViewer} instance.
     * @return The image {@link IResourceHandler} or <code>null</code> if no one is currently set. 
     */
    public IResourceHandler getImageResource() {
    	return this.currentDisplayedResource;
    }
    
    /**
     * Gets the image which is currently displayed.
     * @return The diesried image or <code>null</code> if no image is displayed.
     */
    public BufferedImage getImage() {
    	return this.currentDisplayImage;
    }
    
    /**
     * Needed if this component is only be used for painting but 
     * is not added to a container. The given {@link Runnable} is
     * always invoked after an image has been loaded and a repaint is neeed.
     * 
     * @param run The {@link Runnable} to be invoked.
     */
    public void setRepaintInvokable(Runnable run) {
    	this.repaintRunnable = run;
    }
    
	public void setImageViewerResource(IResourceHandler resourceHandler) {
		//clear the view
		if(resourceHandler==null) {
			this.currentDisplayedResource = null;
            this.currentDisplayImage = null;
            this.transformToView = null;
		}

		//only get a new Image if the file to be displayed has been changed.
		if(currentDisplayedResource==null || !resourceHandler.getResourceString().equals(currentDisplayedResource.getResourceString())) {
			try {
				this.currentDisplayedResource = resourceHandler;
                this.currentDisplayImage = null;  //the old image is not longer used.
                this.transformToView = null; //the transform for the image is not longer used.
			} catch (Exception e) {
                this.currentDisplayImage = null; //the old image is not longer used.
                this.transformToView = null; //the transform for the image is not longer used.
                
                if(e.getClass().getSimpleName().equals("ImageFormatException")) {
                	throw new RuntimeException(e);
                }
                LoggerFactory.log(Level.INFO, this, "Could not get an image provider for the file " + resourceHandler.getName(), e);
			}

			revalidate(); //need for resize
			repaint();
		}
	}
	
	private IImageProvider getImageProvider() {
		if(this.currentDisplayedResource != null) {
			return ImageProviderFactory.getImageProvider(this.currentDisplayedResource);
		}
		return null;
	}
	
	/**
	 * Starts a Thread which loads the image previously set with the
	 * {@link #setImageResource(IResourceHandler, GalleryConfig)} method.
	 * The Thread does a repaint after the image has been successfully loaded.
	 */
	private synchronized void loadImageThreaded() {
		//abort all working swing workers
		if(this.swingWorker!=null) {
			this.swingWorker.cancel(true);
			this.swingWorker = null;
		}
		
		
		this.swingWorker = new SwingWorker<Void, Void>() {
			@Override
			protected void done() {
				//it's not needed to do a repaint if the image could not be loaded.
				if(currentDisplayImage!=null) {
					if(repaintRunnable!=null) {
						repaintRunnable.run(); //a not threaded run
					}
					
					//it's not needed to do a repaint if the viewer is
					//not attached to a component.
					if(getParent()!=null) {
						revalidate();
						repaint();
					}
				}
				swingWorker = null;
			}

			@Override
			protected Void doInBackground() throws Exception {
				IImageProvider imageProvider = getImageProvider();
				if(imageProvider!=null) {
		            //do this only the first time this component is shown. 
		            if(currentDisplayImage==null) {
		            	currentDisplayImage = imageProvider.getImage();
		            }
				}
				return null;
			}
		}; 
		
		this.swingWorker.execute();
	}
	
	/**
	 * Gets the {@link AffineTransform} for the image to be displayed in this {@link SimpleImageViewer} 
	 * instance.
	 * @return The desired {@link AffineTransform}. 
	 */
	private AffineTransform getAffineTransform() {
		if(this.transformToView != null) {
			return this.transformToView;
		}
		
		//get the image rotation value from the exif data.
		double imageRotationDegrees = 0d;

		this.transformToView = null;
        if(this.transformToView==null && currentDisplayImage!=null) {
            transformToView = ImageUtils.getTransformToMatchDimension(currentDisplayImage, getSize(), imageRotationDegrees);
        }
        return transformToView;
	}

	/**
	 * Just paints the image.
	 */
	@Override
	public void paintComponent(Graphics g) {
		//create a black background
		g.setColor(Color.BLACK);
		((Graphics2D)g).fillRect(0, 0, this.getSize().width, this.getSize().height);
		
		if(currentDisplayImage==null) {
			//load the image and return. The loadImage method will does a repaint if the
			//image is ready to paint.
			this.loadImageThreaded();
		} else if(currentDisplayImage!=null) {
			//draw the image with the AffineTransform data.
			((Graphics2D)g).drawImage(currentDisplayImage, getAffineTransform(), this);			
		}
	}

	public void dispose() {
        this.currentDisplayImage = null;
        this.currentDisplayedResource = null;
        this.transformToView = null;
        this.repaintRunnable = null;
	}
}
