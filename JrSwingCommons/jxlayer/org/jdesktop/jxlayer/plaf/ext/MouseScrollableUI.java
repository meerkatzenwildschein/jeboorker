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

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;

/**
 * The {@code MouseScrollableUI} provides the mouse auto-scrolling feature
 * for your applications. Wrap your {@code JScrollPane} with a {@link JXLayer},
 * set an instance of MouseScrollableUI as the {@code JXLayer}'s ui
 * and after that, pressing the middle mouse button inside the {@code JScrollPane}
 * will activate the auto-scrolling mode.
 * <p/>
 * Here is an example of using {@code MouseScrollableUI}:
 * <pre>
 * // a JScrollPane to be enhanced with mouse auto-scrolling
 * JScrollPane myScrollPane = getMyScrollPane();
 * <p/>
 * JXLayer&lt;JScrollPane&gt; layer =
 *          new JXLayer&lt;JScrollPane&gt;(myScrollPane, new MouseScrollableUI());
 * <p/>
 * // add the layer to a frame or a panel, like any other component
 * frame.add(layer);
 * </pre>
 * The MouseScrollableDemo is
 * <a href="https://jxlayer.dev.java.net/source/browse/jxlayer/trunk/src/demo/org/jdesktop/jxlayer/demo/MouseScrollableDemo.java?view=markup">available</a>
 */
