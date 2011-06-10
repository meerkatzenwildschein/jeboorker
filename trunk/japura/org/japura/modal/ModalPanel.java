package org.japura.modal;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

/**
 * Transparent panel
 * <P>
 * Copyright (C) 2009 Carlos Eduardo Leite de Andrade
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
class ModalPanel extends JPanel{

  /**
   * Construtor padr�o.
   */
  public ModalPanel() {
	setOpaque(false);
	addMouseListener(new MouseAdapter() {});
  }

  @Override
  public void paintComponent(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	float alpha = 1f;
	int type = AlphaComposite.SRC_OVER;
	AlphaComposite composite = AlphaComposite.getInstance(type, alpha);
	g2d.setComposite(composite);
	g2d.setColor(new Color(135, 135, 135, 150));
	int width = getRootPane().getWidth();
	int height = getRootPane().getHeight();
	g2d.fill(new Rectangle2D.Double(0, 0, width, height));
  }
}
