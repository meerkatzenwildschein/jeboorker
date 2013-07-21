package org.rr.jeborker.app;

import java.util.Collections;
import java.util.List;

import org.rr.commons.collection.WrapperList;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;

public class BasePathList extends WrapperList<String> {
	
	public BasePathList(List<String> basePaths) {
		super(basePaths);
		Collections.sort(this);
	}
	
	public BasePathList(String basePath) {
		super();
		this.add(basePath);
		Collections.sort(this);
	}
	
	public BasePathList() {
		super();
	}
	
	/**
	 * Tests if all base path elements are visible and returns <code>true</code> if they are and
	 * <code>false</code> if some are not.
	 */
	public boolean isAllPathElementsVisible() {
		for(int i = 0; i < toWrap.size(); i++) {
			String basePath = toWrap.get(i);
			if(!PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).isBasePathVisible(basePath)) {
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
			if(PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).isBasePathVisible(basePath)) {
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
	
	/**
	 * Tests if the given file / path have a valid base path. 
	 * @return <code>true</code> if a base path could be found for the given path or <code>false</code> otherwise.
	 */
	public boolean containsBasePathFor(final String path) {
		for(int i = 0; i < toWrap.size(); i++) {
			String basePath = toWrap.get(i);
			if(path.startsWith(basePath)) {
				return true;
			}
		}
		return false;
	}

}
