package org.rr.commons.utils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.Charsets;

public final class StringUtil implements Serializable {

	private static final long serialVersionUID = 1505813189815359975L;

	public static final String EMPTY = "";

	public static final String NEW_LINE = System.getProperty("line.separator");

	public static final String UTF_8 = Charsets.UTF_8.name();

	private StringUtil() {}

	/**
	 * An empty string array for public use.
	 */
	public static final String[] emptyStringArray = new String[0];

	/**
	 * Returns a specified number of characters from the left side of a <code>String</code>.
	 *
	 * @param text The string to return characters from.
	 * @param length Specifies how many characters to return. If set to 0, an empty string ("") is returned. If set to greater than or equal to the length of the string, the entire string is returned.
	 * @return The part of the given <code>String</code> or null if the parameter text is null.
	 * @see #len(String) to determine the number of characters in a <code>String</code>.
	 * @see #right(String, int)
	 */
	public static final String left(final String text, int length) {
		if (text==null) {
			return null;
		}

		//If set to 0, an empty string ("") is returned
		if (length==0) {
			return EMPTY;
		}

		//If set to greater than or equal to the length of the string, the entire string is returned.
		if (text.length() <= length) {
			return text;
		}

		return text.substring(0, length);
	}

	/**
	 * Returns a specified number of characters from the right side of a <code>String</code>.
	 * @param text The string to return characters from.
	 * @param length Specifies how many characters to return. If set to 0, an empty string ("") is returned. If set to greater than or equal to the length of the string, the entire string is returned.
	 * @return The part of the given <code>String</code> or null if the parameter text is null.
	 * @see #len(String) function to determine the number of characters in a <code>String</code>.
	 * @see #left(String, int)
	 */
	public static final String right(final String text, int length) {
		if (text==null) {
			return null;
		}

		//If set to 0, an empty string ("") is returned.
		if (length==0) {
			return EMPTY;
		}

		//If set to greater than or equal to the length of the string, the entire string is returned.
		if (text.length() <= length) {
			return text;
		}

		return text.substring(text.length()-length);
	}

	/**
	 * Returns a specified number of characters from a <code>String</code>.
	 * @param text The <code>String</code> from which characters are returned.
	 * @param start Specifies the starting position for the operation. If set to greater than the number of characters in the <code>String</code>, an empty string ("") will be returned.
	 * @param length The number of characters to return.
	 * @return The part of the given <code>String</code> or null if the parameter text is null.
	 * @see len function to determine the number of characters in a <code>String</code>.
	 */
	public static final String mid(final String text, int start, int length) {
		if (text==null) {
			return null;
		}

		//If set to 0, an empty string ("") is returned.
		if (start==0) {
			return EMPTY;
		}

	    if (text.length() < start-1) {
	    	return EMPTY;
	    } else if (text.length() < (start-1) + length) {
	    	return text.substring(start-1);
	    } else {
	    	return text.substring(start-1, (start-1) + length);
	    }
	}

	/**
	 * Returns the number of characters in a <code>String</code>. The following example will calculate the value 11:
	 * <pre>
	 * String text = "Just a Text"
	 * int length = len (text);
	 * </pre>
	 * @param text The <code>String</code> from which character count are returned.
	 * @return The number of characters in the <code>String</code> specified in the parameter text. Returns -1 if the parameter text is null.
	 */
	public static final int len(final String text) {
		if(text==null) {
			return -1;
		}
		return text.length();
	}

	/**
	 * Converts a specified <code>String</code> to lower case. The following example creates the <code>String</code> "just a text" from "Just a Text".
	 * <pre>
	 * String text = "Just a Text"
	 * String lcaseText = lCase (text);
	 * </pre>
	 * @param text The <code>String</code> which should be converted to lowercase.
	 * @return a <code>String</code> that has been converted into lower case characters or <code>null</code> if the parameter text is <code>null</code>.
	 */
	public static final String lCase(final String text) {
		if (text==null) {
			return null;
		}
		return text.toLowerCase();
	}

	/**
	 * Converts a specified <code>String</code> to upper case. The following example creates the <code>String</code> "JUST A TEXT" from "Just a Text".
	 * <pre>
	 * String text = "Just a Text"
	 * String uCaseText = uCase (text);
	 * </pre>
	 * Instead of the java {@link String#toUpperCase()} method, this one did not convert
	 * an &szlig; into a 'SS'. The &szlig; keeps untouched.
	 *
	 * @param text The <code>String</code> which should be converted to upper case.
	 * @return a <code>String</code> that has been converted into upper case characters or <code>null</code> if the parameter text is <code>null</code>.
	 */
	public static final String uCase(final String text) {
		if (text==null) {
			return null;
		}

		//test for german ß
		if(text.indexOf('\u00DF')!=-1) {
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < text.length(); i++) {
				if(text.charAt(i) == '\u00DF') {
					result.append(text.charAt(i));
				} else {
					result.append(String.valueOf(text.charAt(i)).toUpperCase());
				}
			}
			return result.toString();
		}

