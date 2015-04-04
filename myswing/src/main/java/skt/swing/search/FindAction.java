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

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.Position;

import org.rr.commons.swing.SwingUtils;

/**
 * @author Santhosh Kumar T
 * @email santhosh@in.fiorano.com
 */
public abstract class FindAction extends AbstractAction implements DocumentListener, KeyListener, PopupMenuListener {
    
	private FindActionPanel searchPanel = new FindActionPanel();
	
	private Popup popup;

	protected JTextField searchField = searchPanel.getSearchField();

	// component on which search is taking place
	protected JComponent comp = null;

	public FindAction() {
		this(null);
	}
	
	public FindAction(JComponent comp) {
		super("incremantal-search");
		this.comp = comp;
		
		int modifiers = KeyEvent.CTRL_MASK;
		if (!isCaseSensitiveSearch()) {
			modifiers = modifiers | KeyEvent.SHIFT_MASK;
		}
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('I', modifiers));

		searchField.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				hidePopup();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
		
		searchPanel.getCloseButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				hidePopup();
			}
		});
	}
	
	private void hidePopup() {
		popup.hide();
		comp.requestFocus();
	}


    /**
     * Returns a <code>Popup</code> instance from the
     * <code>PopupMenuUI</code> that has had <code>show</code> invoked on
     * it. If the current <code>popup</code> is non-null,
     * this will invoke <code>dispose</code> of it, and then
     * <code>show</code> the new one.
     * <p>
     * This does NOT fire any events, it is up the caller to dispatch
     * the necessary events.
     */
	private Popup getPopup(int x, int y) {
        if(popup == null) {
	        popup = PopupFactory.getSharedInstance().getPopup(comp, searchPanel, x, y);
        } else {
        	hidePopup();
        	popup = PopupFactory.getSharedInstance().getPopup(comp, searchPanel, x, y);
        }
        popup.show();
        return popup;
    }

	/*-------------------------------------------------[ ActionListener ]---------------------------------------------------*/

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == searchField) {
			hidePopup();
		} else {
			if(comp == null) {
				comp = (JComponent) ae.getSource();
			}
			searchField.removeActionListener(this);
			searchField.removeKeyListener(this);
			searchField.getDocument().removeDocumentListener(this);
			initSearch(ae);
			searchField.addActionListener(this);
			searchField.addKeyListener(this);
			searchField.getDocument().addDocumentListener(this);

			Point locationOnScreen;
			JScrollPane surroundingScrollPane = SwingUtils.getSurroundingScrollPane(comp);
			if(surroundingScrollPane != null) {
				locationOnScreen = surroundingScrollPane.getLocationOnScreen();
			} else {
				locationOnScreen = comp.getLocationOnScreen();
			}
			getPopup(locationOnScreen.x, locationOnScreen.y);

			searchField.requestFocus();
		}
	}

	// can be overridden by subclasses to change initial search text etc.
	protected void initSearch(ActionEvent ae) {
		searchField.setText(""); // NOI18N
		searchField.setForeground(Color.black);
	}

	private void changed(Position.Bias bias) {
		// note: popup.pack() doesn't work for first character insert
//		popup.setVisible(false);
//		popup.setVisible(true);

		searchField.requestFocus();
		searchField.setForeground(changed(comp, searchField.getText(), bias) ? Color.black : Color.red);
	}
	
	public boolean isCaseSensitiveSearch() {
		return searchPanel.isCaseSensitiveSearch();
	}	

	// should search for given text and select item and
	// return true if search is successfull
	protected abstract boolean changed(JComponent comp, String text, Position.Bias bias);

	/*-------------------------------------------------[ DocumentListener ]---------------------------------------------------*/

	public void insertUpdate(DocumentEvent e) {
		changed(null);
	}

	public void removeUpdate(DocumentEvent e) {
		changed(null);
	}

	public void changedUpdate(DocumentEvent e) {
	}

	/*-------------------------------------------------[ KeyListener ]---------------------------------------------------*/

	protected boolean shiftDown = false;
	protected boolean controlDown = false;

	public void keyPressed(KeyEvent ke) {
		shiftDown = ke.isShiftDown();
		controlDown = ke.isControlDown();

		switch (ke.getKeyCode()) {
		case KeyEvent.VK_UP:
			changed(Position.Bias.Backward);
			break;
		case KeyEvent.VK_DOWN:
			changed(Position.Bias.Forward);
			break;
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	/*-------------------------------------------------[ PopupMenuListener ]---------------------------------------------------*/
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		comp.requestFocus();
		// comp = null; //favor gc
	}

	public void popupMenuCanceled(PopupMenuEvent e) {
	}
}