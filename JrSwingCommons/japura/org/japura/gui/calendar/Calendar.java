package org.japura.gui.calendar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.japura.gui.Images;
import org.japura.gui.PopupMenuBuilder;
import org.japura.gui.calendar.components.CalendarButton;
import org.japura.gui.calendar.components.CalendarSlot;
import org.japura.gui.calendar.components.DayOfMonthSlot;
import org.japura.gui.calendar.components.DayOfWeekSlot;
import org.japura.gui.calendar.components.MonthLabel;
import org.japura.gui.calendar.components.TopBar;
import org.japura.gui.calendar.components.WeekHeaderSlot;
import org.japura.gui.calendar.components.WeekSlot;
import org.japura.gui.calendar.components.YearLabel;
import org.japura.gui.event.DateEvent;
import org.japura.gui.event.DateListener;

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
public class Calendar extends JComponent{

  private static final long serialVersionUID = -3245248937315409078L;
  public static final DayOfWeek SUNDAY = DayOfWeek.SUNDAY;
  public static final DayOfWeek MONDAY = DayOfWeek.MONDAY;
  public static final DayOfWeek THURSDAY = DayOfWeek.THURSDAY;
  public static final DayOfWeek WEDNESDAY = DayOfWeek.WEDNESDAY;
  public static final DayOfWeek TUESDAY = DayOfWeek.TUESDAY;
  public static final DayOfWeek FRIDAY = DayOfWeek.FRIDAY;
  public static final DayOfWeek SATURDAY = DayOfWeek.SATURDAY;

  private PropertiesProvider propertiesProvider;

  private HashMap<CalendarComponentType, List<CalendarSlot>> slots;

  private CalendarButton previousMonthButton;
  private CalendarButton previousYearButton;
  private CalendarButton nextMonthButton;
  private CalendarButton nextYearButton;
  private MonthLabel monthLabel;
  private YearLabel yearLabel;

  private TopBar topPanel;
  private JPanel slotsPanel;

  private PopupMenuBuilder<CalendarComponent> popupMenuBuilder;

  private String[] weekNames;
  private String[] monthNames;
  private Long date;
  private boolean weeksEnabled = false;

  boolean dialogMode;
  private JDialog modalDialog;
  Long selectedDialogDate;

  public Calendar() {
	this(null);
  }

  public Calendar(Locale locale) {
	propertiesProvider = new DefaultPropertiesProvider();
	if (locale != null) {
	  setLocale(locale);
	}
	setBorder(BorderFactory.createLineBorder(Color.BLACK));
	setBackground(Color.WHITE);
	setOpaque(true);
	super.setLayout(new BorderLayout());
	add(getTopPanel(), BorderLayout.NORTH);
	add(getSlotsPanel(), BorderLayout.CENTER);
	updateNames();
	slots = new HashMap<CalendarComponentType, List<CalendarSlot>>();
	rebuild();
	setDate(System.currentTimeMillis());
  }

  public void setPropertiesProvider(PropertiesProvider propertiesProvider) {
	if (propertiesProvider == null) {
	  propertiesProvider = new DefaultPropertiesProvider();
	}
	this.propertiesProvider = propertiesProvider;
  }

  public PropertiesProvider getPropertiesProvider() {
	return propertiesProvider;
  }

  @Override
  protected void paintComponent(Graphics g) {
	if (isOpaque()) {
	  g.setColor(getBackground());
	  g.fillRect(0, 0, getWidth(), getHeight());
	}
  }

  public PopupMenuBuilder<CalendarComponent> getPopupMenuBuilder() {
	return popupMenuBuilder;
  }

  public void setPopupMenuBuilder(PopupMenuBuilder<CalendarComponent> popupMenuBuilder) {
	this.popupMenuBuilder = popupMenuBuilder;
  }

