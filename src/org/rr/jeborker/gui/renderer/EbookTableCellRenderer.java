package org.rr.jeborker.gui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.io.Serializable;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class EbookTableCellRenderer extends EbookTableCellComponent implements TableCellRenderer, Serializable  {

	private static final long serialVersionUID = -4684790158985895647L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		final Component tableCellComponent = super.getTableCellComponent(table, value, isSelected, hasFocus, row, column);
		this.setBackground(Color.WHITE);
		this.setForeground(Color.BLACK);
		return tableCellComponent;
	}
	
}
