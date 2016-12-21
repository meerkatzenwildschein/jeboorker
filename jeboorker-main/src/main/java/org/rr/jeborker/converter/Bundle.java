package org.rr.jeborker.converter;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.net.URL;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Bundle {
	
    /**
     * The resource bundle contained in this object.
     */
    private static ResourceBundle resourceBundle;

    static {
        resourceBundle = ResourceBundle.getBundle(Bundle.class.getName());
    }

    /**
     * Retrieves the resource bundle for this package. Provided in the event the
     * caller wants direct access to the resource bundle.
     * 
     * @return resource bundle for this package.
     */
    public static ResourceBundle getBundle() {
        return resourceBundle;
    } 

    /**
     * Retrieves an object from the localized resource bundle. In most cases
     * this is an image.
     * 
     * @param key
     *            key name of the resource to find.
     * @return URL pointing to the object resource.
     */
    public static URL getResource(String key) {
        return key == null ? null : Bundle.class.getResource("resources/" + key);
    } 

    /**
     * Gets a formated string from a bundle
     * 
     * @param key the key to identify the format string
     * @param arg the value to format
     * 
     * @return the formated string
     */
    public static String getFormattedString(String key, Object arg) {
        String format = null;
        try {
            format = getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
        if (arg == null){
            arg = EMPTY;
        }
        return MessageFormat.format(format, new Object[] { arg });
    }

    /**
     * Gets a formated string
     * 
     * @param key
     *            the format string
     * @param args
     *            the values to format
     * 
     * @return the formated string
     */
    public static String getFormattedString(String key, Object[] args) {
        return MessageFormat.format(getString(key), args);
    }

    /**
     * Retrieves the String resource from this bundle.
     * 
     * @param key
     *            name of String resource to retrieve.
     * 
     * @return resource bundle for this package.
     */
    public static String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException mre) {
            // This will happen if there is no
            // resource for many actions.
            return null;
        }
    }
} 
