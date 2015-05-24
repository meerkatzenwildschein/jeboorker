package org.rr.jeborker.gui.cell;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import javax.swing.JTextField;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.StringUtil;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.swing.LookAndFeelTweaks;

public class DatePropertyCellEditor extends AbstractPropertyEditor {

	private Date date = null;

	public DatePropertyCellEditor() {
		editor = new JTextField();
		((JTextField)editor).setBorder(LookAndFeelTweaks.EMPTY_BORDER);
	}

	public Component getCustomEditor() {
		return editor;
	}

	/**
	 * Returns the Date of the Calendar
	 *
	 * @return the date choosed as a <b>java.util.Date </b>b> object or null is
	 *         the date is not set
	 */
	public Object getValue() {
		return this.getAsDate();
	}

	/**
	 * Sets the Date of the Calendar
	 *
	 * @param value
	 *            the Date object
	 */
	public void setValue(Object value) {
		try {
			if(!(value instanceof Date)) {
				value = DateConversionUtils.toDate(StringUtil.toString(value));
			}
			this.date = (Date) value;
			((JTextField) this.editor).setText(getAsText((Date) value));
		} catch (Exception e) {
			((JTextField) this.editor).setText(EMPTY);
			LoggerFactory.log(Level.WARNING, this, "could not parse the entered value \""+String.valueOf(value)+"\"as Date.", e);
		}
	}

	/**
	 * Returns the Date formated with the locale and formatString set.
	 *
	 * @return the chosen Date as String
	 */
	public String getAsText(Date date) {
		if(date == null) {
			return EMPTY;
		}
		DateFormat formatter = SimpleDateFormat.getDateInstance();
		return formatter.format(date);
	}

	/**
	 * Returns the Date formated with the locale and formatString set.
	 *
	 * @return the chosen Date as String
	 */
	public String getAsText() {
		Date date = (Date) getValue();
		DateFormat formatter = SimpleDateFormat.getDateInstance();
		return formatter.format(date);
	}

	/**
	 * Returns the Date entered into the editor component.
	 *
	 * @return The Date from the editor component.
	 */
	public Date getAsDate() {
		String text = ((JTextField) this.editor).getText();
		if(text==null || text.length()==0) {
			return null;
		}
		try {
			Date result = DateConversionUtils.toDate(text);
			return result;
		} catch (Exception e) {
			//return the date if the entered value could not be parsed.
			LoggerFactory.log(Level.WARNING, this, "could not parse the entered value \""+text+"\"as Date.", e);
			return this.date;
		}
	}

}
