package org.rr.commons.collection;

import java.util.EventListener;

/**
 * An {@link EventListener} interface which can be used for creating 
 * listeners which can be added to the {@link CursableCollection}.
 *
 * @param <E>
 * @param <A>
 */
public interface CursableCollectionListener<E, A> extends EventListener {
	
	/**
	 * Invoked if the cursor has changed.
	 * @param collection The collection where the corsor change has been occured.
	 */
	public void afterCursorChanged(CursableCollection<E, A> collection);
	
	/**
	 * Invoked before the cursor gets changed.
	 * @param collection The collection where the corsor change will occures.
	 */
	public void beforeCursorChanged(CursableCollection<E, A> collection);	
	
}
