package org.rr.commons.collection;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * List that caches underlying list items.
 * 
 * @param <T> The target, transformed type.
 * @param <S> The source type.
 */
public class CacheValueList<T> extends AbstractList<T> {

	Map<Integer, T> cache = new HashMap<>();
	
	List<T> toCache;
	
	public CacheValueList(List<T> toCache) {
		this.toCache = toCache;
	}
	
	@Override
	public T get(int index) {
		Integer key = Integer.valueOf(index);
		if(cache.containsKey(key)) {
			return cache.get(key);
		}
		return cache.put(key, toCache.get(index));
	}

	@Override
	public int size() {
		return toCache.size();
	}

}