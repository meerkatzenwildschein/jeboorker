package org.rr.jeborker.db.item;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DBViewField {

	/**
	 * The name allows to specify a more beautiful name
	 * which can be localized and shown to the ui.  
	 */
	public String name();
	
	/**
	 * Order for sorting and so on. Higher value means that the
	 * field is listed on the top.
	 */
	public int orderPriority();
	
}