  @Override
  public void setEnabled(boolean enabled) {
	getNextYearButton().setEnabled(enabled);
	getNextMonthButton().setEnabled(enabled);
	getPreviousMonthButton().setEnabled(enabled);
	getPreviousYearButton().setEnabled(enabled);
	getMonthLabel().setEnabled(enabled);
	getYearLabel().setEnabled(enabled);
	for (List<CalendarSlot> list : slots.values()) {
	  for (CalendarSlot cs : list) {
		cs.setEnabled(enabled);
	  }
	}
	super.setEnabled(enabled);
  }

  @Override
  public void setLocale(Locale l) {
	super.setLocale(l);
	updateNames();
  }

  @Override
  public Dimension getMinimumSize() {
	return super.getPreferredSize();
  }

  private void updateNames() {
	DateFormatSymbols dfs = new DateFormatSymbols(getLocale());
	weekNames = dfs.getShortWeekdays();
	monthNames = dfs.getMonths();
	Font font = getMonthLabel().getFont();
	FontMetrics fm = getMonthLabel().getFontMetrics(font);
	int width = 0;
	for (String month : monthNames) {
	  width = Math.max(width, fm.stringWidth(month));
	}
	Dimension dim = new Dimension(width, fm.getHeight());
	getMonthLabel().setPreferredSize(dim);
  }

  protected void fireAllListeners(Date oldDate, Date newDate) {
	DateEvent event = new DateEvent(newDate, oldDate);

	DateListener[] listeners = listenerList.getListeners(DateListener.class);
	for (DateListener listener : listeners) {
	  listener.dateChanged(event);
	}
  }

  public void addDateListener(DateListener listener) {
	listenerList.add(DateListener.class, listener);
  }

  public void removeDateListener(DateListener listener) {
	listenerList.remove(DateListener.class, listener);
  }

  @Override
  public final void setLayout(LayoutManager mgr) {}

  private TopBar getTopPanel() {
	if (topPanel == null) {
	  topPanel = new TopBar(this);
	  topPanel.setBackground(getPropertiesProvider().getBackground(topPanel));
	  topPanel.add(getPreviousYearButton());
	  topPanel.add(getPreviousMonthButton());
	  topPanel.add(getMonthLabel());
	  topPanel.add(getYearLabel());
	  topPanel.add(getNextMonthButton());
	  topPanel.add(getNextYearButton());
	}
	return topPanel;
  }

  private JPanel getSlotsPanel() {
	if (slotsPanel == null) {
	  slotsPanel = new JPanel();
	  slotsPanel.setOpaque(false);
	}
	return slotsPanel;
  }

