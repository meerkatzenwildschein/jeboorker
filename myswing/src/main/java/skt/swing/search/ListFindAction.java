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
import javax.swing.text.Position;

/**
 * @author Santhosh Kumar T
 * @email santhosh@in.fiorano.com
 */
public class ListFindAction extends ListModelFindAction {

	public ListFindAction() {
		super();
	}

	protected boolean changed(JComponent comp, String searchString, Position.Bias bias) {
		JList list = (JList) comp;
		boolean startingFromSelection = true;
		int max = list.getModel().getSize();
		int increment = 0;
		if (bias != null)
			increment = (bias == Position.Bias.Forward) ? 1 : -1;
		int startingRow = (list.getLeadSelectionIndex() + increment + max) % max;
		if (startingRow < 0 || startingRow >= list.getModel().getSize()) {
			startingFromSelection = false;
			startingRow = 0;
		}

		int index = getNextMatch(list, list.getModel(), searchString, startingRow, bias);
		if (index != -1) {
			changeSelection(list, index);
			return true;
		} else if (startingFromSelection) {
			index = getNextMatch(list, list.getModel(), searchString, 0, bias);
			if (index != -1) {
				changeSelection(list, index);
				return true;
			}
		}
		return false;
	}

	protected void changeSelection(JList list, int index) {
		if (controlDown)
			list.addSelectionInterval(index, index);
		else
			list.setSelectedIndex(index);
		list.ensureIndexIsVisible(index);
	}
}
