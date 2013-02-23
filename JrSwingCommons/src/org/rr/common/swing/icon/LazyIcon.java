package org.rr.common.swing.icon;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.Icon;

import sun.awt.AppContext;

/**
 * {@link Icon} implementation that loads the icon if it's firstly used. 
 */
public class LazyIcon implements Icon {
    private static final Object TRACKER_KEY = new Object(); // TRACKER_KEY
    /**
     * Id used in loading images from MediaTracker.
     */
    private static int mediaTrackerID;    
    
	private URL url;
	private Image image;
	
	public LazyIcon(URL url) {
		this.url = url;
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.drawImage(image, x, y, c);
	}

	@Override
	public int getIconWidth() {
		return loadIcon().getWidth(null);
	}

	@Override
	public int getIconHeight() {
		return loadIcon().getHeight(null); 
	}
	
	private Image loadIcon() {
		if(image == null) {
			image = Toolkit.getDefaultToolkit().getImage(url);
			loadImage(image);
		}
		return image;
	}
	
    /**
     * Loads the image, returning only when the image is loaded.
     * @param image the image
     */
    protected void loadImage(Image image) {
        MediaTracker mTracker = getTracker();
        synchronized(mTracker) {
            int id = getNextID();

            mTracker.addImage(image, id);
	    try {
                mTracker.waitForID(id, 0);
	    } catch (InterruptedException e) {
		System.out.println("INTERRUPTED while loading Image");
	    }
//            loadStatus = mTracker.statusID(id, false);
            mTracker.removeImage(image, id);

//	    width = image.getWidth(imageObserver);
//	    height = image.getHeight(imageObserver);
	}
    }
    
    /**
     * Returns the MediaTracker for the current AppContext, creating a new
     * MediaTracker if necessary.
     */
    private MediaTracker getTracker() {
        Object trackerObj;
        AppContext ac = AppContext.getAppContext();
        // Opt: Only synchronize if trackerObj comes back null?
        // If null, synchronize, re-check for null, and put new tracker
        synchronized(ac) {
            trackerObj = ac.get(TRACKER_KEY);
            if (trackerObj == null) {
                Component comp = new Component() {};
                trackerObj = new MediaTracker(comp);
                ac.put(TRACKER_KEY, trackerObj);
            }
        }
        return (MediaTracker) trackerObj;
    }    

    /**
     * Returns an ID to use with the MediaTracker in loading an image.
     */
    private int getNextID() {
        synchronized(getTracker()) {
            return ++mediaTrackerID;
        }
    }    
    
}
