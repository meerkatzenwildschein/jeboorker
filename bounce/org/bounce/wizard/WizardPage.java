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

package org.bounce.wizard;

import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

public abstract class WizardPage extends JPanel {
    private static final long serialVersionUID = 7119005231853816073L;
    
    private EventListenerList listeners = null;
    private WizardPage back = null;

    public WizardPage(LayoutManager manager) {
        super(manager);

        listeners = new EventListenerList();
    }
    
    public abstract String getTitle();

    public abstract String getDescription();

    public abstract WizardPage getNext();

    public boolean isFinishEnabled() {
        return true;
    }

    public WizardPage getBack() {
        return back;
    }

    public void setBack(WizardPage back) {
        this.back = back;
    }
    
    /**
     * Called when information on the page has changed and buttons 
     * need to be enabled or disabled accordingly.
     */
    protected void firePageChanged() {
        // Guaranteed to return a non-null array
        Object[] list = listeners.getListenerList();
        
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for ( int i = list.length-2; i >= 0; i -= 2) {
            ((WizardPageListener)list[i+1]).pageChanged(new WizardPageEvent(this));
        }
    }

    public void addWizardPageListener(WizardPageListener listener) {
        listeners.add(WizardPageListener.class, listener);
    }

    public void removeWizardPageListener(WizardPageListener listener) {
        listeners.remove(WizardPageListener.class, listener);
    }
}
