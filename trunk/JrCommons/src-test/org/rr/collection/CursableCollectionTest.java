package org.rr.collection;

import junit.framework.TestCase;

import org.rr.commons.collection.CursableCollection;

public class CursableCollectionTest extends TestCase {

	public void testCursor() {
		CursableCollection<String,String> l = new CursableCollection<String, String>();
		l.append("erster", "att1");
		l.append("zweiter", "att2");
		l.append("dritter", "att3");
		
		if(l.getCursorLocation()!=2) {
			throw new RuntimeException();
		}
		
		if(!l.previous().equals("zweiter")) {
			throw new RuntimeException();
		}
		
		l.append("vierter");
		assertEquals("vierter", l.current());
		assertEquals("zweiter", l.previous());
		
		
		assertEquals("vierter", l.next());
		assertEquals("vierter", l.current());
		
		l.remove(l.previous()); //zweiter weg
		assertEquals("erster", l.current());
		
		l.remove("erster");
		l.remove("vierter");
		
		assertNull(l.current());
		assertNull(l.next());
	}
}
