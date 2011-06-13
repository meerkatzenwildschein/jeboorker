package org.rr.commons.utils;

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
		assertTrue(DateConversionUtils.toDate("23.1.11").toString().equals("Sun Jan 23 00:00:00 CET 2011"));
		assertTrue(DateConversionUtils.toDate("23.1.2011").toString().equals("Sun Jan 23 00:00:00 CET 2011"));
		
		//ISO 8601
		assertTrue(DateConversionUtils.toDate("2003-W14-2").toString().equals("Thu Jan 02 00:00:00 CET 2003"));
		
		assertTrue(DateConversionUtils.toDate("11-1-23").toString().equals("Sun Jan 23 00:00:00 CET 2011"));
		assertTrue(DateConversionUtils.toDate("2011-1-23").toString().equals("Sun Jan 23 00:00:00 CET 2011"));
		
		//us
		assertTrue(DateConversionUtils.toDate("1/23/11").toString().equals("Sun Jan 23 00:00:00 CET 2011"));
		assertTrue(DateConversionUtils.toDate("1/23/2011").toString().equals("Sun Jan 23 00:00:00 CET 2011"));
		
		
		assertTrue(DateConversionUtils.toDate("2010/11/28").toString().equals("Sun Nov 28 00:00:00 CET 2010"));
		
		//ISO_8601_DATE_TIME
		assertTrue(DateConversionUtils.toDateTime("2011-05-07 14:39Z").toString().equals("Sat May 07 14:39:00 CEST 2011"));
		assertTrue(DateConversionUtils.toDateTime("2011-05-07T14:39Z").toString().equals("Sat May 07 14:39:00 CEST 2011"));
		
		//ISO 8601 Ordinal
		assertTrue(DateConversionUtils.toDateTime("2011-127").toString().equals("Sat May 07 00:00:00 CEST 2011"));
		assertEquals(DateConversionUtils.toString(DateConversionUtils.toDateTime("2011-127"), DateConversionUtils.DATE_FORMATS.ISO_8601_ORDINAL),"2011-127");
		
		//Java
		assertTrue(DateConversionUtils.toDate("Sun Jan 23 00:00:00 CET 2011").toString().equals("Sun Jan 23 00:00:00 CET 2011"));
		assertTrue(DateConversionUtils.toDate("Thu Oct 23 00:00:00 CEST 2008").toString().equals("Thu Oct 23 00:00:00 CEST 2008"));
		
		
		//W3C
		assertTrue(DateConversionUtils.toDate("2010-10-13T12:58:49.281000+00:00").toString().equals("Wed Oct 13 00:00:00 CEST 2010"));
		assertTrue(DateConversionUtils.toDateTime("2010-11-27T23:00:11+00:00").toString().equals("Sun Nov 28 00:00:11 CET 2010"));
		assertTrue(DateConversionUtils.toDate("2010-11-27T23:00+00:00").toString().equals("Sun Nov 28 00:00:00 CET 2010"));
		assertTrue(DateConversionUtils.toDate("2010-11").toString().equals("Mon Nov 01 00:00:00 CET 2010"));
		
		//W3C stolen T
		assertTrue(DateConversionUtils.toDate("2010-09-22 11:15:52.216000+02:00").toString().equals("Wed Sep 22 00:00:00 CEST 2010"));
		assertTrue(DateConversionUtils.toDateTime("2010-09-22 11:15:52+02:00").toString().equals("Wed Sep 22 11:15:52 CEST 2010"));
		assertTrue(DateConversionUtils.toDate("2010-09").toString().equals("Wed Sep 01 00:00:00 CEST 2010"));
		assertTrue(DateConversionUtils.toDate("2011").toString().equals("Sat Jan 01 00:00:00 CET 2011"));
		
		//RFC 822
		assertTrue(DateConversionUtils.toDateTime("Wed, 02 Oct 2002 15:00:00 +0200").toString().equals("Wed Oct 02 15:00:00 CEST 2002"));
		assertTrue(DateConversionUtils.toDateTime("Wed, 02 Oct 2002 15:00 +0200").toString().equals("Wed Oct 02 15:00:00 CEST 2002"));
		assertTrue(DateConversionUtils.toDateTime("04 Jul 2001 12:08:56 -0700").toString().equals("Wed Jul 04 21:08:56 CEST 2001"));
		
		//PDF
		assertTrue(DateConversionUtils.toDateTime("D:20001218180738+01'00'").toString().equals("Mon Dec 18 18:07:38 CET 2000"));
		assertTrue(DateConversionUtils.toDateTime("D:200012181807+01'00'").toString().equals("Mon Dec 18 18:07:00 CET 2000"));
		assertTrue(DateConversionUtils.toDateTime("D:20001218180738").toString().equals("Mon Dec 18 18:07:38 CET 2000"));
		assertTrue(DateConversionUtils.toDateTime("D:200012181807").toString().equals("Mon Dec 18 18:07:00 CET 2000"));
		assertTrue(DateConversionUtils.toDateTime("D:200012181807Z").toString().equals("Mon Dec 18 18:07:00 CET 2000"));
		
		assertTrue(DateConversionUtils.toDateTime("D:200212291022+0100").toString().equals("Sun Dec 29 10:22:00 CET 2002"));
		
		//Mac OS X 10.6.7 Quartz PDFContext
		assertTrue(DateConversionUtils.toDateTime("D:20110427120924Z00'00'").toString().equals("Wed Apr 27 00:09:24 CEST 2011"));
		
		assertEquals(DateConversionUtils.DATE_FORMATS.PDF.getString(DateConversionUtils.toDateTime("D:20001218180738+01'00'")) , "D:200012181807+01'00'");
	}
}
