package org.rr.commons.swing;

import org.rr.commons.swing.SwingUtils;

import junit.framework.TestCase;

public class SwingUtilsTest extends TestCase {

	public void testGetUniqueColors() {
		assertTrue(SwingUtils.getUniqueColors(99).size() == 99);
		assertTrue(SwingUtils.getUniqueColors(1).size() >= 1);
		assertTrue(SwingUtils.getUniqueColors(19).size() == 19);
		assertTrue(SwingUtils.getUniqueColors(20).size() == 20);
	}
}
