package org.japura.gui;

import java.awt.FontMetrics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * Label component with a wrap function.
 * <P>
 * The default wrap width is 0 (disabled).
 * <P>
 * HTML code is not allowed. It supports <CODE>\n<CODE> code.
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
public class WrapLabel extends JLabel{

  public static final Align LEFT = Align.LEFT;
  public static final Align RIGHT = Align.RIGHT;
  public static final Align CENTER = Align.CENTER;

  private Align align = Align.LEFT;
  private int wrapWidth = 0;
  private String text;

  /**
   * Constructor
   * 
   */
  public WrapLabel() {
	addComponentListener(new ComponentAdapter() {
	  @Override
	  public void componentResized(ComponentEvent e) {
		wrapText();
	  }
	});
  }

  /**
   * Constructor
   * 
   * @param text
   *          {@link String} the text
   */
  public WrapLabel(String text) {
	this();
	setText(text);
  }

  @Override
  public void setText(String text) {
	this.text = text;
	wrapText();
  }

  /**
   * The plain text is converted to HTML code. The wrap locations are calculated
   * with the defined wrap width and component width.
   */
  private void wrapText() {
	if (getFont() == null || text == null) {
	  return;
	}
	FontMetrics fm = getFontMetrics(getFont());

	StringBuilder tempText = new StringBuilder();
	StringBuilder finalText = new StringBuilder("<html>");
	finalText.append("<STYLE type='text/css'>BODY { text-align: ");
	finalText.append(align.name().toLowerCase());
	finalText.append("}</STYLE><BODY>");

	ArrayList<String> words = new ArrayList<String>();
	text = text.replaceAll("\n", "<BR>");
	String split[] = text.split("<BR>");
	for (int i = 0; i < split.length; i++) {
	  if (split[i].length() > 0) {
		String split2[] = split[i].split("[ \\t\\x0B\\f\\r]+");
		for (int j = 0; j < split2.length; j++) {
		  if (split2[j].length() > 0) {
			words.add(split2[j]);
		  }
		}
	  }

	  if (i < split.length - 1) {
		words.add("<BR>");
	  }
	}

	for (String word : words) {
	  if (word.equals("<BR>")) {
		finalText.append("<BR>");
		tempText.setLength(0);
	  } else {
		tempText.append(" ");
		tempText.append(word);

		int tempWidth =
			SwingUtilities.computeStringWidth(fm, tempText.toString().trim());
		if ((wrapWidth > 0 && tempWidth > wrapWidth)) {

		  int wordSize = SwingUtilities.computeStringWidth(fm, word);
		  if (wordSize >= wrapWidth) {
			finalText.append("...");
			break;
		  }

		  finalText.append("<BR>");
		  tempText.setLength(0);
		  tempText.append(word);
		}

		if (tempText.length() > 0) {
		  finalText.append(" ");
		}
		finalText.append(word);
	  }

	}

	finalText.append("</BODY></html>");
	super.setText(finalText.toString());
  }

  /**
   * Get the wrap width.
   * <P>
   * Value 0 disable the wrap .
   * 
   * @return int
   */
  public int getWrapWidth() {
	return wrapWidth;
  }

  /**
   * Set the wrap width.
   * <P>
   * Value 0 disable the wrap.
   * 
   * @param width
   *          int
   */
  public void setWrapWidth(int width) {
	this.wrapWidth = width;
	wrapText();
  }

  /**
   * Get the alignment for the text
   * 
   * @return {@link WrapLabel.Align}
   */
  public Align getAlign() {
	return align;
  }

  /**
   * Set the alignment for the text
   * 
   * @param align
   *          {@link WrapLabel.Align}
   */
  public void setAlign(Align align) {
	this.align = align;
	wrapText();
  }

  /**
   * The alignment for the text
   */
  public static enum Align {
	LEFT,
	CENTER,
	RIGHT;
  };

}
