package org.rr.commons.collection;

import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * This list stores objects automatically using weak references. Objects are added and removed from the list as normal, but may turn to null at any point (ie,
 * indexOf(x) followed by get(x) may return null). The weak references are only removed when the trimToSize method is called so that the indices remain constant
 * otherwise.
 * <p>
 * 
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.3 $
 */
public class WeakList<T> extends AbstractList<T> {

	/* list of weak references */
	private ArrayList<WeakReference<T>> refs = new ArrayList<WeakReference<T>>();

	/**
	 * Create a weak random-access list.
	 */
	public WeakList() {
	}

	/**
	 * Extract the hard reference out of a weak reference.
	 */
	private T deref(WeakReference<T> o) {
		if (o != null) {
			return o.get();
		} else {
			return null;
		}
	}

	/**
	 * Returns the object at the specified index, or null if the object has been collected.
	 */
	public T get(int index) {
		return deref(refs.get(index));
	}

	/**
	 * Returns the size of the list, including already collected objects.
	 */
	public int size() {
		return refs.size();
	}

	/**
	 * Sets the object at the specified position and returns the previous object at that position or null if it was already collected.
	 */
	public T set(int index, T element) {
		return deref(refs.set(index, new WeakReference<T>(element)));
	}

	/**
	 * Inserts the object at the specified position in the list. Automatically creates a weak reference to the object.
	 */
	public void add(int index, T element) {
		refs.add(index, new WeakReference<T>(element));
	}

	/**
	 * Removes the object at the specified position and returns it or returns null if it was already collected.
	 */
	public T remove(int index) {
		return deref(refs.remove(index));
	}

	/**
	 * Returns a list of hard references to the objects. The returned list does not include the collected elements, so its indices do not necessarily correlate
	 * with those of this list.
	 */
	public List<T> hardList() {
		List<T> result = new ArrayList<>();

		for (int i = 0; i < size(); i++) {
			T tmp = get(i);

			if (tmp != null)
				result.add(tmp);
		}

		return result;
	}

	/**
	 * Compacts the list by removing references to collected objects.
	 */
	public void trimToSize() {
		for (int i = size(); i-- > 0;)
			if (get(i) == null)
				remove(i);
	}
}