package org.rr.jeborker.event;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

import org.rr.jeborker.db.item.EbookPropertyItem;

import com.l2fprod.common.propertysheet.Property;

public class ApplicationEvent extends EventObject {
	
	private List<EbookPropertyItem> items;
	
	private Property metadataProperty;
	
	public ApplicationEvent() {
		super(EMPTY);
	}
	
	public ApplicationEvent(List<EbookPropertyItem> items, Property metadataProperty, Object source) {
		super(source);
		this.items = items;
		this.metadataProperty = metadataProperty;
	}	

	private static final long serialVersionUID = -7161541766120917828L;

	public List<EbookPropertyItem> getItems() {
		return items;
	}

	public void setItems(List<EbookPropertyItem> items) {
		this.items = items;
	}
	
	public void setItem(EbookPropertyItem item) {
		this.items = Arrays.asList(item);
	}

	public Property getMetadataProperty() {
		return metadataProperty;
	}

	public void setMetadataProperty(Property metadataProperty) {
		this.metadataProperty = metadataProperty;
	}

}
