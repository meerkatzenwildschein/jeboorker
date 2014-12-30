package org.rr.collection;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.rr.commons.collection.InsertElementList;

public class InsertElementListTest extends TestCase {

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
	
	public void testList1() {
		String insertValue = "99";
		InsertElementList<String> insertList = new InsertElementList<>(defaultList, insertValue, 0);
		assertEquals(insertValue, insertList.get(0));
		
		insertList = new InsertElementList<>(defaultList, insertValue, 1);
		assertEquals(insertValue, insertList.get(1));
		
		insertList = new InsertElementList<>(defaultList, insertValue, 8);
		assertEquals(insertValue, insertList.get(8));
	}
	
	public void testAdd() {
		String insertValue = "insertValue";
		InsertElementList<String> insertList = new InsertElementList<>(defaultList, insertValue, 3);
		insertList.add(insertValue);
		assertEquals(insertValue, insertList.get(insertList.size()-1));
		
		insertList.add(2, "i2");
		insertList.add(3, "i3");
		insertList.add(5, "i5");
		
		assertEquals("i2", insertList.get(2));
		assertEquals("i3", insertList.get(3));
		assertEquals("i5", insertList.get(5));
		assertEquals(insertValue, insertList.get(4));
		
		assertEquals(2, insertList.indexOf("i2"));
		assertEquals(5, insertList.indexOf("i5"));
		printList(insertList);
	}
	
	private void printList(List<String> list) {
		int count = 0;
		for(String s : list) {
			System.out.println(count++ + ":" + s);
		}
	}
}
