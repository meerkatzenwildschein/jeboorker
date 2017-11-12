package org.rr.commons.utils;

public abstract class RunnableImpl<S, T> {
	public abstract T run(S entry);
}
