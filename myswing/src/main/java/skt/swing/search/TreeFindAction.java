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

import skt.swing.StringConvertor;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.text.Position;

/**
 * @author Santhosh Kumar T
 * @email  santhosh@in.fiorano.com
 */
public class TreeFindAction extends FindAction{

    public TreeFindAction(){
        super();
    }

    protected boolean changed(JComponent comp, String searchString, Position.Bias bias){
        JTree tree = (JTree)comp;
        boolean startingFromSelection = true;
        int max = tree.getRowCount();
        int increment = 0;
        if(bias!=null)
            increment = (bias == Position.Bias.Forward) ? 1 : -1;
        int startingRow = (tree.getLeadSelectionRow() + increment + max) % max;
        if (startingRow < 0 || startingRow >= tree.getRowCount()) {
            startingFromSelection = false;
            startingRow = 0;
        }

        TreePath path = getNextMatch(tree, searchString, startingRow, bias);
        if (path != null) {
            changeSelection(tree, path);
            return true;
        } else if (startingFromSelection) {
            path = getNextMatch(tree, searchString, 0, bias);
            if (path != null) {
                changeSelection(tree, path);
                return true;
            }
        }
        return false;
    }

    // takes care of modifiers - control
    protected void changeSelection(JTree tree, TreePath path){
        if(controlDown){
            tree.addSelectionPath(path);
        }else
            tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }

    public TreePath getNextMatch(JTree tree, String prefix, int startingRow, Position.Bias bias) {
        int max = tree.getRowCount();
        if (prefix == null) {
            throw new IllegalArgumentException();
        }
        if (startingRow < 0 || startingRow >= max) {
            throw new IllegalArgumentException();
        }
        if(!isCaseSensitiveSearch())
            prefix = prefix.toUpperCase();

        // start search from the next/previous element froom the
        // selected element
        int increment = (bias==null || bias == Position.Bias.Forward) ? 1 : -1;
        int row = startingRow;
        do {
            TreePath path = tree.getPathForRow(row);
            StringConvertor convertor = (StringConvertor)comp.getClientProperty(StringConvertor.class);
			String text = convertor !=null ? convertor.toString(path) : tree.convertValueToText(path.getLastPathComponent(), tree.isRowSelected(row),
                            tree.isExpanded(row), true, row, false);

            if(!isCaseSensitiveSearch())
                text = text.toUpperCase();
            if (text.startsWith(prefix)) {
                return path;
            }
            row = (row + increment + max) % max;
        } while (row != startingRow);
        return null;
    }
}