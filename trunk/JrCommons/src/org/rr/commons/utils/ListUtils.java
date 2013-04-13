package org.rr.commons.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rr.commons.collection.CompoundList;
import org.rr.commons.collection.InternalStringList;

public final class ListUtils implements Serializable {

	private static final long serialVersionUID = 5442821752302966293L;

	private ListUtils() {
	}

	/**
	 * Creates a <code>String</code> that consists of a number of substrings
	 * in an array.
	 * 
	 * @param values
	 *            A n'th-dimensional array that contains the substrings to be
	 *            joined.
	 * @param delimiter
	 *            The character(s) used to separate the substrings in the
	 *            returned <code>String</code>. If this parameter is null, 
	 *            the <i>,</i> character will be used as delimiter char.
	 *            
	 * @return The processed <code>String</code>.
	 * @see #split(String, String, int, int)
	 */
	public static String join(final List<? extends Object> values, final String delimiter) {
		if(values==null || values.size() == 0) {
			return "";
		}
		return ArrayUtils.join(values.toArray(new Object[values.size()]), delimiter);
	}
	
	/**
	 * Creates a zero-based, one-dimensional array containing a specified number
	 * of substrings. per default, binary comperation is used without any result limit.
	 * 
	 * @param text
	 *            A <code>String</code> containing substrings and delimiters.
	 *            If expression is a zero-length string(""), Split returns an
	 *            empty array, that is, an array with no elements and no data.
	 * @param delimiter
	 *            A <code>String</code> used to identify the substring limits.
	 *            If this parameter is null, the whole String will be returned within an
	 *            array with a size of 1.
	 *            </ul>
	 * @return The splitted <code>String</code> array. All result 
	 * @see #join(Object[], String)
	 */
	public static List<String> split(final String text, final String delimiter) {
		return split(text, delimiter, -1, UtilConstants.COMPARE_BINARY);
	}

	/**
	 * Creates a zero-based, one-dimensional array containing a specified number
	 * of substrings.
	 * 
	 * @param text
	 *            A <code>String</code> containing substrings and delimiters.
	 *            If expression is a zero-length string(""), Split returns an
	 *            empty array, that is, an array with no elements and no data.
	 * @param delimiter
	 *            A <code>String</code> used to identify the substring limits.
	 *            If this parameter is null, the whole String will be returned within an
	 *            array with a size of 1.
	 * @param limit
	 *            Number of substrings to be returned; -1 indicates that all
	 *            substrings are returned.
	 * @param compare
	 *            Specifies the string comparison to use.
	 *            <ul>
	 *            <li>0 = vbBinaryCompare - Perform a binary comparison (case
	 *            sensitive)</li>
	 *            <li>1 = vbTextCompare - Perform a textual comparison (case
	 *            insensitive)</li>
	 *            </ul>
	 * @return The splitted <code>String</code> array. All result 
	 * @see #join(Object[], String)
	 */
	public static List<String> split(final String text, final String delimiter, final int limit, final int compare) {
		if (compare > UtilConstants.COMPARE_TEXT || compare < UtilConstants.COMPARE_BINARY) {
                    throw new RuntimeException("Mode not supported");
		}

		if (text == null) {
			return null;
		}
		
		final ArrayList<String> list = new InternalStringList();

		if (delimiter == null) {
			list.add(text);
			return list;
		}

		if (delimiter.length() == 0) {
			return list; //empty list
		}
		
		if(limit == 0) {
			return list; //empty list
		}
		
		final String lowerCaseDelimiter = delimiter.toLowerCase();
		String lowerCaseExpression = text.toLowerCase();
		String textValue = text; //create a copy for the text parameter to work with it.

		while ((compare == UtilConstants.COMPARE_TEXT && !(lowerCaseExpression.indexOf(lowerCaseDelimiter) == -1))
				|| (compare == UtilConstants.COMPARE_BINARY && !(textValue.indexOf(delimiter) == -1))) {
			int index = -1;
			if (compare == UtilConstants.COMPARE_BINARY) {
				index = textValue.indexOf(delimiter);
			} else {
				index = lowerCaseExpression.indexOf(lowerCaseDelimiter);
			}
			list.add(textValue.substring(0, index));
			textValue = textValue.substring(index + delimiter.length());
			lowerCaseExpression = lowerCaseExpression.substring(index + lowerCaseDelimiter.length());
			
			if(limit!=-1 && list.size() == limit) {
				break;
			}
		}
		
		if(limit == -1 || list.size() < limit) {
			list.add(textValue);
		}
		return list;
	}
	
