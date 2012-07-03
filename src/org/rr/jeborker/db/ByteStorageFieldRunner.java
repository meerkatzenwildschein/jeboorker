package org.rr.jeborker.db;

import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.Transient;

import org.rr.commons.utils.ReflectionUtils;

/**
 * Class that allows to perform the implemented run action to each transient byte field 
 * of the given {@link IDBObject} instance. No need to repeat the field iteration.
 */
public abstract class ByteStorageFieldRunner  {
	
	final IDBObject item;
	
	ByteStorageFieldRunner(final IDBObject item) {
		this.item = item;
	}
	
	public abstract void run(Field field);
	
	public IDBObject getItem() {
		return this.item;
	}

	public void start() {
		final List<Field> fields = ReflectionUtils.getFields(item.getClass(), ReflectionUtils.VISIBILITY_VISIBLE_ALL);
		for (Field field : fields) {
			if(field.getType().getName().equals(byte[].class.getName())) {
				if(field.getAnnotation(Transient.class) != null) {
					run(field);
				}
			}
		}			
	}

}
