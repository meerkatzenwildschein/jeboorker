package org.rr.jeborker.gui.additional;

import java.lang.reflect.Field;
import java.util.Comparator;

import org.rr.jeborker.db.item.ViewField;

/**
 * Can be used for sorting the DBViewField fields into the right order.
 */
public class EbookPropertyItemFieldComperator implements Comparator<Field> {

	@Override
	public int compare(Field o1, Field o2) {
		ViewField annotation1 = o1.getAnnotation(ViewField.class);
		ViewField annotation2 = o2.getAnnotation(ViewField.class);
		if(annotation1!=null && annotation2!=null) {
			return Integer.valueOf(annotation1.orderPriority()).compareTo(Integer.valueOf(annotation2.orderPriority())) * -1;
		}
		return 0;
	}
}
