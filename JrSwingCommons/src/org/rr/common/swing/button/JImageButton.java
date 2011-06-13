package org.rr.common.swing.button;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

public class JImageButton extends JButton implements MouseListener {
	private static final long serialVersionUID = 7601392503622654799L;

	private Icon normalIcon;
	private Icon mousePressedIcon;
	private boolean mousePressed = false;
	private boolean mouseOver = false;
	private int horizontalAlignment = SwingConstants.CENTER;
	private int verticalAlignment = SwingConstants.CENTER;

	public JImageButton(Icon normalIcon, Icon mousePressedIcon) {
		this.normalIcon = normalIcon;
		this.mousePressedIcon = mousePressedIcon;
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setMargin(new Insets(0, 0, 0, 0));
		this.setMargin(new Insets(0, 0, 0, 0));
		this.setPreferredSize(new Dimension(normalIcon.getIconWidth(), normalIcon.getIconHeight()));
		this.addMouseListener(this);
		this.setDoubleBuffered(false);
		this.setOpaque(true);
	}
	
	public int getHorizontalAlignment() {
		return horizontalAlignment;
	}

	/**
	 * Sets the horizontal alignment of the image.
	 * use {@link SwingConstants#LEFT}, {@link SwingConstants#RIGHT} or {@link SwingConstants#CENTER}
	 */
	public void setHorizontalAlignment(int horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	public int getVerticalAlignment() {
		return verticalAlignment;
	}

	/**
	 * Sets the horizontal alignment of the image.
	 * use {@link SwingConstants#TOP}, {@link SwingConstants#WEST} or {@link SwingConstants#BOTTOM}
	 */
	public void setVerticalAlignment(int verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}

	@Override
	public void paintComponent(final Graphics g) {
		Icon img;
		if (this.mousePressed) {
			img = mousePressedIcon;
		} else {
			img = normalIcon;
		}
		
		int x = 0;
		int y = 0;
		
		switch(getHorizontalAlignment()) {
			case SwingConstants.LEFT:
				x = 0;
				break;
			case SwingConstants.CENTER:
				x = (getWidth()/2) - (img.getIconWidth()/2);
				break;
			case SwingConstants.RIGHT:
				x = getWidth() - img.getIconWidth();
		}
		
		switch(getVerticalAlignment()) {
			case SwingConstants.TOP:
				y = 0;
				break;
			case SwingConstants.CENTER:
				y = (getHeight()/2) - (img.getIconHeight()/2);
				break;
			case SwingConstants.BOTTOM:
				x = getWidth() - img.getIconWidth();				
		}
		
		//paint the background.
		Color bg = getParent().getBackground();
		g.setColor(bg);
		if(!isOpaque()) {
			g.fillRect(0, 0, getWidth(), getHeight());
		} else {
			//the parent should draw the background.
			getParent().repaint();
			
		}
		
		if(mouseOver) {
			img.paintIcon(this, g, x, y);
		} else {
			Composite originalComposite = ((Graphics2D) g).getComposite();
			AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, .5f);
			((Graphics2D) g).setComposite(alpha);
			img.paintIcon(this, g, x, y);
			((Graphics2D) g).setComposite(originalComposite);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mouseOver = true;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		mouseOver = false;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mousePressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mousePressed = false;
	}
}
