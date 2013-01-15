package org.rr.commons.collection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides support for a LRU (least recently used) cache.
 * This cache is not thread safe and would need to wrap with Collections.synchronizedMap() if thread safety is needed.
 */
public class LRUCacheMap<K, V> extends LinkedHashMap<K, V>{
	
	private static final long serialVersionUID = 4709696324495431394L;
	
	private final int maxSize;
	
	/**
	 * @param maxSize maximum number of entries.
	 */
	public LRUCacheMap(final int maxSize) {
		super(maxSize * 4 / 3, 0.75f, true);
		this.maxSize = maxSize;
	}

	@Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > maxSize;
    }
}
