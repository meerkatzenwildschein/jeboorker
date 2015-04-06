package org.rr.jeborker.gui.cell;

import java.awt.Component;

import org.rr.commons.swing.SwingUtils;

class RendererUtils {
	
	static void setColor(Component comp, boolean isSelected) {
		if(isSelected) {
			comp.setBackground(SwingUtils.getSelectionBackgroundColor());
			comp.setForeground(SwingUtils.getSelectionForegroundColor());
		} else {
			comp.setBackground(SwingUtils.getBackgroundColor());
			comp.setForeground(SwingUtils.getForegroundColor());
		}
	}

}
