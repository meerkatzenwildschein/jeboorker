package skt.swing.search;

/**
 * MySwing: Advanced Swing Utilites
 * Copyright (C) 2005  Santhosh Kumar T
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
 */

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

/**
 * @author Santhosh Kumar T
 * @email  santhosh@in.fiorano.com
 */
public class IncrementalSearchKeyListener extends KeyAdapter{
    private boolean armed = false;
    private FindAction findAction;

    public IncrementalSearchKeyListener(FindAction findAction){
        this.findAction = findAction;
    }

    public void keyPressed(KeyEvent e){
        int modifiers = e.getModifiers();
        int keyCode = e.getKeyCode();
        if(keyCode==KeyEvent.VK_PLUS ||
                keyCode==KeyEvent.VK_MINUS ||
                keyCode==KeyEvent.VK_ADD ||
                keyCode==KeyEvent.VK_SUBTRACT){
            return;
        }

        if((modifiers>0 && modifiers!=KeyEvent.SHIFT_MASK) || e.isActionKey())
            return;
        char c = e.getKeyChar();
        if(!Character.isISOControl(c)
                && keyCode!=KeyEvent.VK_SHIFT
                && keyCode!=KeyEvent.VK_ESCAPE){
            armed = true;
            e.consume();
        }
    }

    public void keyTyped(KeyEvent e){
        if(armed){
            final KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);
            findAction.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, null));
            findAction.searchField.setText(String.valueOf(stroke.getKeyChar()));
            e.consume();
            armed = false;
        }
    }
}