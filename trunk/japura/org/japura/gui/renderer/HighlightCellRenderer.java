package org.japura.gui.renderer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * 
 * <P>
 * Copyright (C) 2010 Carlos Eduardo Leite de Andrade
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
 * 
 */
public class HighlightCellRenderer extends DefaultListCellRenderer{

  private StringBuffer sb = new StringBuffer();
  private String highlightText = "";
  private boolean caseSensitive;

  public HighlightCellRenderer(boolean caseSensitive) {
	this.caseSensitive = caseSensitive;
  }

  public void setHighlightText(String highlightText) {
	this.highlightText = highlightText;
	if (this.highlightText == null)
	  this.highlightText = "";
  }

  public String getHighlightText() {
	return highlightText;
  }

  public boolean isCaseSensitive() {
	return caseSensitive;
  }

  @Override
  public Component getListCellRendererComponent(JList list, Object value,
												int index, boolean isSelected,
												boolean cellHasFocus) {
	super.getListCellRendererComponent(list, value, index, isSelected,
		cellHasFocus);

	if (highlightText.length() == 0) {
	  return this;
	}

	String originalText = value.toString();
	String text = null;
	if (caseSensitive) {
	  text = originalText;
	} else {
	  text = originalText.toLowerCase();
	}

	List<Integer> indexs = new ArrayList<Integer>();
	int i = 0;
	while (i > -1) {
	  i = text.indexOf(highlightText, i);
	  if (i > -1) {
		indexs.add(i);
		i += highlightText.length();
	  }
	}

	sb.setLength(0);
	sb.append("<html>");

	int is = 0;
	for (int ii : indexs) {
	  sb.append(originalText.substring(is, ii));
	  sb.append("<u>");
	  sb.append(originalText.substring(ii, ii + highlightText.length()));
	  sb.append("</u>");
	  is = ii + highlightText.length();
	}
	if (is < originalText.length()) {
	  sb.append(originalText.substring(is, originalText.length()));
	}

	sb.append("</html>");
	setText(sb.toString());
	return this;
  }

}