	/**
	 * convenience method and does the same as the <code>{@link #split(String, String, int, int)}</code> but
	 * with UtilConstants.COMPARE_BINARY as compare parameter.
	 * 
	 * @see ##split(String, String, int, int)
	 */
	public static List<String> split(final String text, final String delimiter, final int limit) {
		return split(text, delimiter, limit, UtilConstants.COMPARE_BINARY);
	}
	
	/**
	 * Fast split with a character only separator.
	 * @param text The text to be splitted.
	 * @param separator The separator.
	 * @return The splitted values.
	 */
	public static List<String> split(String text, char separator) {
		if(text == null) {
			return Collections.emptyList();
		}
		
		ArrayList<String> result = new ArrayList<String>(StringUtils.occurrence(text, String.valueOf(separator), UtilConstants.COMPARE_BINARY) + 1);
		int last = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if(c == separator) {
				char[] separated = new char[i - last];
				text.getChars(last, i, separated, 0);
				result.add(String.valueOf(separated));
				last = i+1;
			}
		}
		
		char[] separated = new char[text.length() - last];
		if(separated.length > 0 || text.isEmpty() || text.charAt(text.length()-1) == separator) {
			text.getChars(last, text.length(), separated, 0);
			result.add(String.valueOf(separated));
		}
		
