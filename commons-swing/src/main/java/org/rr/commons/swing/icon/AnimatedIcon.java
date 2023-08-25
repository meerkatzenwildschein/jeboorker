/* Copyright (c) 2006-2007 Timothy Wall, All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.  
 */
package org.rr.commons.swing.icon;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.*;

import org.rr.commons.swing.image.GifDecoder;

//import sun.awt.image.GifImageDecoder;
//import sun.awt.image.ImageDecoder;
//import sun.awt.image.InputStreamImageSource;

/** 
 * Taken from the furbelow project. http://sourceforge.net/projects/furbelow
 * 
 * Ensures animated icons are properly handled within objects that use
 * renderers within a {@link CellRendererPane} to render the icon.  Keeps
 * a list of repaint rectangles to be used to queue repaint requests when
 * the animated icon indicates an update.  The set of repaint rectangles
 * is cleared after the repaint requests are queued.
 * @author twall
 */
public class AnimatedIcon implements Icon {

    /** Cache results to reduce decoding overhead. */
    private static Map decoded = new WeakHashMap();
    
    /** Returns whether the given icon is an animated GIF. */
    public static boolean isAnimated(Icon icon) {
        if (icon instanceof ImageIcon) {
            Image image = ((ImageIcon)icon).getImage();
            if (image != null) {
                // Quick check for commonly-occurring animated GIF comment
                Object comment = image.getProperty("comment", null);
                if (String.valueOf(comment).startsWith("GifBuilder"))
                    return true;

                // Check cache of already-decoded images
                if (decoded.containsKey(image)) {
                    return Boolean.TRUE.equals(decoded.get(image));
                }

                InputStream is = null;
                try {
                    URL url = new URL(icon.toString());
                    is = url.openConnection().getInputStream();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
//                if (is == null) {
//                    try {
//                        // Beware: lots of hackery to obtain the image input stream
//                        // Be sure to catch security exceptions
//                        ImageProducer p = image.getSource();
//                        if (p instanceof InputStreamImageSource) {
//                            Method m = InputStreamImageSource.class.getDeclaredMethod("getDecoder", null);
//                            m.setAccessible(true);
//                            ImageDecoder d = (ImageDecoder)m.invoke(p, null);
//                            if (d instanceof GifImageDecoder) {
//                                GifImageDecoder gd = (GifImageDecoder)d;
//                                Field input = ImageDecoder.class.getDeclaredField("input");
//                                input.setAccessible(true);
//                                is = (InputStream)input.get(gd);
//                            }
//                        }
//                    }
//                    catch(Exception e) {
//                        e.printStackTrace();
//                    }
//                }
                if (is != null) {
                    GifDecoder decoder = new GifDecoder();
                    decoder.read(is);
                    boolean animated = decoder.getFrameCount() > 1;
                    decoded.put(image, Boolean.valueOf(animated));
                    return animated;
                }
            }
            return false;
        }
        return icon instanceof AnimatedIcon;
    }
    
    private ImageIcon original;
    private Set repaints = new HashSet();

    /** For use by derived classes that don't have an original. */
    protected AnimatedIcon() { }
    
    /** Create an icon that takes care of animating itself on components
     * which use a CellRendererPane.
     */
    public AnimatedIcon(ImageIcon original) {
        this.original = original;
        new AnimationObserver(this, original);
    }
    
    /** Trigger a repaint on all components on which we've previously been 
     * painted.
     */
    protected synchronized void repaint() {
        for (Iterator i=repaints.iterator();i.hasNext();) {
            ((RepaintArea)i.next()).repaint();
        }
        repaints.clear();
    }
    public int getIconHeight() {
        return original.getIconHeight();
    }
    public int getIconWidth() {
        return original.getIconWidth();
    }
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        paintFrame(c, g, x, y);
        if (c != null) {
            int w = getIconWidth();
            int h = getIconHeight();
            AffineTransform tx = ((Graphics2D)g).getTransform();
            w = (int)(w * tx.getScaleX());
            h = (int)(h * tx.getScaleY());
            registerRepaintArea(c, x, y, w, h);
        }
    }
    protected void paintFrame(Component c, Graphics g, int x, int y) {
        original.paintIcon(c, g, x, y);
    }
    /** Register repaint areas, which get get cleared once the repaint request
     * has been queued.
     */
    protected void registerRepaintArea(Component c, int x, int y, int w, int h) {
        repaints.add(new RepaintArea(c, x, y, w, h));
    }
    
    /** Object to encapsulate an area on a component to be repainted. */
    private class RepaintArea {
        public int x, y, w, h;
        public Component component;
        private int hashCode;
        public RepaintArea(Component c, int x, int y, int w, int h) {
            Component ancestor = findNonRendererAncestor(c);
            if (ancestor != c) {
                Point pt = SwingUtilities.convertPoint(c, x, y, ancestor);
                c = ancestor;
                x = pt.x;
                y = pt.y;
            }
            this.component = c;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            String hash = String.valueOf(x) + "," + y + ":" + c.hashCode();
            this.hashCode = hash.hashCode();
        }
        /** Find the first ancestor <em>not</em> descending from a 
         * {@link CellRendererPane}.
         */
        private Component findNonRendererAncestor(Component c) {
            Component ancestor = SwingUtilities.getAncestorOfClass(CellRendererPane.class, c);
            if (ancestor != null && ancestor != c && ancestor.getParent() != null) {
                c = findNonRendererAncestor(ancestor.getParent());
            }
            return c;
        }
        /** Queue a repaint request for this area. */
        public void repaint() {
            component.repaint(x, y, w, h);
        }
        public boolean equals(Object o) {
            if (o instanceof RepaintArea) {
                RepaintArea area = (RepaintArea)o;
                return area.component == component
                    && area.x == x && area.y == y
                    && area.w == w && area.h == h;
            }
            return false;
        }
        /** Since we're using a HashSet. */
        public int hashCode() {
            return hashCode;
        }
        public String toString() {
            return "Repaint(" + component.getClass().getName() + "@" + x + "," + y + " " + w + "x" + h + ")";
        }
    }

    /** Detect changes in the original animated image, and remove self
     * if the target icon is GC'd.
     * @author twall
     */
    private static class AnimationObserver implements ImageObserver {
        private WeakReference ref;
        private ImageIcon original;
        public AnimationObserver(AnimatedIcon animIcon, ImageIcon original) {
            this.original = original;
            this.original.setImageObserver(this);
            ref = new WeakReference(animIcon);
        }
        /** Queue repaint requests for all known painted areas. */
        public boolean imageUpdate(Image img, int flags, int x, int y, int width, int height) {
            if ((flags & (FRAMEBITS|ALLBITS)) != 0) {
                AnimatedIcon animIcon = (AnimatedIcon)ref.get();
                if (animIcon != null) {
                    animIcon.repaint();
                }
                else
                    original.setImageObserver(null);
            }
            // Return true if we want to keep painting
            return (flags & (ALLBITS|ABORT)) == 0;
        }
    }
}
