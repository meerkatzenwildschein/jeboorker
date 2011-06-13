package org.rr.common.swing.icon;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class DecoratedIcon implements Icon {

	private Icon originalIcon;
	private Icon decorationIcon;
	private int xDiff;
	private int yDiff;
	private Location location;

	public static enum Location {
		UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, LOWER_RIGHT
	};

	/**
	 * Creates a new {@link DecoratedIcon} instance. The default location
	 * of the decoration icon is {@link Location#LOWER_LEFT}.
	 * @param original The icon.
	 * @param decoration The decoration for the icon to be displayed.
	 */
	public DecoratedIcon(Icon original, Icon decoration) {
		this(original, decoration, Location.LOWER_LEFT);
	}

	/**
	 * Creates a new {@link DecoratedIcon} instance.
	 * @param original The icon.
	 * @param decoration The decoration for the icon to be displayed.
	 * @param location The location of the decoration. Use one of the following
	 * constants: 
	 * <ul>
	 * 	<li>{@link Location#UPPER_LEFT}</li>
	 *  <li>{@link Location#UPPER_RIGHT}</li>
	 *  <li>{@link Location#LOWER_LEFT}</li>
	 *  <li>{@link Location#LOWER_RIGHT}</li>
	 * </ul>
	 */
	public DecoratedIcon(Icon original, Icon decoration, Location location) {
		this.location = location;
		this.originalIcon = original;
		this.decorationIcon = decoration;
		if (decoration.getIconHeight() > original.getIconHeight() || decoration.getIconWidth() > original.getIconWidth()) {
			throw new IllegalArgumentException("Decoration must be smaller than the original");
		}
		this.xDiff = originalIcon.getIconWidth() - decorationIcon.getIconWidth();
		this.yDiff = originalIcon.getIconHeight() - decorationIcon.getIconHeight();
	}

	/**
	 * Gets the height of the icon specified with the
	 * <code>original</code> parameter of the constructor.
	 */
	public int getIconHeight() {
		return originalIcon.getIconHeight();
	}

	/**
	 * Gets the width of the icon specified with the
	 * <code>original</code> parameter of the constructor.
	 */
	public int getIconWidth() {
		return originalIcon.getIconWidth();
	}

    /**
     * Draw the original icon and it's decoration at the specified location.
     */
	public void paintIcon(Component owner, Graphics g, int x, int y) {
		// paint original icon
		originalIcon.paintIcon(owner, g, x, y);

		int decorationX = x;
		int decorationY = y;

		// x location
		if (location == Location.UPPER_RIGHT || location == Location.LOWER_RIGHT) {
			decorationX += xDiff;
		}

		// y location
		if (location == Location.LOWER_LEFT || location == Location.LOWER_RIGHT) {
			decorationY += yDiff;
		}

		decorationIcon.paintIcon(owner, g, decorationX, decorationY);
	}
}
