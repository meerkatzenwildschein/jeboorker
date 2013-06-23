package org.rr.jeborker.gui.renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.swing.components.StarRater;
import org.rr.commons.utils.CommonUtils;

public class StarRatingPropertyRenderer extends JPanel implements TableCellRenderer {
	
	private static final long serialVersionUID = -9177633701463286482L;
	
	private StarRater starRater;
	
	public StarRatingPropertyRenderer() {
		starRater = new StarRater();
		this.setLayout(new BorderLayout());
		this.add(starRater, BorderLayout.CENTER);
	}

	@SuppressWarnings("rawtypes")
	public void setRatingValue(Object value) {
		if(value instanceof List) {
			value = ((List)value).get(0);
		}		
		Number number = CommonUtils.toNumber(value);
		if(number !=null) {
			float rating = number.floatValue() / 2f;
			((StarRater)starRater).setRating(rating);
		} else {
			LoggerFactory.log(Level.WARNING, this, "could not parse the entered value \""+String.valueOf(value)+"\"as Number.");			
		}
	}
	
	public void setValue(Object value) {
		this.setRatingValue(value);
	}
	
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setValue(value);
        RendererUtils.setColor(this, isSelected);
        return this;
    }	

}
