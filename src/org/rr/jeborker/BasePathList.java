package org.rr.jeborker;

import java.util.Collections;
import java.util.List;

import org.rr.commons.collection.WrapperList;

public class BasePathList extends WrapperList<String> {
	
	BasePathList(List<String> basePaths) {
		super(basePaths);
		Collections.sort(basePaths);
	}
	
	/**
	 * Tests if all base path elements are visible and returns <code>true</code> if they are and
	 * <code>false</code> if some are not.
	 */
	public boolean isAllPathElementsVisible() {
		for(int i = 0; i < toWrap.size(); i++) {
			String basePath = toWrap.get(i);
			if(!JeboorkerPreferences.isBasePathVisible(basePath)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Tests if no base path elements are visible and returns <code>true</code> if they are and
	 * <code>false</code> if some are not.
	 */
	public boolean isNoPathElementsVisible() {
		for(int i = 0; i < toWrap.size(); i++) {
			String basePath = toWrap.get(i);
			if(JeboorkerPreferences.isBasePathVisible(basePath)) {
				return false;
			}
		}
		return true;
	}

}
