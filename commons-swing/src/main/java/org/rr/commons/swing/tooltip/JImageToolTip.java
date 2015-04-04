package org.rr.commons.swing.tooltip;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JToolTip;
import javax.swing.UIManager;

/**
 * A tool tip capable of displaying an image.
 * 
 * @author Collin Fagan
 */
public class JImageToolTip extends JToolTip {

	private static final long serialVersionUID = 2284228909791458845L;

	private int width;

	private int height;

	private Image image;

	/**
	 * @param image
	 *            - image to display
	 * @param scale
	 *            - scale factor
	 */
	public JImageToolTip(Image image, double scale) {
		super();

		this.width = (int) (image.getWidth(null) * scale);
		this.height = (int) (image.getHeight(null) * scale);

		this.image = image;

		setPreferredSize(new Dimension(width, height));

		setBorder(BorderFactory.createLineBorder(UIManager.getColor("PopupMenu.border")));
	}

	/**
	 * 
	 * @param image
	 */
	public JImageToolTip(Image image) {
		this(image, 1);
	}

	/**
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = ((Graphics2D) g);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		g2d.setColor(getBackground());
		g2d.fillRect(0, 0, width, height);

		paintBorder(g2d);

		Insets insets = getInsets();
		g2d.drawImage(image, insets.left, insets.top, width - (insets.left + insets.right), height - (insets.top + insets.bottom), null);
	}
}