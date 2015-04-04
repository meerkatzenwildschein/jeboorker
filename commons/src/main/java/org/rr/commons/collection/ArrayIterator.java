package org.rr.commons.collection;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/** 
 * From apache.commons.collection
 * @version $Revision: 647116 $ $Date: 2008-04-11 12:23:08 +0100 (Fri, 11 Apr 2008) $
 */
public class ArrayIterator<E> implements ListIterator<E> {
	/**
     * Holds the index of the last item returned by a call to <code>next()</code>
     * or <code>previous()</code>. This is set to <code>-1</code> if neither method
     * has yet been invoked. <code>lastItemIndex</code> is used to to implement 
     * the {@link #set} method.
     *
     */
    protected int lastItemIndex = -1;
    
	/** The array to iterate over */    
    protected List<E> array;
    /** The start index to loop from */
    protected int startIndex = 0;
    /** The end index to loop to */
    protected int endIndex = 0;
    /** The current iterator index */
    protected int index = 0;
    
    // Constructors
    // ----------------------------------------------------------------------
    /**
     * Constructs an ArrayIterator that will iterate over the values in the
     * specified array.
     *
     * @param array the array to iterate over.
     * @throws IllegalArgumentException if <code>array</code> is not an array.
     * @throws NullPointerException if <code>array</code> is <code>null</code>
     */
    public ArrayIterator(final List<E> array) {
        super();
        setArray(array);
    }

    /**
     * Checks whether the index is valid or not.
     * 
     * @param bound  the index to check
     * @param type  the index type (for error messages)
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    protected void checkBound(final int bound, final String type ) {
        if (bound > this.endIndex) {
            throw new ArrayIndexOutOfBoundsException(
              "Attempt to make an ArrayIterator that " + type +
              "s beyond the end of the array. "
            );
        }
        if (bound < 0) {
            throw new ArrayIndexOutOfBoundsException(
              "Attempt to make an ArrayIterator that " + type +
              "s before the start of the array. "
            );
        }
    }

    // Iterator interface
    //-----------------------------------------------------------------------
    /**
     * Returns true if there are more elements to return from the array.
     *
     * @return true if there is a next element to return
     */
    public boolean hasNext() {
        return (index < endIndex);
    }

    /**
     * Returns the next element in the array.
     *
     * @return the next element in the array
     * @throws NoSuchElementException if all the elements in the array
     *  have already been returned
     */
    public E next() {
        if (hasNext() == false) {
            throw new NoSuchElementException();
        }
        this.lastItemIndex = this.index;
        
        E e = array.get(index);
        index++;
        return e;
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException always
     */
    public void remove() {
        throw new UnsupportedOperationException("remove() method is not supported");
    }
    
    // ListIterator interface
    //-----------------------------------------------------------------------
    /**
     * Returns true if there are previous elements to return from the array.
     *
     * @return true if there is a previous element to return
     */
    public boolean hasPrevious() {
        return (this.index > this.startIndex);
    }

    /**
     * Gets the previous element from the array.
     * 
     * @return the previous element
     * @throws NoSuchElementException if there is no previous element
     */
    public E previous() {
        if (hasPrevious() == false) {
            throw new NoSuchElementException();
        }
        this.lastItemIndex = --this.index;
        //return Array.get(this.array, this.index);
        return this.array.get(this.index);
    }    

    // Properties
    //-----------------------------------------------------------------------
    /**
     * Gets the array that this iterator is iterating over. 
     *
     * @return the array this iterator iterates over, or <code>null</code> if
     *  the no-arg constructor was used and {@link #setArray(Object)} has never
     *  been called with a valid array.
     */
    public List<E> getArray() {
        return array;
    }
    
    /**
     * Sets the array that the ArrayIterator should iterate over.
     * <p>
     * If an array has previously been set (using the single-arg constructor
     * or this method) then that array is discarded in favour of this one.
     * Iteration is restarted at the start of the new array.
     * Although this can be used to reset iteration, the {@link #reset()} method
     * is a more effective choice.
     *
     * @param array the array that the iterator should iterate over.
     * @throws IllegalArgumentException if <code>array</code> is not an array.
     * @throws NullPointerException if <code>array</code> is <code>null</code>
     */
    private void setArray(final List<E> array) {
        // Array.getLength throws IllegalArgumentException if the object is not
        // an array or NullPointerException if the object is null.  This call
        // is made before saving the array and resetting the index so that the
        // array iterator remains in a consistent state if the argument is not
        // an array or is null.
        this.endIndex = array.size();
        this.startIndex = 0;
        this.array = array;
        this.index = 0;
    }
    
    /**
     * Resets the iterator back to the start index.
     */
    public void reset() {
        this.index = this.startIndex;
        this.lastItemIndex = -1;
    }
    
    /**
     * Gets the next index to be retrieved.
     * 
     * @return the index of the item to be retrieved next
     */
    public int nextIndex() {
        return this.index - this.startIndex;
    }

    /**
     * Gets the index of the item to be retrieved if {@link #previous()} is called.
     * 
     * @return the index of the item to be retrieved next
     */
    public int previousIndex() {
        return this.index - this.startIndex - 1;
    }

    /**
     * This iterator does not support modification of its backing collection, and so will
     * always throw an {@link UnsupportedOperationException} when this method is invoked.
     *
     * @throws UnsupportedOperationException always thrown.
     * @see java.util.ListIterator#set
     */
    public void add(E o) {
        this.array.add(o);
    }
    
    /**
     * Sets the element under the cursor.
     * <p>
     * This method sets the element that was returned by the last call 
     * to {@link #next()} of {@link #previous()}. 
     * <p>
     * <b>Note:</b> {@link ListIterator} implementations that support
     * <code>add()</code> and <code>remove()</code> only allow <code>set()</code> to be called
     * once per call to <code>next()</code> or <code>previous</code> (see the {@link ListIterator}
     * javadoc for more details). Since this implementation does
     * not support <code>add()</code> or <code>remove()</code>, <code>set()</code> may be
     * called as often as desired.
     *
     * @see java.util.ListIterator#set
     */
    public void set(E o) {
        if (this.lastItemIndex == -1) {
            throw new IllegalStateException("must call next() or previous() before a call to set()");
        }

        this.array.set( this.lastItemIndex, o);
    }    
}
