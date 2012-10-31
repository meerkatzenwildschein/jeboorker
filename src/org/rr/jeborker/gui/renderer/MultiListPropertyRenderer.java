package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;

import com.l2fprod.common.model.DefaultObjectRenderer;
import com.l2fprod.common.model.ObjectRenderer;

public class MultiListPropertyRenderer extends DefaultTableCellRenderer {
	
	private static final String noChanges = "<" + Bundle.getString("ComboBoxPropertyEditor.noChanges") + ">";
	
	private static final String clear = "<" + Bundle.getString("ComboBoxPropertyEditor.clear") + ">";
	
    private ObjectRenderer objectRenderer = new DefaultObjectRenderer();

    public void setValue(Object value) {
        if (value == null || value.toString().length() == 0) {
            setText(clear);
        } else if(value instanceof List) {
        	Object o = ListUtils.first((List<?>)value);
            if(o == null) {
            	//null value means no changes
            	setText(noChanges);            	
            } else if(StringUtils.toString(o).isEmpty()) {
            	setText(clear);
            } else {
            	setText(StringUtils.toString(o));
            }
        } else {
        	setText(clear);
        }
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setValue(value);
        RendererUtils.setColor(this, isSelected);
        return this;
    }

    protected String convertToString(Object value) {
        return objectRenderer.getText(value);
    }

    protected Icon convertToIcon(Object value) {
        return null;
    }
    

}
