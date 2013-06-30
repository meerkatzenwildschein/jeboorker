package org.rr.jeborker;

import java.util.Collections;
import java.util.List;

import org.rr.commons.collection.WrapperList;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;

public class BasePathList extends WrapperList<String> {
	
	BasePathList(List<String> basePaths) {
		super(basePaths);
		Collections.sort(this);
	}
	
	BasePathList(String basePath) {
		super();
		this.add(basePath);
		Collections.sort(this);
	}
	
	BasePathList() {
		super();
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
	
	/**
	 * Find the best matching base path for the given {@link IResourceHandler}.
	 * @return The desired base path or <code>null</code> if no base path is matching.
	 */
	public IResourceHandler getBasePathForFile(IResourceHandler resource) {
		String resourceString = resource.toString();
		for(int i = 0; i < toWrap.size(); i++) {
			String basePath = toWrap.get(i);
			if(resourceString.startsWith(basePath)) {
				return ResourceHandlerFactory.getResourceHandler(basePath);
			}
		}
		return null;
	}

}
