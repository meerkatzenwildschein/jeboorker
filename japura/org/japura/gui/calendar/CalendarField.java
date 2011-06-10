package org.japura.gui.calendar;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.japura.gui.I18nStringKeys;
import org.japura.gui.Images;
import org.japura.gui.model.DateDocument;
import org.japura.i18n.I18nManager;

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
public class CalendarField extends JComponent{

  private static final long serialVersionUID = -3037260699569201200L;
  private DateDocument dateDocument;
  private JTextField textField;
  private JLabel calendarButton;
  private String calendarWindowTitle;
  private PropertiesProvider propertiesProvider;

  public CalendarField() {
	this(new DateDocument());
  }

  public CalendarField(DateDocument dateDocument) {
	if (dateDocument == null) {
	  dateDocument = new DateDocument();
	}
	setDateDocument(dateDocument);
	setLayout(new BorderLayout(3, 0));
	add(getTextField(), BorderLayout.CENTER);
	add(getCalendarButton(), BorderLayout.EAST);
  }

  public PropertiesProvider getPropertiesProvider() {
	return propertiesProvider;
  }

  public void setPropertiesProvider(PropertiesProvider propertiesProvider) {
	this.propertiesProvider = propertiesProvider;
  }

  public void setCalendarButtonIcon(URL urlImage) {
	ImageIcon ii = new ImageIcon(urlImage);
	getCalendarButton().setIcon(ii);
  }

  @Override
  protected void paintComponent(Graphics g) {
	if (isOpaque()) {
	  g.setColor(getBackground());
	  g.fillRect(0, 0, getWidth(), getHeight());
	}
  }

  public void setDateDocument(DateDocument dateDocument) {
	if (dateDocument != null) {
	  this.dateDocument = dateDocument;
	  setLocale(dateDocument.getLocale());
	}
  }

  public DateDocument getDateDocument() {
	return dateDocument;
  }

  @Override
  public void setEnabled(boolean enabled) {
	super.setEnabled(enabled);
	getTextField().setEnabled(enabled);
	getCalendarButton().setEnabled(false);
  }

  protected JTextField getTextField() {
	if (textField == null) {
	  textField = new JTextField(8);
	  textField.setDocument(getDateDocument());
	}
	return textField;
  }

  public void setCalendarWindowTitle(String calendarWindowTitle) {
	this.calendarWindowTitle = calendarWindowTitle;
  }

  public String getCalendarWindowTitle() {
	return calendarWindowTitle;
  }

  private JLabel getCalendarButton() {
	if (calendarButton == null) {
	  calendarButton = new JLabel();
	  URL url = Images.CALENDAR;
	  ImageIcon ii = new ImageIcon(url);
	  calendarButton.setIcon(ii);
	  calendarButton.addMouseListener(new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
		  if (isEnabled() == false) {
			return;
		  }
		  Date date = getDateDocument().getDate();
		  if (date == null) {
			date = new Date(System.currentTimeMillis());
		  }
		  String title = getCalendarWindowTitle();
		  if (title == null) {
			title =
				I18nManager.getString(I18nStringKeys.CALENDAR_WINDOW_TITLE
					.getKey());
		  }
		  Long newDate =
			  Calendar.showAsDialog(getDateDocument().getLocale(),
				  date.getTime(), title, CalendarField.this,
				  getPropertiesProvider());
		  if (newDate != null) {
			getDateDocument().setDate(newDate);
		  }
		}
	  });
	}
	return calendarButton;
  }

}
