package org.rr.commons.utils;


public interface UtilConstants {

	/**
	 * Comperation mode for binary (case sensitive) comperation.
	 */
	public static final int COMPARE_BINARY = 0;
	
	/**
	 * Comperation mode for textural (case insensitive) comperation.
	 */
	public static final int COMPARE_TEXT = 1;	
	
	// Constants for FirstWeekInYear
	public static final int FIRSTJAN1 = 1;

	public static final int FIRSTFOURDAYS = 2;

	public static final int FIRSTFULLWEEK = 3;

	public static final int DIVISION_TO_SECOND = 1000;

	public static final int DIVISION_TO_MINUTE = 60;

	public static final int DIVISION_TO_HOUR = 60;

	public static final int DIVISION_TO_DAY = 24;

	public static final int DIVISION_TO_WEEK = 7;

	public static final int MULTIPLICATION_TO_QUARTER = 3;

	public static final int ONE_WEEK = 7;

	public static final int ONE_DAY = 1;
	
	public static final int FIRST_QUARTER=1;
	
	public static final int SECOND_QUARTER=2;
	
	public static final int THIRD_QUARTER=3;
	
	public static final int FOURTH_QUARTER=4;	
	
	/**
	 * Specifies the type of data for the VBStringFunctions.format method.
	 */
	public static final int FORMAT_TYPE_UNKNOWN = -1;

	/**
	 * Specifies the type of data for the VBStringFunctions.format method.
	 */
	public static final int FORMAT_TYPE_TIME = 0;

	/**
	 * Specifies the type of data for the VBStringFunctions.format method.
	 */
	public static final int FORMAT_TYPE_DATE = 1;

	/**
	 * Specifies the type of data for the VBStringFunctions.format method.
	 */
	public static final int FORMAT_TYPE_NUMERIC = 2;
	
	/**
	 * Specifies the type of data for the VBStringFunctions.format method.
	 */
	public static final int FORMAT_TYPE_STRING = 3;

	/**
	 * Specifies the search type for functions like the VBVAlueListFunction.filter function.
	 * SEARCH_STRING defines searching only for the specified string.
	 */
	public static final int SEARCH_DEFAULT = 0;
	
	/**
	 * Specifies the search type for functions like the VBVAlueListFunction.filter function.
	 * SEARCH_REGEXP defines using the specified string as regualr expression for searching.
	 */	
	public static final int SEARCH_REGEXP = 1;
	
	/**
	 * Specifies the search algorithm bubblesort for functions like <code>{@link ListUtils#sort(Object[], int, int)}</code>
	 */
	public static final int SORT_BUBBLE = 0;

	/**
	 * Specifies the direction for example as search direction for functions like <code>{@link ListUtils#sort(Object[], int, int)}</code>.
	 */
	public static final int DIRECTION_ASC = 0;

	/**
	 * Specifies the direction for example as search direction for functions like <code>{@link ListUtils#sort(Object[], int, int)}</code>.
	 */
	public static final int DIRECTION_DESC = 1;
}
