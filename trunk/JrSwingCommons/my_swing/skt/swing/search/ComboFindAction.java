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

// @author Santhosh Kumar T - santhosh@in.fiorano.com
public class ComboFindAction extends ListModelFindAction {

	public ComboFindAction() {
		super();
	}

	protected boolean changed(JComponent comp, String searchString, Position.Bias bias) {
		JComboBox combo = comp instanceof JComboBox ? (JComboBox) comp : (JComboBox) SwingUtilities.getAncestorOfClass(JComboBox.class, comp);
		boolean startingFromSelection = true;
		int max = combo.getModel().getSize();
		int increment = 0;
		if (bias != null)
			increment = (bias == Position.Bias.Forward) ? 1 : -1;
		int startingRow = (combo.getSelectedIndex() + increment + max) % max;
		if (startingRow < 0 || startingRow >= combo.getModel().getSize()) {
			startingFromSelection = false;
			startingRow = 0;
		}

		int index = getNextMatch(combo, combo.getModel(), searchString, startingRow, bias);
		if (index != -1) {
			combo.setSelectedIndex(index);
			return true;
		} else if (startingFromSelection) {
			index = getNextMatch(combo, combo.getModel(), searchString, 0, bias);
			if (index != -1) {
				combo.setSelectedIndex(index);
				return true;
			}
		}
		return false;
	}
}