  private CalendarButton getPreviousYearButton() {
	if (previousYearButton == null) {
	  previousYearButton =
		  new CalendarButton(this, CalendarComponentType.PREVIOUS_YEAR_BUTTON);
	  PropertiesProvider pp = getPropertiesProvider();
	  previousYearButton.setForeground(pp.getForeground(previousYearButton));
	  previousYearButton.setDisabledForeground(pp
		  .getDisabledForeground(previousYearButton));
	  previousYearButton.setMouseOverForeground(pp
		  .getMouseOverForeground(previousYearButton));
	  previousYearButton.addMouseListener(new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
		  if (isEnabled()) {
			previousYear();
		  }
		}
	  });
	}
	return previousYearButton;
  }

  private CalendarButton getPreviousMonthButton() {
	if (previousMonthButton == null) {
	  previousMonthButton =
		  new CalendarButton(this, CalendarComponentType.PREVIOUS_MONTH_BUTTON);
	  PropertiesProvider pp = getPropertiesProvider();
	  previousMonthButton.setForeground(pp.getForeground(previousMonthButton));
	  previousMonthButton.setDisabledForeground(pp
		  .getDisabledForeground(previousMonthButton));
	  previousMonthButton.setMouseOverForeground(pp
		  .getMouseOverForeground(previousMonthButton));
	  previousMonthButton.addMouseListener(new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
		  if (isEnabled()) {
			previousMonth();
		  }
		}
	  });
	}
	return previousMonthButton;
  }

  private CalendarButton getNextMonthButton() {
	if (nextMonthButton == null) {
	  nextMonthButton =
		  new CalendarButton(this, CalendarComponentType.NEXT_MONTH_BUTTON);
	  PropertiesProvider pp = getPropertiesProvider();
	  nextMonthButton.setForeground(pp.getForeground(nextMonthButton));
	  nextMonthButton.setDisabledForeground(pp
		  .getDisabledForeground(nextMonthButton));
	  nextMonthButton.setMouseOverForeground(pp
		  .getMouseOverForeground(nextMonthButton));
	  nextMonthButton.addMouseListener(new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
		  if (isEnabled()) {
			nextMonth();
		  }
		}
	  });
	}
	return nextMonthButton;
  }

  private CalendarButton getNextYearButton() {
	if (nextYearButton == null) {
	  nextYearButton =
		  new CalendarButton(this, CalendarComponentType.NEXT_YEAR_BUTTON);
	  PropertiesProvider pp = getPropertiesProvider();
	  nextYearButton.setForeground(pp.getForeground(nextYearButton));
	  nextYearButton.setDisabledForeground(pp
		  .getDisabledForeground(nextYearButton));
	  nextYearButton.setMouseOverForeground(pp
		  .getMouseOverForeground(nextYearButton));
	  nextYearButton.addMouseListener(new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
		  if (isEnabled()) {
			nextYear();
		  }
		}
	  });
	}
	return nextYearButton;
  }

  private MonthLabel getMonthLabel() {
	if (monthLabel == null) {
	  monthLabel = new MonthLabel(this);
	  monthLabel.setForeground(getPropertiesProvider()
		  .getForeground(monthLabel));
	}
	return monthLabel;
  }

  private YearLabel getYearLabel() {
	if (yearLabel == null) {
	  yearLabel = new YearLabel(this);
	  yearLabel.setForeground(getPropertiesProvider().getForeground(yearLabel));
	}
	return yearLabel;
  }

  private boolean isWeeksEnabled() {
	return weeksEnabled;
  }

  private void addSlot(CalendarSlot cs, CalendarComponentType st) {
	List<CalendarSlot> list = slots.get(st);
	if (list == null) {
	  list = new ArrayList<CalendarSlot>();
	  slots.put(st, list);
	}
	list.add(cs);
	getSlotsPanel().add(cs);
  }

  private void rebuild() {
	int columnsTotal = 7;
	if (isWeeksEnabled()) {
	  columnsTotal++;
	}

	getSlotsPanel().removeAll();
	getSlotsPanel().setLayout(new GridLayout(0, columnsTotal, 0, 0));

	slots.clear();

	if (isWeeksEnabled()) {
	  addSlot(new WeekHeaderSlot(this), CalendarComponentType.WEEK_HEADER);
	}
	DayOfWeek week = getPropertiesProvider().getStartDayOfWeek();
	for (int i = 0; i < 7; i++) {
	  String name = weekNames[week.getJUDayOfWeek()];
	  addSlot(new DayOfWeekSlot(this, name, week),
		  CalendarComponentType.DAY_WEEK_HEADER);
	  week = week.nextDayOfWeek();
	}

	for (int i = 0; i < columnsTotal * 6; i++) {
	  if (isWeeksEnabled() && i % 8 == 0) {
		addSlot(new WeekSlot(this), CalendarComponentType.WEEK);
	  } else {
		addSlot(new DayOfMonthSlot(this), CalendarComponentType.DAY_MONTH);
	  }
	}

	getSlotsPanel().revalidate();
  }

  public void update() {
	rebuild();
	updateDate();

	PropertiesProvider pp = getPropertiesProvider();
	
	getPreviousYearButton().setForeground(
		pp.getForeground(getPreviousYearButton()));
	getPreviousMonthButton().setForeground(
		pp.getForeground(getPreviousMonthButton()));
	getNextMonthButton().setForeground(pp.getForeground(getNextMonthButton()));
	getNextYearButton().setForeground(pp.getForeground(getNextYearButton()));
	
	getPreviousYearButton().setDisabledForeground(
    pp.getDisabledForeground(getPreviousYearButton()));
  getPreviousMonthButton().setDisabledForeground(
    pp.getDisabledForeground(getPreviousMonthButton()));
  getNextMonthButton().setDisabledForeground(pp.getDisabledForeground(getNextMonthButton()));
  getNextYearButton().setDisabledForeground(pp.getDisabledForeground(getNextYearButton()));
  
  getPreviousYearButton().setMouseOverForeground(
    pp.getMouseOverForeground(getPreviousYearButton()));
  getPreviousMonthButton().setMouseOverForeground(
    pp.getMouseOverForeground(getPreviousMonthButton()));
  getNextMonthButton().setMouseOverForeground(pp.getMouseOverForeground(getNextMonthButton()));
  getNextYearButton().setMouseOverForeground(pp.getMouseOverForeground(getNextYearButton()));
	
	getMonthLabel().setForeground(pp.getForeground(getMonthLabel()));
	getYearLabel().setForeground(pp.getForeground(getYearLabel()));
	getTopPanel().setBackground(pp.getBackground(getTopPanel()));
	repaint();
  }

  private void updateDate() {
	GregorianCalendar gc = new GregorianCalendar();
	gc.setTimeInMillis(date);
	gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
	gc.set(GregorianCalendar.MINUTE, 0);
	gc.set(GregorianCalendar.SECOND, 0);
	gc.set(GregorianCalendar.MILLISECOND, 0);
	date = gc.getTimeInMillis();

	int currentDay = gc.get(GregorianCalendar.DAY_OF_MONTH);
	int currentMonth = gc.get(GregorianCalendar.MONTH);
	String monthName = monthNames[currentMonth];
	getMonthLabel().setText(monthName);

	int currentYear = gc.get(GregorianCalendar.YEAR);
	getYearLabel().setText(Integer.toString(currentYear));

	gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
	int dayOfWeek = gc.get(GregorianCalendar.DAY_OF_WEEK);
	DayOfWeek week = DayOfWeek.getDayOfWeek(dayOfWeek);

	DayOfWeek s = getPropertiesProvider().getStartDayOfWeek();
	int previous = 0;
	while (s.equals(week) == false) {
	  s = s.nextDayOfWeek();
	  previous++;
	}
	gc.add(GregorianCalendar.DAY_OF_MONTH, (-1) * previous);

	List<CalendarSlot> list = slots.get(CalendarComponentType.DAY_MONTH);
	for (CalendarSlot sl : list) {
	  DayOfMonthSlot dms = (DayOfMonthSlot) sl;
	  int day = gc.get(GregorianCalendar.DAY_OF_MONTH);
	  int month = gc.get(GregorianCalendar.MONTH);
	  int year = gc.get(GregorianCalendar.YEAR);
	  dms.setDate(day, month, year);
	  if (month == currentMonth) {
		dms.setCurrentMonth(true);
		dms.setSelected(day == currentDay);
	  } else {
		dms.setCurrentMonth(false);
		dms.setSelected(false);
	  }
	  gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
	}
	repaint();
  }

  public void previousYear() {
	GregorianCalendar gc = new GregorianCalendar();
	gc.setTimeInMillis(date);
	gc.add(GregorianCalendar.YEAR, -1);
	setDate(gc.getTimeInMillis());
  }

  public void nextYear() {
	GregorianCalendar gc = new GregorianCalendar();
	gc.setTimeInMillis(date);
	gc.add(GregorianCalendar.YEAR, 1);
	setDate(gc.getTimeInMillis());
  }

  public void nextMonth() {
	GregorianCalendar gc = new GregorianCalendar();
	gc.setTimeInMillis(date);
	gc.add(GregorianCalendar.MONTH, 1);
	setDate(gc.getTimeInMillis());
  }

  public void previousMonth() {
	GregorianCalendar gc = new GregorianCalendar();
	gc.setTimeInMillis(date);
	gc.add(GregorianCalendar.MONTH, -1);
	setDate(gc.getTimeInMillis());
  }

  public DayOfWeek getDayOfWeek(CalendarComponent cc) {
	if (cc instanceof DayOfMonthSlot) {
	  DayOfMonthSlot slot = (DayOfMonthSlot) cc;
	  return slot.getDayOfWeek();
	}
	if (cc instanceof DayOfWeekSlot) {
	  DayOfWeekSlot slot = (DayOfWeekSlot) cc;
	  return slot.getDayOfWeek();
	}
	return null;
  }

  public boolean isSelected(CalendarComponent cc) {
	if (cc instanceof DayOfMonthSlot) {
	  DayOfMonthSlot slot = (DayOfMonthSlot) cc;
	  return slot.isSelected();
	}
	return false;
  }

  public boolean isCurrentMonth(CalendarComponent cc) {
	if (cc instanceof DayOfMonthSlot) {
	  DayOfMonthSlot slot = (DayOfMonthSlot) cc;
	  return slot.isCurrentMonth();
	}
	return false;
  }

  public Integer getYear(CalendarComponent cc) {
	if (cc instanceof DayOfMonthSlot) {
	  DayOfMonthSlot slot = (DayOfMonthSlot) cc;
	  return slot.getYear();
	}
	return null;
  }

  public Integer getMonth(CalendarComponent cc) {
	if (cc instanceof DayOfMonthSlot) {
	  DayOfMonthSlot slot = (DayOfMonthSlot) cc;
	  return slot.getMonth();
	}
	return null;
  }

  public Integer getDay(CalendarComponent cc) {
	if (cc instanceof DayOfMonthSlot) {
	  DayOfMonthSlot slot = (DayOfMonthSlot) cc;
	  return slot.getDay();
	}
	return null;
  }

  public Date getDate(CalendarComponent cc) {
	if (cc instanceof DayOfMonthSlot) {
	  DayOfMonthSlot slot = (DayOfMonthSlot) cc;
	  return slot.getDate();
	}
	return null;
  }

  public void setDate(long date) {
	Date oldDate = null;
	if (this.date != null) {
	  oldDate = new Date(this.date);
	}
	Date newDate = new Date(date);
	this.date = date;
	updateDate();
	fireAllListeners(oldDate, newDate);
  }

  public Date getDate() {
	return new Date(date);
  }

  public boolean isDialogMode() {
	return dialogMode;
  }

  public void disposeDialog(long selectedTime) {
	if (isDialogMode()) {
	  this.selectedDialogDate = selectedTime;
	  getModalDialog().dispose();
	}
  }

  JDialog getModalDialog() {
	if (modalDialog == null) {
	  modalDialog = new JDialog();
	  ImageIcon ii = new ImageIcon(Images.CALENDAR);
	  modalDialog.setIconImage(ii.getImage());
	  modalDialog.add(this);
	  modalDialog.pack();
	  modalDialog.setResizable(false);
	  modalDialog.setModal(true);
	}
	return modalDialog;
  }

  public static Long showAsDialog(Locale locale, long date, String dialogTitle,
								  Component relativeComponent) {
	return showAsDialog(locale, date, dialogTitle, relativeComponent, null);
  }

  public static Long showAsDialog(Locale locale, long date, String dialogTitle,
								  Component relativeComponent,
								  PropertiesProvider propertiesProvider) {
	Calendar calendar = new Calendar(locale);
	calendar.setDate(date);
	if (propertiesProvider != null) {
	  calendar.setPropertiesProvider(propertiesProvider);
	}
	calendar.dialogMode = true;
	calendar.getModalDialog().setTitle(dialogTitle);
	calendar.getModalDialog().setLocationRelativeTo(relativeComponent);
	calendar.getModalDialog().setVisible(true);
	return calendar.selectedDialogDate;
  }

}
