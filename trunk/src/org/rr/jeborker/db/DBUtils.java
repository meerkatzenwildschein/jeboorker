package org.rr.jeborker.db;

import org.rr.commons.utils.StringUtils;

public class DBUtils {

	/**
	 * Escape the given string so it can be used in a query
	 * @param s The string to be escaped.
	 * @return The escaped String.
	 */
	public static String escape(String s) {
		String resultValue = s;
		if(resultValue != null) {
			//The Backslash is the escape character and is needed to be doubled.
			if(resultValue.indexOf('\\') != -1) {
					resultValue = StringUtils.replace(resultValue, "\\", "\\\\");
			}
			
			//Single quotes marks the sql string in the query.
			if(resultValue.indexOf('\'') != -1) {
				resultValue = StringUtils.replace(resultValue, "'", "\\'");
			}
		}
		
		return resultValue;
	}
}