		return result;
	}	
	
	/**
	 * Splits the given <code>text</code> into a string array where each
	 * array entry has a string length, specified with the <code>length</code> parameter.
	 * If the rest of the string is smaller than the specified <code>length</code>, the last
	 * entry will be filled up with whitespace's. For example:
	 * <br><br>
	 * <pre><code>
	 * chunkSplit("teststring", 3)
	 * returns: String[] {"tes", "tst","rin","g  "}  
	 * </code></pre>
	 * 
	 * @param text The string to be splitted
	 * @param length The length of each string in the result.
	 * @return A string array where each string has the given length. 
	 */
	public static List<String> chunkSplit(final String text, int length) {
		if(text==null || text.length()==0) {
			return new ArrayList<String>(0);
		}
		
		if(length<=0) {
			throw new RuntimeException("length for chunk splitting must be larger than 0 - given value is: '" + length+"'");
		}
		
		List<String> result = new ArrayList<String>( (int)MathUtils.roundUp((double)text.length()/(double)length, 0) );
		StringBuilder buffer = new StringBuilder(length);
		for (int i = 1; i <= text.length(); i++) {
			buffer.append(text.charAt(i-1));
			if(i>0 && i%length==0) {
				result.add(buffer.toString());
				buffer = new StringBuilder(length);
			}
		}
		if(buffer.length()>0) {
			String lastEntry = StringUtils.padRight(buffer.toString(), length);
			result.add(lastEntry);
		}
		
		return result;
	}
	
	public static <T>List<T> filter(List<T> values, final List<?> match, final boolean include, final int compare, final int searchType) {
		ArrayList<T> result = new ArrayList<T>(values.size());
			for (Object o : match) {
			List<T> filtered = filter(values, o, include, compare, searchType);
			result.addAll(filtered);
		}
		
		return distinct(result, compare);
	}

	/**
	 * Dos the same as the filter(String[], String, boolean, int, boolean)
	 * method but uses VBConstants.SEARCH_DEFAULT per default for the searchType parameter.
	 * 
	 * @see #filter(String[], String, boolean, int, boolean)
	 * @return The filtered array.
	 */
	public static <T>List<T> filter(final List<T> values, final Object filterValue, final boolean include, final int compare) {
		return filter(values, filterValue, include, compare, UtilConstants.SEARCH_DEFAULT);
	}

	/**
	 * The Filter function returns a zero-based array that contains a subset of
	 * a <code>String</code> array based on a filter criteria.
	 * 
	 * @param values
	 *            A one-dimensional array of <code>String</code>s to be
	 *            searched. The data type of this array specifies the 
	 *            array data type for the result.
	 * @param match
	 *            The <code>Object</code> to search for. This can also be an array. Each value of the array
	 *            will be used as filter value.
	 * @param include
	 *            A Boolean value that indicates whether to return the
	 *            substrings that include or exclude value. True returns the
	 *            subset of the array that contains value as a substring. False
	 *            returns the subset of the array that does not contain value as
	 *            a substring.
	 * @param compare
	 *            Specifies the string comparison to use.
	 *            <ul>
	 *            <li>0 = VBConstants.COMPARE_BINARY - Perform a binary
	 *            comparison (case sensitive) using the equals method of each object.</li>
	 *            <li>1 = VBConstants.COMPARE_TEXT - Perform a textual
	 *            comparison (case insensitive). Using text comparison, the
	 *            <code>toString()</code> method on the given object value
	 *            will be used for comparison.</li>
	 *            </ul>
	 * @param searchType
	 * 			  The searchType parameter specifies how to use the search object specified
	 * 			  with the match parameter. Possible values are:
	 * 			  <UL>
	 * 			  <LI>0 = VBConstants.SEARCH_DEFAULT - performs a simple equals comperation to the <code>match</code> object.</LI>
	 * 			  <LI>1 = VBConstants.SEARCH_REGEXP - use the value specified in the parameter named <code>match</code> as regular expression.</LI>
	 * 			  <LI>2 = VBConstants.SEARCH_LIKE - use the value specified in the parameter named <code>match</code> like the LIKE operator.</LI>
	 * 			  </UL>
	 * 
	*  			  Using VBConstants.SEARCH_REGEXP or VBConstants.SEARCH_LIKE,
	 *            the <code>toString()</code> method on the given
	 *            object value will be used for regexp. or like matching.
	 * @return A new, filtered array instance.
	 */
	@SuppressWarnings("unchecked")
	public static <T>List<T> filter(List<T> values, final Object match, final boolean include, final int compare, final int searchType) {
		//test the comperation mode
		if (compare > UtilConstants.COMPARE_TEXT || compare < UtilConstants.COMPARE_BINARY) {
			throw new RuntimeException("Mode not supported");
		}
		
		//test the array for null.
		if (values==null) {
                    throw new RuntimeException("Array is null");
		}
		
		//test if the filterValue is an array
		Object[] processFilterValue;
		if ( ArrayUtils.isArray(match) ) {
			processFilterValue = (Object[]) match;
		} else {
			processFilterValue = new Object[] {match};
		}
		
		ArrayList<T> result = new ArrayList<T>();
		//loop all filter values
		for (int i = 0; i < processFilterValue.length; i++) {
			//loop each entry of the input array
			for (T next : values) {
				boolean found;
				if(next instanceof List) {
					//if the value to be compared is an array, filter and add it.
					result.add((T) filter((List<?>) next, match, include, compare, searchType));
				} else {
					// compare process
					if (searchType == UtilConstants.SEARCH_REGEXP) {
						if (compare == UtilConstants.COMPARE_TEXT) {
							String pattern = String.valueOf(processFilterValue[i]).toLowerCase();

							Pattern p = Pattern.compile(pattern);
							Matcher m = p.matcher(String.valueOf(next).toLowerCase());
							found = m.matches();
						} else {
							String pattern = String.valueOf(processFilterValue[i]);

							Pattern p = Pattern.compile(pattern);
							Matcher m = p.matcher(String.valueOf(next));
							found = m.matches();
						}
					} else {
						if (compare == UtilConstants.COMPARE_TEXT) {
							if (String.valueOf(next).equalsIgnoreCase(String.valueOf(processFilterValue[i]))) {
								found = true;
							} else {
								found = false;
							}
						} else {
							if (next.equals(processFilterValue[i]) || next == processFilterValue[i]) {
								found = true;
							} else {
								found = false;
							}
						}
					}
					// add process
					if (found && include) {
						result.add(next);
					} else if (!found && !include) {
						result.add(next);
					}
				}
			} // end inner loop
		} // end outer loop

		return result;
	}

	
	/**
	 * Merges the two array given with the arguments <code>first</code> and
	 * <code>second</code>.
	 * 
	 * @param first
	 *            An array to be merged with the array given with the parameter
	 *            <code>second</code>. The data type of the first array specifies the 
	 *            array data type for the result.
	 * @param second
	 *            An array to be merged with the array given with the parameter
	 *            <code>first</code>.
	 * @return The merged array. Never will return null.
	 * 
	 * @throws RuntimeException if the array given with the first parameter is not an instance of the array given with the second parameter. 
	 */
	public static <T>List<T> union(final List<T> first, final List<T> second) {
		//handle null values
		if (first==null && second==null) {
			return new ArrayList<T>(0);
		} else if (first==null) {
			return second;
		} else if (second==null) {
			return first;
		}
		
		return new CompoundList<T>(first, second);
	}


	/**
	 * Determine all entries existing in both given arrays and creates a new
	 * array which contains all duplicate entries.
	 * 
	 * @param first
	 *            A one dimensional array to be searched for entries exiting in the array given
	 *            with the <code>second</code> parameter. The data type of the first array specifies the 
	 *            array data type for the result.
	 * @param second
	 *            A one dimensional array to be searched for entries exiting in the array given
	 *            with the <code>first</code> parameter. If this parameter is null, the first array
	 *            will be searched for duplicate values within it self.
	 * @param compare
	 *            Specifies the string comparison to use.
	 *            <ul>
	 *            <li>0 = VBConstants.vbBinaryCompare - Perform a binary comparison (case sensitive)</li>
	 *            <li>1 = VBConstants.vbTextCompare - Perform a textual comparison (case insensitive)</li>
	 *            </ul>
	 * 
	 * @return A new array containing all duplicate entries.
	 */
	public static <T>List<T> duplicates(final List<T> first, final List<T> second, final int compare) {
		//test the comperation mode
		if (compare > UtilConstants.COMPARE_TEXT || compare < UtilConstants.COMPARE_BINARY) {
			throw new RuntimeException("Mode not supported");
		}
		
		if (second==null) {
			return duplicates(first, compare);
		}
		
		ArrayList<T> arrayList = new ArrayList<T>();
		for (T firstNext : first) {
			int count = 0;

			for (T secondNext : second) {
				//test if equals
				if (compare == UtilConstants.COMPARE_BINARY) {
					if (firstNext.equals(secondNext)) {
						count++;
					}
				} else {
					//Text compare
					if (String.valueOf(firstNext).equalsIgnoreCase(String.valueOf(secondNext))) {
						count++;
					}
				}
			}

			if (count > 0 && !arrayList.contains(firstNext)) {
            arrayList.add(firstNext);
			}
		}

		return arrayList;
	}
	
	/**
	 * Searches for duplicate entries within the given array. 
	 * 
	 * @param values The array which should be searched for duplicates.
	 * @param compare
	 *            Specifies the string comparison to use.
	 *            <ul>
	 *            <li>0 = VBConstants.vbBinaryCompare - Perform a binary comparison (case sensitive)</li>
	 *            <li>1 = VBConstants.vbTextCompare - Perform a textual comparison (case insensitive)</li>
	 *            </ul>
	 * @return An array with the type of the array given with the values parameter containing all duplicate entries (but only one of the duplicate entries).
	 */
	public static <T>List<T> duplicates(final List<T> values, final int compare) {
		List<T> result = new ArrayList<T>(values.size());
		Iterator<T> iterator = values.iterator();
		for (int i = 0; iterator.hasNext(); i++) {
			T next = iterator.next();
			if ( indexOf(values, next, compare, UtilConstants.SEARCH_DEFAULT)!=i ) {
				result.add(next);
			}
		}
		
		//if there're more than two duplicate values, there will be some more than only one entry. 
		result = distinct(result, compare);
		
		return result;
	}
	
	
	/**
	 * Determine all entries that did not existing in both given arrays and
	 * creates a new one which contains all non redundant entries.
	 * 
	 * @param first
	 *            A one dimensional array to be searched for entries not exiting in the array given
	 *            with the <code>second</code> parameter. The data type of the first array specifies the 
	 *            array data type for the result.
	 * @param second
	 *            A one dimensional array to be searched for entries not exiting in the array given
	 *            with the <code>first</code> parameter.
	 * @param compare
	 *            Specifies the string comparison to use.
	 *            <ul>
	 *            <li>0 = VBConstants.vbBinaryCompare - Perform a binary comparison (case sensitive)</li>
	 *            <li>1 = VBConstants.vbTextCompare - Perform a textual comparison (case insensitive)</li>
	 *            </ul>
	 * @return A new array containing all non redundant entries.
	 */
	public static <T>List<T> difference(final List<T> first, final List<T> second, final int compare) {
		//test the comperation mode
		if (compare > UtilConstants.COMPARE_TEXT || compare < UtilConstants.COMPARE_BINARY) {
			throw new RuntimeException("Mode not supported");
		}
		
		List<T> arrayList = new ArrayList<T>();
		List<T> duplicateArray = duplicates(first, second, compare);

		arrayList=copyDifference(first, arrayList, duplicateArray, compare);
		arrayList=copyDifference(second, arrayList, duplicateArray, compare);

		//create the result array 
		return arrayList;
	}
	
	public static <T>List<T> difference(final List<T> first, final List<T> second) {
		return difference(first, second, UtilConstants.COMPARE_BINARY);
	}

	private static <T>List<T> copyDifference(final List<T> object, final List<T> previousValues, final List<T> duplicateArray, final int compare) {
		Iterator<T> iterator = object.iterator();
		while (iterator.hasNext()) {
			T next = iterator.next();
			int count = 0;

			for (T t : duplicateArray) {
				if (compare == UtilConstants.COMPARE_BINARY) {
					if (next.equals(t)) {
						count++;
					}
				} else {
					if (String.valueOf(next).equalsIgnoreCase(String.valueOf(t))) {
						count++;
					}
				}
			}

			if (count == 0 && !previousValues.contains(next)) {
				previousValues.add(next);
			}
			
		}
		
		return previousValues;
	}

	/**
	 * Returns the index of the first occurrence of a value in a n'th-dimensional Array specified with the <code>values</code> parameter. 
	 * If the search value is found in a n'th dimension of the <code>values</code> array, the position for the array which contains 
	 * the value, in the first dimension is returned.  
	 * <br><br>
	 * This method is using the search type <code>VBConstants.SEARCH_DEFAULT</code>.
	 * 
	 * @param values The array to be searched for the desired value.
	 * @param match The <code>Object</code> to be searched.
	 * @param compare
	 *            Specifies the string comparison to use.
	 *            <ul>
	 *            <li>0 = VBConstants.vbBinaryCompare - Perform a binary comparison (case sensitive)</li>
	 *            <li>1 = VBConstants.vbTextCompare - Perform a textual comparison (case insensitive)</li>
	 *            </ul>
	 *            
	 * @return The index for the found <code>match</code> in the <code>values</code> array.
	 */
	public static int indexOf(final List<Object> values, final Object match, final int compare) {
		return indexOf(values, match, compare, UtilConstants.SEARCH_DEFAULT);
	}
	
	/**
	 * Returns the index of the first occurrence of a value in a n'th-dimensional Array specified with the <code>values</code> parameter. 
	 * If the search value is found in a n'th dimension of the <code>values</code> array, the position for the array which contains 
	 * the value, in the first dimension is returned.  
	 * 
	 * @param values The array to be searched for the desired value.
	 * @param match The <code>Object</code> to be searched.
	 * @param compare
	 *            Specifies the string comparison to use.
	 *            <ul>
	 *            <li>0 = VBConstants.vbBinaryCompare - Perform a binary comparison (case sensitive)</li>
	 *            <li>1 = VBConstants.vbTextCompare - Perform a textual comparison (case insensitive)</li>
	 *            </ul>
	 * @param searchType
	 * 			  The searchType parameter specifies how to use the search object specified
	 * 			  with the match parameter. Possible values are:
	 * 			  <UL>
	 * 			  <LI>0 = VBConstants.SEARCH_DEFAULT - performs a simple equals comperation to the <code>match</code> object.</LI>
	 * 			  <LI>1 = VBConstants.SEARCH_REGEXP - use the value specified in the parameter named <code>match</code> as regular expression.</LI>
	 * 			  <LI>2 = VBConstants.SEARCH_LIKE - use the value specified in the parameter named <code>match</code> like the LIKE operator.</LI>
	 * 			  </UL>
	 * 
	 *  			  Using VBConstants.SEARCH_REGEXP or VBConstants.SEARCH_LIKE,
	 *            the <code>toString()</code> method on the given
	 *            object value will be used for regexp. or like matching.
	 *            
	 *            
	 * @return The index for the found <code>match</code> in the <code>values</code> array.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static int indexOf(final List<?> values, final Object match, final int compare, final int searchType) {
		//test the comperation mode
		if (compare > UtilConstants.COMPARE_TEXT || compare < UtilConstants.COMPARE_BINARY) {
			throw new RuntimeException("Mode not supported");
		}
		
		//test the array for null.
		if (values==null) {
			throw new RuntimeException("Values is null");
		}
		
		//test if the filterValue is an array
		Object[] processFilterValue;
		if ( ArrayUtils.isArray(match) ) {
			processFilterValue = (Object[]) match;
		} else {
			processFilterValue = new Object[] {match};
		}
		
		//loop all values
		for (int i = 0; i < processFilterValue.length; i++) {
			boolean canCompareTo = true;
			
			//loop each entry of the input array
			Iterator<?> iterator = values.iterator();
			for (int j = 0; iterator.hasNext(); j++) {
				final Object next = iterator.next();
				if(next instanceof List) {
					//if the value to be compared is an array, filter and add it.
					if (indexOf((List<?>)next, match, compare, searchType)!=-1) {
						return j;
					}
				} else {
					// compare process
					if (searchType == UtilConstants.SEARCH_REGEXP) {
						if (compare == UtilConstants.COMPARE_TEXT) {
							String pattern = String.valueOf(processFilterValue[i]).trim().toLowerCase();
							
							Pattern p = Pattern.compile(pattern);
							Matcher m = p.matcher(String.valueOf(next).toLowerCase());
							if (m.matches()) {
								return j;
							}
						} else {
							String pattern = String.valueOf(String.valueOf(processFilterValue[i])).toLowerCase();
							
							Pattern p = Pattern.compile(pattern);
							Matcher m = p.matcher(String.valueOf(next));
							if (m.matches()) {
								return j;
							}
						}
					} else {
						if (next==null && match==null) {
							return j;
						} else if (next==null && processFilterValue[i]==null) {
							return j;
						} else if (next==null && processFilterValue[i]!=null) {
							continue;
						}
						
						if (compare == UtilConstants.COMPARE_TEXT) {
							if (String.valueOf(next).trim().equalsIgnoreCase(String.valueOf(processFilterValue[i]).trim())) {
								return j;
							}
						} else {
							if(canCompareTo && next instanceof Comparable && processFilterValue[i] instanceof Comparable) {
								try {
									if(((Comparable)next).compareTo(processFilterValue[i]) == 0) {
										return j;
									}
								} catch (Exception e) {
									//could happens if the Comparable is generic and the class types did not match.
									if (next.equals(processFilterValue[i]) || next == processFilterValue[i]) {
										return j;
									}		
									canCompareTo = false;
								}
							} else {
								if (next.equals(processFilterValue[i]) || next == processFilterValue[i]) {
									return j;
								}
							}
						}
					}
				} 
			} // end inner loop
		} // end outer loop
		return -1;
	}
	
	
	
	/**
	 * Removes all repetioned entries from the array specified with the <code>values</code>
	 * parameter.
	 * 
	 * @param values The array where duplicated entries should be removed.
	 * @param compare
	 *            Specifies the string comparison to use.
	 *            <ul>
	 *            <li>0 = vbBinaryCompare - Perform a binary comparison (case
	 *            sensitive)</li>
	 *            <li>1 = vbTextCompare - Perform a textual comparison (case
	 *            insensitive)</li>
	 *            </ul>
	 * @return A new created array without any duplicated entries.
	 */
	public static <T>List<T> distinct(final List<T> values, final int compare) {
		final ArrayList<T> result = new ArrayList<T>(values.size());
		final Iterator<T> iterator = values.iterator();

		for (int i = 0; iterator.hasNext(); i++) {
			final T next = iterator.next();
			if ( indexOf(values, next, compare, UtilConstants.SEARCH_DEFAULT) == i ) {
				result.add(next);
			}
		}
		
		return result;
	}
	
	public static <T>List<T> distinct(final List<T> values) {
		return distinct(values, UtilConstants.COMPARE_BINARY);
	}

	/**
	 * Extracts these part of the <code>values</code> array which is defined
	 * between <code>start</code> and <code>end</code>. The first array entry
	 * starts with 0.
	 * <br><br>
	 * If the start or end parameter ist out of range, the range will be resized
	 * to a valid value. No <code>ArrayIndexOutOfRange</code> exception is thrown.
	 * 
	 * @param values The array to be extracted.
	 * @param start The start position.
	 * @param end The end position.
	 * @return A new array instance with the same type as the array specified 
	 * 	with the <code>values</code> parameter which contains all values between 
	 * 	<code>start</code> and <code>end</code>.
	 */
	public static <T>List<T> extract(final List<T> values, int start, int end) {
		//create the resized array 
		List<T> result = new ArrayList<T>(end);
		for(int i = start; i < values.size() && i < end; i++) {
			result.add(values.get(i));
		}
		return result;
	}
	
	/**
	 * Search for the value specified with the <code>match</code> parameter and
	 * return the position for the first found occurrence or -1 if no match was found. 
	 * 
	 * @param values The array to be searched.
	 * @param match The value to be searched in the array. This can be also an array with values to be searched.
	 * @param compare
	 *            Specifies the string comparison to use.
	 *            <ul>
	 *            <li>0 = VBConstants.vbBinaryCompare - Perform a binary comparison (case sensitive)</li>
	 *            <li>1 = VBConstants.vbTextCompare - Perform a textual comparison (case insensitive)</li>
	 *            </ul>
	 * @param searchType
	 * 			  The searchType parameter specifies how to use the search object specified
	 * 			  with the match parameter. Possible values are:
	 * 			  <UL>
	 * 			  <LI>0 = VBConstants.SEARCH_DEFAULT - performs a simple equals comperation to the <code>match</code> object.</LI>
	 * 			  <LI>1 = VBConstants.SEARCH_REGEXP - use the value specified in the parameter named <code>match</code> as regular expression.</LI>
	 * 			  <LI>2 = VBConstants.SEARCH_LIKE - use the value specified in the parameter named <code>match</code> like the LIKE operator.</LI>
	 * 			  </UL>
	 * 
	 *  			  Using VBConstants.SEARCH_REGEXP or VBConstants.SEARCH_LIKE,
	 *            the <code>toString()</code> method on the given
	 *            object value will be used for regexp. or like matching.
	 *            
	 * @return The index of the first found occurrence or -1 if no match was found.
	 */
	public static boolean exists(final List<?> values, final Object match, final int compare, final int searchType) {
		return indexOf(values, match, compare, searchType)!=-1;
	}
	
	/**
	 * Sets the given value to the desired index of the given list. If the size of the list
	 * isn't large enough, the list will be filled with <code>null</code> values.
	 * @param <T>
	 * @param values The list where the value should be set to.
	 * @param value The value to be set into the list
	 * @param idx The list index where the value should be inserted into the list.
	 */
	public static <T>void set(final List<T> values, T value, int idx) {
		if(values.size() > idx) {
			values.set(idx, value);
		} else {
			for (int i = values.size(); i <= idx; i++) {
				values.add(null);
			}
			set(values, value, idx);
		}
	}
	
	/**
	 * Get a value from the given {@link List}.
	 * @param values The list containing the values where a value should be returned from.
	 * @param idx The index of the value to be returned in the given list.
	 * @return The value from the index or <code>null</code> if the list is smaller than the 
	 * index requires.
	 */
	public static <T>T get(final List<T> values, int idx) {
		if(values.size() > idx) {
			return values.get(idx);
		}
		return null;
	}

	/**
	 * gets the first entry from the given list. If the list is <code>null</code> 
	 * or empty <code>null</code> is returned.
	 * @param object The list where the first entry should be fetched from.
	 * @return The desired list entry or <code>null</code>.
	 */
	public static <T> T first(List<T> object) {
		if(object != null && !object.isEmpty()) {
			return object.get(0);
		}
		return null;
	}
}
