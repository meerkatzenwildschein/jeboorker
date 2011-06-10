/*
 * $Id: ThreeStateCheckBox2.java,v 1.1 2008/01/28 21:02:16 edankert Exp $
 *
 * Copyright (c) 2002 - 2008, Edwin Dankert, Evgeniy Smelik
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

import javax.swing.*;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.io.Serializable;

/**
 * Three state check box enhancement
 *
 * @date: 14.11.2007
 * @author Edwin Dankert <edankert@gmail.com>, Evgeniy Smelik <sever@yandex.ru>
 */
public class ThreeStateCheckBox2 extends JCheckBox {
    private static final long serialVersionUID = -1842373249474550802L;

    public static final String NOT_SELECTED = "NOT_SELECTED";
    public static final String SELECTED = "SELECTED";
    public static final String DONT_CARE = "DONT_CARE";

    private String state = DONT_CARE;

    public ThreeStateCheckBox2(String aString) {
        this(aString, DONT_CARE);
    }

    public ThreeStateCheckBox2(String aString, String state) {
        super(aString);

        Icon icon = UIManager.getIcon("CheckBox.icon");
        ThreeStateCheckBoxIcon threeStateCheckBoxIcon = new ThreeStateCheckBoxIcon(icon);
        setIcon(threeStateCheckBoxIcon);

        setSelected(state);
    }

    public String getSelected() {
        return state;
    }

    public void setSelected(boolean selected) {
        if (selected) {
            state = SELECTED;
        } else {
            state = NOT_SELECTED;
        }

        super.setSelected(selected);
    }

    public void setSelected(String state) {
        this.state = state;

        super.setSelected(SELECTED.equals(state) || DONT_CARE.equals(state));

        repaint();
    }

    private static class ThreeStateCheckBoxIcon implements Icon, UIResource, Serializable {
        private Icon icon;

        public ThreeStateCheckBoxIcon(Icon icon) {
            this.icon = icon;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            ThreeStateCheckBox cb = (ThreeStateCheckBox) c;

            if (DONT_CARE.equals(cb.getSelected())) {
                Graphics2D g2d = ((Graphics2D) g);
                Composite composite = g2d.getComposite();
                AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
                g2d.setComposite(alphaComposite);
                icon.paintIcon(c, g, x, y);
                g2d.setComposite(composite);
            } else {
                icon.paintIcon(c, g, x, y);
            }
        }

        public int getIconWidth() {
            return icon.getIconWidth();
        }

        public int getIconHeight() {
            return icon.getIconWidth();
        }
    }
}