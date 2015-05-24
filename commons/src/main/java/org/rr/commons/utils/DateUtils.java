package org.rr.commons.utils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils implements Serializable {
	
	private static final long serialVersionUID = -8094026773415416927L;

	private DateUtils() {};

	/**
	 * The given value will be added to the specified interval of the date
	 * 
	 * @param interval
	 *            The part of the date which has to be added:
	 *            <li>"yyyy" for year</li>
	 *            <li>"m" for month</li>
	 *            <li>"q" for quarter</li>
	 *            <li>"d" for day</li>
	 *            <li>"w" for weekday</li>
	 *            <li>"ww" for week of Year</li>
	 *            <li>"y" for day of year</li>
	 *            <li>"h" for hour</li>
	 *            <li>"m" for minute</li>
	 *            <li>"s" for second</li>
	 * 
	 * <pre>
	 * In this method, &quot;d&quot;,&quot;w&quot; and &quot;y&quot; are the same,
	 * they only add one day to the date,
	 * &quot;ww&quot; adds one full week(7 days).
	 * </pre>
	 * 
	 * @param value
	 *            The value which has to be added to the interval
	 * @param date
	 *            The date which will be changed
	 * @return The changed date
	 */
	public static Date dateAdd(final String interval, final int value, final Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
		dateFormat.format(date);
		Calendar calendar = dateFormat.getCalendar();
		if (interval.equals("yyyy")) {
			calendar.add(Calendar.YEAR, value);
		} else if (interval.equals("m")) {
			calendar.add(Calendar.MONTH, value);
		} else if (interval.equals("q")) {
			calendar.add(Calendar.MONTH, value * UtilConstants.MULTIPLICATION_TO_QUARTER);
		} else if (interval.equals("d") || interval.equals("w") || interval.equals("y")) {
			calendar.add(Calendar.DAY_OF_MONTH, value);
		} else if (interval.equals("ww")) {
			calendar.add(Calendar.WEEK_OF_YEAR, value);
		} else if (interval.equals("h")) {
			calendar.add(Calendar.HOUR, value);
		} else if (interval.equals("n")) {
			calendar.add(Calendar.MINUTE, value);
		} else if (interval.equals("s")) {
			calendar.add(Calendar.SECOND, value);
		}

		return calendar.getTime();
	}

	/**
	 * The method calculates the difference of the two dates for one part of the
	 * date
	 * 
	 * @param interval
	 *            The part of the two dates, of which the difference will be
	 *            calculated:
	 *            <li>"yyyy" for year</li>
	 *            <li>"m" for month</li>
	 *            <li>"q" for quarter</li>
	 *            <li>"d" for day</li>
	 *            <li>"w" for full week</li>
	 *            <li>"ww" for week of Year</li>
	 *            <li>"y" for day of year</li>
	 *            <li>"h" for hour</li>
	 *            <li>"m" for minute</li>
	 *            <li>"s" for second</li>
	 * 
	 * <pre>
	 *                                            In this method &quot;w&quot; shows how many FULL weeks are completed,
	 *                                                        &quot;ww&quot; shows how often the first day of the week is contained,
	 *                                                        &quot;d&quot; and &quot;y&quot; are the same, they show the difference of days.
	 * </pre>
	 * 
	 * @param date1
	 *            The beginning date
	 * @param date2
	 *            The ending date
	 * @param firstDayOfWeek Specifies what the first day of the week is; e.g., <code>SUNDAY</code> in the U.S.,
     * 		<code>MONDAY</code> in Germany.
	 *            <li>Calendar.SUNDAY</li>
	 *            <li>Calendar.MONDAY</li>
	 *            <li>Calendar.TUESDAY</li>
	 *            <li>Calendar.WEDNESDAY</li>
	 *            <li>Calendar.THURSDAY</li>
	 *            <li>Calendar.FRIDAY</li>
	 *            <li>Calendar.SATURDAY</li>
	 * @param firstWeekOfYear
	 *            The week beginning the year; parameters could be:
	 *            <li>FIRSTJAN1, the first week of the year is the week
	 *            in which January the 1st occurs</li>
	 *            <li>FIRSTFOURDAYS, the beginning week is the first
	 *            week with at least four days</li>
	 *            <li>FIRSTFULLWEEK, the beginning week is the first
	 *            full week</li>
	 * @return The difference of the two dates for the specified interval
	 * @throws ParseException
	 */
	public static long dateDiff(final String interval, final Date date1, final Date date2, final int firstDayOfWeek, final int firstWeekOfYear) throws ParseException {
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss z");
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss z");
		boolean negative = false;
		boolean quarter = false;
		boolean goInLoop = false;
		if (date1.before(date2)) {
			dateFormat1.format(date1);
			dateFormat2.format(date2);
		} else {
			dateFormat1.format(date2);
			dateFormat2.format(date1);
			negative = true;
		}

		Calendar calendar1 = dateFormat1.getCalendar();
		Calendar calendar2 = dateFormat2.getCalendar();
		int year1 = calendar1.get(Calendar.YEAR);
		int year2 = calendar2.get(Calendar.YEAR);
		int month1 = calendar1.get(Calendar.MONTH) + 1;
		int month2 = calendar2.get(Calendar.MONTH) + 1;
		double milliseconds1 = calendar1.getTimeInMillis();
		double milliseconds2 = calendar2.getTimeInMillis();
		double diffMilliseconds = milliseconds2 - milliseconds1;
		double diffSeconds = diffMilliseconds / UtilConstants.DIVISION_TO_SECOND;
		double diffMinutes = diffSeconds / UtilConstants.DIVISION_TO_MINUTE;
		double diffHours = diffMinutes / UtilConstants.DIVISION_TO_HOUR;
		double diffDays = diffHours / UtilConstants.DIVISION_TO_DAY;

		long diffResult = 0;
		int intervalNumber = 0;
		String specifiedStringForDateFormat = null;
		String specifiedStringForParsing = null;
		String specifiedStringForParsing2 = null;

		if (interval.equals("yyyy")) {
			intervalNumber = Calendar.YEAR;
			specifiedStringForDateFormat = "yyyy";
			specifiedStringForParsing = String.valueOf(year1);
			specifiedStringForParsing2 = String.valueOf(year2);
			goInLoop = true;
		} else if (interval.equals("m")) {
			intervalNumber = Calendar.MONTH;
			specifiedStringForDateFormat = "MM.yyyy";
			specifiedStringForParsing = month1 + "." + year1;
			specifiedStringForParsing2 = month2 + "." + year2;
			goInLoop = true;
		} else if (interval.equals("d") || interval.equals("y")) {
			diffResult =  (long) MathUtils.roundUp(diffDays, 0);
		} else if (interval.equals("h")) {
			diffResult =  (long) MathUtils.roundUp(diffHours, 0);
		} else if (interval.equals("n")) {
			diffResult =  (long) MathUtils.roundUp(diffMinutes, 0);
		} else if (interval.equals("s")) {
			diffResult =  (long) MathUtils.roundUp(diffSeconds, 0);
		} else if (interval.equals("w")) {
			diffResult =  (long) (diffDays / UtilConstants.DIVISION_TO_WEEK);
		} else if (interval.equals("ww")) {
			if (date1.before(date2)) {
				diffResult = getNumbersOfDays(date1, date2, firstDayOfWeek, calendar1);
			} else {
				diffResult = getNumbersOfDays(date2, date1, firstDayOfWeek, calendar1);
			}
			goInLoop = false;
		} else if (interval.equals("q")) {
			intervalNumber = Calendar.MONTH;
			specifiedStringForDateFormat = "MM.yyyy";
			specifiedStringForParsing = month1 + "." + year1;
			specifiedStringForParsing2 = month2 + "." + year2;
			quarter = true;
		}
		if (goInLoop) {
			SimpleDateFormat dateFormatSpeciefied1 = new SimpleDateFormat(specifiedStringForDateFormat);
			SimpleDateFormat dateFormatSpeciefied2 = new SimpleDateFormat(specifiedStringForDateFormat);
			Date specifiedDate1 = dateFormatSpeciefied1.parse(specifiedStringForParsing);
			Date specifiedDate2 = dateFormatSpeciefied2.parse(specifiedStringForParsing2);
			dateFormatSpeciefied1.format(specifiedDate1);
			dateFormatSpeciefied2.format(specifiedDate2);
			Calendar specifiedCalendar1 = dateFormatSpeciefied1.getCalendar();
			Calendar specifiedCalendar2 = dateFormatSpeciefied2.getCalendar();

			int j = 0;
			int quarters = 0;

			while (specifiedCalendar1.before(specifiedCalendar2)) {
				int oldmonth = specifiedCalendar1.get(Calendar.MONTH);

				specifiedCalendar1.add(intervalNumber, 1);
				j++;
				if (quarter(oldmonth) != (quarter(specifiedCalendar1.get(Calendar.MONTH)))) {
					quarters++;
				}

				if (quarter) {
					diffResult = quarters;
				} else {
					diffResult = j;
				}

			}
		}

		if (negative) {
			return -diffResult;
		} else {
			return diffResult;
		}

	}

	/**
	 * This method returns the appendant quarter of a month
	 * 
	 * @param month
	 *            This is the month of which the Method will return the quarter
	 * @return The quarter of the month
	 */
	private static int quarter(final int month) {
		int quarter = 0;
		if (month == Calendar.JANUARY || month == Calendar.FEBRUARY || month == Calendar.MARCH) {
			quarter = UtilConstants.FIRST_QUARTER;
		} else if (month == Calendar.APRIL || month == Calendar.MAY || month == Calendar.JUNE) {
			quarter = UtilConstants.SECOND_QUARTER;
		} else if (month == Calendar.JULY || month == Calendar.AUGUST || month == Calendar.SEPTEMBER) {
			quarter = UtilConstants.THIRD_QUARTER;
		} else if (month == Calendar.OCTOBER || month == Calendar.NOVEMBER || month == Calendar.DECEMBER) {
			quarter = UtilConstants.FOURTH_QUARTER;
		}
		return quarter;
	}
	
	/**
	 * Determines the quarter for the given <code>Date</code>.
	 * @param date The date  for that the quarter should be determined.
	 * @return The quarter for the given date. 1 = the first quarter and 4 = the fourth quarter.
	 */
	public static int quarter(final Date date) {
		return quarter( Integer.parseInt(new SimpleDateFormat("M").format(date)) );
	}

	/**
	 * This method will return the number of days (which is given as first day
	 * of week) in the period between the 2 dates
	 * 
	 * @param date1
	 *            The beginning date
	 * @param date2
	 *            The ending date
	 * @param firstDayOfWeek Specifies what the first day of the week is; e.g., <code>SUNDAY</code> in the U.S.,
     * 		<code>MONDAY</code> in Germany.
	 *            <li>Calendar.SUNDAY</li>
	 *            <li>Calendar.MONDAY</li>
	 *            <li>Calendar.TUESDAY</li>
	 *            <li>Calendar.WEDNESDAY</li>
	 *            <li>Calendar.THURSDAY</li>
	 *            <li>Calendar.FRIDAY</li>
	 *            <li>Calendar.SATURDAY</li>
	 * @param calendar1
	 *            The calendar of the beginning date
	 * @param diffResult
	 *            The difference of the two dates
	 * 
	 * @return The number of days which are the same like the firstdayofweek in
	 *         the difference
	 * @throws ParseException
	 */
	private static int getNumbersOfDays(Date date1, final Date date2, final int firstDayOfWeek, final Calendar calendar1) throws ParseException {
		int numberOfDays = 0;

		while (date1.before(date2)) {
			SimpleDateFormat format = new SimpleDateFormat("E", Locale.US);

			int actualDay = calendar1.get(Calendar.DAY_OF_YEAR);
			calendar1.set(Calendar.DAY_OF_YEAR, actualDay + UtilConstants.ONE_DAY);
			date1 = calendar1.getTime();
			String actualDayString = format.format(date1);

			boolean firstdayofweekFound = false;
			if (firstDayOfWeek == Calendar.SUNDAY && actualDayString.equals("Sun")) {
				firstdayofweekFound = true;
			} else if (firstDayOfWeek == Calendar.MONDAY && actualDayString.equals("Mon")) {
				firstdayofweekFound = true;
			} else if (firstDayOfWeek == Calendar.TUESDAY && actualDayString.equals("Tue")) {
				firstdayofweekFound = true;
			} else if (firstDayOfWeek == Calendar.WEDNESDAY && actualDayString.equals("Wed")) {
				firstdayofweekFound = true;
			} else if (firstDayOfWeek == Calendar.THURSDAY && actualDayString.equals("Thu")) {
				firstdayofweekFound = true;
			} else if (firstDayOfWeek == Calendar.FRIDAY && actualDayString.equals("Fri")) {
				firstdayofweekFound = true;
			} else if (firstDayOfWeek == Calendar.SATURDAY && actualDayString.equals("Sat")) {
				firstdayofweekFound = true;
			}

			if (firstdayofweekFound) {
				while (!date1.after(date2)) {
					actualDay = calendar1.get(Calendar.DAY_OF_YEAR);
					calendar1.set(Calendar.DAY_OF_YEAR, actualDay + UtilConstants.ONE_WEEK);
					date1 = calendar1.getTime();
					actualDayString = format.format(date1);
					numberOfDays++;
				}
			}

		}
		return numberOfDays;
	}

	/**
	 * The method returns the part of the date which is given as interval
	 * 
	 * @param interval
	 *            The part of the date which will be shown:
	 *            <li>"yyyy" for year</li>
	 *            <li>"m" for month</li>
	 *            <li>"q" for quarter</li>
	 *            <li>"d" for day</li>
	 *            <li>"w" for weekday</li>
	 *            <li>"ww" for week of Year</li>
	 *            <li>"y" for day of year</li>
	 *            <li>"h" for hour</li>
	 *            <li>"m" for minute</li>
	 *            <li>"s" for second</li>
	 * 
	 * <pre>
	 *                                            In this method &quot;d&quot; and &quot;y&quot; are NOT the same,
	 *                                                        &quot;d&quot; gives the day in the month, &quot;y&quot; the day in the year, 
	 *                                                        &quot;w&quot; gives the number of day in the week, dependent on the first day of week,
	 *                                                        &quot;ww&quot; gives the number of week in the year, dependent on the first day of week 
	 *                                                        and the first week of the year.
	 * </pre>
	 * 
	 * @param date
	 *            The date from which you want to take a part
	 * @param firstDayOfWeek Specifies what the first day of the week is; e.g., <code>SUNDAY</code> in the U.S.,
     * 		<code>MONDAY</code> in Germany.
	 *            <li>Calendar.SUNDAY</li>
	 *            <li>Calendar.MONDAY</li>
	 *            <li>Calendar.TUESDAY</li>
	 *            <li>Calendar.WEDNESDAY</li>
	 *            <li>Calendar.THURSDAY</li>
	 *            <li>Calendar.FRIDAY</li>
	 *            <li>Calendar.SATURDAY</li>
	 * @param firstWeekOfYear 
	 *            The week beginning the year; parameters could be:
	 *            <li>{@link UtilConstants#FIRSTJAN1}, the first week of the year is the week
	 *            in which January the 1st occurs</li>
	 *            <li>{@link UtilConstants#FIRSTFOURDAYS}, the beginning week is the first
	 *            week with at least four days</li>
	 *            <li>{@link UtilConstants#FIRSTFULLWEEK}, the beginning week is the first
	 *            full week</li>
	 * @return The part of the day which is chosen in the interval.
	 * @throws ParseException
	 */
	public static int datePart(final String interval, final Date date, final int firstDayOfWeek, final int firstWeekOfYear) throws ParseException {
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
		dateFormat1.format(date);
		Calendar calendar = dateFormat1.getCalendar();

		int part = 0;

		if (interval.equals("yyyy")) {
			int year = calendar.get(Calendar.YEAR);
			part = year;
		} else if (interval.equals("m")) {
			int month = calendar.get(Calendar.MONTH) + 1;
			part = month;
		} else if (interval.equals("q")) {
			int quarter = 0;
			if (calendar.get(Calendar.MONTH) == Calendar.JANUARY | calendar.get(Calendar.MONTH) == Calendar.FEBRUARY
					|| calendar.get(Calendar.MONTH) == Calendar.MARCH) {
				quarter = 1;
			} else if (calendar.get(Calendar.MONTH) == Calendar.APRIL || calendar.get(Calendar.MONTH) == Calendar.MAY
					|| calendar.get(Calendar.MONTH) == Calendar.JUNE) {
				quarter = 2;
			} else if (calendar.get(Calendar.MONTH) == Calendar.JULY || calendar.get(Calendar.MONTH) == Calendar.AUGUST
					|| calendar.get(Calendar.MONTH) == Calendar.SEPTEMBER) {
				quarter = 3;
			} else if (calendar.get(Calendar.MONTH) == Calendar.OCTOBER || calendar.get(Calendar.MONTH) == Calendar.NOVEMBER
					|| calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
				quarter = 4;
			}
			part = quarter;
		} else if (interval.equals("d")) {
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			part = day;
		} else if (interval.equals("y")) {
			int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
			part = dayOfYear;
		} else if (interval.equals("h")) {
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			part = hour;
		} else if (interval.equals("n")) {
			int minute = calendar.get(Calendar.MINUTE);
			part = minute;
		} else if (interval.equals("s")) {
			int second = calendar.get(Calendar.SECOND);
			part = second;
		} else if (interval.equals("w")) {

			// calculate the number of the day in the week
			// it is dependent on the first day of the week
			// the algorithmus starts with the first day of the week
			// and ends with the day in the date

			calendar.setFirstDayOfWeek(firstDayOfWeek);
			calendar.setMinimalDaysInFirstWeek(UtilConstants.FIRSTJAN1);
			int dayinWeek = calendar.get(Calendar.DAY_OF_WEEK);
			part = getWeekday(firstDayOfWeek,dayinWeek);

			
		} else if (interval.equals("ww")) {
			calendar.setFirstDayOfWeek(firstDayOfWeek);

			// java and access don�t have the same parameters for
			// fist week of the year
			// thats why this algorithmus has to convert this

			int javaFirstWeek = 1;
			if (firstWeekOfYear == UtilConstants.FIRSTJAN1) {
				javaFirstWeek = 1;
			} else if (firstWeekOfYear == UtilConstants.FIRSTFOURDAYS) {
				javaFirstWeek = 4;
			} else if (firstWeekOfYear == UtilConstants.FIRSTFULLWEEK) {
				javaFirstWeek = 7;
			}
			calendar.setMinimalDaysInFirstWeek(javaFirstWeek);
			int weekInYear = calendar.get(Calendar.WEEK_OF_YEAR);
			part = weekInYear;
		}

		return part;
	}

	/**
	 * The Method returns a date with the specified year, month and day
	 * 
	 * @param year
	 *            The year in the new date
	 * @param month
	 *            The month in the new year
	 * @param day
	 *            The day in the new year
	 * @return The new date with the assigned year, month and day
	 * @throws ParseException
	 */
	public static Date dateSerial(final int year, final int month, final int day) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		Date date = dateFormat.parse(day + "." + month + "." + year);
		return date;
	}

	/**
	 * The method returns the year of the date
	 * 
	 * @param date
	 *            The date from which you want to take the year
	 * @return The year of the date
	 */
	public static int year(final Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy");

		return Integer.parseInt(formatter.format(date));
	}

	/**
	 * The method returns the month of the date
	 * 
	 * @param date
	 *            The date from which you want to take the month
	 * @return The month of the date
	 */
	public static int month(final Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM");

		return Integer.parseInt(formatter.format(date));
	}

	/**
	 * The method returns the day of the date
	 * 
	 * @param date
	 *            The date from which you want to take the day
	 * @return The day for the given date.
	 */
	public static int day(final Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd");

		return Integer.parseInt(formatter.format(date));
	}

	/**
	 * The method returns the hour of the date
	 * 
	 * @param date
	 *            The date from which you want to take the hour
	 * @return The hour of the date
	 */
	public static int hour(final Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH");

		return Integer.parseInt(formatter.format(date));
	}

	/**
	 * The method returns the minute of the date
	 * 
	 * @param date
	 *            The date from which you want to take the minute
	 * @return The minute of the date
	 */
	public static int minute(final Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("mm");

		return Integer.parseInt(formatter.format(date));
	}

	/**
	 * The method returns the second of the date
	 * 
	 * @param date
	 *            The date from wich you want to take the second
	 * @return The second of the date
	 */
	public static int second(final Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("ss");

		return Integer.parseInt(formatter.format(date));
	}
	
	
	/**
	 * Gets the localized name of a month
	 * 
	 * @param month The number of month of which the method will return the name
	 * @param abbreviate If true, the method returns the abbreviate.<br>
	 * If false, the method returns the full name of the month.
	 * @return The name or abbreviate of the month<br>
	 * The display is localized and dependent on the language settings in the system control
	 */
	public static String monthName(final int month, final boolean abbreviate){
		SimpleDateFormat dateFormat=null;
		if(abbreviate){
			 dateFormat=new SimpleDateFormat("MMM");
		}else{
			 dateFormat=new SimpleDateFormat("MMMM");
		}
		
		Calendar calendar=dateFormat.getCalendar();
		calendar.set(2000, month-1, 1, 0, 0, 0);
		
		String monthName=dateFormat.format(calendar.getTime());
		
		return monthName;
	}

	
	/**
	 * The method gets the number of a day
	 * 
	 * @param date The date of which you want to know the weekday
	 * @param firstDayOfWeek Specifies what the first day of the week is; e.g., <code>SUNDAY</code> in the U.S.,
     * 		<code>MONDAY</code> in Germany.
	 *            <li>Calendar.SUNDAY</li>
	 *            <li>Calendar.MONDAY</li>
	 *            <li>Calendar.TUESDAY</li>
	 *            <li>Calendar.WEDNESDAY</li>
	 *            <li>Calendar.THURSDAY</li>
	 *            <li>Calendar.FRIDAY</li>
	 *            <li>Calendar.SATURDAY</li>
	 * @return The number of the day in the week
	 */
	public static int weekday(final Date date, final int firstDayOfWeek){
		SimpleDateFormat dateFormat=new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		
		dateFormat.format(date);
		
		Calendar calendar=dateFormat.getCalendar();
		calendar.setFirstDayOfWeek(firstDayOfWeek);
		calendar.setMinimalDaysInFirstWeek(UtilConstants.FIRSTJAN1);
		
		int weekday=calendar.get(Calendar.DAY_OF_WEEK);

		int i = getWeekday(firstDayOfWeek, weekday);
		
		return i;
	}

	
	/**
	 * This method gets the number of a day
	 * 
	 * @param firstDayOfWeek Specifies what the first day of the week is; e.g., <code>SUNDAY</code> in the U.S.,
     * 		<code>MONDAY</code> in Germany.
	 *            <li>Calendar.SUNDAY</li>
	 *            <li>Calendar.MONDAY</li>
	 *            <li>Calendar.TUESDAY</li>
	 *            <li>Calendar.WEDNESDAY</li>
	 *            <li>Calendar.THURSDAY</li>
	 *            <li>Calendar.FRIDAY</li>
	 *            <li>Calendar.SATURDAY</li>
	 * @param weekday The day of which the method will return the number in the week
	 * @return The number of the day in the week
	 */
	private static int getWeekday(int firstDayOfWeek, final int weekday) {
		int i = 1;
		while (firstDayOfWeek != weekday) {
			if (firstDayOfWeek >= Calendar.SATURDAY) {
				firstDayOfWeek = Calendar.SUNDAY - 1;
			}
			firstDayOfWeek++;
			i++;

		}
		return i;
	}
	
	/**
	 * Calculates the calendar week for the given <code>date</code>. 
	 * 
	 * @param date The date for the calculation of the calendar week.
	 * @param firstDayOfWeek Specifies what the first day of the week is; e.g., <code>SUNDAY</code> in the U.S.,
     * 		<code>MONDAY</code> in Germany.
	 *            <li>Calendar.SUNDAY</li>
	 *            <li>Calendar.MONDAY</li>
	 *            <li>Calendar.TUESDAY</li>
	 *            <li>Calendar.WEDNESDAY</li>
	 *            <li>Calendar.THURSDAY</li>
	 *            <li>Calendar.FRIDAY</li>
	 *            <li>Calendar.SATURDAY</li>
	 * @return The calendar week for the given <code>date</code>.
	 */
	public static int calendarWeek(final Date date, final int firstDayOfWeek) {
		final Calendar calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(firstDayOfWeek);
		calendar.setTime(date);
		return calendar.get(Calendar.WEEK_OF_YEAR);
	}
	
	/**
	 * Gets the date for a day in the specified <code>calendarWeek</code>.  
	 * <BR><BR>
	 * 
	 * <pre>
	 * The example returns the date for the monday of the first calendar week in
	 * the year 2008:
	 * <code>
	 * dayOfCalendarWeek(1, Calendar.MONDAY, 2008, Calendar.SUNDAY)
	 * </code></pre>
	 * 
	 * @param calendarWeek Specifies the clandear week where the day should be located.
	 * @param day Specifies the desired day of the week.
	 *            <li>Calendar.SUNDAY</li>
	 *            <li>Calendar.MONDAY</li>
	 *            <li>Calendar.TUESDAY</li>
	 *            <li>Calendar.WEDNESDAY</li>
	 *            <li>Calendar.THURSDAY</li>
	 *            <li>Calendar.FRIDAY</li>
	 *            <li>Calendar.SATURDAY</li>
	 * @param year The year of the desired calandar week. 
	 * @param firstDayOfWeek Specifies what the first day of the week is; e.g., <code>SUNDAY</code> in the U.S.,
     * 		<code>MONDAY</code> in Germany.
	 *            <li>Calendar.SUNDAY</li>
	 *            <li>Calendar.MONDAY</li>
	 *            <li>Calendar.TUESDAY</li>
	 *            <li>Calendar.WEDNESDAY</li>
	 *            <li>Calendar.THURSDAY</li>
	 *            <li>Calendar.FRIDAY</li>
	 *            <li>Calendar.SATURDAY</li>
	 * @return The date for the given values.
	 */
	public static Date dayOfCalendarWeek(final int calendarWeek, final int day, final int year, final int firstDayOfWeek) {
		final Calendar calendar = Calendar.getInstance(); 
		calendar.set( Calendar.WEEK_OF_YEAR, calendarWeek ); 
		calendar.set( Calendar.DAY_OF_WEEK, day );
		calendar.set( Calendar.YEAR, year);
		calendar.setFirstDayOfWeek(firstDayOfWeek);
		return calendar.getTime();
	}
	
	/**
	 * Calculates the date for a desired day in a week given with the <code>date</code> parameter.
	 * <BR><BR>
	 * <pre>
	 * The example returns the date for the monday of the week containing the 5.1.2008.
	 * <code>
	 * dayofweek("5.1.2008", Calendar.MONDAY, Calendar.MONDAY)
	 * </code>
	 *  
	 * @param date The date which specifies the week range where the desired day should be located.
	 * @param day The day of the week which was specified with the <code>date</code> parameter.
	 *            <li>Calendar.SUNDAY</li>
	 *            <li>Calendar.MONDAY</li>
	 *            <li>Calendar.TUESDAY</li>
	 *            <li>Calendar.WEDNESDAY</li>
	 *            <li>Calendar.THURSDAY</li>
	 *            <li>Calendar.FRIDAY</li>
	 *            <li>Calendar.SATURDAY</li>
	 * @param firstDayOfWeek Specifies what the first day of the week is; e.g., <code>SUNDAY</code> in the U.S.,
     * 		<code>MONDAY</code> in Germany.
	 *            <li>Calendar.SUNDAY</li>
	 *            <li>Calendar.MONDAY</li>
	 *            <li>Calendar.TUESDAY</li>
	 *            <li>Calendar.WEDNESDAY</li>
	 *            <li>Calendar.THURSDAY</li>
	 *            <li>Calendar.FRIDAY</li>
	 *            <li>Calendar.SATURDAY</li>
	 * @return The date for the given values.
	 */
	public static Date dayOfWeek(final Date date, final int day, final int firstDayOfWeek) {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set( Calendar.DAY_OF_WEEK, day );
		calendar.setFirstDayOfWeek(firstDayOfWeek);
		return calendar.getTime();
	}
	
	/**
	 * The method returns the name of a day
	 * 
	 * @param weekday The number of the day of which the method will return the name
	 * @param abbreviate If true, the method returns the abbreviate.<br>
	 * If false, the method returns the full name of the day.
	 * @param firstDayOfWeek
	 *            <li>Calendar.SUNDAY</li>
	 *            <li>Calendar.MONDAY</li>
	 *            <li>Calendar.TUESDAY</li>
	 *            <li>Calendar.WEDNESDAY</li>
	 *            <li>Calendar.THURSDAY</li>
	 *            <li>Calendar.FRIDAY</li>
	 *            <li>Calendar.SATURDAY</li>
	 * @return The name or abbreviate of the day
	 * The display is localized and dependent on the language settings in the system control
	 */
	public static String weekdayName(final int weekday, final boolean abbreviate, final int firstDayOfWeek){
		SimpleDateFormat dateFormat=null;
		
		if(abbreviate){
			 dateFormat = new SimpleDateFormat("E");
		} else {
			 dateFormat = new SimpleDateFormat("EEEE");
		}
		Calendar calendar = dateFormat.getCalendar();
		
		calendar.set(Calendar.DAY_OF_WEEK,firstDayOfWeek);
		calendar.add(Calendar.DAY_OF_WEEK,weekday-1);
		
		String weekdayName = dateFormat.format(calendar.getTime());
		
		return weekdayName;
	}
	
	/**
	 * Gets a Calendar instance for the given Date. 
	 * @param date The date where the Calendar instance should be created for.
	 * @return The desired Calendar instance or <code>null</code> if the given date is <code>null</code>.
	 */
	public static Calendar toCalendar(final Date date) {
		if(date==null) {
			return null;
		}
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}
	
    /**
     * Returns the last millisecond of the specified date.
     *
     * @param date Date to calculate end of day from
     * @return Last millisecond of <code>date</code>
     */
    public static Date endOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        synchronized(calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MILLISECOND, 999);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            return calendar.getTime();
        }
    }


    /**
     * Returns a new Date with the hours, milliseconds, seconds and minutes
     * set to 0.
     *
     * @param date Date used in calculating start of day
     * @return Start of <code>date</code>
     */
    public static Date startOfDay(Date date) {
    	Calendar calendar = Calendar.getInstance();
        synchronized(calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            return calendar.getTime();
        }
    }

    /**
     * Compares the given Dates against each other and tests if they're equal.
     * @param value First value to be tested
     * @param editorValue Second value to be tested
     * @return <code>true</code> if both given values are equal and <code>false</code> otherwiese.
     */
	public static boolean equals(Date value, Date editorValue) {
		return StringUtil.toString(value).equals(StringUtil.toString(editorValue));
	}

	public static boolean notEqual(Date value, Date editorValue) {
		return !equals(value, editorValue);
	}	
}