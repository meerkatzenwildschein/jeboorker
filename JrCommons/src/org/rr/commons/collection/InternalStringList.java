package org.rr.commons.collection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * ArrayList implementation which takes sure that all strings added
 * be the {@link String#intern()} ones. 
 */
public class InternalStringList extends ArrayList<String>{

	private static final long serialVersionUID = 5633330906727403357L;

	@Override
	public void add(int index, String element) {
		super.add(index, element.intern());
	}

	@Override
	public boolean add(String e) {
		return super.add(e.intern());
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
		for (String string : c) {
			add(string);
		}
		return !c.isEmpty();
	}

	@Override
	public boolean addAll(int index, Collection<? extends String> c) {
		for (String string : c) {
			add(index, string);
		}
		return !c.isEmpty();
	}

	
}
