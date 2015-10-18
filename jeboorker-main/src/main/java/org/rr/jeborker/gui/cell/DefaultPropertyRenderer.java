package org.rr.jeborker.gui.cell;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.utils.StringUtil;

public class DefaultPropertyRenderer extends DefaultTableCellRenderer {
	
	private static final long serialVersionUID = -9177633701463286482L;
	
	public DefaultPropertyRenderer() {
	}
	
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    	Component tableCellRendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    	SwingUtils.setColor(tableCellRendererComponent, isSelected);
    	return tableCellRendererComponent;
    }


	@Override
	protected void setValue(Object value) {
    	if(value instanceof IResourceHandler) {
    		String text = ((IResourceHandler)value).getName();
    		setText(text);
    	} else {
    		String text = StringUtil.toString(value);
    		setText(text);
    	}
	}
 

}
