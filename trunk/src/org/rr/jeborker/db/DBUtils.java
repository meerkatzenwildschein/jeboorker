package org.rr.jeborker.db;

import org.rr.commons.utils.StringUtils;

import com.j256.ormlite.stmt.Where;

public class DBUtils {
	
	/**
	 * Tells if the given where condition is empty and unusable or not.
	 * @param where The condition to test.
	 * @return <code>true</code> if the given condition is empty and <code>false</code> otherwise.
	 */
	public static <T> boolean isEmpty(Where<T, T> where) {
		return where == null || where.toString().contains("empty");
	}

	/**
	 * Escape the given string so it can be used in a query
	 * 
	 * @param s
	 *            The string to be escaped.
	 * @return The escaped String.
	 */
	public static String escape(String s) {
		String resultValue = s;
		if (resultValue != null) {
			// The Backslash is the escape character and is needed to be doubled.
			if (resultValue.indexOf('\\') != -1) {
				resultValue = StringUtils.replace(resultValue, "\\", "\\\\");
			}

			// Single quotes marks the sql string in the query.
			if (resultValue.indexOf('\'') != -1) {
				resultValue = StringUtils.replace(resultValue, "'", "\\'");
			}
		}

		return resultValue;
	}
}
