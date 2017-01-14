package org.rr.commons.utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

public final class CommonUtils implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private CommonUtils() {
	}

	/**
	 * regex Pattern to match double value strings.
	 */
	private static Pattern doublePattern = Pattern.compile("-?\\d+(\\.\\d*)?");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static int compareTo(Object value1, Object value2) {
		if (value1 == null && value2 == null) {
			return 0;
		}

		final Comparable first;
		final Comparable second;
		if (value1 instanceof Comparable) {
			first = (Comparable) value1;
		} else {
			if (value1 != null) {
				first = String.valueOf(value1);
			} else {
				first = "0";
			}
		}

		if (value2 instanceof Comparable) {
			second = (Comparable) value2;
		} else {
			if (value2 != null) {
				second = String.valueOf(value2);
			} else {
				second = "0";
			}
		}

		if (first.getClass().getName().equals(second.getClass().getName())) {
			return first.compareTo(second);
		} else {
			return String.valueOf(first).compareTo(String.valueOf(second));
		}
	}



	/**
	 * Tests the given string if it could be parsed into double.
	 * 
	 * Please note, that the double parser has more options than this method.
	 * This is only a quick test for values like "1.2298" or something else.
	 * 
	 * @param string
	 *            The string to be tested.
	 * @return <code>true</code> if the given string could be pased into a
	 *         Double or <code>false</code> otherwise.
	 */
	public static boolean isDouble(String string) {
		if (string == null || string.length() == 0) {
			return false;
		}

		return doublePattern.matcher(string).matches();
	}

	/**
	 * Tests if the given string matches to an integer value. Valid 
	 * values are +/- at the start of the string and digits only
	 * at the rest.
	 * 
	 * @param string
	 *            The string to be tested.
	 * @return <code>true</code> if we have an integer value and
	 *         <code>false</code> if not.
	 */
	public static boolean isInteger(String string) {
		if (string == null || string.length() == 0) {
			return false;
		}

		final int start;
		if (string.charAt(0) == '-') {
			start = 1;
		} else {
			start = 0;
		}

		for (int i = start; i < string.length(); i++) {
			if (!Character.isDigit(string.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Creates a <code>Number</code> object for the <code>Object</code>
	 * specified with the <code>object</code> parameter.
	 * 
	 * @param object
	 *            The <code>Object</code> to be converted into a
	 *            <code>Number</code>
	 * @return The <code>Number</code> object or <code>0</code> if the
	 *         <code>Object</code> could not be converted.
	 */
	public static final Number toNumber(final Object object) throws NumberFormatException {
		return toNumber(object, 0);
	}
	
	public static final Number toNumber(final Object object, Number defaultValue) throws NumberFormatException {
		if (object instanceof Number) {
			return (Number) object;
		} else if (object == null) {
			return Integer.valueOf(0);
		}

		try {
			String numeric = String.valueOf(object);
			if (numeric == null || numeric.length() == 0) {
				numeric = "0";
			}

			// handle 0x64 and VB &H20AC hex values (convert to decimal ones)
			if (numeric.startsWith("0x") || numeric.startsWith("&H")) {
				numeric = String.valueOf(Integer.parseInt(numeric.substring(2), 16));
			}

			// convert true or false to a numeric node
			if (numeric.toLowerCase().equals("true")) {
				numeric = "1";
			} else if (numeric.toLowerCase().equals("false")) {
				numeric = "0";
			}

			try {
				int comma = numeric.indexOf(',');
				if (comma != -1) {
					numeric = numeric.replace(',', '.');
				}

				// Try to create a BigDecimal value if possible
				try {
					return new BigDecimal(numeric);
				} catch (Exception e) {
				}

				// Try to create an Long if possible
				try {
					return Long.valueOf(numeric);
				} catch (Exception e) {
				}

				try {
					return Double.valueOf(numeric);
				} catch (Exception e) {
				}
			} catch (Exception e) {
				throw new NumberFormatException("Could not format " + object
						+ " to a number");
			}
			throw new NumberFormatException("Could not format " + object
					+ " to a number");
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	/**
	 * Tests if the operating system is a linux os. 
	 * @return <code>true</code> if it's linux and <code>false</code> otherwise.
	 */
	public static boolean isLinux() {
		Object osName = System.getProperties().get("os.name");
		if(String.valueOf(osName).toLowerCase().indexOf("linux") != -1) {
			return true;
		}
		return false;
	}
	

	public static long calculateCrc(byte[] data) {
		CRC32 crc = new CRC32();
		crc.update(data);
		return crc.getValue();
	}
}
