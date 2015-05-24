package org.rr.jeborker.gui.cell;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtil;

import com.l2fprod.common.model.DefaultObjectRenderer;
import com.l2fprod.common.model.ObjectRenderer;

public class MultiListPropertyRenderer extends JComboBox implements TableCellRenderer {
	
	private static final String noChanges = "<" + Bundle.getString("ComboBoxPropertyEditor.noChanges") + ">";
	
	private static final String clear = "<" + Bundle.getString("ComboBoxPropertyEditor.clear") + ">";
	
    private ObjectRenderer objectRenderer = new DefaultObjectRenderer();
    
    private DefaultComboBoxModel model = new DefaultComboBoxModel();
    
    public MultiListPropertyRenderer() {
    	this.setOpaque(true);
    	this.setBackground(Color.RED);
    	this.setEditable(true);
    	this.setModel(model);
    	((JTextField)this.getEditor().getEditorComponent()).setBorder(new EmptyBorder(new Insets(0, 3, 0, 0)));
    }

    public void setValue(Object value) {
        if (value == null || value.toString().length() == 0) {
            setText(clear);
        } else if(value instanceof List) {
        	Object o = ListUtils.first((List<?>)value);
            if(o == null) {
            	//null value means no changes
            	setText(noChanges);            	
            } else if(StringUtil.toString(o).isEmpty()) {
            	//empty string is clear
            	setText(clear);
            } else {
            	setText(StringUtil.toString(o));
            }
        } else {
        	setText(clear);
        }
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
    
    private void setText(String text) {
    	this.model.removeAllElements();
    	this.model.addElement(text);
    	this.model.setSelectedItem(text);
    }

}
