package org.rr.commons.swing.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;

import javax.swing.AbstractAction;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.swing.SwingUtils;

/**
 * Opens a web browser at the given URL. Also takes a name for use in menus
 * or on buttons.
 */
public class WebLinkAction extends AbstractAction {
    private String url;
    
    public WebLinkAction(String name, String url) {
        this.url = url;
        // Offer the url to anything that wants a tool tip for this action.
        putValue(SHORT_DESCRIPTION, url);
        putValue(NAME, name);
    }
    
    public WebLinkAction(String name, File file) {
        this(name, urlFromFile(file));
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
        	SwingUtils.openURL(url);
        } catch (Exception ex) {
            LoggerFactory.getLogger().log(Level.WARNING, "Could not open url " + url, ex);
        }
    }
    
    private static String urlFromFile(File file) {
        return file.toURI().toString();
    }
}
