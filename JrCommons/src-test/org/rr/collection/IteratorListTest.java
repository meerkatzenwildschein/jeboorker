package org.rr.collection;

import java.util.ArrayList;

import org.rr.commons.collection.IteratorList;

import junit.framework.TestCase;

public class IteratorListTest extends TestCase {

	public void testCursor() {
		ArrayList<String> first = new ArrayList<String>();
		first.add("1");
		first.add("2");
		first.add("3");
		first.add("4");
		
		IteratorList<String> t = new IteratorList<String>(first.iterator());
		System.out.println(t.get(1));
		System.out.println(t.get(3));
		System.out.println(t.get(0));
	}
}
