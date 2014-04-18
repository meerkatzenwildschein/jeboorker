package org.rr.commons.utils;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {

	public void testFindMiddle() {
		String text = "text to find something";
		String search = "to";
		int find = StringUtils.find(text, search, 0, UtilConstants.COMPARE_BINARY);
		assertEquals(5, find);

		find = StringUtils.find(text, search, 0, UtilConstants.COMPARE_TEXT);
		assertEquals(5, find);
	}
	
	public void testFindEnd() {
		String text = "text to find something";
		String search = "something";
		int find = StringUtils.find(text, search, 0, UtilConstants.COMPARE_BINARY);
		assertEquals(13, find);
		
		find = StringUtils.find(text, search, 0, UtilConstants.COMPARE_TEXT);
		assertEquals(13, find);
	}
	
	public void testFindBegin() {
		String text = "text to find something";
		String search = "text";
		int find = StringUtils.find(text, search, 0, UtilConstants.COMPARE_BINARY);
		assertEquals(0, find);
		
		find = StringUtils.find(text, search, 0, UtilConstants.COMPARE_TEXT);
		assertEquals(0, find);
	}
	
	public void testFindStart() {
		String text = "text to find something";
		String search = "text";
		int find = StringUtils.find(text, search, 5, UtilConstants.COMPARE_BINARY);
		assertEquals(-1, find);
		
		find = StringUtils.find(text, search, 5, UtilConstants.COMPARE_TEXT);
		assertEquals(-1, find);
	}
	
	public void testLtrim() {
		String text = "abcdefghij";
		char[] trims = new char[] {'a', 'b'};
		String result = StringUtils.ltrim(text, trims);
		assertEquals("cdefghij", result);
		
		trims = new char[] {'a'};
		result = StringUtils.ltrim(text, trims);
		assertEquals("bcdefghij", result);		
		
		trims = new char[] {};
		result = StringUtils.ltrim(text, trims);
		assertEquals("abcdefghij", result);		
	}
	
	public void testOccurence() {
		String text = "abcdefghija";
		assertEquals(2, StringUtils.occurrence(text, "a"));
		
		text = "aabcdefghij";
		assertEquals(2, StringUtils.occurrence(text, "a"));
		
		text = "bcdefghij";
		assertEquals(0, StringUtils.occurrence(text, "a"));
	}
	
}
