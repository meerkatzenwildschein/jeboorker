package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.util.logging.Level;

import org.rr.common.swing.StarRater;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.CommonUtils;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

public class StarRatingPropertyEditor extends AbstractPropertyEditor {
	
	public StarRatingPropertyEditor() {
		editor = new StarRater();
	}
	
	public Component getCustomEditor() {
		return editor;
	}
	
	public void setValue(Object value) {
		Number number = CommonUtils.toNumber(value);
		if(number !=null) {
			float rating = number.floatValue() / 2f;
			((StarRater)editor).setRating(rating);
		} else {
			LoggerFactory.log(Level.WARNING, this, "could not parse the entered value \""+String.valueOf(value)+"\"as Number.");			
		}
	}
	
	/**
	 * Returns the rating as string
	 * 
	 * @return the rating value. This value is an integer between 1 and 10.
	 */
	public Object getValue() {
		float rating = ((StarRater)editor).getSelection();
		Float valueOf = Float.valueOf(rating * 2f);
		return String.valueOf(valueOf.intValue());
	}	

}
