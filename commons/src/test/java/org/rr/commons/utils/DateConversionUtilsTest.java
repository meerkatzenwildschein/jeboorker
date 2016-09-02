package org.rr.commons.utils;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

public class DateConversionUtilsTest extends TestCase {

	
	//http://en.wikipedia.org/wiki/Date_format_by_country
	//http://en.wikipedia.org/wiki/ISO_8601_usage
	//http://en.wikipedia.org/wiki/DIN_5008
	public void testToDate() {
		assertEquals(DateConversionUtils.detectFromat("1.11.2011"), DateConversionUtils.DATE_FORMATS.DIN_5008);
		assertEquals(DateConversionUtils.detectFromat("11-11-1"), DateConversionUtils.DATE_FORMATS.ISO_8601_DATE);
		assertEquals(DateConversionUtils.detectFromat("11/1/2011"), DateConversionUtils.DATE_FORMATS.US_ANSI);
		
		//German old DIN
		assertTrue(DateConversionUtils.toDate("23.1.11").toString().equals(getDate(23, 01, 2011).toString()));
		assertTrue(DateConversionUtils.toDate("23.1.2011").toString().equals(getDate(23, 01, 2011).toString()));
		
		//ISO 8601
		assertTrue(DateConversionUtils.toDate("2003-W14-2").toString().equals(getDate(02, 01, 2003).toString()));
		
		assertTrue(DateConversionUtils.toDate("11-1-23").toString().equals(getDate(23, 01, 2011).toString()));
		assertTrue(DateConversionUtils.toDate("2011-1-23").toString().equals(getDate(23, 01, 2011).toString()));
		
		//us
		assertTrue(DateConversionUtils.toDate("1/23/11").toString().equals(getDate(23, 01, 2011).toString()));
		assertTrue(DateConversionUtils.toDate("1/23/2011").toString().equals(getDate(23, 01, 2011).toString()));
		
		assertTrue(DateConversionUtils.toDate("2010/11/28").toString().equals(getDate(28, 11, 2010).toString()));
		
		//ISO_8601_DATE_TIME
		assertTrue(DateConversionUtils.toDateTime("2011-05-07 14:39Z").toString().equals(getDate(07, 05, 2011, 14, 39, 00).toString()));
		assertTrue(DateConversionUtils.toDateTime("2011-05-07T14:39Z").toString().equals(getDate(07, 05, 2011, 14, 39, 00).toString()));
		
		//ISO 8601 Ordinal
		assertTrue(DateConversionUtils.toDateTime("2011-127").toString().equals(getDate(7, 5, 2011).toString()));
		assertEquals(DateConversionUtils.toString(DateConversionUtils.toDateTime("2011-127"), DateConversionUtils.DATE_FORMATS.ISO_8601_ORDINAL),"2011-127");
		
		//Java
		assertTrue(DateConversionUtils.toDate("Sun Jan 23 00:00:00 CET 2011").toString().equals(getDate(23, 01, 2011).toString()));
		assertTrue(DateConversionUtils.toDate("Thu Oct 23 00:00:00 CEST 2008").toString().equals(getDate(23, 10, 2008).toString()));
		
		
		//W3C
		//2008-03-08T19:21:00.000Z
		assertTrue(DateConversionUtils.toDate("2008-03-08T19:21:00.000Z").toString().equals(getDate(8, 3, 2008).toString()));
		assertTrue(DateConversionUtils.toDateTime("2008-03-08T19:21:00.000Z").toString().equals(getDate(8, 3, 2008, 20, 21, 0).toString()));
		assertTrue(DateConversionUtils.toDate("2008-03-08T19:21:00.000Z").toString().equals(getDate(8, 3, 2008).toString()));
		assertTrue(DateConversionUtils.toDate("2010-10-13T12:58:49.281000+00:00").toString().equals(getDate(13, 10, 2010).toString()));
		assertTrue(DateConversionUtils.toDateTime("2010-11-27T23:00:11+00:00").toString().equals(getDate(28, 11, 2010, 0, 0, 11).toString()));
		assertTrue(DateConversionUtils.toDate("2010-11-27T23:00+00:00").toString().equals(getDate(28, 11, 2010, 0, 0, 0).toString()));
		assertTrue(DateConversionUtils.toDate("2010-11").toString().equals(getDate(1, 11, 2010).toString()));
		assertTrue(DateConversionUtils.toDate("2009-08-04T00:00:00").toString().equals(getDate(4, 8, 2009).toString()));		
		
		//W3C stolen T
		assertTrue(DateConversionUtils.toDate("2010-09-22 11:15:52.216000+02:00").toString().equals(getDate(22, 9, 2010).toString()));
		assertTrue(DateConversionUtils.toDateTime("2010-11-22 11:15:52+02:00").toString().equals(getDate(22, 11, 2010, 10, 15, 52).toString()));
		assertTrue(DateConversionUtils.toDate("2010-09").toString().equals(getDate(1, 9, 2010).toString()));
		assertTrue(DateConversionUtils.toDate("2011").toString().equals(getDate(1, 1, 2011).toString()));
		
		//RFC 822
		assertTrue(DateConversionUtils.toDateTime("Wed, 02 Nov 2002 15:00:00 +0200").toString().equals(getDate(2, 11, 2002, 14, 0, 0).toString()));
		assertTrue(DateConversionUtils.toDateTime("Wed, 02 Nov 2002 15:00 +0200").toString().equals(getDate(2, 11, 2002, 14, 0, 0).toString()));
		assertTrue(DateConversionUtils.toDateTime("04 Nov 2001 12:08:56 -0700").toString().equals(getDate(4, 11, 2001, 20, 8, 56).toString()));
		
		//PDF
		assertTrue(DateConversionUtils.toDateTime("D:20001218180738+01'00'").toString().equals(getDate(18, 12, 2000, 18, 7, 38).toString()));
		assertTrue(DateConversionUtils.toDateTime("D:200012181807+01'00'").toString().equals(getDate(18, 12, 2000, 18, 7, 0).toString()));
		assertTrue(DateConversionUtils.toDateTime("D:20001218180738").toString().equals(getDate(18, 12, 2000, 18, 7, 38).toString()));
		assertTrue(DateConversionUtils.toDateTime("D:200012181807").toString().equals(getDate(18, 12, 2000, 18, 7, 0).toString()));
		assertTrue(DateConversionUtils.toDateTime("D:200012181807Z").toString().equals(getDate(18, 12, 2000, 18, 7, 0).toString()));
		
		assertTrue(DateConversionUtils.toDateTime("D:200212291022+0100").toString().equals(getDate(29, 12, 2002, 10, 22, 0).toString()));
		
		//Mac OS X 10.6.7 Quartz PDFContext
		assertTrue(DateConversionUtils.toDateTime("D:20110427120924Z00'00'").toString().equals(getDate(27, 4, 2011, 0, 9, 24).toString()));
		
		assertEquals(DateConversionUtils.DATE_FORMATS.PDF.getString(DateConversionUtils.toDateTime("D:20001218180738+01'00'")) , "D:200012181807+01'00'");
		
		//2005-07-04T00:00:+0Z
		assertEquals(DateConversionUtils.DATE_FORMATS.ISO_8601_DATE_TIME.getString(DateConversionUtils.toDateTime("2005-11-04T00:00:+0Z")) , "2005-11-04 01:00Z");
	}

	private Date getDate(int day, int month, int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, day, 0, 0, 0);
		return calendar.getTime();
	}

	private Date getDate(int day, int month, int year, int hour, int minute, int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, day, hour, minute, second);
		return calendar.getTime();
	}
	
}
