package org.rr.jeborker.gui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.io.Serializable;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

public class EbookTableCellRenderer extends EbookTableCellComponent implements TableCellRenderer, Serializable  {

	private static final long serialVersionUID = -4684790158985895647L;
	
	private final Color selectedBgColor;
	
	private final Color selectedFgColor;	
	
	public EbookTableCellRenderer() {
		final Color color = UIManager.getColor("Table.selectionBackground");
		final int brighter = 20;
		selectedBgColor = new Color(color.getRed() + brighter, color.getGreen() + brighter, color.getBlue() + brighter);		
		
//		selectedBgColor = UIManager.getColor("TextField.selectionBackground");	
		selectedFgColor = UIManager.getColor("Table.selectionForeground");
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		final Component tableCellComponent = super.getTableCellComponent(table, value, isSelected, hasFocus, row, column);
		if(isSelected) {
			this.setBackground(selectedBgColor);	
			this.setForeground(selectedFgColor);
		} else {		
			this.setBackground(UIManager.getColor("Table.background"));
			this.setForeground(UIManager.getColor("Table.foreground"));			
		}
		
		return tableCellComponent;
	}
	
}
