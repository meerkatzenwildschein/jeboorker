package org.rr.jeborker.gui.resources;

import java.net.URL;

public class ImageResourceBundle {

    /**
     * Retrieves an object from the localized resource bundle. In most cases
     * this is an image.
     * 
     * @param key
     *            key name of the resource to find.
     * @return URL pointing to the object resource.
     */
    public static URL getResource(String key) {
        return key == null ? null : ImageResourceBundle.class.getResource(key);
    } 

} 
