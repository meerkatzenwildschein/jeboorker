package org.rr.commons.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ArrayUtils {
	
	private ArrayUtils() {}
	
	/**
	 * Tests if a specified variable is an array. If the variable is an array,
	 * it returns <code>true</code>, otherwise, it returns <code>false</code>.
	 * 
	 * @param value
	 *            The value to test.
	 * @return A Boolean value that indicates whether a specified variable is an
	 *         array.
	 */
	public static boolean isArray(final Object value) {
		if (value != null && (value instanceof Object[] || value.getClass().isArray()) ) {
			return true;
		}

		return false;
	}
	
    /**
     * Creates an array for the given value.
     * @param <T>
     * @param values
     * @return An empty array if values is null, the array if we have a list or an array given with values or, at last, the given
     *  value in an array of the type of the given value.
     */
    @SuppressWarnings("unchecked")
	public static <T>T[] toArray(final T values) {
        if(values==null) {
            return null;
        } else if(values instanceof List) {
            return (T[]) ((List<T>)values).toArray();
        } else if(isArray(values)) {
            try {
                return (T[])values;
            } catch(Exception e) {
                T[] t = (T[]) java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), ((T[])values).length);
                System.arraycopy(values, 0, t, 0, ((T[])values).length);
                return t;
            }
        } else {
            T[] t = (T[]) java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), 1);
            t[0] = values;
            return t;
        }
    }	
    
	/**
	 * Fetches the value from the given array at the specified index. If too small
	 * for the requested index, <code>null</code> will be returned.
	 * <BR><BR>
	 * Never throws any kind of <code>{@link Exception}</code>.
	 * 
	 * @param values The array from which the value should be fetched.
	 * @param index The index where the value is located the array.
	 * @return The array value or <code>null</code>.
	 */
	public static <T>T get(final T[] values, int index) {
		if(index < 0 || values==null || values.length <= index) {
			return null;
		} else {
			return values[index];
		}
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
	public static String join(final Object[] values, final String delimiter) {
		//use the default delimiter if the parameter delimiter is null.
		String delimiterValue = delimiter;
		if (delimiterValue == null) {
			delimiterValue = ",";
		}
		
		StringBuilder returnValue = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof Object[]) {
				returnValue.append(join((Object[])values[i], delimiter));
			} else {
				returnValue.append(values[i]);
			}
			if (i + 1 != values.length) {
				returnValue.append(delimiterValue);
			}
		}
		return returnValue.toString();
	}
	
	
	/**
	 * Sets a range of elements in the Array to zero. In practice, a new array with
	 * a size of 0 are created and returned. 
	 * 
	 * @param values The array to be cleared.
	 * @return a new, empty array instance with the same type as the given array.
	 * 
	 * @see #resize(Object[], int)
	 */
	@SuppressWarnings("unchecked")
	public static <T>T[] clear(final T[] values) {
		//create the empty result array 
		return (T[]) java.lang.reflect.Array.newInstance(
				values.getClass().getComponentType(), 0);
	}
	
	/**
	 * Deletes the last element of an array. The returned array is 
	 * a new array instance which has been decreased by one. If the
	 * array is <code>null</code> or has a length of 0, a new, empty
	 * array instance will be returned.
	 * 
	 * @param values The array which last entry should be removed.
	 * @return A new array instance without the last entry.
	 * 
	 * @see #resize(Object[], int)
	 * @see #append(Object[], Object)
	 */
	public static <T>T[] detachLast(final T[] values) {
		//returns an empty array if there are no values to detach.
		if (values==null || values.length==0) {
			return clear(values);
		}
		
		T[] result = resize(values, values.length-1);
		return result;
	}
	
	/**
	 * Deletes the first element of an array. The returned array is 
	 * a new array instance which has been decreased by one. If the
	 * array is <code>null</code> or has a length of 0, a new, empty
	 * array instance will be returned.
	 * 
	 * @param values The array which first entry should be removed.
	 * @return A new array instance without the first entry.
	 * 
	 * @see #resize(Object[], int)
	 * @see #append(Object[], Object)
	 */
	public static <T>T[] detachFirst(final T[] values) {
		//returns an empty array if there are no values to detach.
		if (values==null || values.length==0) {
			return clear(values);
		}
		
		T[] result = extract(values, 1, values.length);
		return result;
	}	
	
	
	/**
	 * Changes the size of an array to the specified new size. 
	 * Existing data records will be deleted if the <code>size</code>
	 * is smaller than the specified array. If the size is larger than
	 * the <code>values</code> array, the additional array fields contains
	 * <code>null</code> values.
	 * 
	 * @param values The array to be resized. If this parameter is <code>null</code> a new Object array with the 
	 * 	desired size will be created.
	 * @param size The new array size. If a negative value is specified, the size will be 
	 * set to the <code>values</code> length.
	 * 
	 * @return The resized array.
	 */
	@SuppressWarnings("unchecked")
	public static <T>T[] resize(final T[] values, int size) {
		if (values==null) {
			return (T[]) java.lang.reflect.Array.newInstance(Object.class, 0);
		}
		
		//is the size negative, just set it to the length of the given array.
		//An exact copy of the given array will be created.
		if (size < 0) {
			size = values.length;
		}
		
		//create the resized array 
		T[] result = (T[]) java.lang.reflect.Array.newInstance(
				values.getClass().getComponentType(), size);
		
		//fetch the number of array elements to copy
		int copy = size > values.length ? values.length : size;
		
		System.arraycopy(values, 0, result, 0, copy);
		
		return result;
	}
	
	/**
	 * Appends the <code>Object</code> specified with the <code>value</code> parameter to
	 * the object array specified with the <code>values</code> parameter. The <code>values</code>
	 * array don't be touched or modified in any case! A new by one increased array of the same data type as the
	 * given <code>values</code> array will be created and the <code>value</code> are appended at 
	 * the end of the increased array. 
	 * <br><br>
	 * Take sure that the data type of the value matches to the data type of the <code>values</code> array.
	 * 
	 * @param values Array to be resized by one. 
	 * @param value The value to be appended at the end of the increased array.
	 * @return The by one increased array with the <code>value</code> in the last field.
	 * 
	 * @throws RuntimeException if the array given with the <code>values</code> parameter did not matches to the <code>value</code> parameter.
	 * @throws NullPointerException if one of the specified parameters are <code>null</code>.
	 * 
	 * @see #union(Object[], Object[])
	 * @see #resize(Object[], int)
	 * @see #prepend(Object[], Object)
	 */
	public static <T>T[] append(final T[] values, final T value) {
		T[] result = resize(values, values.length+1 );
		result[values.length] = value;
		return result;
	}
	
	/**
	 * Prepends the <code>Object</code> specified with the <code>value</code> parameter to
	 * the object array specified with the <code>values</code> parameter. The <code>values</code>
	 * array don't be touched or modified in any case! A new by one increased array of the same data type as the
	 * given <code>values</code> array will be created and the <code>value</code> are appended at
	 * the end of the increased array. 
	 * <br><br>
	 * Take sure that the data type of the value matches to the data type of the <code>values</code> array.
	 * 
	 * @param values Array to be resized by one. 
	 * @param value The value to be appended at the beginning of the increased array.
	 * @return The by one increased array with the <code>value</code> in the first field.
	 * 
	 * @throws RuntimeException if the array given with the <code>values</code> parameter did not matches to the <code>value</code> parameter.
	 * @throws NullPointerException if one of the specified parameters are <code>null</code>.
	 * 
	 * @see #union(Object[], Object[])
	 * @see #resize(Object[], int)
	 * @see #append(Object[], Object)
	 */
	@SuppressWarnings("unchecked")
	public static <T>T[] prepend(final T[] values, final T value) {
		T[] result = (T[]) java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), values.length+1);
		System.arraycopy(values, 0, result, 1, values.length);
		result[0] = value;
		return result;
	}
	
	
	/**
	 * Randomizes the order of the elements in the array specified
	 * with the <code>values</code> array. A new instance of the array
	 * will be created. The order of the given array won't be touched.
	 * 
	 * @param values The array to be shuffled.
	 * @return The shuffled array.
	 */
	@SuppressWarnings("unchecked")
	public static <T>T[] shuffle(final T[] values) {
		List<T> list = Arrays.asList(resize(values, values.length));
		Collections.shuffle(list);
		
		T[] result = (T[])list.toArray((Object[]) java.lang.reflect.Array.newInstance(
				values.getClass().getComponentType(), list.size()));
		
		return result;
	}
	
	/**
	 * Extracts a random entry from the array specified with the
	 * <code>values</code> parameter.
	 * 
	 * @param values The values array from where a random entry should be returned. 
	 * @return A random entry from the specified array. Returns <code>null</code> 
	 * 	if the given array is <code>null</code> or has a size of 0.
	 */
	public static <T>T randomValue(final T[] values) {
		if (values==null || values.length==0) {
			return null;
		}
		T[] shuffled = shuffle(values);
		return shuffled[0];
	}
	
	/**
	 * Removes an entry of an array from the location specified with the
	 * <code>index</code> parameter. A new array instance will be created. The 
	 * given array won't be touched. If the specified index is to large, no entry
	 * will be removed but a new array instance will be returned.
	 * 
	 * @param values The array from which an entry should be removed.
	 * @param index The location where the entry, which should be removed, resists. 
	 * 	The index starts with 0 for the first array entry.
	 * @return The new array instance decreased by one, without the value to be removed
	 * 	or a new array instance with all entries if the index is before or behind of one of the 
	 * 	existing array entries.
	 */
	@SuppressWarnings("unchecked")
	public static <T>T[] remove(final T[] values, int index) {	
		if (index > values.length-1 || index < 0) {
			return resize(values, values.length);
		}
		
		T[] result = (T[]) java.lang.reflect.Array.newInstance(
				values.getClass().getComponentType(), values.length-1);
		
		int pos = 0;
		for (int i = 0; i < values.length; i++) {
			if (i!=index) {
				result[pos] = values[i];
				pos++;
			}
		}
		
		return result;
	}
	
	/**
	 * Stores the <code>Object</code> specified with the <code>value</code> parameter to
	 * the object array specified with the <code>values</code> parameter at the specified <code>index</code>. 
	 * If the index is behind the arrays size, the Array will be automatically resized, so the index matches.
	 * If another <code>Object</code> is already stored at the given <code>index</code>, it will be replaced
	 * by this new <code>value</code>. If the <code>index</code> is negative, the <code>value</code> will be
	 * stored at it's negative location. The whole array will be shifted by the negative <code>index</code>.
	 * <BR><BR>
	 * <b>Attention!</b> if the given array is large enough, it will not be copied. The value will be put into
	 * the given array instance specified with the <code>values</code> parameter and returned.
	 * <br><br>
	 * Take sure that the data type of the value matches to the data type of the <code>values</code> array.
	 * 
	 * @param values The array an value should be put into.
	 * @param value The value to be appended at the specified index of the array.
	 * @return The array with the <code>value</code> in the field with the specified index.
	 * 
	 * @throws RuntimeException if the array given with the <code>values</code> parameter did not matches to the <code>value</code> parameter.
	 * 
	 * @see #union(Object[], Object[])
	 * @see #resize(Object[], int)
	 */
	@SuppressWarnings("unchecked")
	public static <T>T[] put(final T[] values, final T value, int index) {
		T[] result = values;
		if (values==null) {
			//create an array with the type of the given value parameter.
			if(value==null) {
				result = (T[]) java.lang.reflect.Array.newInstance(Object.class, 0);
			} else {
				result = (T[]) java.lang.reflect.Array.newInstance(value.getClass(), index+1);				
			}
		} else if (values.length-1 < index) {
			result = resize(values, index+1 );
		}
		
		//is the index smaller than 0, append the value to the front of the array
		//and shift the array by the negative index value
		if (index < 0) {
			int newSize = values.length + (index * -1);
			//create the resized array 
			result = (T[]) java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), newSize);
			System.arraycopy(values, 0, result, result.length - values.length, values.length);
			result[0] = value;
			return result;
		}
		
		result[index] = value;
		return result;
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
	@SuppressWarnings("unchecked")
	public static <T>T[] extract(final T[] values, int start, int end) {
		//test if the end is lower than the start value and switch them
		if (start > end) {
			int tmp = start;
			start = end;
			end = tmp;
		}
		
		//test if the end is behind the array.
		if (values.length < end) {
			end = values.length;
		}
		
		//test if the start is lower than 0
		if (start < 0) {
			start = 0;
		}
		
		//determine the new size for the array
		int size = end - start;

		//create the resized array 
		T[] result = (T[]) java.lang.reflect.Array.newInstance(
				values.getClass().getComponentType(), size);

		System.arraycopy(values, start, result, 0, size);
		
		return result;
	}
	
    /**
     * Creates a separated array. The separation will be defined with any n'th value. 
     * <br><br>
     * For example an array like <code>new String[] {"1", "yes", "2", "no", "3", "abort"}</code>
     * which is specified with the <code>values</code> parameter and the <code>n</code> value is 2, the result looks like:
     * <code>new String[] {new String[] {1,2,3}, new String[] {"yes","no","abort"}}</code> 
     * 
     * @param values All parameters that should be arranged.
     * @param n The number of groups that should be build.
     * @return An Object array that contains all grouped arrays.
     */
	@SuppressWarnings("unchecked")
    public static <T>T[][] divide(final T[] values, int n) {
		T[][] holder = (T[][]) java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), new int[] {n, values.length/n});
		
    	int count = 0;
    	for (int i=0; i < (values.length / n); i++) {
    		for (int j=0; j < n; j++) {
    			holder[j][i] = values[count++];
    		}
    	}
    	return holder;
    }
	
	/**
	 * Reverses the order of the elements in a n'th-dimensional Array. The
	 * original array won't be touched. 
	 * 
	 * @param values The array to be reversed.
	 * 
	 * @return The reversed array.
	 */
	@SuppressWarnings("unchecked")
	public static <T>T[] reverse(final T[] values) {
		T[] result = (T[]) java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), values.length);
		int index = 0;
		for (int i = result.length; i > 0; i--) {
			if (values[i-1] instanceof Object[]) {
				result[index++] = (T) reverse((T[])values[i-1]);
			} else {
				result[index++] = values[i-1];
			}
		}

	    return result;
	}	
	
	/**
	 * Prints all given values to the standard output.
	 * @param values The values to be printed.
	 */
	public static void print(int[] values) {
		for (int i : values) {
			System.out.println(i);
		}
	}
}
