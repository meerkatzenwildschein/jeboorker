package org.rr.commons.utils;

import java.util.List;

import junit.framework.TestCase;

public class ListUtilsTest extends TestCase {

	public void testSplit() {
		List<String> split = ListUtils.split("1,2,3", ",", -1, UtilConstants.COMPARE_BINARY);
		assertEquals(3, split.size());
		
		split = ListUtils.split("1,2,3", ",", 1, UtilConstants.COMPARE_BINARY);
		assertEquals(1, split.size());		
		
		split = ListUtils.split("1,2,3", ",", 3, UtilConstants.COMPARE_BINARY);
		assertEquals(3, split.size());	
		
		split = ListUtils.split("1,2,3", ",", 0, UtilConstants.COMPARE_BINARY);
		assertEquals(0, split.size());				
	}
	
	public void testChunkSplit() {
		List<String> chunkSplit = ListUtils.chunkSplit("teststring", 3); //[tes, tst, rin, g  ]
		assertEquals(4, chunkSplit.size());
		assertEquals("tes", chunkSplit.get(0));
		assertEquals("tst", chunkSplit.get(1));
		assertEquals("rin", chunkSplit.get(2));
		assertEquals("g  ", chunkSplit.get(3));
	}
}
