package org.rr.jeborker.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.UIManager;

import org.rr.common.swing.SwingUtils;

class RendererUtils {
	private static final Color selectedBgColor;
	
	private static final Color selectedFgColor;
	
	private static final Color bgColor;
	
	private static final Color fgColor;
	
	static {
		selectedBgColor = SwingUtils.getSelectionBackgroundColor();
		selectedFgColor = SwingUtils.getSelectionForegroundColor();
		
		bgColor = UIManager.getColor("Table.background");
		fgColor = UIManager.getColor("Table.foreground");				
	}
	
	static void setColor(Component comp, boolean isSelected) {
		if(isSelected) {
			comp.setBackground(selectedBgColor);
			comp.setForeground(selectedFgColor);
		} else {
			comp.setBackground(bgColor);
			comp.setForeground(fgColor);
		}
	}

}
