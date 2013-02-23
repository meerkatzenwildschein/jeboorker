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
import javax.swing.text.Position;

/**
 * @author Santhosh Kumar T
 * @email santhosh@in.fiorano.com
 */
public class TableFindAction extends FindAction {

	private JTable table;

	public TableFindAction(JTable table) {
		super(table);
		this.table = table;
	}

	protected boolean changed(JComponent comp, String searchString, Position.Bias bias) {
		boolean startingFromSelection = true;
		int max = table.getRowCount();
		int increment = 0;
		if (bias != null)
			increment = (bias == Position.Bias.Forward) ? 1 : -1;
		int startingRow = (table.getSelectionModel().getLeadSelectionIndex() + increment + max) % max;
		if (startingRow < 0 || startingRow >= table.getRowCount()) {
			startingFromSelection = false;
			startingRow = 0;
		}

		int index = getNextMatch(table, searchString, startingRow, bias);
		if (index != -1) {
			changeSelection(table, index);
			return true;
		} else if (startingFromSelection) {
			index = getNextMatch(table, searchString, 0, bias);
			if (index != -1) {
				changeSelection(table, index);
				return true;
			}
		}
		return false;
	}

	protected void changeSelection(JTable table, int index) {
		if (controlDown) {
			table.addRowSelectionInterval(index, index);
		} else {
			table.setRowSelectionInterval(index, index);
		}
		int column = table.getSelectedColumn();
		if (column == -1) {
			column = 0;
		}
		table.scrollRectToVisible(table.getCellRect(index, column, true));
	}

	public int getNextMatch(JTable table, String prefix, int startIndex, Position.Bias bias) {
		int column = table.getSelectedColumn();
		if (column == -1)
			column = 0;
		int max = table.getRowCount();
		if (prefix == null) {
			throw new IllegalArgumentException();
		}
		if (startIndex < 0 || startIndex >= max) {
			throw new IllegalArgumentException();
		}

		if (!isCaseSensitiveSearch()) {
			prefix = prefix.toUpperCase();
		}

		// start search from the next element after the selected element
		int increment = (bias == null || bias == Position.Bias.Forward) ? 1 : -1;
		int index = startIndex;
		do {
			Object item = table.getValueAt(index, column);

			if (item != null) {
				StringConvertor convertor = (StringConvertor) comp.getClientProperty(StringConvertor.class);
				String text = convertor != null ? convertor.toString(item) : item.toString();
				if (!isCaseSensitiveSearch()) {
					text = text.toUpperCase();
				}

				if (text != null && text.indexOf(prefix) != -1) {
					return index;
				}
			}
			index = (index + increment + max) % max;
		} while (index != startIndex);
		return -1;
	}
}
