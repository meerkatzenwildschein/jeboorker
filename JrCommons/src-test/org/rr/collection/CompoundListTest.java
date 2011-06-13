package org.rr.collection;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.rr.commons.collection.CompoundList;

public class CompoundListTest extends TestCase {

	public void test1() {
		ArrayList<String> first = new ArrayList<String>();
		ArrayList<String> second = new ArrayList<String>();
		
		first.add("null");
		first.add("eins");
		first.add("zwei");
		first.add("drei");
		
		second.add("vier");
		second.add("fünf");
		second.add("sechs");
		
		CompoundList<String> l = new CompoundList<String>(first, second);
		assertEquals(7, l.size());
		assertEquals("vier", l.get(4));
	}
}
