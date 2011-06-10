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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

/**
 * TODO Move to bounce package.
 * 
 * @version $Revision$, $Date$
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class QDialog extends JDialog {
    private static final long serialVersionUID = 7919182113007451993L;
    
    private boolean cancelled = false;

    public QDialog() throws HeadlessException {
        this((Frame)null, false);
    }

    public QDialog(Frame frame) throws HeadlessException {
        this(frame, false);
    }

    public QDialog(Frame frame, boolean flag) throws HeadlessException {
        this(frame, null, flag);
    }

    public QDialog(Frame frame, String s) throws HeadlessException {
        this(frame, s, false);
    }

    public QDialog(Frame frame, String s, boolean flag) throws HeadlessException {
        super(frame, s, flag);

        init();
    }

    public QDialog(Frame frame, String s, boolean flag, GraphicsConfiguration graphicsconfiguration) {
        super(frame, s, flag, graphicsconfiguration);

        init();
    }

    public QDialog(Dialog dialog) throws HeadlessException {
        this(dialog, false);
    }

    public QDialog(Dialog dialog, boolean flag) throws HeadlessException {
        this(dialog, null, flag);
    }

    public QDialog(Dialog dialog, String s) throws HeadlessException {
        this(dialog, s, false);
    }

    public QDialog(Dialog dialog, String s, boolean flag) throws HeadlessException {
        super(dialog, s, flag);
        
        init();
    }

    public QDialog(Dialog dialog, String s, boolean flag, GraphicsConfiguration graphicsconfiguration) throws HeadlessException {
        super(dialog, s, flag, graphicsconfiguration);

        init();
    }
    
    private void init() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                cancel();
            }
        });
        
        getRootPane().getActionMap().put("escapeAction", new AbstractAction() {
            private static final long serialVersionUID = 214284864967274296L;
            public void actionPerformed(ActionEvent event) {
                cancel();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "escapeAction");
    }

    public void cancel() {
        cancelled = true;
        setVisible(false);
    }

    public void close() {
        cancelled = false;
        setVisible(false);
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
}
