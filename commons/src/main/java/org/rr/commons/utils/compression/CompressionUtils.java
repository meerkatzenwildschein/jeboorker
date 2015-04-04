package org.rr.commons.utils.compression;

public class CompressionUtils {
	
	/**
	 * Tells if the zip data should only be stored and not be compressed.
	 * @param name The name of the zip entry
	 * @return <code>true</code> for store only and <code>false</code> for compress.
	 */
	public static boolean isStoreOnlyFile(String name) {
		name = name.toLowerCase();
		if(name.endsWith(".jpg") || name.endsWith(".jpeg")) {
			return true;
		} else if(name.endsWith(".png")) {
			return true;
		} else if(name.endsWith(".gif")) {
			return true;
		} else if(name.endsWith(".zip") || name.endsWith(".rar")) {
			return true;
		}
		return false;
	}
}
