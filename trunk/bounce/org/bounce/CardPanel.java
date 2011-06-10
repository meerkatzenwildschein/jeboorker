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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.bounce.event.CardEvent;
import org.bounce.event.CardPanelListener;

/**
 * Creates a panel with a CardLayout.
 * @param <C> the type of panels this can contain.
 * 
 * @version $Revision$, $Date$
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class CardPanel<C extends JComponent> extends JPanel {
	private static final long serialVersionUID = 3258135768999737650L;
	
    private List<CardPanelListener<C>> listenerList = new ArrayList<CardPanelListener<C>>();

    private CardLayout layout = null;
	private JPanel center = null;
	private JScrollPane scroller = null;
	private C current = null;

	/**
	 * Card Panel constructor
	 */
	public CardPanel() {
		this( false);
	}
	
	protected CardPanel(boolean scrollable) {
		super(new BorderLayout());
		
		layout = new CardLayout();
		center = new JPanel(layout);
		
//		setBorder( new EmptyBorder( 10, 10, 10, 10));
		if (scrollable) {
			scroller = new JScrollPane(center,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroller.setPreferredSize(new Dimension(160, 100));

			add(scroller, BorderLayout.CENTER);
		} else {
			add(center, BorderLayout.CENTER);
		}
	}
	
	/**
	 * Adds the card.
	 * 
	 * @param card the card to add.
	 */
	public void add(C card) {		
		center.add(card, toString(card));

        fireCardAdded(card);
	}
	
	/**
	 * Show an added card
	 * 
	 * @param card the card to show.
	 */
	public void show(C card) {
        current = card;
        layout.show(center, toString(card));
        
        fireCardChanged(card);
	}
	
	/**
	 * Remove the panel.
	 * 
	 * @param card the card to remove.
	 */
	public void remove(C card) {
		center.remove(card);

        fireCardRemoved(card);
	}

	/**
	 * Get the current shown card.
	 * 
	 * @return the current card.
	 */
	public C selected() {
		return current;
	}
	
    public void addCardPanelListener(CardPanelListener<C> l) {
        listenerList.add(l);
    }

    public void removeCardPanelListener(CardPanelListener<C> l) {
        listenerList.remove(l);
    }

    protected void fireCardChanged(C card) {
        List<CardPanelListener<C>> listeners = new ArrayList<CardPanelListener<C>>(listenerList);
        CardEvent<C> e = new CardEvent<C>(this, card);

        for (CardPanelListener<C> listener : listeners) {
            listener.cardChanged(e);
        }
    }

    protected void fireCardAdded(C card) {
        List<CardPanelListener<C>> listeners = new ArrayList<CardPanelListener<C>>(listenerList);
        CardEvent<C> e = new CardEvent<C>(this, card);

        for (CardPanelListener<C> listener : listeners) {
            listener.cardAdded(e);
        }
    }

    protected void fireCardRemoved(C card) {
        List<CardPanelListener<C>> listeners = new ArrayList<CardPanelListener<C>>(listenerList);
        CardEvent<C> e = new CardEvent<C>(this, card);

        for (CardPanelListener<C> listener : listeners) {
            listener.cardRemoved(e);
        }
    }

    private static final String toString(Object panel) {
        return panel.getClass().getName() + "@" + Integer.toHexString(panel.hashCode());
    }
}