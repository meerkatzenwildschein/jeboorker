package org.rr.collection;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.rr.commons.collection.CompoundList;

public class CompoundListTest extends TestCase {
	
	ArrayList<String> first;
	ArrayList<String> second;
	
	@Override
	protected void setUp() throws Exception {
		first = new ArrayList<String>();
		second = new ArrayList<String>();
		
		first.add("null");
		first.add("eins");
		first.add("zwei");
		first.add("drei");
		
		second.add("vier");
		second.add("fünf");
		second.add("sechs");
	}

	public void test1() {
		CompoundList<String> l = new CompoundList<String>(first, second);
		assertEquals(7, l.size());
		assertEquals("vier", l.get(4));
	}
	
	public void testAddWithIndex() {
		CompoundList<String> l = new CompoundList<String>(first, second);
		l.add(0, "p0");
		l.add(5, "p5");
		l.add(4, "p4");
		l.add(6, "p6");
		
		assertEquals("p0", l.get(0));
		assertEquals("p4", l.get(4));
		assertEquals("p6", l.get(6));
	}

}
