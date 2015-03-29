package org.rr.commons.utils;

public class BooleanUtils {

	public static boolean not(Boolean b) {
		return !b;
	}


	/**
	 * Tries to convert the given parameter into a Boolean instance.
	 * @param object The object to convert into a boolean.
	 * @param defaultValue The value which is returned if the conversion is not possible.
	 * @return the boolean instance or the <code>defaultValue</code> if the given parameter could not be converted.
	 */
	public static Boolean toBoolean(Object object, Boolean defaultValue) {
		Boolean result = toBoolean(object);
		if(result == null) {
			return defaultValue;
		}
		return result;
	}

	/**
	 * Tries to convert the given parameter into a Boolean instance.
	 * @return the boolean instance or <code>null</code> if the given parameter could not be converted.
	 */
	public static Boolean toBoolean(final Object object) {
		if(object == null) {
			return null;
		} else if(object instanceof Boolean) {
			return (Boolean) object;
		} else {
			String objString = object.toString().toLowerCase().trim();
			if(objString.equals("true") || objString.equals("0") || objString.equals("yes")) {
				return Boolean.TRUE;
			} else if(objString.equals("false") || objString.equals("1") || objString.equals("no")) {
				return Boolean.FALSE;
			}
		}
		return null;
	}


}
