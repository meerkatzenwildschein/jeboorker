package org.rr.common.swing.button;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/*
 * A popup button JavaBean component.
 * Copyright (C) 2008  Dr Christos Bohoris
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Contact information for Dr Christos Bohoris:
 * E-mail: sirius@connectina.com
 * Address: 1 Vasileos Konstantinou Street, 
 * Agioi Anargiri, 13561, Athens, Greece
 */
public class JPopupButton extends JButton {

	private static final long serialVersionUID = 8030814226706594541L;
	
	private boolean cursorEntered = false;
    private JPopupMenu popup = new JPopupMenu();

    /**
     * Creates a default popup button.
     */
    public JPopupButton() {
        addMouseListener(new ButtonMouseListener());
        setFocusPainted(false);
        setMargin(new Insets(2, 2, 2, 2));
    }

    /**
     * Paint the side arrow indicator along with the standard button.
     * 
     * @param g the graphics context
     */
    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int alpha = cursorEntered == true ? 128 : 64;
        g2d.setColor(new Color(getForeground().getRed(), getForeground().getGreen(), getForeground().getBlue(), alpha));
        Path2D.Float arrow = new Path2D.Float();
        int size = 7;
        int margin = 2;

        int w = getWidth();
        int h = getHeight();

        arrow.moveTo(w - margin, h - size - margin);
        arrow.lineTo(w - size - margin, h - margin);
        arrow.lineTo(w - margin, h - margin);
        g2d.fill(arrow);
    }

    /**
     * Add a menu item to the popup menu.
     * 
     * @param item the menu item
     */
    public void addMenuItem(JMenuItem item) {
        popup.add(item);
    }

    /**
     * Add a component to the popup menu.
     * 
     * @param component the swing component
     */
    public void addComponent(JComponent component) {
        popup.add(component);
    }

    /**
     * Remove the component with the specified index from the popup menu.
     * 
     * @param index the index
     */
    public void removeItem(int index) {
        popup.remove(index);
    }

    private class ButtonMouseListener extends MouseAdapter {

        /**
         * A mouse button was clicked.
         * 
         * @param e a mouse event
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            popup.show(JPopupButton.this, 0, getHeight());
        }

        /**
         * A mouse button was entered.
         * 
         * @param e a mouse event
         */
        @Override
        public void mouseEntered(MouseEvent e) {
            cursorEntered = true;
        }

        /**
         * A mouse button was exited.
         * 
         * @param e a mouse event
         */
        @Override
        public void mouseExited(MouseEvent e) {
            cursorEntered = false;
        }
        
    }
    
}