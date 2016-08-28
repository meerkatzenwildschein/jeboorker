package org.rr.commons.utils;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class DateConversionUtils {
	
    public static interface DateFormat {
    	
    	public Date getDate(String dateString);
    	
    	public boolean isMatching(String dateString);
    	
    	public String getString(Date date);
    }
    
	public static enum DATE_FORMATS implements DateFormat {
		DIN_5008 {
			//11.2.2011 - Germany, Latvia, Armenia, Azerbaijan, Belarus, Bulgaria, Georgia, Greenland
			//Iceland, Kyrgyzstan, Macedonia, Poland, Romania, Serbia, Switzerland, Tajikistan, 
			private final Pattern GERMAN_DATE_PATTERN = Pattern.compile("\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}");
			
			public Date getDate(String dateString) {
				Calendar calendar = Calendar.getInstance(Locale.US);
				String[] split = dateString.split("\\.");
				calendar.set(formatToYear(CommonUtils.toNumber(split[2])), CommonUtils.toNumber(split[1]).intValue()-1, CommonUtils.toNumber(split[0]).intValue(), 0, 0, 0);
				
				return calendar.getTime();
			}

			public boolean isMatching(String dateString) {
				return GERMAN_DATE_PATTERN.matcher(dateString).matches();
			}

			public String getString(Date date) {
				return new SimpleDateFormat("dd.MM.yyyy").format(date);
			}
		},
		MSZ_ISO_8601_2003 {
			//2011.11.2 - Hungary - MSZ ISO 8601:2003
			private final Pattern HUNGARY_DATE_PATTERN = Pattern.compile("\\d{4}\\.\\d{1,2}\\.\\d{1,2}");
			
			public Date getDate(String dateString) {
				Calendar calendar = Calendar.getInstance(Locale.US);
				String[] split = dateString.split("\\.");
				calendar.set(formatToYear(CommonUtils.toNumber(split[0])), CommonUtils.toNumber(split[1]).intValue()-1, CommonUtils.toNumber(split[2]).intValue(), 0, 0, 0);
				
				return calendar.getTime();
			}

			public boolean isMatching(String dateString) {
				return HUNGARY_DATE_PATTERN.matcher(dateString).matches();
			}

			public String getString(Date date) {
				return new SimpleDateFormat("yyyy.dd.MM").format(date);
			}
		},		
		ISO_8601_DATE {
			//2011-11-2
			private final Pattern EU_DATE_PATTERN = Pattern.compile("\\d{2,4}\\-\\d{1,2}\\-\\d{1,2}");
			
			public Date getDate(String dateString) {
				Calendar calendar = Calendar.getInstance(Locale.US);
				String[] split = dateString.split("-");
				calendar.set(formatToYear(CommonUtils.toNumber(split[0])), CommonUtils.toNumber(split[1]).intValue()-1, CommonUtils.toNumber(split[2]).intValue(), 0, 0, 0);
				return calendar.getTime();
			}
			
			public boolean isMatching(String dateString) {
				return EU_DATE_PATTERN.matcher(dateString).matches();
			}	
			
			public String getString(Date date) {
				return new SimpleDateFormat("yyyy-MM-dd").format(date);
			}			
		},
		ISO_8601_WEEK {
			//2003-W14-2 (Date with week number)
			private final Pattern ISO_8601_DATE_PATTERN = Pattern.compile("\\d{4}-[A-Z]??\\d{2}-\\d");
			
			private final String FORMAT_PATTERN = "yyyy-'W'ww-d";
			
			public Date getDate(String dateString) {
				try {
					return new SimpleDateFormat(FORMAT_PATTERN, Locale.US).parse(dateString);
				} catch (ParseException e) {
					throw new RuntimeException("could not parse date " + dateString);
				}
			}
			
			public boolean isMatching(String dateString) {
				return ISO_8601_DATE_PATTERN.matcher(dateString).matches();
			}

			public String getString(Date date) {
				return new SimpleDateFormat(FORMAT_PATTERN).format(date);
			}			
		},	
		ISO_8601_DATE_TIME {
			//Separate date and time in UTC: 	2011-05-07 14:39Z
			//Combined date and time in UTC: 	2011-05-07T14:39Z
			private final Pattern ISO_8601_DATE_TIME_PATTERN_1 = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[T\\s]\\d{2}:\\d{2}Z");
			
			//2005-07-04T00:00:+0Z
			//2005-07-04 00:00:+0Z
			private final Pattern ISO_8601_DATE_TIME_PATTERN_2 = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[\\sT]\\d{2}:\\d{2}:[+-]\\d{1,4}Z");
			
			private final String FORMAT_PATTERN_1 = "yyyy-MM-dd hh:mm'Z'";
			
			private final String FORMAT_PATTERN_2 = "yyyy-MM-dd hh:mm:Z";
			
			public Date getDate(String dateString) {
				try {
					dateString = dateString.replace('T', ' ');
					if(ISO_8601_DATE_TIME_PATTERN_1.matcher(dateString).matches()) {
						return new SimpleDateFormat(FORMAT_PATTERN_1, Locale.US).parse(dateString);
					} else if(ISO_8601_DATE_TIME_PATTERN_2.matcher(dateString).matches()) {
						//format 2005-07-04 00:00:+0Z -> 2005-07-04 00:00:+0000Z
						final int idx = dateString.indexOf(":+") != -1 ? dateString.indexOf(":+") : dateString.indexOf(":-");
						final String timezone = dateString.substring(idx + 2, dateString.indexOf('Z', idx));
						
						dateString = dateString.substring(0, idx + 2); //2005-07-04 00:00:+
						if(timezone.length() == 1) {
							dateString += "0" + timezone + "00";
						} else if(timezone.length() == 2) {
							dateString += "0" + timezone + "0";
						} else if(timezone.length() == 3) {
							dateString += "0" + timezone;
						} else if(timezone.length() == 4) {
							dateString += timezone;
						}
						return new SimpleDateFormat(FORMAT_PATTERN_2, Locale.US).parse(dateString);
					} else {
						throw new RuntimeException("unknown format for " + dateString);
					}
				} catch (ParseException e) {
					throw new RuntimeException("could not parse date " + dateString);
				}
			}
			
			public boolean isMatching(String dateString) {
				return ISO_8601_DATE_TIME_PATTERN_1.matcher(dateString).matches() || ISO_8601_DATE_TIME_PATTERN_2.matcher(dateString).matches();
			}

			public String getString(Date date) {
				return new SimpleDateFormat(FORMAT_PATTERN_1, Locale.US).format(date);
			}			
		},	
		ISO_8601_ORDINAL {
			//2011-127
			private final Pattern ISO_8601_DATE_TIME_PATTERN = Pattern.compile("\\d{4}-\\d{3}");
			
			public Date getDate(String dateString) {
				List<String> split = ListUtils.split(dateString, "-", 2, UtilConstants.COMPARE_BINARY);
				Calendar calendar = Calendar.getInstance(Locale.US);
				calendar.set(formatToYear(CommonUtils.toNumber(split.get(0))), 0, 1, 0, 0, 0);
				calendar.set(Calendar.DAY_OF_YEAR, CommonUtils.toNumber(split.get(1)).intValue());
				return calendar.getTime();
			}
			
			public boolean isMatching(String dateString) {
				return ISO_8601_DATE_TIME_PATTERN.matcher(dateString).matches();
			}

			public String getString(Date date) {
				Calendar calendar = Calendar.getInstance(Locale.US);
				calendar.setTime(date);
				int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
				int year = calendar.get(Calendar.YEAR);
				return formatToYear(Integer.valueOf(year)) + "-" + new DecimalFormat("##00").format(dayOfYear);
			}			
		},		
		US_ANSI {
			//11/28/2010 - ANSI INCITS 30-1997 (R2008) and NIST FIPS PUB 4-2
			private final Pattern US_DATE_PATTERN = Pattern.compile("\\d{1,2}\\/\\d{1,2}\\/\\d{2,4}");
			
			public Date getDate(String dateString) {
				Calendar calendar = Calendar.getInstance(Locale.US);
				String[] split = dateString.split("/");
				calendar.set(formatToYear(CommonUtils.toNumber(split[2])), CommonUtils.toNumber(split[0]).intValue()-1, CommonUtils.toNumber(split[1]).intValue(), 0, 0, 0);
				return calendar.getTime();
			}
			
			public boolean isMatching(String dateString) {
				return US_DATE_PATTERN.matcher(dateString).matches();
			}
			
			public String getString(Date date) {
				return new SimpleDateFormat("MM/dd/yyyy").format(date);
			}			
		},
		ISO_ARP_010_1989 {
			//Iran, Korea, South Africa
			//2010/11/28 
			private final Pattern ISO_ARP_010_1989_DATE_PATTERN = Pattern.compile("\\d{4}\\/\\d{1,2}\\/\\d{1,2}");
			
			public Date getDate(String dateString) {
				Calendar calendar = Calendar.getInstance(Locale.US);
				String[] split = dateString.split("/");
				calendar.set(formatToYear(CommonUtils.toNumber(split[0])), CommonUtils.toNumber(split[1]).intValue()-1, CommonUtils.toNumber(split[2]).intValue(), 0, 0, 0);
				return calendar.getTime();
			}
			
			public boolean isMatching(String dateString) {
				return ISO_ARP_010_1989_DATE_PATTERN.matcher(dateString).matches();
			}
			
			public String getString(Date date) {
				return new SimpleDateFormat("yyyy/MM/dd").format(date);
			}			
		},		
		W3C_MILLISECOND {
			//2010-10-13T12:58:49.281000+00:00
			//2010-09-22 11:15:52.216000+02:00
			private final Pattern W3C_MILLISECOND_DATE_PATTERN_1 = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[\\sT]\\d{2}:\\d{2}:\\d{2}\\.\\d{1,6}[+-]\\d{2}:??\\d{2}");
			
			//2008-03-08T19:21:00.000Z
			private final Pattern W3C_MILLISECOND_DATE_PATTERN_2 = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[\\sT]\\d{2}:\\d{2}:\\d{2}\\.\\d{1,6}Z");
			
			public Date getDate(String dateString) {
				try {
					if(dateString.indexOf(' ') != -1) {
						dateString = StringUtil.replace(dateString, " ", "T");
					}
					if(dateString.endsWith("Z")) {
						dateString = dateString.substring(0, dateString.length() -1) + "+00:00";
					}
					return new W3CDateFormat().parse(dateString);
				} catch (ParseException e) {
					throw new RuntimeException("could not parse date " + dateString);
				}
			}
			
			public boolean isMatching(String dateString) {
				return W3C_MILLISECOND_DATE_PATTERN_1.matcher(dateString).matches() 
						|| W3C_MILLISECOND_DATE_PATTERN_2.matcher(dateString).matches();
			}
			
			public String getString(Date date) {
				return new W3CDateFormat(W3CDateFormat.Pattern.MILLISECOND).format(date);
			}			
		},
		W3C_SECOND {
			//2010-11-27T23:00:00+00:00
			//2010-11-27 23:00:00+00:00
			private final Pattern W3C_SECOND_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[\\sT]\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:??\\d{2}");
			
			public Date getDate(String dateString) {
				try {
					if(dateString.indexOf(' ')!=-1) {
						dateString = StringUtil.replace(dateString, " ", "T");
					}
					return new W3CDateFormat().parse(dateString);
				} catch (ParseException e) {
					throw new RuntimeException("could not parse date " + dateString);
				}
			}
			
			public boolean isMatching(String dateString) {
				return W3C_SECOND_DATE_PATTERN.matcher(dateString).matches();
			}
			
			public String getString(Date date) {
				return new W3CDateFormat(W3CDateFormat.Pattern.SECOND).format(date);
			}			
		},
		W3C_MINUTE {
			//2010-11-27T23:00+00:00
			//2010-11-27 23:00+00:00
			private final Pattern W3C_MINUTE_DATE_PATTERN_1 = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[\\sT]\\d{2}:\\d{2}[+-]\\d{2}:??\\d{2}");
			
			//2009-08-04T00:00:00
			//2009-08-04 00:00:00			
			private final Pattern W3C_MINUTE_DATE_PATTERN_2 = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[\\sT]\\d{2}:\\d{2}:\\d{2}");
			
			public Date getDate(String dateString) {
				try {
					if(dateString.indexOf(' ')!=-1) {
						dateString = StringUtil.replace(dateString, " ", "T");
					}
					return new W3CDateFormat().parse(dateString);
				} catch (ParseException e) {
					throw new RuntimeException("could not parse date " + dateString);
				}
			}
			
			public boolean isMatching(String dateString) {
				return W3C_MINUTE_DATE_PATTERN_1.matcher(dateString).matches() || W3C_MINUTE_DATE_PATTERN_2.matcher(dateString).matches();
			}
			
			public String getString(Date date) {
				return new W3CDateFormat(W3CDateFormat.Pattern.MINUTE).format(date);
			}			
		},
		W3C_MINUTE2 {
			private final Pattern W3C_MINUTE_DATE_PATTERN_1 = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[\\sT]\\d{2}:\\d{2}:\\d{2}");
			
			public Date getDate(String dateString) {
				try {
					if(dateString.indexOf(' ')!=-1) {
						dateString = StringUtil.replace(dateString, " ", "T");
					}
					return new W3CDateFormat().parse(dateString);
				} catch (ParseException e) {
					throw new RuntimeException("could not parse date " + dateString);
				}
			}
			
			public boolean isMatching(String dateString) {
				return W3C_MINUTE_DATE_PATTERN_1.matcher(dateString).matches();
			}
			
			public String getString(Date date) {
				return new W3CDateFormat(W3CDateFormat.Pattern.MINUTE).format(date);
			}			
		},
		W3C_MINUTE_MONTH {
			//2010-11
			//2010-11
			private final Pattern W3C_MINUTE_MONTH_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}");
			
			public Date getDate(String dateString) {
				try {
					return new W3CDateFormat(W3CDateFormat.Pattern.MONTH).parse(dateString);
				} catch (ParseException e) {
					throw new RuntimeException("could not parse date " + dateString);
				}
			}
			
			public boolean isMatching(String dateString) {
				return W3C_MINUTE_MONTH_DATE_PATTERN.matcher(dateString).matches();
			}
			
			public String getString(Date date) {
				return new W3CDateFormat(W3CDateFormat.Pattern.MONTH).format(date);
			}			
		},	
		W3C_MINUTE_YEAR {
			//2011
			public Date getDate(String dateString) {
				Calendar calendar = Calendar.getInstance(Locale.US);
				calendar.set(CommonUtils.toNumber(dateString).intValue(), 0, 1, 0, 0, 0);
				return calendar.getTime();
			}
			
			public boolean isMatching(String dateString) {
				if(dateString.length()==4 && CommonUtils.isInteger(dateString)) {
					return true;
				}
				return false;
			}	
			
			public String getString(Date date) {
				return new W3CDateFormat(W3CDateFormat.Pattern.YEAR).format(date);
			}			
		},
		RFC822_1 {
			//Wed, 02 Oct 2002 15:00:00 +0200
			//Wed, 04 Jul 2001 12:08:56 -0700
			private final Pattern RFC822_1_DATE_PATTERN = Pattern.compile("[A-Z][a-z]{2},\\s\\d\\d\\s[A-Z][a-z]{2}\\s\\d{4}\\s\\d{2}:\\d{2}:\\d{2}\\s[+-]\\d{4}");

			private final String FORMAT_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
			
			public Date getDate(String dateString) {
				try {
					return new SimpleDateFormat(FORMAT_PATTERN, Locale.US).parse(dateString);
				} catch (ParseException e) {
					throw new RuntimeException("could not parse date " + dateString);
				}
			}
			
			public boolean isMatching(String dateString) {
				return RFC822_1_DATE_PATTERN.matcher(dateString).matches();
			}

			@Override
			public String getString(Date date) {
				return new SimpleDateFormat(FORMAT_PATTERN).format(date);
			}			
		},
		RFC822_2 {
			//Wed, 02 Oct 2002 15:00 +0200
			//Wed, 04 Jul 2001 12:08 -0700
			private final Pattern RFC822_1_DATE_PATTERN = Pattern.compile("[A-Z][a-z]{2},\\s\\d\\d\\s[A-Z][a-z]{2}\\s\\d{4}\\s\\d{2}:\\d{2}\\s[+-]\\d{4}");

			private final String FORMAT_PATTERN = "EEE, dd MMM yyyy HH:mm z";
			
			public Date getDate(String dateString) {
				try {
					return new SimpleDateFormat(FORMAT_PATTERN, Locale.US).parse(dateString);
				} catch (ParseException e) {
					throw new RuntimeException("could not parse date " + dateString);
				}
			}
			
			public boolean isMatching(String dateString) {
				return RFC822_1_DATE_PATTERN.matcher(dateString).matches();
			}

			public String getString(Date date) {
				return new SimpleDateFormat(FORMAT_PATTERN).format(date);
			}			
		},
		RFC822_3 {
			//02 Oct 2002 15:00:00 +0200
			//04 Jul 2001 12:08:56 -0700
			private final Pattern RFC822_1_DATE_PATTERN = Pattern.compile("\\d{2}\\s[A-Z][a-z]{2}\\s\\d{4}\\s\\d{2}:\\d{2}:\\d{2}\\s[+-]\\d{4}");

			private final String FORMAT_PATTERN = "dd MMM yyyy HH:mm:ss z";
			
			public Date getDate(String dateString) {
				try {
					return new SimpleDateFormat(FORMAT_PATTERN, Locale.US).parse(dateString);
				} catch (ParseException e) {
					throw new RuntimeException("could not parse date " + dateString);
				}
			}
			
			public boolean isMatching(String dateString) {
				return RFC822_1_DATE_PATTERN.matcher(dateString).matches();
			}

			public String getString(Date date) {
				return new SimpleDateFormat(FORMAT_PATTERN).format(date);
			}			
		},
		JAVA {
			//Thu Dec 13 00:00:00 CET 2012
			//Fri Nov 11 00:00:00 CET 2011
			//Sun Jan 23 00:00:00 CET 2011
			//Thu Oct 23 00:00:00 CEST 2008
			private final Pattern JAVA_DATE_PATTERN = Pattern.compile("[A-Z][a-z]{2}\\s[A-Z][a-z]{2}\\s\\d{2}\\s\\d{2}\\:\\d{2}:\\d{2}\\s[A-Z]{3,4}\\s\\d{4}");
			
			public Date getDate(String dateString) {
				SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
				format.setDateFormatSymbols(new DateFormatSymbols(Locale.US));
				try {
					return format.parse(dateString);
				} catch (ParseException e) {
					throw new RuntimeException("could not parse date " + dateString);
				}
			}
			
			public boolean isMatching(String dateString) {
				boolean match = JAVA_DATE_PATTERN.matcher(dateString).matches();
				return match;
			}

			@Override
			public String getString(Date date) {
				return date.toString();
			}			
		},
		PDF {
			//D:20001218180738+01'00'
			//D:200012181807+01'00'
			//D:200012181807Z
			//D:200212291022+0100
			//D:20110427120924Z00'00' Mac OS X 10.6.7 Quartz PDFContext
			private final Pattern OO_PDF_DATE_PATTERN1 = Pattern.compile("D:\\d{12,14}[+-Z]\\d{2}\\'??\\d{2}\\'??Z??");
			
			private final Pattern OO_PDF_DATE_PATTERN2 = Pattern.compile("D:\\d{12,14}Z??");
			
			public Date getDate(String dateString) {
				final String saved = dateString;
				dateString = StringUtil.replace(dateString, "'", EMPTY).substring(2);
				if(dateString.indexOf('Z')!=-1) {
					//D:200012181807Z
					//D:20110427120924Z00'00'
					dateString = dateString.substring(0, dateString.indexOf('Z'));
				}
				
				try {
					if(dateString.length()==19) {
						//20001218180738+0100
						return new SimpleDateFormat("yyyyMMddhhmmssZ").parse(dateString);
					} else if(dateString.length()==17) {
						//200012181807+0100
						return new SimpleDateFormat("yyyyMMddhhmmZ").parse(dateString);	
					} else if(dateString.length()==14) {
						//20001218180738
						return new SimpleDateFormat("yyyyMMddhhmmss").parse(dateString);	
					} else if(dateString.length()==12) {
						//200012181807
						return new SimpleDateFormat("yyyyMMddhhmm").parse(dateString);	
					}
					return new SimpleDateFormat("yyyyMMddhhmmZ").parse(dateString);	
				} catch (ParseException e) {
					throw new RuntimeException("could not parse date " + saved);
				}
			}
			
			public boolean isMatching(String dateString) {
				return OO_PDF_DATE_PATTERN1.matcher(dateString).matches() || OO_PDF_DATE_PATTERN2.matcher(dateString).matches();
			}

			@Override
			public String getString(Date date) {
				//D:199812231952−08'00'
				String dateString = new SimpleDateFormat("'D:'yyyyMMddHHmmZ").format(date);
				dateString = dateString.substring(0, dateString.length()-2) + "'" +  dateString.substring(dateString.length()-2) + "'";
				return dateString;
			}			
		},
	}

	/**
	 * Some pattern find in the free wildness. They will also be tried if no of the
	 * well specified ones will match.
	 */
    private static final SimpleDateFormat[] POTENTIAL_FORMATS = new SimpleDateFormat[] {
        new SimpleDateFormat("EEEE, dd MMM yyyy hh:mm:ss a"),
        new SimpleDateFormat("EEEE, MMM dd, yyyy hh:mm:ss a"),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"), 
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"),
        new SimpleDateFormat("MM/dd/yyyy hh:mm:ss"),
        new SimpleDateFormat("dd MMM yy HH:mm z"), //Two digit year RFC822
        new SimpleDateFormat("EEEE, MMM dd, yyyy"), // Acrobat Distiller 1.0.2 for Macintosh
        new SimpleDateFormat("EEEE MMM dd, yyyy HH:mm:ss"), // ECMP5
        new SimpleDateFormat("EEEE MMM dd HH:mm:ss z yyyy"), // GNU Ghostscript 7.0.7
        new SimpleDateFormat("EEEE, MMM dd, yyyy 'at' hh:mma"), // Acrobat Net Distiller 1.0 for Windows
        new SimpleDateFormat("yyyyMMddhhmmZ"), //200908242027+0200
        new SimpleDateFormat("E, dd. MMMMM yyyy hh:mm"), //Thursday, 20. March 2003 16:40
    };	

    /**
     * Tries to detect the dateformat for the given String.
     * @param dateString The string with a date in an unknown format,.
     * @return The fitting format for the given date string or <code>null</code> if no format could be found.
     */
	public static DateFormat detectFromat(final String dateString) {
		for (DateFormat dateFormat : DATE_FORMATS.class.getEnumConstants()) {
			if(dateFormat.isMatching(dateString)) {
				return dateFormat;
			}
		}
		return null;
	}
	
	/**
	 * Formats the given date into a String with the specified format.
	 * @param date The date to be formatted.
	 * @param format The date format for the string result.
	 * @return The desired string or <code>null</code> if the date couldn't be formatted.
	 */
	public static String toString(final Date date, final DATE_FORMATS format) {
		if(format==null || date==null) {
			return null;
		}
		return format.getString(date);
	}	
	
	/**
	 * Tries to create a date from the given date string but without the time part.
	 * @param dateString The date string to be parsed into a date.
	 * @return The desired date or <code>null</code> if the date format could not be detected.
	 */
	public static Date toDate(String dateString) {
		final Date dateTime = toDateTime(dateString);
		if(dateTime!=null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dateTime);
			
			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
			return calendar.getTime();
		} 
		return dateTime;
	}
	
	/**
	 * Tries to create a date time from the given date string.
	 * @param dateString The date string to be parsed into a date.
	 * @return The desired date or <code>null</code> if the date format could not be detected.
	 */
	public static Date toDateTime(String dateString) {
		if(dateString == null || dateString.isEmpty()) {
			return null;
		}
		
		DateConversionUtils.DateFormat detectFromat = detectFromat(dateString);
		if(detectFromat != null) {
			return detectFromat.getDate(dateString);
		} else if(CommonUtils.isInteger(dateString)) {
			return new Date(CommonUtils.toNumber(dateString).longValue());
		} else {
			for (int i = 0; i < POTENTIAL_FORMATS.length; i++) {
				try {
					Date parse = POTENTIAL_FORMATS[i].parse(dateString);
					return parse;
				} catch (ParseException e) {
				} catch (Exception e) {
					 e.printStackTrace();
				 }
			}
		} 
		return null;
	}	
	
	/**
	 * Converts a nummer into a year value. If the number is a two digit one,
	 * the century will be added to it.
	 * @param number The year value.
	 * @return A four digit year value.
	 */
	private static int formatToYear(Number number) {
		if(number!=null) {
			int year = number.intValue();
			if(year < 100) {
				int currentTwoDigitYear = Integer.valueOf(String.valueOf(DateUtils.year(new Date())).substring(2));
				if(year < currentTwoDigitYear+2) {
					year += 2000;
				} else {
					year += 1900;
				}
			}
			return year;
		}
		throw new RuntimeException("No year value specified.");
	}
}
