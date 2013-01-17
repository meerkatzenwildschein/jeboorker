package org.rr.collection;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.rr.commons.collection.BlindElementList;

public class BlindElementListTest extends TestCase {

	List<String> defaultList = new ArrayList<String>(){
		private static final long serialVersionUID = 702417544486552180L;
		{
		add("0");
		add("1");
		add("2");
		add("3");
		add("4");
		add("5");
		add("6");
		add("7");
	}};
	
	
	public void testList() {
		BlindElementList<String> insertList;
		insertList = new BlindElementList<String>(defaultList, 0);
		assertEquals(defaultList.get(1), insertList.get(0));
		
		insertList = new BlindElementList<String>(defaultList, 5);
		assertEquals(defaultList.get(6), insertList.get(5));
		
		insertList = new BlindElementList<String>(defaultList, 7);
		assertEquals(defaultList.size() - 1, insertList.size());

		printList(insertList);		
	}
	
	private void printList(List<String> list) {
		for(String s : list) {
			System.out.println(s);
		}
	}
}
