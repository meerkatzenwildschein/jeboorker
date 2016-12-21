package org.rr.commons.utils;

import junit.framework.TestCase;

public class TextUtilsTest extends TestCase {
	
	public void testRemovePageNumbers() {
		String text = "abc\n1\ndfdfdf\n02\nhgfhfgh\n99\n03";
		String removedPageNumbers = TextUtils.removePageNumbers(text).toString();
		assertFalse(removedPageNumbers.contains("1"));
		assertFalse(removedPageNumbers.contains("02"));
		assertFalse(removedPageNumbers.contains("03"));
	}

}
