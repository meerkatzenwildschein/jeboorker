package org.rr.commons.collection;

import java.util.List;

/**
 * A {@link List} interface which allows to add a close method to the list type. 
 */
public interface ICloseableList<T> extends List<T> {

	public void close();
}
