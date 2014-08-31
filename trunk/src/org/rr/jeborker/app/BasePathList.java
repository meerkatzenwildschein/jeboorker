package org.rr.jeborker.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.collection.CompoundList;
import org.rr.commons.collection.WrapperList;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;

public class BasePathList extends WrapperList<String> {

	private static String importBasePath;

	private final List<String> stickyBasePath = new ArrayList<String>();

	public BasePathList(List<String> basePaths) {
		super(Collections.<String>emptyList());
		init(basePaths);
	}

	public BasePathList(String basePath) {
		super();
		init(null);
		this.add(basePath);
	}

	public BasePathList() {
		super();
		init(null);
	}

	/**
	 * Initialize a newly created {@link BasePathList} instance.
	 * @param basePaths The base paths which should be initially contained by this {@link BasePathList} instance.
	 */
	private void init(List<String> basePaths) {
		super.toWrap = new CompoundList<String>(basePaths != null ? basePaths : super.toWrap, stickyBasePath,
				CompoundList.ADD_TO_FIRST);
		Collections.sort(toWrap);
		stickyBasePath.add(getImportBasePath());
	}

	/**
	 * Get the base path for the default import folder.
	 * @return The path for the import folder. Never returns <code>null</code>.
	 */
	public static String getImportBasePath() {
		if(importBasePath == null) {
			importBasePath = APreferenceStore.getConfigDirectory() + "Import" + File.separator;
			IResourceHandler importPathResourceHandler = ResourceHandlerFactory.getResourceHandler(importBasePath);
			if(!importPathResourceHandler.exists()) {
				LoggerFactory.log(Level.INFO, BasePathList.class, "Creating folder " + importBasePath + " for import.");
				try {
					importPathResourceHandler.mkdirs();
				} catch (IOException e) {
					LoggerFactory.log(Level.WARNING, BasePathList.class, "Failed to create import directory " + importBasePath);
				}
			}
		}
		return importBasePath;
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
	 * Tests if the given file / path starts with a valid base path.
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

	/**
	 * Provides a contains method with taking under account that a directory path statement can end with a path separator or not.
	 *
	 * @param path
	 *            The path to be tested if it is contained by {@link BasePathList} or not.
	 * @return <code>true</code> if the given path is in the {@link BasePathList} and <code>false</code> otherwise.
	 */
	@Override
	public boolean contains(Object path) {
		String c = StringUtils.toString(path);
		if (StringUtils.isEmpty(c)) {
			return false;
		}

		for (String s : toWrap) {
			if (StringUtils.equals(s, c)) {
				return true;
			} else {
				IResourceHandler first = ResourceHandlerFactory.getResourceHandler(c);
				IResourceHandler second = ResourceHandlerFactory.getResourceHandler(s);
				if (first != null && second != null && StringUtils.equals(first.getResourceString(), second.getResourceString())) {
					return true;
				}
			}
		}
		return false;
	}
}
