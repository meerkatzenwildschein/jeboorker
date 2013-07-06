package org.rr.commons.utils;

import java.util.ArrayList;
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
	
	public void testSet() {
		ArrayList<String> l = new ArrayList<String>();
		l.add("1");
		
		ListUtils.set(l, "1new", 0);
		assertEquals("1new", l.get(0));	
		
		ListUtils.set(l, "farawy", 5);
		assertEquals("farawy", l.get(5));
		
		ListUtils.set(l, "farawy2", 6);
		assertEquals("farawy2", l.get(6));
		
		ListUtils.set(l, "farawy3", 6);
		assertEquals("farawy3", l.get(6));		
		
	}
	
	public void testSplitChar() {
		List<String> split = ListUtils.split("aay,b,c,,,", ',');
		assertEquals("[aay, b, c, , , ]", split.toString());
		
		split = ListUtils.split("", ',');
		assertEquals(1, split.size());
		
	}
	
	public void testIndexedToString() {
		long currentTimeMillis;
		ArrayList<String> list = new ArrayList<String>();
		for(int i = 0; i < 100; i++) {
			list.add("/home/user/ebooks/test/folder name with spaces/some book with a name.epub" + i);
		}
		
		currentTimeMillis = System.currentTimeMillis();
		String indexedListString = ListUtils.toIndexedString(list, ';');
		long indexedToStringTime = System.currentTimeMillis() - currentTimeMillis;
		System.out.println("index toString: " + indexedToStringTime);
		
		currentTimeMillis = System.currentTimeMillis();
		String oldFashionListString = ListUtils.join(list, ";");
		long oldToStringTime = System.currentTimeMillis() - currentTimeMillis;
		System.out.println("old toString: "+oldToStringTime);

		
		currentTimeMillis = System.currentTimeMillis();
		List<String> fromIndexString = ListUtils.fromIndexString(indexedListString, ';');
		long indexedTime = System.currentTimeMillis() - currentTimeMillis;
		System.out.println("indexed toList: "+indexedTime);
		
		currentTimeMillis = System.currentTimeMillis();
		List<String> split = ListUtils.split(oldFashionListString, ";");
		long oldFashionTime = System.currentTimeMillis() - currentTimeMillis;
		System.out.println("old toList: " + oldFashionTime);
		
		assertEquals(list.size(), fromIndexString.size());
		assertEquals(split.size(), fromIndexString.size());
		
		//performance
		assertTrue(indexedTime < oldFashionTime);
		
	}	
}
