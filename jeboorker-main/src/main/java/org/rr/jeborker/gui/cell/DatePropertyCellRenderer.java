package org.rr.jeborker.gui.cell;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.rr.commons.swing.SwingUtils;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.ListUtils;

public class DatePropertyCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	
	private DateFormat dateFormat;

    public DatePropertyCellRenderer() {
        this(DateFormat.getDateInstance());
    }

    public DatePropertyCellRenderer(String formatString) {
        this(formatString, Locale.getDefault());
    }

    public DatePropertyCellRenderer(String formatString, Locale l) {
        this(new SimpleDateFormat(formatString, l));
    }

    public DatePropertyCellRenderer(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setValue(Object value) {
        if (value == null || value.toString().length() == 0) {
            setText(EMPTY);
        } else if(value instanceof Date){
            setText(dateFormat.format((Date) value));
        } else if(value instanceof String) {
        	Date date = DateConversionUtils.toDate((String) value);
        	if(date != null) {
        		setText(dateFormat.format((Date) date));
        	} else {
        		setText(String.valueOf(value));
        	}
        } else if(value instanceof List<?>) {
    		Object first = ListUtils.first((List<?>) value);
    		setText(String.valueOf(first));        	
        }
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setValue(value);
        SwingUtils.setColor(this, isSelected);
        return this;
    }

}
