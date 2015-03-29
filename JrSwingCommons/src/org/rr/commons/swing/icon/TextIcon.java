package org.rr.commons.swing.icon;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

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

	public static enum Location {
		UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, LOWER_RIGHT
	};

	public TextIcon(int width, int height, String text, Font font, Location location, Insets insets) {
		this.width = width;
		this.height = height;
		this.text = text;
		this.font = font;
		this.location = location;
		this.insets = insets;
	}

	public int getIconWidth() {
		return width;
	}

	public int getIconHeight() {
		return height;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		g = g.create(x, y, width, height);

		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		Rectangle2D stringBounds = metrics.getStringBounds(text, g);
		switch (location) {
		case UPPER_LEFT:
			g.drawString(text, insets.left, insets.top + (int) stringBounds.getHeight());
			break;
		case LOWER_LEFT:
			g.drawString(text, insets.left, height - insets.bottom);
			break;
		case UPPER_RIGHT:
			g.drawString(text, getIconWidth() - (int) stringBounds.getWidth() - insets.right, insets.top + (int) stringBounds.getHeight());
			break;
		case LOWER_RIGHT:
			g.drawString(text, getIconWidth() - (int) stringBounds.getWidth() - insets.right, height - insets.bottom);
			break;
		}
		g.dispose();
	}

}