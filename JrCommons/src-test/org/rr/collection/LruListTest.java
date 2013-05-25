package org.rr.collection;

import java.util.Iterator;

import org.rr.commons.collection.LruList;

import junit.framework.TestCase;

public class LruListTest extends TestCase {

	public void testList1() {
		int len = 5;
		LruList<String> l = new LruList<String>(len);
		for(int i=0; i < 8; i++) {
			l.add(String.valueOf(i+"val"));
		}
		assertEquals("[3val, 4val, 5val, 6val, 7val]", l.toString());
		assertEquals(len, l.size());
		
		
		Iterator<String> iterator = l.iterator();
		String testResult = "";
		while(iterator.hasNext()) {
			testResult += iterator.next();
		}
		assertEquals("3val4val5val6val7val", testResult);
		
		l.clear();
		assertEquals(0, l.size());
	}
}
