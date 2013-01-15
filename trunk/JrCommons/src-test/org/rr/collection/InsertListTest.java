package org.rr.collection;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.rr.commons.collection.InsertList;

public class InsertListTest extends TestCase {

	List<String> defaultList = new ArrayList<String>(){
		private static final long serialVersionUID = 702417544486552180L;
		{
		add("1");
		add("2");
		add("3");
		add("4");
		add("5");
		add("6");
		add("7");
		add("8");
	}};
	
	public void testList1() {
		String insertValue = "99";
		InsertList<String> insertList = new InsertList<String>(defaultList, insertValue, 0);
		assertEquals(insertValue, insertList.get(0));
		
		insertList = new InsertList<String>(defaultList, insertValue, 1);
		assertEquals(insertValue, insertList.get(1));
		
		printList(insertList);
		
		insertList = new InsertList<String>(defaultList, insertValue, 8);
		assertEquals(insertValue, insertList.get(8));
	}
	
	private void printList(List<String> list) {
		for(String s : list) {
			System.out.println(s);
		}
	}
}
