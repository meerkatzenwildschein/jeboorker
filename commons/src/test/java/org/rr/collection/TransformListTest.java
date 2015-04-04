package org.rr.collection;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.rr.commons.collection.TransformValueList;

public class TransformListTest extends TestCase {

	public void test() {
		ArrayList<Boolean> listWithBoolean = new ArrayList<>();
		listWithBoolean.add(Boolean.TRUE);
		listWithBoolean.add(Boolean.FALSE);
		listWithBoolean.add(Boolean.TRUE);
		
		List<String> stringList = new TransformValueList<Boolean, String>(listWithBoolean) {

		    @Override
		    public String transform(Boolean source) {
		        return source != null ? source.toString() : "false";
		    }
		    
		};
		
		for(String s : stringList) {
			System.out.println(s);
		}
	}
}
