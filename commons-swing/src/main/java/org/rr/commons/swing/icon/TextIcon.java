package org.rr.commons.swing.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

import org.rr.commons.collection.Pair;

/**
 * An {@link Icon} implementation which only shows a text at a configurable position. It can be used with {@link DecoratedIcon} to create a
 * text overlay for another icon.
 */
public class TextIcon implements Icon {

	private final int width;

	private final int height;

	private final String text;

	private final Font font;

	private final Location location;

	private final Insets insets;

	private final Color bgColor;
	
	private final Color fgColor;
	
	public static enum Location {
		UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, LOWER_RIGHT
	};

	public TextIcon(int width, int height, String text, Font font, Location location, Insets insets, Color fgColor, Color bgColor) {
		this.width = width;
		this.height = height;
		this.text = text;
		this.font = font;
		this.location = location;
		this.insets = insets;
		this.bgColor = bgColor;
		this.fgColor = fgColor;
	}

	public int getIconWidth() {
		return width;
	}

	public int getIconHeight() {
		return height;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		g = g.create(x, y, width, height);
		try {
			g.setFont(font);
			
			FontMetrics metrics = g.getFontMetrics();
			Rectangle2D stringBounds = metrics.getStringBounds(text, g);
			
			Pair<Integer, Integer> textLocation = getTextLocation(g, stringBounds);
			int textX = textLocation.getE();
			int textY = textLocation.getF();
			
			if(bgColor != null) {
				paintTextBackground(g, stringBounds, textX, textY);
			}
			
			if(fgColor != null) {
				g.setColor(Color.WHITE);
			}
			
			setupAntialiasing(g);

			g.drawString(text, textX, textY);
		} finally {
			g.dispose();	
		}
	}

	protected void paintTextBackground(Graphics g, Rectangle2D stringBounds, int textX, int textY) {
		Color color = g.getColor();
		g.setColor(bgColor);
		g.fillRect(textX - 1, (int) (textY - stringBounds.getHeight() + 1), (int) stringBounds.getWidth() + 1, (int) (stringBounds.getHeight() + 1));
		g.setColor(color);
	}

	protected void setupAntialiasing(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
	}
	
	private Pair<Integer, Integer> getTextLocation(Graphics g, Rectangle2D stringBounds) {
		int textX = 0;
		int textY = 0;
		switch (location) {
		case UPPER_LEFT:
			textX = insets.left;
			textY = insets.top + (int) stringBounds.getHeight();
			break;
		case LOWER_LEFT:
			textX = insets.left;
			textY = height - insets.bottom;
			break;
		case UPPER_RIGHT:
			textX = getIconWidth() - (int) stringBounds.getWidth() - insets.right;
			textY = insets.top + (int) stringBounds.getHeight();
			break;
		case LOWER_RIGHT:
			textX = getIconWidth() - (int) stringBounds.getWidth() - insets.right;
			textY = height - insets.bottom;
			break;
		}
		return new Pair<Integer, Integer>(Math.max(0, textX), Math.max(0, textY));
	}

}