public class MouseScrollableUI extends AbstractLayerUI<JScrollPane>
        implements ActionListener {

    private JXLayer<? extends JScrollPane> currentLayer;
    private Point scrollOrigin;
    private Point mousePoint;
    private Timer timer;

    private JLabel indicator;
    private Icon crissCrossIcon;
    private Icon horizontalIcon;
    private Icon verticalIcon;
    
    private Map<Icon, Cursor> cursorMap;

    // It is used to make a component consume mouse events
    private final MouseListener emptyMouseListener = new MouseAdapter() {};

    private ComponentListener componentListener = new ComponentAdapter() {
        @SuppressWarnings("unchecked")
        public void componentResized(ComponentEvent e) {
            JXLayer<JScrollPane> layer = (JXLayer<JScrollPane>) e.getComponent();
            if (indicator.isShowing() && layer == currentLayer) {
                deactivateMouseScrolling(layer);
            }
        }
    };

    /**
     * Creates a new instance of {@code MouseScrollableUI}.
     */
    public MouseScrollableUI() {
        timer = new Timer(20, this);
        cursorMap = new HashMap<Icon, Cursor>();
        
        crissCrossIcon = 
                new ImageIcon(getClass().getResource("images/criss-cross.png"));
        cursorMap.put(crissCrossIcon, 
                Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        horizontalIcon = 
                new ImageIcon(getClass().getResource("images/horizontal.png"));
        cursorMap.put(horizontalIcon, 
                Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        verticalIcon = 
                new ImageIcon(getClass().getResource("images/vertical.png"));
        cursorMap.put(verticalIcon, 
                Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        
        indicator = new JLabel(crissCrossIcon);
        // Make it consume mouse events
        indicator.addMouseListener(emptyMouseListener);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        c.addComponentListener(componentListener);
        JXLayer l = (JXLayer) c;
        l.getGlassPane().setLayout(null);
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        c.removeComponentListener(componentListener);
        JXLayer l = (JXLayer) c;
        l.getGlassPane().setLayout(new FlowLayout());
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Activates the mouse auto-scrolling if {@code e} is a scrolling trigger.
     *
     * @see #isMouseScrollingTrigger(MouseEvent)
     */
    @Override
    protected void processMouseEvent(MouseEvent e, JXLayer<? extends JScrollPane> l) {
        super.processMouseEvent(e, l);
        Icon icon = getIconForIndicator(l);
        if (isMouseScrollingTrigger(e) && icon != null) {
            currentLayer = l;
            scrollOrigin = SwingUtilities.convertPoint(e.getComponent(),
                    e.getPoint(), currentLayer);
            activateMouseScrolling(scrollOrigin, currentLayer, icon);
            mousePoint = scrollOrigin;
            e.consume();
        }
    }

    /**
     * Returns {@code true} if {@code mouseEvent} is a mouse auto-scrolling trigger.
     *
     * @param mouseEvent the mouseEvent to be tested
     * @return {@code true} if {@code mouseEvent} is a mouse auto-scrolling trigger,
     *         otherwise returns {@code false}
     */
    protected boolean isMouseScrollingTrigger(MouseEvent mouseEvent) {
        return mouseEvent.getID() == MouseEvent.MOUSE_PRESSED
                && mouseEvent.getButton() == MouseEvent.BUTTON2
                && !mouseEvent.isPopupTrigger();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Scrolls the {@code JXLayer}'s view {@code JScrollPane}
     * if mouse scrolling is activated
     */
    @Override
    protected void processMouseMotionEvent(MouseEvent e, JXLayer<? extends JScrollPane> l) {
        super.processMouseMotionEvent(e, l);
        if (indicator.isShowing()) {
            mousePoint = SwingUtilities.convertPoint(e.getComponent(),
                    e.getPoint(), currentLayer);
            updateViewPosition(scrollOrigin, mousePoint, currentLayer);
        }
    }

    private void updateViewPosition(Point scrollOrigin, Point mousePoint,
                                    JXLayer<? extends JScrollPane> layer) {
        JViewport viewport = layer.getView().getViewport();
        Point indicatorPoint =
                SwingUtilities.convertPoint(layer, mousePoint, indicator);
        if (!indicator.contains(indicatorPoint)) {
            Point viewPosition = viewport.getViewPosition();
            int x = mousePoint.x - scrollOrigin.x;
            int y = mousePoint.y - scrollOrigin.y;
            // make the scrolling more smooth
            viewPosition.x += x / 5;
            viewPosition.y += y / 5;
            if (viewPosition.x > viewport.getView().getWidth() - viewport.getWidth()) {
                viewPosition.x = viewport.getView().getWidth() - viewport.getWidth();
            }
            if (viewPosition.x < 0) {
                viewPosition.x = 0;
            }
            if (viewPosition.y > viewport.getView().getHeight() - viewport.getHeight()) {
                viewPosition.y = viewport.getView().getHeight() - viewport.getHeight();
            }
            if (viewPosition.y < 0) {
                viewPosition.y = 0;
            }
            viewport.setViewPosition(viewPosition);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Preprocesses the {@code AWTEvent} to deactivate mouse scrolling
     * when any key was pressed or focus left the {@code JXLayer}.
     */
    @Override
    public void eventDispatched(AWTEvent e, JXLayer<? extends JScrollPane> l) {
        if (indicator.isShowing()) {
            if (e instanceof MouseEvent) {
                MouseEvent mouseEvent = (MouseEvent) e;
                if (mouseEvent.getID() != MouseEvent.MOUSE_WHEEL
                        && mouseEvent.getID() != MouseEvent.MOUSE_PRESSED
                        && (mouseEvent.getID() != MouseEvent.MOUSE_RELEASED
                        && mouseEvent.getID() != MouseEvent.MOUSE_CLICKED
                        || scrollOrigin.equals(mousePoint))) {
                    super.eventDispatched(e, l);
                    return;
                }
            }
            deactivateMouseScrolling(currentLayer);
            if (e instanceof InputEvent) {
                InputEvent inputEvent = (InputEvent) e;
                inputEvent.consume();
            }
        } else {
            super.eventDispatched(e, l);
        }
    }

    public void actionPerformed(ActionEvent e) {
        updateViewPosition(scrollOrigin, mousePoint, currentLayer);
    }

    private void activateMouseScrolling(Point point, 
                                        JXLayer<? extends JScrollPane> layer,
                                        Icon icon) {
        layer.getGlassPane().addMouseListener(emptyMouseListener);
        layer.getGlassPane().add(indicator);
        Dimension prefSize = indicator.getPreferredSize();
        indicator.setBounds(point.x - prefSize.width / 2,
                point.y - prefSize.height / 2,
                prefSize.width, prefSize.height);
        indicator.setIcon(icon);
        indicator.setCursor(cursorMap.get(icon));
        layer.getGlassPane().repaint();
        timer.start();
    }

    private Icon getIconForIndicator(JXLayer<? extends JScrollPane> layer) {
        JViewport viewport = layer.getView().getViewport();
        Dimension extentSize = viewport.getExtentSize();
        Icon icon = null;
        if (extentSize.width >= viewport.getView().getWidth()
                && extentSize.height >= viewport.getView().getHeight()
                || extentSize.width < viewport.getView().getWidth()
                && extentSize.height < viewport.getView().getHeight()) {
            icon = crissCrossIcon;
        } else if (extentSize.width < viewport.getView().getWidth()) {
            icon = horizontalIcon;
        } else if (extentSize.height < viewport.getView().getHeight()) {
            icon = verticalIcon;
        }
        return icon;
    }
    
    private void deactivateMouseScrolling(JXLayer<? extends JScrollPane> layer) {
        layer.getGlassPane().removeMouseListener(emptyMouseListener);
        indicator.setCursor(null);
        layer.getGlassPane().remove(indicator);
        layer.getGlassPane().repaint();
        timer.stop();
    }
}
