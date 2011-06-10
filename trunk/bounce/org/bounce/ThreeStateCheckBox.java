/*
 * $Id$
 *
 * Copyright (c) 2002 - 2008, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *   may  be used to endorse or promote products derived from this software 
 *   without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.bounce;

import java.awt.Component;
import java.awt.Graphics;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class ThreeStateCheckBox extends JCheckBox {
    private static final long serialVersionUID = -6262164294612004192L;
    
    public static final String NOT_SELECTED = "NOT_SELECTED";
    public static final String SELECTED = "SELECTED";
    public static final String DONT_CARE = "DONT_CARE";

    private String state = DONT_CARE;

    public ThreeStateCheckBox(String aString) {
        this(aString, DONT_CARE);
    }

    public ThreeStateCheckBox(String aString, String state) {
        super(aString, new ThreeStateCheckBoxIcon());
        
        setSelected(state);
    }

    public String getSelected() {
        return state;
    }
    
    public void setSelected(boolean selected) {
        if (selected) {
            state = ThreeStateCheckBox.SELECTED;
        } else {
            state = ThreeStateCheckBox.NOT_SELECTED;
        }

        super.setSelected(selected);
    }

    public void setSelected(String state) {
        this.state = state;

        super.setSelected(state == ThreeStateCheckBox.SELECTED);

        repaint();
    }

    private static class ThreeStateCheckBoxIcon implements Icon, UIResource, Serializable {
        private static final long serialVersionUID = -1699947264495219246L;

        protected int getControlSize() {
            return 13;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            ThreeStateCheckBox cb = (ThreeStateCheckBox)c;
            int controlSize = getControlSize();

            if (cb.isEnabled()) {
                if (cb.getModel().isPressed() && cb.getModel().isArmed()) {
                    g.setColor(MetalLookAndFeel.getControlShadow());
                    g.fillRect(x, y, controlSize - 1, controlSize - 1);
                    drawPressed3DBorder(g, x, y, controlSize, controlSize);
                } else {
                    drawFlush3DBorder(g, x, y, controlSize, controlSize);
                }
                g.setColor(MetalLookAndFeel.getControlInfo());
            } else {
                g.setColor(MetalLookAndFeel.getControlShadow());
                g.drawRect(x, y, controlSize - 1, controlSize - 1);
            }

            if (cb.getSelected() == DONT_CARE) {
                drawRect(c, g, x, y);
            }

            if (cb.getSelected() == SELECTED) {
                if (cb.isBorderPaintedFlat()) {
                    x++;
                }
                drawCheck(c, g, x, y);
            }
        }

        protected void drawRect(Component c, Graphics g, int x, int y) {
            int controlSize = getControlSize();
            g.fillRect(x+3, y+3, controlSize - 7, controlSize - 7);
        }

        protected void drawCheck(Component c, Graphics g, int x, int y) {
            int controlSize = getControlSize();
            g.fillRect(x + 3, y + 5, 2, controlSize - 8);
            g.drawLine(x + (controlSize - 4), y + 3, x + 5, y
                    + (controlSize - 6));
            g.drawLine(x + (controlSize - 4), y + 4, x + 5, y
                    + (controlSize - 5));
        }

        private void drawFlush3DBorder(Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);
            g.setColor(MetalLookAndFeel.getControlDarkShadow());
            g.drawRect(0, 0, w - 2, h - 2);
            g.setColor(MetalLookAndFeel.getControlHighlight());
            g.drawRect(1, 1, w - 2, h - 2);
            g.setColor(MetalLookAndFeel.getControl());
            g.drawLine(0, h - 1, 1, h - 2);
            g.drawLine(w - 1, 0, w - 2, 1);
            g.translate(-x, -y);
        }

        private void drawPressed3DBorder(Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);
            drawFlush3DBorder(g, 0, 0, w, h);
            g.setColor(MetalLookAndFeel.getControlShadow());
            g.drawLine(1, 1, 1, h - 2);
            g.drawLine(1, 1, w - 2, 1);
            g.translate(-x, -y);
        }

        public int getIconWidth() {
            return getControlSize();
        }

        public int getIconHeight() {
            return getControlSize();
        }
    }
}
