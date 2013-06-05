/**
 * Copyright (c) 2006-2009, Alexander Potochkin
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of the JXLayer project nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jdesktop.jxlayer.plaf.ext;

import org.jdesktop.jxlayer.plaf.AbstractBufferedLayerUI;
import org.jdesktop.jxlayer.JXLayer;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

/**
 * The {@code DebugRepaintingUI} provides a visual indication
 * of repainting {@link JXLayer}s subcomponents.
 * <p/>
 * With this tool you can examine your GUI and eliminate unnecessary painting,
 * moreover it is useful just to see how Swing paints the core components:
 * <br/> - What part of a JTree is repainted when you open a node?
 * <br/> - What is repainted when you open a popup?
 * <p/>
 * {@code DebugRepaintingUI} will help you to speed up
 * the painting of your custom components.
 * <p/>
 * Here is an example of using {@code DebugRepaintingUI}:
 * <pre>
 *  JComponent myComponent = getMyComponent(); // just any component
 * <p/>
 * DebugRepaintingUI&lt;JComponent&gt; debugUI = new DebugRepaintingUI();
 * JXLayer&lt;JComponent&gt; layer = new JXLayer&lt;JComponent&gt;(myComponent, debugUI);
 * <p/>
 * // add the layer to a frame or a panel, like any other component
 * frame.add(layer);
 * </pre>
 * <p/>
 * The DebugRepaintingDemo is
 * <a href="https://jxlayer.dev.java.net/source/browse/jxlayer/trunk/src/demo/org/jdesktop/jxlayer/demo/DebugRepaintingDemo.java?view=markup">available</a>
 */
public class DebugRepaintingUI extends AbstractBufferedLayerUI<JComponent>
        implements ActionListener {
    private Map<Shape, DebugPainterEntry> shapeMap
            = new HashMap<Shape, DebugPainterEntry>();
    private final Timer timer;

    /**
     * Creates a new {@code DebugRepaintingUI}
     * with {@code 50} milliseconds set for the animation timer between-event delay.
     */
    public DebugRepaintingUI() {
        this(50);
    }

    /**
     * Creates a new {@code DebugRepaintingUI}
     * with the specified time set for the animation timer between-event delay.
     *
     * @param delay the between-event delay for the animation timer animation
     */
    public DebugRepaintingUI(int delay) {
        timer = new Timer(delay, this);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This method is overridden to provide a specific repainting indication.
     */
    protected void paintLayer(Graphics2D g2, JXLayer<? extends JComponent> l) {
        super.paintLayer(g2, l);

        Shape clip = g2.getClip();
        // skip the visual effect if {@code DebugRepaintingUI} is not dirty
        if (!isDirty() && clip != null && !shapeMap.containsKey(clip)) {
            Rectangle bounds = clip.getBounds();

            // create inverted image for the current clip
            BufferedImage buf = new BufferedImage(bounds.width, bounds.height,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics bufg = buf.createGraphics();
            bufg.setColor(Color.WHITE);
            bufg.fillRect(0, 0, buf.getWidth(), buf.getHeight());
            bufg.setXORMode(Color.BLACK);
            bufg.drawImage(getBuffer(), 0, 0, bounds.width, bounds.height,
                    bounds.x, bounds.y, bounds.x + bounds.width,
                    bounds.y + bounds.height, null);
            bufg.dispose();
            shapeMap.put(clip, new DebugPainterEntry(buf, bounds.x, bounds.y));
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        // Painting clip shapes
        Set<Shape> shapes = shapeMap.keySet();
        for (Iterator<Shape> iterator = shapes.iterator(); iterator.hasNext();)
        {
            Shape shape = iterator.next();
            DebugPainterEntry entry = shapeMap.get(shape);
            if (entry.alpha <= 0) {
                iterator.remove();
                continue;
            }
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, entry.alpha));
            g2.drawImage(entry.image, entry.x, entry.y, null);
            entry.alpha -= .1;
        }
    }

    public void actionPerformed(ActionEvent e) {
        // Mark {@code DebugRepaintingUI} as diry
        // if we still have the repainted areas to animate
        if (!shapeMap.isEmpty()) {
            setDirty(true);
        } else {
            // stop the timer if animation is completed
            timer.stop();
        }
    }

    /**
     * An utility class which defines the image of the specific clipping area,
     * its coordinates and the current alpha value.
     */
    private static class DebugPainterEntry {
        private Image image;
        private int x;
        private int y;
        private float alpha = 1;

        public DebugPainterEntry(Image image, int x, int y) {
            this.image = image;
            this.x = x;
            this.y = y;
        }
    }
}

