package org.rr.commons.utils;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {

	public void testFindMiddle() {
		String text = "text to find something";
		String search = "to";
		int find = StringUtil.find(text, search, 0, UtilConstants.COMPARE_BINARY);
		assertEquals(5, find);

		find = StringUtil.find(text, search, 0, UtilConstants.COMPARE_TEXT);
		assertEquals(5, find);
	}
	
	public void testFindEnd() {
		String text = "text to find something";
		String search = "something";
		int find = StringUtil.find(text, search, 0, UtilConstants.COMPARE_BINARY);
		assertEquals(13, find);
		
		find = StringUtil.find(text, search, 0, UtilConstants.COMPARE_TEXT);
		assertEquals(13, find);
	}
	
	public void testFindBegin() {
		String text = "text to find something";
		String search = "text";
		int find = StringUtil.find(text, search, 0, UtilConstants.COMPARE_BINARY);
		assertEquals(0, find);
		
		find = StringUtil.find(text, search, 0, UtilConstants.COMPARE_TEXT);
		assertEquals(0, find);
	}
	
	public void testFindStart() {
		String text = "text to find something";
		String search = "text";
		int find = StringUtil.find(text, search, 5, UtilConstants.COMPARE_BINARY);
		assertEquals(-1, find);
		
		find = StringUtil.find(text, search, 5, UtilConstants.COMPARE_TEXT);
		assertEquals(-1, find);
	}
	
	public void testLtrim() {
		String text = "abcdefghij";
		char[] trims = new char[] {'a', 'b'};
		String result = StringUtil.ltrim(text, trims);
		assertEquals("cdefghij", result);
		
		trims = new char[] {'a'};
		result = StringUtil.ltrim(text, trims);
		assertEquals("bcdefghij", result);		
		
		trims = new char[] {};
		result = StringUtil.ltrim(text, trims);
		assertEquals("abcdefghij", result);		
	}
	
	public void testOccurence() {
		String text = "abcdefghija";
		assertEquals(2, StringUtil.occurrence(text, "a"));
		
		text = "aabcdefghij";
		assertEquals(2, StringUtil.occurrence(text, "a"));
		
		text = "bcdefghij";
		assertEquals(0, StringUtil.occurrence(text, "a"));
	}
	
	public void testStripTrailing() {
		String text = "abcdefghija";
		assertTrue(StringUtil.stripTrailing(text, 'a').length() == text.length() - 1 );
		assertTrue(StringUtil.stripTrailing(text, 'b').length() == text.length());
	}
	
}
