package org.rr.collection;

import org.rr.commons.collection.WeakList;

import junit.framework.TestCase;

public class WeakListTest extends TestCase {

	public void testWeakness() {
		WeakList<Object> testList = new WeakList<Object>();
		for (int i = 0; i < 50; i++) {
			testList.add(new Object());
		}
		System.gc();
		System.gc();
		System.gc();

		testList.trimToSize();
		
		for (int i = 0; i < testList.size(); i++) {
			System.out.println(testList.get(i));
		}
		
		System.out.println(testList.size());
	}
	
}
