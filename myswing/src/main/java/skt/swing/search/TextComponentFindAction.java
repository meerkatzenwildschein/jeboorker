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
import javax.swing.event.PopupMenuEvent;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author Santhosh Kumar T
 * @email  santhosh@in.fiorano.com
 */
public class TextComponentFindAction extends FindAction implements FocusListener{

    public TextComponentFindAction(){
        super();
    }

    // 1. inits searchField with selected text
    // 2. adds focus listener so that textselection gets painted
    //    even if the textcomponent has no focus
    protected void initSearch(ActionEvent ae){
        super.initSearch(ae);
        JTextComponent textComp = (JTextComponent)ae.getSource();
        String selectedText = textComp.getSelectedText();
        if(selectedText!=null)
            searchField.setText(selectedText);
        searchField.removeFocusListener(this);
        searchField.addFocusListener(this);
    }

    protected boolean changed(JComponent comp, String str, Position.Bias bias){
        JTextComponent textComp = (JTextComponent)comp;
        int offset = bias==Position.Bias.Forward ? textComp.getCaretPosition() : textComp.getCaret().getMark()-1;

        int index = getNextMatch(textComp, str, offset, bias);
        if(index!=-1){
            textComp.select(index, index+str.length());
            return true;
        } else{
            offset = bias==null || bias==Position.Bias.Forward ? 0 : textComp.getDocument().getLength();
            index = getNextMatch(textComp, str, offset, bias);
            if(index!=-1){
                textComp.select(index, index+str.length());
                return true;
            } else
                return false;
        }
    }

    protected int getNextMatch(JTextComponent textComp, String str, int startingOffset, Position.Bias bias){
        String text = null;

        // get text from document, otherwize it won't work with JEditorPane with html
        try{
            text = textComp.getDocument().getText(0, textComp.getDocument().getLength());
        } catch(BadLocationException e){
            throw new RuntimeException("This should never happen!");
        }

        if(!isCaseSensitiveSearch()){
            str = str.toUpperCase();
            text = text.toUpperCase();
        }

        return bias==null || bias==Position.Bias.Forward
                ? text.indexOf(str, startingOffset)
                : text.lastIndexOf(str, startingOffset);
    }

    /*-------------------------------------------------[ PopupMenuListener ]---------------------------------------------------*/
    // ensures that the selection is visible
    // because textcomponent doesn't show selection
    // when they don't have focus

    public void popupMenuWillBecomeVisible(PopupMenuEvent e){
        super.popupMenuWillBecomeVisible(e);
        Caret caret = ((JTextComponent)comp).getCaret();
        caret.setVisible(true);
        caret.setSelectionVisible(true);
    }

    public void focusGained(FocusEvent e){
    }

    public void focusLost(FocusEvent e){}
}