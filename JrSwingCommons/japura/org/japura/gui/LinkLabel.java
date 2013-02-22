package org.japura.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * <P>
 * Copyright (C) 2011 Carlos Eduardo Leite de Andrade
 * <P>
 * This library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <P>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <P>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <A
 * HREF="www.gnu.org/licenses/">www.gnu.org/licenses/</A>
 * <P>
 * For more information, contact: <A HREF="www.japura.org">www.japura.org</A>
 * <P>
 * 
 * @author Carlos Eduardo Leite de Andrade
 */
public class LinkLabel extends JLabel{

  private static final long serialVersionUID = -260221785668861855L;

  private Cursor mouseOverCursor;

  public LinkLabel() {
	ImageIcon ii = new ImageIcon(Images.LINK_HAND);
	mouseOverCursor =
		Toolkit.getDefaultToolkit().createCustomCursor(ii.getImage(),
			new Point(12, 6), "linkHand");
	putClientProperty("html.disable", Boolean.TRUE);
	setForeground(Color.BLUE);
	addMouseListener(new MouseAdapter() {
	  @Override
	  public void mousePressed(MouseEvent e) {
		doClick();
	  }

	  @Override
	  public void mouseExited(MouseEvent e) {
		setCursor(Cursor.getDefaultCursor());
	  }

	  @Override
	  public void mouseEntered(MouseEvent e) {
		setCursor(mouseOverCursor);
	  }
	});
  }

  public void doClick() {
	if (isEnabled()) {
	  fireActionPerformed();
	}
  }

  protected void paintComponent(Graphics g) {
	Graphics2D g2d = (Graphics2D) g.create();
	Insets insets = getInsets();
	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	String text = getText();
	if (text.length() > 0) {
	  if (isEnabled()) {
		g2d.setColor(getForeground());
	  } else {
		Color color = UIManager.getColor("Label.disabledForeground");
		if (color == null) {
		  color = Color.LIGHT_GRAY;
		}
		g2d.setColor(color);
	  }

	  int x = insets.left;

	  Icon icon = getIcon();

	  AttributedString as = new AttributedString(text);
	  as.addAttribute(TextAttribute.FONT, getFont());
	  as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0,
		  text.length());
	  FontMetrics fm = getFontMetrics(getFont());

	  if (getVerticalTextPosition() == SwingConstants.CENTER) {
		if (icon != null) {
		  int y = (getHeight() - icon.getIconHeight()) / 2;
		  y = Math.max(0, y);
		  icon.paintIcon(this, g2d, x, y);
		  x += icon.getIconWidth() + getIconTextGap();
		}

		int v =
			(getHeight() - (insets.bottom + insets.top + fm.getHeight())) / 2;
		g2d.drawString(as.getIterator(), x,
			insets.top + fm.getAscent() + fm.getLeading() + v);
	  } else if (getVerticalTextPosition() == SwingConstants.TOP) {
		if (icon != null) {
		  icon.paintIcon(this, g2d, x, insets.top);
		  x += icon.getIconWidth() + getIconTextGap();
		}

		g2d.drawString(as.getIterator(), x,
			insets.top + fm.getAscent() + fm.getLeading());
	  } else if (getVerticalTextPosition() == SwingConstants.BOTTOM) {
		if (icon != null) {
		  icon.paintIcon(this, g2d, x,
			  getHeight() - (insets.bottom + icon.getIconHeight()));
		  x += icon.getIconWidth() + getIconTextGap();
		}

		g2d.drawString(as.getIterator(), x,
			getHeight() - (insets.bottom + fm.getDescent()));
	  }
	}
  }

  public void addActionListener(ActionListener listener) {
	if (listener != null) {
	  listenerList.add(ActionListener.class, listener);
	}
  }

  public void removeActionListener(ActionListener listener) {
	if (listener != null) {
	  listenerList.remove(ActionListener.class, listener);
	}
  }

  /**
   * Returns an array of all the <code>ActionListener</code>s added to this
   * LinkLabel with addActionListener().
   * 
   * @return all of the <code>ActionListener</code>s added or an empty array if
   *         no listeners have been added
   */
  public ActionListener[] getActionListeners() {
	return (ActionListener[]) (listenerList.getListeners(ActionListener.class));
  }

  protected void fireActionPerformed() {
	ActionEvent event =
		new ActionEvent(LinkLabel.this, ActionEvent.ACTION_PERFORMED,
			"linkClicked", System.currentTimeMillis(), 0);

	ActionListener listeners[] =
		listenerList.getListeners(ActionListener.class);
	for (ActionListener listener : listeners) {
	  listener.actionPerformed(event);
	}
  }
}