		return text.toUpperCase();
	}

	/**
	 * Searches for all occurrences specified with the <code>search</code> parameter in the <code>text</code>.
	 * @param text The text to be searched in.
	 * @param search The <code>String</code> value to be searched for in the <code>text</code>
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>
	 * @return The number of occurrences found within the <code>text</code> parameter.
	 */
	public static final synchronized int occurrence(String text, String search, int compare) {
		//should be synchronized because it used a static string array which is not thread save!
		return occurrence(text, new String[] {search}, compare, null, null );
	}

	/**
	 * Searches for all occurrences specified with the <code>search</code> parameter in the <code>text</code>.
	 * @param text The text to be searched in.
	 * @param search The <code>String</code> value to be searched for in the <code>text</code>
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>
	 * @param openExclusion An array with Strings containing the marker that defines the beginning of an area in the given text which will not be processed.
	 * @param closeExclusion An array with Strings containing the marker that defines the end of an area in the given text which will not be processed.
	 * @return The number of occurrences found within the <code>text</code> parameter.
	 */
	public static final synchronized int occurrence(String text, String search, int compare, List<String> openExclusion, List<String> closeExclusion) {
		//should be synchronized because it used a static string array which is not thread save!
		return occurrence(text, new String[] {search}, compare, openExclusion, closeExclusion );
	}

	/**
	 * Searches for all occurrences specified with the <code>search</code> parameter in the <code>text</code>.
	 * @param text The text values to be searched in.
	 * @param search The <code>String</code> values to be searched for in the <code>text</code>
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>
	 * @param openExclusion An array with Strings containing the marker that defines the beginning of an area in the given text which will not be processed.
	 * @param closeExclusion An array with Strings containing the marker that defines the end of an area in the given text which will not be processed.
	 * @return The number of occurrences found within the <code>text</code> parameter.
	 */
	public static final int occurrence(String[] text, String[] search, int compare, List<String> openExclusion, List<String> closeExclusion) {
		int result = 0;
		for (int i = 0; i < text.length; i++) {
			result += occurrence(text[i], search, compare, openExclusion, closeExclusion );
		}
		return result;
	}

	public static final int occurrence(String text, String search) {
		int idx = 0;
		int count = 0;
		while((idx = text.indexOf(search, idx)) != -1) {
			idx += search.length();
			count ++;
		}
		return count;
	}

	/**
	 * Searches for all occurrences specified with the <code>search</code> parameter in the <code>text</code>.
	 * @param text The text to be searched in.
	 * @param search The <code>String</code> values to be searched for in the <code>text</code>
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>
	 * @param openExclusion An array with Strings containing the marker that defines the beginning of an area in the given text which will not be processed.
	 * @param closeExclusion An array with Strings containing the marker that defines the end of an area in the given text which will not be processed.
	 * @return The number of occurrences found within the <code>text</code> parameter.
	 */
	public static final int occurrence(String text, String[] search, int compare, List<String> openExclusion, List<String> closeExclusion) {
		if (compare > UtilConstants.COMPARE_TEXT || compare < UtilConstants.COMPARE_BINARY) {
			throw new RuntimeException("ModeNotSupported");
		}

		//the could be no occurrences here
		if (text == null || text.length()==0) {
			return 0;
		}

		if(openExclusion==null && closeExclusion==null) {
			int result = 0;
			for(String s : search) {
				result += occurrence(text, s);
			}
			return result;
		}

		if(openExclusion==null) {
			openExclusion = Collections.emptyList();
		}

		if (closeExclusion==null) {
			closeExclusion = Collections.emptyList();
		}

		int result = 0;

		//init exclusion countarrays
		int exclusionCount = 0;

		for(int i=0; i<text.length(); i++) {
			//brace count
			if ( exclusionCount<=0 && ListUtils.filter(openExclusion, String.valueOf(text.charAt(i)), true, UtilConstants.COMPARE_BINARY, UtilConstants.SEARCH_DEFAULT).size() > 0 ) {
				exclusionCount++;
				continue;
			} else if ( exclusionCount>0 && ListUtils.filter(closeExclusion, String.valueOf(text.charAt(i)), true, UtilConstants.COMPARE_BINARY, UtilConstants.SEARCH_DEFAULT).size() > 0 ) {
				exclusionCount--;
				continue;
			} else if ( ListUtils.filter(openExclusion, String.valueOf(text.charAt(i)), true, UtilConstants.COMPARE_BINARY, UtilConstants.SEARCH_DEFAULT).size() > 0 ) {
				exclusionCount++;
				continue;
			} else if ( ListUtils.filter(closeExclusion, String.valueOf(text.charAt(i)), true, UtilConstants.COMPARE_BINARY, UtilConstants.SEARCH_DEFAULT).size() > 0 ) {
				exclusionCount--;
				continue;
			}
			startsWith(search, new String[]{text}, 0);
			if (exclusionCount<=0 && (compare==UtilConstants.COMPARE_BINARY && startsWith(search, new String[]{text.toLowerCase()}, i)!=-1) || (compare==UtilConstants.COMPARE_TEXT && startsWith(search, new String[]{text.toLowerCase()}, i)!=-1) ) {
				result++;
			}
		}
		return result;
	}

    static int startsWith(final String[] searchValues, final String[] valueStartsWith, int start) {
        for (int i=0; i<searchValues.length; i++) {
        	for (int j = 0; j < valueStartsWith.length; j++) {
        		String value = valueStartsWith[j].substring(start);
                if ( value.toLowerCase().startsWith(searchValues[i].toLowerCase()) ) {
                    return searchValues[i].length();
                }
			}
        }
        return -1;
    }

	/**
	 * Removes spaces on both sides of a <code>String</code>. The following example creates the <code>String</code> "Just a Text" from " Just a Text  ".
	 * <pre>
	 * String text = " Just a Text  "
	 * String trimText = trim (text);
	 * </pre>
	 * @param text The <code>String</code> that should be processed.
	 * @return without any leading or trailing white spaces or null if the parameter text is null.
	 */
	public static final String trim(final String text) {
		if (text==null) {
			return null;
		}
		return text.trim();
	}

	/**
	 * Trims the given <code>text</code> and takes all given <code>trims</code> under account.
	 * @param text The text to be trimmed.
	 * @param trims The trim chars.
	 * @return The desired trimmed string.
	 */
	public static final String ltrim(String text, char ... trims) {
		if (text==null) {
			return null;
		}
		int len = text.length();
		int start = 0;
		for(int i = 0; i < len; i++) {
			int a = start;
			for(int j = 0; j < trims.length; j++) {
				if(text.charAt(i) == trims[j]) {
					start ++;
					break;
				}
			}
			if(a == start) {
				break;
			}
		}

		if(start > 0) {
			return text.substring(start, len);
		}
		return text;
	}


	/**
	 * Removes spaces on the left side of a <code>String</code>. The following example creates the <code>String</code> "Just a Text  " from " Just a Text  ".
	 * <pre>
	 * String text = " Just a Text  "
	 * String trimText = trim (text);
	 * </pre>
	 * @param text The <code>String</code> that should be processed.
	 * @return without any leading white spaces or null if the parameter text is null.
	 *
	 * @see #rtrim(String)
	 */
	public static final String ltrim(final String text) {
		if (text==null) {
			return null;
		}

		if (text.length()==0) {
			return text;
		}

		for (int i = 0; i < text.length(); i++) {
			if(text.charAt(i)!=' ') {
				return text.substring(i);
			}
		}

		//looks like there is no other character excepting whitespaces in the string. Just cut them all by returning an empty string.
		return EMPTY;
	}

	/**
	 * Removes spaces on the right side of a <code>String</code>. The following example creates the <code>String</code> "Just a Text  " from " Just a Text  ".
	 * <pre>
	 * String text = " Just a Text  "
	 * String trimText = trim (text);
	 * </pre>
	 * @param text The <code>String</code> that should be processed.
	 * @return without any trailing white spaces or null if the parameter text is null.
	 *
	 * @see #ltrim(String)
	 */
	public static final String rtrim(final String text) {
		if (text==null) {
			return null;
		}

		StringBuilder returnValue = new StringBuilder(text.length());
		boolean found = false;

		for (int i=text.length(); i>0; i--) {
			char c = text.charAt(i-1);

			if (found || c != ' ') {
				returnValue.append(c);
				found=true;
			}
		}

		return strReverse(returnValue.toString());
	}

	/**
	 * Creates a string consisting of the specified number of white spaces.
	 * @param size Number of white spaces.
	 * @return A String with the specified number of white spaces.
	 */
	public static final String space(int size) {
		return string(size, ' ');
	}

	/**
	 * Creates a string that contains a repeating character of a specified length.
	 *
	 * @param size Number of recurrences.
	 * @param repeat The character that should be repeated. The first character of the <code>String</code> will be used.
	 * @return A String with the specified number of repeating characters.
	 */
	public static final String string(int size, char repeat) {
		if (size<=0) {
			return EMPTY;
		}
		StringBuilder returnValue = new StringBuilder(size);

		for (int i=0; i<size; i++) {
			returnValue.append(repeat);
		}
		return returnValue.toString();
	}

	/**
	 * Reverse the order of characters in a <code>String</code>
	 *
	 * @param text The <code>String</code> to be reverse.
	 * @return The reversed <code>String</code>
	 */
	public static final String strReverse(final String text) {
		StringBuilder returnValue = new StringBuilder(text.length());
	   for (int i=text.length(); i>0; ) {
		   returnValue.append(text.charAt(--i));
	   }
	   return returnValue.toString();
	}

	/**
	 * Returns the position of the first occurrence of one <code>String</code> within another.
	 * @param start Specifies the starting position for each search. Invoke with 1 for start with the first character.
	 * @param text The <code>String</code> to be searched.
	 * @param search The <code>String</code> to search for.
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>
	 * @return The index for the first match. The first character is at position 1. If the match could not be found, 0 will be returned.
	 * @throws RuntimeException If the parameter mode is not 0 or 1.
	 */
	public static final int inStr(final String text, final String search, int start, int compare) {
		if (compare > UtilConstants.COMPARE_TEXT || compare < UtilConstants.COMPARE_BINARY) {
			throw new RuntimeException("ModeNotSupported");
		}

		if (compare==UtilConstants.COMPARE_BINARY) {
			return text.indexOf(search, start-1)+1;
		} else {
			return text.toLowerCase().indexOf(search.toLowerCase(), start-1)+1;
		}
	}

        /**
	 * Returns the position of the first occurrence of one <code>String</code> within another.
	 * @param start Specifies the starting position for each search. Invoke with 1 for start with the first character.
         * @param end Specifies the end position for each search. The first char starts with 1.
	 * @param text The <code>String</code> to be searched.
	 * @param search The <code>String</code> to search for.
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>
	 * @return The index for the first match. The first character is at position 1. If the match could not be found, 0 will be returned.
	 * @throws RuntimeException If the parameter mode is not 0 or 1.
	 */
        public static final int inStr(final String text, final String search, int start, int end, int compare) {
		if (compare > UtilConstants.COMPARE_TEXT || compare < UtilConstants.COMPARE_BINARY) {
			throw new RuntimeException("ModeNotSupported");
		}

                if(start <= 0) {
                    start=1;
                }

                if(end <= 0 || end > text.length()) {
                    end = text.length() + 1;
                }
                final String testText = (compare == UtilConstants.COMPARE_BINARY ? text.substring(start-1, end-1) : text.substring(start-1, end-1).toLowerCase());
                final String testSearch = compare == UtilConstants.COMPARE_BINARY ? search : search.toLowerCase();

                return testText.indexOf(testSearch) + start;
        }

	/**
	 * Returns the position of the last occurrence of one <code>String</code> within another.
	 * @param text The <code>String</code> to be searched.
	 * @param search The <code>String</code> to search for.
	 * @param start Specifies the starting position for each search. The search begins at the last character if the value -1 is given. The first char starts with 1.
         * @param end Specifies the end position for each search. The first char starts with 1.
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>
	 * @return The index for the last match. The first character is at position 1. If the match could not be found, 0 will be returned.
	 * @throws RuntimeException If the parameter mode is not 0 or 1.
	 */
    public static final int inStrRev(final String text, final String search, int start, int end, int compare) {
		if (compare > UtilConstants.COMPARE_TEXT || compare < UtilConstants.COMPARE_BINARY) {
			throw new RuntimeException("ModeNotSupported");
		}

        if(start <= 0) {
            start=text.length()+1;
        }

        if(end <= 0 || end > text.length()) {
            end = 1;
        }

        //if we have a negative range, just swap.
        if(start < end) {
            int tmp = start;
            start = end;
            end = tmp;
        }

        final String testText = (compare == UtilConstants.COMPARE_BINARY ? text.substring(end-1, start-1) : text.substring(end-1, start-1).toLowerCase());
        final String testSearch = compare == UtilConstants.COMPARE_BINARY ? search : search.toLowerCase();

        return testText.lastIndexOf(testSearch) + end;
    }

	/**
	 * Returns the position of the last occurrence of one <code>String</code> within another.
	 * @param text The <code>String</code> to be searched.
	 * @param search The <code>String</code> to search for.
	 * @param start Specifies the starting position for each search. The search begins at the last character if the value -1 is given.
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>
	 * @return The index for the last match. The first character is at position 1. If the match could not be found, 0 will be returned.
	 * @throws RuntimeException If the parameter mode is not 0 or 1.
	 */
	public static final int inStrRev(final String text, final String search, int start, int compare) {
		if (compare > UtilConstants.COMPARE_TEXT || compare < UtilConstants.COMPARE_BINARY) {
			throw new RuntimeException("ModeNotSupported");
		}

		if (start==-1) {
			if (compare == UtilConstants.COMPARE_BINARY) {
				return text.lastIndexOf(search)+1;
			} else {
				return text.toLowerCase().lastIndexOf(search.toLowerCase())+1;
			}
		}
		if (compare == UtilConstants.COMPARE_BINARY) {
			return text.lastIndexOf(search, start-1)+1;
		} else {
			return text.toLowerCase().lastIndexOf(search.toLowerCase(), start-1)+1;
		}
	}

	/**
	 * Replaces a specified part of a <code>String</code> with another <code>String</code> a specified number of times.
	 *
	 * @param text The <code>String</code> to be searched.
	 * @param search The part of the <code>String</code> that will be replaced.
	 * @param replacement The replacement <code>String</code>.
	 * @param start Specifies the start position. Use 1 for starting with the first character.
	 * @param count Specifies the number of substitutions to perform. Use -1 for making all possible substitutions.
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = VBConstants.COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = VBConstants.COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>
	 * @return The processed <code>String</code>.
	 * @throws RuntimeException If the parameter mode is not 0 or 1.
	 */
	public static final String replace(final String text, final String search, final String replacement, int start, int count, int compare) {
		return replace(text, search, replacement, start, count, compare, null, null);
	}

	/**
	 * This is just a convenience method and does the same as the <code>replace(String, String, String, int, int, int)</code> method.
	 * The replacement starts at the position 0 and performs all possible substitutions. The comperatrion method is VBConstants.COMPARE_BINARY.
	 *
	 * @param text The <code>String</code> to be searched.
	 * @param search The part of the <code>String</code> that will be replaced.
	 * @param replacement The replacement <code>String</code>.
	 * @return The processed <code>String</code>.
	 */
	public static final String replace(final String text, final String search, final String replacement) {
		return replace(text, search, replacement, 0, -1, UtilConstants.COMPARE_BINARY, null, null);
	}

    /**
	 * This is just a convenience method and does the same as the <code>replace(String, String, String, int, int, int)</code> method to
     * all entries in search. The replacement starts at the position 0 and performs all possible substitutions.
	 *
	 * @param text The <code>String</code> to be searched.
	 * @param search The parts of the <code>String</code> that will be replaced.
	 * @param replacement The replacement <code>String</code>.
	 * @return The processed <code>String</code>.
	 */
	public static final String replace(final String text, final String[] search, final String replacement, int compare) {
            String result = text;
            for (int i = 0; i < search.length; i++) {
                result = replace(result, search[i], replacement, 0, -1, compare, null, null);

            }
            return result;
	}

	/**
	 * convenience method and does the same as the <code>replace(String, String, String[], int)</code> but
	 * with UtilConstants.COMPARE_BINARY as compare parameter.
	 *
	 * @see #replace(String, String[], String, int)
	 */
	public static final String replace(final String text, final String[] search, final String replacement) {
		return replace(text, search, replacement, UtilConstants.COMPARE_BINARY);
	}

	/**
	 * Replaces a specified part of a <code>String</code> with another <code>String</code> a specified number of times. No replacements
	 * will be performed between The open and close definition characters defined with the parameters exclusionBegin and exclusionEnd.
	 *
	 * @param text The <code>String</code> to be searched.
	 * @param search The part of the <code>String</code> that will be replaced.
	 * @param replacement The replacement <code>String</code>
	 * @param start Specifies the start position. Use 1 for starting with the first character.
	 * @param count Specifies the number of substitutions to perform. Use -1 for making all possible substitutions.
	 * @param compare Specifies the string comparison to use.
	 * @param openExclusion An array with Strings containing the marker that defines the beginning of an area in the given text which will not be processed.
	 * @param closeExclusion An array with Strings containing the marker that defines the end of an area in the given text which will not be processed.
	 * <ul>
	 * <li>0 = VBConstants.COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = VBConstants.COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>
	 * @return The processed <code>String</code>.
	 * @throws RuntimeException If the parameter mode is not 0 or 1.
	 */
	static final String replace(final String text, final String search, final String replacement, int start, int count, int compare, List<String> openExclusion, List<String> closeExclusion) {
		if (compare > UtilConstants.COMPARE_TEXT || compare < UtilConstants.COMPARE_BINARY) {
			throw new RuntimeException("ModeNotSupported");
		}
		if(text==null) {
			return EMPTY;
		}

		if(openExclusion==null) {
			openExclusion = Collections.emptyList();
		}
		if(closeExclusion==null) {
			closeExclusion = Collections.emptyList();
		}

		StringBuilder testBuffer = new StringBuilder(text);

		//correct the start position to allow searching from the 0th position
		if (start==0) {
			start=1;
		}

		//init exclusion countarrays
		int exclusionCount = 0;

		for(int i=(start-1); i<testBuffer.length() && count !=0; i++) {
			//Bracket count
			if ( exclusionCount<=0 && ListUtils.filter(openExclusion, String.valueOf(testBuffer.toString().charAt(i)), true, UtilConstants.COMPARE_BINARY, UtilConstants.SEARCH_DEFAULT).size() > 0 ) {
				exclusionCount++;
				continue;
			} else if ( exclusionCount>0 && ListUtils.filter(closeExclusion, String.valueOf(testBuffer.toString().charAt(i)), true, UtilConstants.COMPARE_BINARY, UtilConstants.SEARCH_DEFAULT).size() > 0 ) {
				exclusionCount--;
				continue;
			} else if ( ListUtils.filter(openExclusion, String.valueOf(testBuffer.toString().charAt(i)), true, UtilConstants.COMPARE_BINARY, UtilConstants.SEARCH_DEFAULT).size() > 0 ) {
				exclusionCount++;
				continue;
			} else if ( ListUtils.filter(closeExclusion, String.valueOf(testBuffer.toString().charAt(i)), true, UtilConstants.COMPARE_BINARY, UtilConstants.SEARCH_DEFAULT).size() > 0 ) {
				exclusionCount--;
				continue;
			}

			if (exclusionCount<=0 && (compare==UtilConstants.COMPARE_BINARY && testBuffer.toString().startsWith(search, i)) || (compare==UtilConstants.COMPARE_TEXT && testBuffer.toString().toLowerCase().startsWith(search.toLowerCase(), i)) ) {
				//found - replace the value that was found
				testBuffer.replace(i, i+search.length(), replacement);

				//correct the index
				i += replacement.length()-1;

				//count
				count--;
			}
		}
		return testBuffer.toString();
	}

	/**
	 * Compares two <code>String</code> and returns a value that represents the result of the comparison.
	 * @param text1
	 * @param text2
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>
	 * @return The comperation value.
	 * @throws RuntimeException If the parameter mode is not 0 or 1.
	 */
	public static final int strComp(final String text1, final String text2, int compare) {
		if (compare > UtilConstants.COMPARE_TEXT || compare < UtilConstants.COMPARE_BINARY) {
			throw new RuntimeException("ModeNotSupported");
		}

		if (compare==UtilConstants.COMPARE_BINARY) {
			return text1.compareTo(text2);
		} else {
			return text1.toLowerCase().compareTo(text2.toLowerCase());
		}
	}

    /**
     * Removes or Preserves all characters specified with the parameter text2 in the
     * text given with parameter text1.
     * @param text1 <code>String</code> which should be striped.
     * @param text2 <code>String</code> containing all characters to be removed or obtained from the String specified with the parameter text1
     *
     * @param preserve Specifies if the characters in text2 should be removed or preserved in text1.
     * <code>false</code> will preserve the characters specified in text2 and strips all other characters.
     * <code>true</code> removes all characters specified in text2 from text1.
     * @param compare Specifies the string comparison to use.
     * <ul>
     * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
     * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
     * </ul>
     * @return The striped <code>String</code> or null if one of the parameters is <code>null</code>.
     */
    public static String strip(String text1, String text2, boolean preserve, int compare) {
        if(text2 == null || text1 == null) {
            return EMPTY;
        }
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < text1.length(); i++) {
            char stripChar = text1.charAt(i);

            int stripCharIndex = -1;
            if (compare == UtilConstants.COMPARE_TEXT) {
                stripCharIndex = text2.toLowerCase().indexOf(String.valueOf(stripChar).toLowerCase());
            } else {
                stripCharIndex = text2.indexOf(stripChar);
            }

            if(preserve && stripCharIndex == -1) {
                result.append(stripChar);
            } else if (!preserve && stripCharIndex != -1) {
                result.append(stripChar);
            }
        }

        return result.toString();
    }
    
	/**
	 * Remove a given trailing character from a given text and return the result. If the given text ends with a multiple amount of the
	 * trailing character only the last one is removed.
	 * 
	 * @param text The text which should be striped.
	 * @param c The character to be tested in <code>text</code>.
	 * @return The striped text or <code>null</code> if the given text is <code>null</code>.
	 */
	public static String stripTrailing(String text, char c) {
		if (text != null && text.charAt(text.length() - 1) == c) {
			return text.substring(0, text.length() - 1);
		}
		return text;
	}
	
	/**
	 * Remove a given trailing character from a given text and return the result. If the given text ends with a multiple amount of the
	 * trailing character only the last one is removed.
	 * 
	 * @param text The text which should be striped.
	 * @param c Some character to be tested in <code>text</code>.
	 * @return The striped text or <code>null</code> if the given text is <code>null</code>.
	 */
	public static String stripTrailing(String text, char[] c) {
		for (char d : c) {
			String stripped = stripTrailing(text, d);
			if (text != stripped) {
				return stripped;
			}
		}
		return text;
	}

    /**
     * Creates a <code>String</code> with space (" ") characters appended
     * at the beginning of the string to give the <code>String</code> a length
     * equal to the <code>length</code> parameter. If
     * the length of <code>text</code> is greater than <code>length</code>,
     * than the <code>text</code> will be cut to the <code>length</code>.
     *
     * @param text a string to operate on
     * @param length the desired final length of the string
     * @return a string with spaces prepended to it, or just <code>string</code>
     *
     * @see #padRight(String, int)
     * @see #fillLeft(String, char, int)
     */
    public static String padLeft(String text, int length) {
    	return fillLeft(text, ' ', length);
    }

    /**
     * Creates a <code>String</code> with <code>fillChar</code> characters appended
     * at the beginning of the string to give the <code>String</code> a length
     * equal to the <code>length</code> parameter. If
     * the length of <code>text</code> is greater than <code>length</code>,
     * than the <code>text</code> will be cut to the <code>length</code>.
     * <BR><BR>
     * Does the same as <code>{@link #padLeft(String, int)}</code> but
     * allows to specify the character to be used for filling up the String.
     *
     * @param text a string to operate on
     * @param fillChar characters to use for prepend.
     * @param length length the desired final length of the string
     * @return a string with spaces prepended to it, or just <code>string</code>
     *
     * @see #padLeft(String, int)
     */
    public static String fillLeft(String text, char fillChar, int length) {
    	if (text.length() > length) {
    		return text.substring(0, length);
    	}

    	StringBuilder fillBuffer = new StringBuilder(length);
    	int textLength = text.length();
    	synchronized (fillBuffer) {
	    	while (textLength++ < length) {
	    		fillBuffer.append(fillChar);
	    	}
    	}
    	fillBuffer.append(text);
    	return fillBuffer.toString();
    }

    /**
     * Creates a <code>String</code> with space (" ") characters appended
     * at the end of the string to give the <code>String</code> a length
     * equal to the <code>length</code> parameter. If
     * the length of <code>text</code> is greater than <code>length</code>,
     * than the <code>text</code> will be cut to the <code>length</code>.
     *
     * @param text a string to operate on
     * @param length the desired final length of the string
     * @return a string with spaces prepended to it, or just <code>string</code>
     *
     * @see #padLeft(String, int)
     */
    public static String padRight(String text, int length) {
    	return fillRight(text, ' ', length);
    }

    /**
     * Creates a <code>String</code> with <code>fillChar</code> characters appended
     * at the end of the string to give the <code>String</code> a length
     * equal to the <code>length</code> parameter.  If
     * the length of <code>text</code> is greater than <code>length</code>,
     * than the <code>text</code> will be cut to the <code>length</code>.
     * <BR><BR>
     * Does the same as <code>{@link #padRight(String, int)}</code> but
     * allows to specify the character to be used for filling up the String.
     *
     * @param text a string to operate on
     * @param fillChar characters to use for append.
     * @param length length the desired final length of the string
     * @return a string with spaces prepended to it, or just <code>string</code>
     *
     * @see #padRight(String, int)
     */
    public static String fillRight(String text, char fillChar, int length) {
    	if (text.length() > length) {
    		return text.substring(0, length);
    	}

    	StringBuilder fillBuffer = new StringBuilder(length);
    	fillBuffer.append(text);
    	int textLength = text.length();
    	synchronized (fillBuffer) {
	    	while (textLength++ < length) {
	    		fillBuffer.append(fillChar);
	    	}
    	}
    	return fillBuffer.toString();
    }

    /**
     * Ensure that the first character of the provided string is upper case. If the first character
     * is an &szlig; it won't capitalized.
     *
     * @param text The <code>String</code> to be capitalized.
     * @return The capitalized <code>String</code>.
     */
    public static String capitalize(String text) {
    	StringBuilder result = new StringBuilder(toString(text));
    	if (result.length()==0) {
    		return text;
    	} else if (result.length() > 1 && Character.isUpperCase(result.charAt(0))) {
			return text;
		}

    	//replace the first character with an uppercase one.
    	result.setCharAt( 0, uCase( String.valueOf(result.charAt(0)) ).charAt(0) );

    	return result.toString();
    }

    /**
     * Ensure that the first character of the provided string is lower case. If the first character
     * is an &szlig; it won't uncapitalized.
     *
     * @param text The <code>String</code> to be capitalized.
     * @return The capitalized <code>String</code>.
     */
    public static String uncapitalize(String text) {
    	if(text==null) {
    		return null;
    	}

    	StringBuilder result = new StringBuilder(text);
    	if (result.length()==0) {
    		return text;
    	} else if (result.length() > 1 && Character.isLowerCase(result.charAt(0))) {
			return text;
		}

    	//replace the first character with an uppercase one.
    	result.setCharAt( 0, lCase( String.valueOf(result.charAt(0)) ).charAt(0) );

    	return result.toString();
    }

    /**
     * The <code>{@link #normalize(String, char[], boolean)}<7CODE> method is designed for removing
     * unwanted characters within a <code>{@link String}</code>. The characters to be removed or to
     * be kept can be specified with the <code>chars</code> parameter. These characters are kept
     * or removed depending on the <code>allow</code> parameter. the result is a String which no longer
     * contains any unwanted character.
     *
     * @param text The text to be normalized. If this parameter is <code>null</code>, the result is also <code>null</code>.
     * @param chars The characters to be kept or removed. If the given array is <code>null</code>, it will be handled like an empty array.
     * 	You can also use the predefined character sets <code>{@link #CHARSET_ASCII7}</code> or <code>{@link #CHARSET_BASE_64}</code>
     * @param allow The allow flag toggles the keep or remove option. Is this property set to <code>true</code>,
     * all characters within the <code>chars</code> array will be removed from the String. On <code>false</code>
     * the characters specified with the <code>chars</code> array are these ones which will be kept back. That
     * means that all the other characters will be removed for the result.
     *
     * @return The normalized {@link String} without any unwanted character.
     */
    public static String normalize(String text, char[] chars, boolean allow) {
    	if(text==null) {
    		return text;
    	}

    	if(chars==null) {
    		chars = new char[0];
    	}

    	StringBuilder result = new StringBuilder();

    	for (int i = 0; i < text.length(); i++) {
			char textProcessChar = text.charAt(i);
			boolean charFound = false;
			for (int j = 0; j < chars.length; j++) {
				if(chars[j]==textProcessChar) {
					charFound = true;
				}
			}

			if(charFound==true && allow) {
				result.append(textProcessChar);
			} else if (charFound==false && !allow) {
				result.append(textProcessChar);
			}
		}

    	return result.toString();
    }

    /**
     * Creates the substring of the parameter <code>text1</code> that
     * follows the first occurrence of the parameter <code>text2</code>
     * in the <code>text1</code> parameter string, or the empty string if the <code>text1</code>
     * parameter string does not contain the <code>text2</code> parameter string. If
     * the <code>text1</code> parameter is <code>null</code>, <code>null</code> will be returned.
     * Should never throws any kind of <code>{@link Exception}</code>.
     * <BR><BR>
     *
     * <pre><b>Examples:</b><code>
     * substringAfter("1999/05/01","/") = "05/01"
     * substringAfter("1999/05/01","19") = "99/05/01"
     * substringAfter("1999/05/01","2") = ""
     * </code></pre>
     *
     * @param text1 The string to be searched for a match specified with the <code>text2</code> parameter.
     * @param search The string to be searched within the <code>text1</code> parameter string.
     * @param forward <code>true</code> if the <code>text2</code> should be searched forward in <code>text1</code> or
     *  <code>false</code> for backward searching. backward searching will find the last occurrence of <code>text2</code> in <code>text1</code>.
     * @param returnAllForNotFound if <code>true</code>, the result for <code>substringAfter("1999/05/01","2")</code> is not
     * 	an empty string but the full string given with <code>text1</code>
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>

     * @return The substring to be created.
     */
    public static String substringAfter(String text1, String search, boolean forward, boolean returnAllForNotFound, int compare) {
    	final String result = substringAfter(text1, search, forward, compare);
    	if(returnAllForNotFound && result!=null && result.length()==0) {
    		return text1;
    	}
    	return result;
    }

    /**
     * Creates the substring of the parameter <code>text1</code> that
     * follows the first occurrence of the parameter <code>text2</code>
     * in the <code>text1</code> parameter string, or the empty string if the <code>text1</code>
     * parameter string does not contain the <code>text2</code> parameter string. If
     * the <code>text1</code> parameter is <code>null</code>, <code>null</code> will be returned.
     * Should never throws any kind of <code>{@link Exception}</code>.
     * <BR><BR>
     *
     * <pre><b>Examples:</b><code>
     * substringAfter("1999/05/01","/") = "05/01"
     * substringAfter("1999/05/01","19") = "99/05/01"
     * substringAfter("1999/05/01","2") = ""
     * </code></pre>
     *
     * @param text1 The string to be searched for a match specified with the <code>text2</code> parameter.
     * @param search The string to be searched within the <code>text1</code> parameter string.
     * @param forward <code>true</code> if the <code>text2</code> should be searched forward in <code>text1</code> or
     *  <code>false</code> for backward searching. backward searching will find the last occurrence of <code>text2</code> in <code>text1</code>.
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>

     * @return The substring to be created.
     */
    public static String substringAfter(String text1, String search, boolean forward, int compare) {
    	if (text1==null) {
    		return null;
    	} else if (search == null) {
    		return EMPTY;
    	}

    	// SHOULD BE EXACTLY THE SAME AS IN THE #substringBefore METHOD!!!
    	int index;
    	if( compare == UtilConstants.COMPARE_TEXT ) {
    		if(forward) {
    			index = text1.toLowerCase().indexOf(search.toLowerCase());
    		} else {
    			index = text1.toLowerCase().lastIndexOf(search.toLowerCase());
    		}
    	} else {
    		if(forward) {
    			index = text1.indexOf(search);
    		} else {
    			index = text1.lastIndexOf(search);
    		}
    	} // END EXAXTLY THE SAME

    	if(index==-1) {
    		return EMPTY;
    	}

    	return text1.substring(index+search.length());
    }

    public static String substringAfter(String text1, String search, boolean forward) {
    	return substringAfter(text1, search, forward, UtilConstants.COMPARE_BINARY);
    }

    /**
     * Creates the substring of the parameter <code>text1</code> that
     * follows the first occurrence of the parameter <code>text2</code>
     * in the <code>text1</code> parameter string, or the empty string if the <code>text1</code>
     * parameter string does not contain the <code>text2</code> parameter string. If
     * the <code>text1</code> parameter is <code>null</code>, <code>null</code> will be returned.
     * Should never throws any kind of <code>{@link Exception}</code>.
     * <BR><BR>
     *
     * <pre><b>Examples:</b><code>
     * substringBefore("1999/05/01","/") = "1999"
     * substringBefore("1999/05/01","19") = ""
     * substringBefore("1999/05/01","2") = ""
     * </code></pre>
     *
     * @param text1 The string to be searched for a match specified with the <code>text2</code> parameter.
     * @param search The string to be searched within the <code>text1</code> parameter string.
     * @param forward <code>true</code> if the <code>text2</code> should be searched forward in <code>text1</code> or
     *  <code>false</code> for backward searching. backward searching will find the last occurrence of <code>text2</code> in <code>text1</code>.
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>

     * @return The substring to be created.
     */
    public static String substringBefore(String text1, String search, boolean forward, int compare) {
    	if (text1==null) {
    		return null;
    	} else if (search == null) {
    		return EMPTY;
    	}

    	// SHOULD BE EXACTLY THE SAME AS IN THE #substringAfter METHOD!!!
    	int index;
    	if( compare == UtilConstants.COMPARE_TEXT ) {
    		if(forward) {
    			index = text1.toLowerCase().indexOf(search.toLowerCase());
    		} else {
    			index = text1.toLowerCase().lastIndexOf(search.toLowerCase());
    		}
    	} else {
    		if(forward) {
    			index = text1.indexOf(search);
    		} else {
    			index = text1.lastIndexOf(search);
    		}
    	} // END EXAXTLY THE SAME

    	if(index==-1) {
    		return EMPTY;
    	}

    	return text1.substring(0, index);
    }

    public static String substringBefore(String text1, String search, boolean forward) {
    	return substringBefore(text1, search, forward, UtilConstants.COMPARE_BINARY);
    }

    /**
     * Simply returns the string representation of the given object. If the
     * given object is <code>null</code>, an empty string is returned.
     *
     * @param value The value where the string representation should be fetched for.
     * @return The string representation. Never returns <code>null</code>.
     */
    public static String toString(Object value) {
    	if(value==null) {
    		return EMPTY;
    	}
    	return String.valueOf(value);
    }

    /**
     * Simply returns the string representation of the given object. If the
     * given object is <code>null</code>, an empty string is returned.
     *
     * @param value The value where the string representation should be fetched for.
     * @return The string representation. Never returns <code>null</code>.
     */
    public static String toString(Object value, boolean preserveNull) {
    	if(value == null) {
    		if(preserveNull) {
    			return null;
    		} else {
    			return EMPTY;
    		}
    	}
    	return String.valueOf(value);
    }

    /**
     * Removes multiple white spaces from the given String.
     * @param text The string where the white spaces should be removed from.
     * @return The cleaned text or <code>null</code> if the given text is <code>null</code>.
     */
    public static String removeMultipleWhiteSpaces(String text) {
    	if(text == null) {
    		return null;
    	}
    	return text.trim().replaceAll(" +", " ");
    }

    /**
     * Tests a string if the first two arguments cross match to the second both arguments.
     * For example "create" and "creation" should be compared as a true value, both can be
     * given as the second  argments to return true. If any of the first both string did not
     * match to the second ones, <code>false</code> is returned.
     *
     * @param first First string to be tested
     * @param second Second string to be tested
     * @param compFirst The first result
     * @param compSecond The second result
     * @return <code>true</code> if match and <code>false</code> otherwise.
     */
    public static boolean compareTwice(final String first, final String second, final String compFirst, final String compSecond) {
    	if(first == null || second == null || compFirst == null || compSecond == null) {
    		return false;
    	}

    	if(first.equals(compFirst) || first.equals(compSecond)) {
        	if(second.equals(compFirst) || second.equals(compSecond)) {
        		return true;
        	}
    	}
    	return false;
    }

    /**
     * Tests if the given string is empty. That means it's <code>null</code> or
     * have a trimmed length of null.
     * @param text The text to be tested.
     * @return <code>true</code> if the string is empty or <code>false</code> otherwise.
     */
    public static boolean isEmpty(CharSequence text) {
		if (text == null) {
			return true;
		}
		for (int i = 0; i < text.length(); i++) {
			if (! Character.isWhitespace(text.charAt(i))) {
				return false;
			}
		}
		return true;
    }

    /**
     * Tests if the given string is not empty. That means it's <code>null</code> or
     * have a trimmed length of null.
     * @param text The text to be tested.
     * @return <code>true</code> if the string is not empty or <code>false</code> otherwise.
     */
    public static boolean isNotEmpty(CharSequence text) {
    	return !isEmpty(text);
    }


    /**
     * <p>Checks if a String is whitespace, empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     * @since 2.0
     */
    public static boolean isBlank(CharSequence str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks if a String is not empty (""), not null and not whitespace only.</p>
     *
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is
     *  not empty and not null and not whitespace
     * @since 2.0
     */
    public static boolean isNotBlank(CharSequence str) {
        return !StringUtil.isBlank(str);
    }

    /**
     * Encodes a string into a Soundex value. Soundex is an encoding used to
     * relate similar names, but can also be used as a general
     * purpose scheme to find word with similar phonemes.
     *
     * @param stringToEncode The <code>String</code> to be encoded using the soundex algorithm.
     * @return The soundex encoded <code>String</code>
     */
    public static String soundex(String stringToEncode) {
        int CODE_LEN = 4; //the soundex code has a length of 4 digits

        char[] charArrayToEncode = stringToEncode.toUpperCase().toCharArray();
        char firstLetter = charArrayToEncode[0];

        //Loop all letters to convert them to numeric code
        for (int i = 0; i < charArrayToEncode.length; i++) {
        	//Only handle ASCII letters
        	if (charArrayToEncode[i]>='A' && charArrayToEncode[i]<='Z') {
	            switch (charArrayToEncode[i]) {
	                case 'B':
	                case 'F':
	                case 'P':
	                case 'V': { charArrayToEncode[i] = '1'; break; }

	                case 'C':
	                case 'G':
	                case 'J':
	                case 'K':
	                case 'Q':
	                case 'S':
	                case 'X':
	                case 'Z': { charArrayToEncode[i] = '2'; break; }

	                case 'D':
	                case 'T': { charArrayToEncode[i] = '3'; break; }

	                case 'L': { charArrayToEncode[i] = '4'; break; }

	                case 'M':
	                case 'N': { charArrayToEncode[i] = '5'; break; }

	                case 'R': { charArrayToEncode[i] = '6'; break; }

	                default:  { charArrayToEncode[i] = '0'; break; }
	            }
        	} else {
        		charArrayToEncode[i] = '0';
        	}
        }

        //Remove all duplicates and 0 values
        StringBuilder result = new StringBuilder(String.valueOf(firstLetter));
        char last = charArrayToEncode[0];
        for (int i = 1; i < charArrayToEncode.length; i++) {
            if (charArrayToEncode[i] != '0' && charArrayToEncode[i] != last) {
                result.append(charArrayToEncode[i]);
                if (result.length()==CODE_LEN) break;
            }
            last = charArrayToEncode[i];
        }

        //Pad with 0
        for (int i = result.length(); i < CODE_LEN; i++) {
        	result.append('0');
        }

        return result.toString();
    }

    /**
     * Encodes a <code>String</code> using the trivial "Rot13" substitution cipher.
     * Rot13 works by replacing each upper
	 * and lower case letters with the letter 13 positions ahead or behind
	 * it in the alphabet. The encryption algorithm is symmetric - applying
	 * the same algorithm a second time recovers the original message.
	 *
     * @param strinToEncode The <code>String</code> to be encoded with the rot13 algorithm.
     * @return The rot13 encoded or decodes <code>String</code>.
     */
    public static String rot13(String strinToEncode) {
    	StringBuilder result = new StringBuilder(strinToEncode.length());
        for (int i = 0; i < strinToEncode.length(); i++) {
            char c = strinToEncode.charAt(i);
            if       (c >= 'a' && c <= 'm') c += 13;
            else if  (c >= 'n' && c <= 'z') c -= 13;
            else if  (c >= 'A' && c <= 'M') c += 13;
            else if  (c >= 'A' && c <= 'Z') c -= 13;
            result.append(c);
        }
        return result.toString();
    }

    /**
     * Decodes a punycode encoded string. For example "abcdef-qua4k" to "abæcdöef"
     * @param toDecode The string to be decoded.
     * @return The decoded String.
     */
    public static String decodePunycode(String toDecode) {
    	try {
    		if(toDecode!=null) {
	    		String decoded = Punycode.decode(toDecode);
				return decoded.toString();
    		}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }

    /**
     * Encodes a string with punycode. For example "abæcdöef" to "abcdef-qua4k"
     * @param toEncode The string to be encoded.
     * @return The encoded string.
     */
    public static String encodePunycode(String toEncode) {
    	if(toEncode == null) {
    		return null;
    	}

    	try {
			String encoded = Punycode.encode(toEncode);
			return encoded;
		} catch (Punycode.PunycodeException e) {
			return null;
		}
    }

    /**
     * Decodes an URL encoded string. For example something like "abc%20d" to "abc d"
     * @param toDecode The string to be decoded.
     * @return The decoded string or <code>null</code> if the encoding fails.
     */
    public static String decodeURL(String toDecode) {
    	if(toDecode == null) {
    		return null;
    	}

    	try {
    		if(toDecode.indexOf('%')!=-1) {
    			return URLDecoder.decode(toDecode, "UTF-8");
    		} else {
    			return toDecode;
    		}
		} catch (UnsupportedEncodingException e) {
			return null;
		}
    }

    /**
     * Decodes an URL encoded string. For example something like "abc%20d" to "abc d"
     * @param toEncode The string to be decoded.
     * @return The decoded string or <code>null</code> if the encoding fails.
     */
    public static String encodeURL(String toEncode) {
    	if(toEncode == null) {
    		return null;
    	}

    	try {
			return URLEncoder.encode(toEncode, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
    }

    /**
     * Tests if the given text ends with the given search char. Works with
     * String as well as with StringBuilder or any other {@link CharSequence} implementations.
     * @param text The text to be tested if it ends with.
     * @param search The char to be tested if the given text ends with it.
     * @return <code>true</code> if the given text ends with the given char and <code>false</code> otherwise.
     */
    public static boolean endsWith(CharSequence text, char search) {
    	if(text != null && text.length() > 0) {
    		return text.charAt(text.length() - 1) == search;
    	}
    	return false;
    }

    /**
     * Tests if the given text ends with the given search String. Works with
     * String as well as with StringBuilder or any other {@link CharSequence} implementations.
     * @param text The text to be tested if it ends with.
     * @param search The String to be tested if the given text ends with it.
     * @return <code>true</code> if the given text ends with the given search String and <code>false</code> otherwise.
     */
    public static boolean endsWith(CharSequence text, String search) {
    	if(text != null && text.length() >= search.length()) {
    		for(int i = search.length() - 1, j = text.length() - 1; i >= 0; i--, j--) {
    			char textChar = text.charAt(j);
    			char searchChar = search.charAt(i);
    			if(textChar != searchChar) {
    				return false;
    			}
    		}
    	}
    	return true;
    }

    /**
     * Searches for the given <code>search</code> string in the given <code>text</code>.
     * This implementation is faster than using {@link #inStr(String, String, int, int)}.
     * @param text The text to be searched.
     * @param search The search string to find.
     * @param start The start location for the find process in the <code>text</code>
	 * @param compare Specifies the string comparison to use.
	 * <ul>
	 * <li>0 = COMPARE_BINARY - Perform a binary comparison (case sensitive)</li>
	 * <li>1 = COMPARE_TEXT - Perform a textual comparison (case insensitive)</li>
	 * </ul>
     * @return The index of the searched string.
     */
    public static int find(CharSequence text, String search, int start, int compare) {
    	if(search == null || search.length() == 0) {
    		return 0;
    	}

    	if(text == null || text.length() == 0) {
    		return -1;
    	}

    	if(start < 0) {
    		start = 0;
    	}

    	final int length = text.length() - search.length() + 1;
    	final char firstSearchChar = compare == UtilConstants.COMPARE_BINARY ? search.charAt(0) : Character.toLowerCase(search.charAt(0));
    	for (int i = start; i < length; i++) {
    		final char c = text.charAt(i);
    		if(c == firstSearchChar) {
    			boolean found = true;
    			int searchLen = search.length();
    			for(int j = 0; j < searchLen; j++) {
    				final char cj = text.charAt(i + j);
    				final char cs = search.charAt(j);
    				if(compare == UtilConstants.COMPARE_BINARY && cj != cs) {
    					found = false;
    					break;
    				} else if (compare == UtilConstants.COMPARE_TEXT
    						&& Character.toLowerCase(cj) != Character.toLowerCase(cs)) {
    					found = false;
    					break;
    				}
    			}

    			if(found) {
    				return i;
    			}
    		}
    	}
    	return -1;
    }


	/**
	 * Quotes the given text with the given char.
	 * @param text Text that should be surrounded with the given quote char.
	 * @param quote quote char
	 * @return The quoted text. Returns <code>null</code> if the given text is <code>null</code>.
	 */
	public static String quote(String text, char quote) {
		if(text == null) {
			return null;
		}
		return quote + text + quote;
	}

	/**
	 * Whether the given source string ends with the given suffix, ignoring case.
	 *
	 * @param source
	 * @param suffix
	 * @return
	 */
	public static boolean endsWithIgnoreCase(String source, String suffix) {
		if (isEmpty(suffix)) {
			return true;
		}
		if (isEmpty(source)) {
			return false;
		}
		if (suffix.length() > source.length()) {
			return false;
		}
		return source.substring(source.length() - suffix.length()).toLowerCase().endsWith(suffix.toLowerCase());
	}

		/**
	 * If the given text is null return "", the original text otherwise.
	 *
	 * @param text
	 * @return
	 */
	public static String defaultIfNull(String text) {
		return defaultIfNull(text, "");
	}

	/**
	 * If the given text is null return "", the given defaultValue otherwise.
	 *
	 * @param text
	 * @param defaultValue
	 * @return
	 */
	public static String defaultIfNull(String text, String defaultValue) {
		if (text == null) {
			return defaultValue;
		}
		return text;
	}

	/**
	 * Null-safe string comparator
	 *
	 * @param text1
	 * @param text2
	 * @return
	 */
	public static boolean equals(String text1, String text2) {
		if (text1 == null) {
			return (text2 == null);
		}
		return text1.equals(text2);
	}
	

	/**
	 * Null-safe string comparator ignoring case
	 *
	 * @param text1
	 * @param text2
	 * @return
	 */
	public static boolean equalsIgnoreCase(String text1, String text2) {
		if (text1 == null) {
			return (text2 == null);
		}
		return text1.equalsIgnoreCase(text2);
	}

	/**
	 * Pretty toString printer.
	 *
	 * @param keyValues
	 * @return
	 */
	public static String toString(Object ... keyValues) {
		StringBuilder result = new StringBuilder();
		result.append('[');
		for (int i = 0; i < keyValues.length; i += 2) {
			if (i > 0) {
				result.append(", ");
			}
			result.append(keyValues[i]);
			result.append(": ");
			Object value = null;
			if ((i + 1) < keyValues.length) {
				value = keyValues[i + 1];
			}
			if (value == null) {
				result.append("<null>");
			} else {
				result.append('\'');
				result.append(value);
				result.append('\'');
			}
		}
		result.append(']');
		return result.toString();
	}

	public static int hashCode(String ... values) {
		int result = 31;
		for (int i = 0; i < values.length; i++) {
			result ^= String.valueOf(values[i]).hashCode();
		}
		return result;
	}

	/**
	 * Gives the substring of the given text before the given separator.
	 *
	 * If the text does not contain the given separator then the given text is returned.
	 *
	 * @param text
	 * @param separator
	 * @return
	 */
	public static String substringBefore(String text, char separator) {
		if (isEmpty(text)) {
			return text;
		}
		int sepPos = text.indexOf(separator);
		if (sepPos < 0) {
			return text;
		}
		return text.substring(0, sepPos);
	}

	/**
	 * Gives the substring of the given text before the last occurrence of the given separator.
	 *
	 * If the text does not contain the given separator then the given text is returned.
	 *
	 * @param text
	 * @param separator
	 * @return
	 */
	public static String substringBeforeLast(String text, char separator) {
		if (isEmpty(text)) {
			return text;
		}
		int cPos = text.lastIndexOf(separator);
		if (cPos < 0) {
			return text;
		}
		return text.substring(0, cPos);
	}

	/**
	 * Gives the substring of the given text after the last occurrence of the given separator.
	 *
	 * If the text does not contain the given separator then "" is returned.
	 *
	 * @param text
	 * @param separator
	 * @return
	 */
	public static String substringAfterLast(String text, char separator) {
		if (isEmpty(text)) {
			return text;
		}
		int cPos = text.lastIndexOf(separator);
		if (cPos < 0) {
			return EMPTY;
		}
		return text.substring(cPos + 1);
	}

	/**
	 * Gives the substring of the given text after the given separator.
	 *
	 * If the text does not contain the given separator then "" is returned.
	 *
	 * @param text
	 * @param separator
	 * @return
	 */
	public static String substringAfter(String text, char c) {
		if (isEmpty(text)) {
			return text;
		}
		int cPos = text.indexOf(c);
		if (cPos < 0) {
			return EMPTY;
		}
		return text.substring(cPos + 1);
	}

    //-----------------------------------------------------------------------
    /**
     * <p>Escapes the characters in a <code>String</code> to be suitable to pass to
     * an SQL query.</p>
     *
     * <p>For example,
     * <pre>statement.executeQuery("SELECT * FROM MOVIES WHERE TITLE='" +
     *   StringEscapeUtils.escapeSql("McHale's Navy") +
     *   "'");</pre>
     * </p>
     *
     * <p>At present, this method only turns single-quotes into doubled single-quotes
     * (<code>"McHale's Navy"</code> => <code>"McHale''s Navy"</code>). It does not
     * handle the cases of percent (%) or underscore (_) for use in LIKE clauses.</p>
     *
     * see http://www.jguru.com/faq/view.jsp?EID=8881
     * @param str  the string to escape, may be null
     * @return a new String, escaped for SQL, <code>null</code> if null string input
     */
    public static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        return StringUtil.replace(str, "'", "''");
    }

    /**
     * Get the text between the first occurrence of <code>begin</code> and the <code>end</code> part including
     * the begin and the end part.
     *
     * @param text The text where a sub part should be extracted from
     * @param begin The begin part in <code>text</code>.
     * @param end The end part in <code>text</code>.
     * @return The text part or <code>null</code> if <code>text</code> did not contain any matching part.
     */
		public static String between(String text, String begin, String end) {
			int beginIndex = text.indexOf(begin);
			if(beginIndex != -1) {
				int endIndex = text.indexOf(end, beginIndex + begin.length());
				if(endIndex != -1) {
					return text.substring(beginIndex, endIndex + end.length());
				}
			}
			return null;
		}
		
		/**
		 * Creates a string from the given strings where each of them is separated by the given separator.
		 * @param separator The separator which separates each given string.
		 * @param s The strings to be joined together.
		 * @return The joined string.
		 */
		public static String join(String separator, String... s) {
			StringBuffer b = new StringBuffer();
			for (String string : s) {
				if (isNotEmpty(string)) {
					if (b.length() != 0) {
						b.append(separator);
					}
					b.append(string);
				}
			}
			return b.toString();
		}

}
