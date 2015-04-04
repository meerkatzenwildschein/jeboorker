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

import javax.swing.JComponent;
import javax.swing.ListModel;
import javax.swing.text.Position;

import skt.swing.StringConvertor;

/**
 * @author Santhosh Kumar T
 * @email  santhosh@in.fiorano.com
 */
public abstract class ListModelFindAction extends FindAction{

    protected ListModelFindAction(){
        super();
    }

    public int getNextMatch(JComponent comp, ListModel model, String prefix, int startIndex, Position.Bias bias){
        int max = model.getSize();
        if(prefix==null){
            throw new IllegalArgumentException();
        }
        if(startIndex<0 || startIndex>= max){
            throw new IllegalArgumentException();
        }

        if(!isCaseSensitiveSearch()) {
            prefix = prefix.toUpperCase();
        }

        // start search from the next element after the selected element
        int increment = (bias==null || bias == Position.Bias.Forward) ? 1 : -1;
        int index = startIndex;
        do{
            Object item = model.getElementAt(index);

            if(item!=null){
                StringConvertor convertor = (StringConvertor)comp.getClientProperty(StringConvertor.class);
                String text = convertor!=null ? convertor.toString(item) : item.toString();
                if(!isCaseSensitiveSearch()) {
                    text = text.toUpperCase();
                }

                if(text!=null && text.startsWith(prefix)){
                    return index;
                }
            }
            index = (index+increment+max)%max;
        } while(index!=startIndex);
        return -1;
    }
}