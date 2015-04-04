/**
 * MySwing: Advanced Swing Utilites
 * Copyright (C) 2005  Santhosh Kumar T
 * <p/>
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

package skt.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Stack;

/**
 * Utility class to get Current/Last Active Window.
 * Usage:
 * <pre>
 *    ActiveWindowTracker.findActiveWindow();
 * </pre>
 * this is always guaranteed to return non-null window
 * <br>
 * NOTE:
 * <br>
 * Ensure that ActiveWindowTracker class is loaded
 * before any window is shown, to get accurate results
 *
 * @author Santhosh Kumar T
 * @email santhosh@in.fiorano.com
 */
public class ActiveWindowTracker{
    static Stack showingWindows = new Stack();

    private static WindowListener windowListener = new WindowAdapter(){
        public void windowDeactivated(WindowEvent we){
            if(!we.getWindow().isShowing())
                windowHiddenOrClosed(we);
        }

        public void windowClosed(WindowEvent we){
            windowHiddenOrClosed(we);
        }

        private void windowHiddenOrClosed(WindowEvent we){
            we.getWindow().removeWindowListener(windowListener);
            showingWindows.remove(we.getWindow());
        }
    };

    private static PropertyChangeListener propListener = new PropertyChangeListener(){
        public void propertyChange(PropertyChangeEvent evt){
            if(evt.getNewValue()!=null){
                Window window = (Window)evt.getNewValue();
                if(!showingWindows.contains(window)){
                    window.addWindowListener(windowListener);
                    showingWindows.remove(window);
                }
                showingWindows.push(window);
            }
        }
    };

    static{
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addPropertyChangeListener("activeWindow", propListener);
    }

    public static Window findActiveWindow(){
        if(!showingWindows.isEmpty())
            return (Window)showingWindows.peek();
        else{
            // Trick to get the shared frame instance.
            JDialog dlg = new JDialog();
            Window owner = dlg.getOwner();
            dlg.dispose();
            return owner;
        }
    }
}