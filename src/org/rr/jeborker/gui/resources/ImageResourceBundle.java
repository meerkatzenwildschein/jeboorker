package org.rr.jeborker.gui.resources;

import java.net.URL;
import java.util.WeakHashMap;

import javax.swing.Icon;

import org.rr.common.swing.icon.LazyIcon;

public class ImageResourceBundle {

	private static WeakHashMap<String, Icon> imageIconCache = new WeakHashMap<String, Icon>();
	
    /**
     * Retrieves an object from the localized resource bundle. In most cases
     * this is an image.
     * 
     * @return URL pointing to the object resource.
     */
    public static URL getResourceURL(String name) {
        return name == null ? null : ImageResourceBundle.class.getResource(name);
    } 
    
    /**
     * Load the ImageIcon with the given name. For example "file_16.png".
     * @return The image icon or <code>null</code> if an icon with the given name did not exists.
     */
    public static Icon getResourceAsImageIcon(String name) {
    	Icon icon = imageIconCache.get(name);
    	if(icon == null) {
    		icon = new LazyIcon(ImageResourceBundle.getResourceURL(name));
    		imageIconCache.put(name, icon);
    	}
    	return icon;
    }

} 
