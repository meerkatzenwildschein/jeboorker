package org.rr.commons.mufs;

public interface ResourceNameFilter {

	/**
	 * Tells if the resource to be loaded should be accepted.
	 * 
	 * @param loader The {@link IResourceHandler} instance to be tested.
	 * @return <code>true</code> if the given {@link IResourceHandler} should be accepted or <code>false</code> otherwise.
	 */
	public boolean accept(IResourceHandler loader);
}
