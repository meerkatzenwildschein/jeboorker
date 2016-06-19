package org.rr.commons.swing.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JLabel;

import org.rr.commons.swing.SwingUtils;

public class JRLabel extends JLabel {

	private Color underline;
	
	private int underlineInset;
	
	private int underlineThinkness = 1;

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(underline != null) {
			drawUnderline(g);
		}
	}
	
	private void drawUnderline(Graphics g) {
		Rectangle clipBounds = g.getClipBounds();
		int y = (int) (clipBounds.getHeight() - 1) + underlineInset;
		int width = getTextWidth();
		
		Color savedColor = g.getColor();
		g.setColor(underline);
		for(int i = 0; i < underlineThinkness; i++) {
			g.drawLine(0, y - i, width, y - i);
		}
		g.setColor(savedColor);
	}
	
	public int getTextWidth() {
		return (int) SwingUtils.getTextDimension(getText(), getFont()).getWidth();
	}

	public Color getUnderline() {
		return underline;
	}

	public void setUnderline(Color underline) {
		this.underline = underline;
	}
	
	
	public int getUnderlineInset() {
		return underlineInset;
	}

	/**
	 * A negative value in pixel to modify the underline y location.  
	 * 
	 * @param underlineInset A negative value in pixel.
	 */
	public void setUnderlineInset(int underlineInset) {
		this.underlineInset = underlineInset;
	}
	
	public int getUnderlineThinkness() {
		return underlineThinkness;
	}

	/**
	 * Set the thickness of the underline line.
	 * @param underlineThinkness The thickness in pixel.
	 */
	public void setUnderlineThinkness(int underlineThinkness) {
		this.underlineThinkness = underlineThinkness;
	}
	
}
