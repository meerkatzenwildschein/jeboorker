package org.rr.jeborker.db.item;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Index {

	/**
	 * UNIQUE, doesn't allow duplicates. For composite index means uniqueness of composite keys.
     * NOTUNIQUE, allows duplicates
     * FULLTEXT, by indexing any single word of the text. It's used in query with the operator CONTAINSTEXT
     * DICTIONARY, like UNIQUE but in case the key already exists replace the record with the new one 
	 */
	public String type();

}
