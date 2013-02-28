package org.rr.jeborker.gui.resources;

import java.net.URL;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ImageResourceBundle {

	private static WeakHashMap<String, Icon> imageIconCache = new WeakHashMap<String, Icon>();
	
	public static final Icon FOLDER_OPEN_16_ICON = ImageResourceBundle.getResourceAsImageIcon("folder_open_16.png");
	
	public static final Icon FOLDER_CLOSE_16_ICON = ImageResourceBundle.getResourceAsImageIcon("folder_closed_16.png");
	
	public static final Icon FILE_16_ICON = ImageResourceBundle.getResourceAsImageIcon("file_16.png");
	
	
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
    		icon = new ImageIcon(ImageResourceBundle.getResourceURL(name));
    		imageIconCache.put(name, icon);
    	}
    	return icon;
    }

} 
