package org.rr.jeborker.gui.cell;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyEditor;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JPanel;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.components.StarRater;
import org.rr.commons.utils.CommonUtils;

public class StarRatingPropertyEditor extends JPanel implements PropertyEditor {
	
	private static final long serialVersionUID = 8069880556160736365L;

	private StarRater starRater;

	public StarRatingPropertyEditor() {
		starRater = new StarRater();
		this.setLayout(new BorderLayout());
		this.add(starRater, BorderLayout.CENTER);
		SwingUtils.setColor(this, true);
	}

	public Component getCustomEditor() {
		return this;
	}

	@SuppressWarnings("rawtypes")
	public void setValue(Object value) {
		if(value instanceof List) {
			value = ((List)value).get(0);
		}
		Number number = CommonUtils.toNumber(value);
		if (number != null) {
			float rating = number.floatValue() / 2f;
			starRater.setRating(rating);
		} else {
			LoggerFactory.log(Level.WARNING, this, "could not parse the entered value \"" + String.valueOf(value) + "\"as Number.");
		}
	}

	/**
	 * Returns the rating as string
	 * 
	 * @return the rating value. This value is an integer between 1 and 10.
	 */
	public Object getValue() {
		float rating = starRater.getSelection();
		Float valueOf = Float.valueOf(rating * 2f);
		return String.valueOf(valueOf.intValue());
	}

	@Override
	public boolean isPaintable() {
		return false;
	}

	@Override
	public void paintValue(Graphics gfx, Rectangle box) {
	}

	@Override
	public String getJavaInitializationString() {
		return null;
	}

	@Override
	public String getAsText() {
		return null;
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
	}

	@Override
	public String[] getTags() {
		return null;
	}

	@Override
	public boolean supportsCustomEditor() {
		return false;
	}

}
