package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.rr.commons.utils.DateConversionUtils;

import com.l2fprod.common.model.DefaultObjectRenderer;
import com.l2fprod.common.model.ObjectRenderer;

public class DatePropertyCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	
    private ObjectRenderer objectRenderer = new DefaultObjectRenderer();
    
	private DateFormat dateFormat;

    public DatePropertyCellRenderer() {
        this(DateFormat.getDateInstance(DateFormat.SHORT));
    }

    public DatePropertyCellRenderer(String formatString) {
        this(formatString, Locale.getDefault());
    }

    public DatePropertyCellRenderer(Locale l) {
        this(DateFormat.getDateInstance(DateFormat.SHORT, l));
    }

    public DatePropertyCellRenderer(String formatString, Locale l) {
        this(new SimpleDateFormat(formatString, l));
    }

    public DatePropertyCellRenderer(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setValue(Object value) {
        if (value == null || value.toString().length() == 0) {
            setText("");
        } else if(value instanceof Date){
            setText(dateFormat.format((Date) value));
        } else if(value instanceof String) {
        	Date date = DateConversionUtils.toDate((String) value);
        	setText(dateFormat.format((Date) date));
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
