/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.rr.commons.collection;

import java.util.AbstractList;
import java.util.Iterator;

/**
 * A list that only keep the last N elements added. Only support for 
 * {@link #add(Object)}, {@link #get(int)}, {@link #size()}, {@link #iterator()},
 * {@link #clear()} and {@link #toString()}.
 */
public class LruList<E> extends AbstractList<E> {

	protected LRUCacheMap<Integer, E> map;
	
	private int max;
	
	private int count;
	
	public LruList(int size) {
		map = new LRUCacheMap<Integer, E>(size);
		max = size;
	}
	
	private Integer getIndex(int index) {
		if(count > max) {
			if(map.size() < max) {
				return Integer.valueOf(map.size() - max + index);
			} 
		}
		return Integer.valueOf(index);
	}
	
	@Override
	public E get(int index) {
		return map.get(getIndex(index));
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean add(E e) {
		map.put(count++, e);
		return true;
	}
   
    public String toString() {
    	return map.values().toString();
    }

	@Override
	public void clear() {
		map.clear();
		count = 0;
	}

	@Override
	public Iterator<E> iterator() {
		return map.values().iterator();
	} 
    
}
