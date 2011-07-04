package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.util.logging.Level;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;

import org.rr.common.swing.StarRater;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.CommonUtils;

public class StarRatingPropertyRenderer extends DefaultTableCellRenderer implements ListCellRenderer {
	
	private static final long serialVersionUID = -9177633701463286482L;
	
	private StarRater renderer;

	public StarRatingPropertyRenderer() {
		renderer = new StarRater();
	}

	public void setRatingValue(Object value) {
		Number number = CommonUtils.toNumber(value);
		if(number !=null) {
			float rating = number.floatValue() / 2f;
			((StarRater)renderer).setRating(rating);
		} else {
			LoggerFactory.log(Level.WARNING, this, "could not parse the entered value \""+String.valueOf(value)+"\"as Number.");			
		}
	}
	
	public void setValue(Object value) {
		this.setRatingValue(value);
	}


	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		this.setRatingValue(value);
		return renderer;
	}	
	
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setValue(value);
        RendererUtils.setColor(this, isSelected);
        return renderer;
    }	

}
