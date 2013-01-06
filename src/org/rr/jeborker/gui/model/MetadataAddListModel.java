package org.rr.jeborker.gui.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Action;

import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.metadata.MetadataProperty;

public class MetadataAddListModel extends AbstractListModel {
	
	private static final long serialVersionUID = 661585601885502869L;
	
	private List<MetadataProperty> supportedMetaData = null;
	
	private List<Action> actionsForMetadataProperties = null;
	
	private List<MetadataProperty> currentMetaData;
	
	private final EbookPropertyItem item;
	
	public MetadataAddListModel(final List<MetadataProperty> supportedMetaData, final List<MetadataProperty> currentMetaData, final EbookPropertyItem item) {
		this.item = item;
		this.currentMetaData = currentMetaData;
		this.supportedMetaData = supportedMetaData;
	}
	
	@Override
	public int getSize() {
		if(supportedMetaData == null) {
			initialize();
		}
		return supportedMetaData.size();
	}
	
	@Override
	public Object getElementAt(int index) {
		if(supportedMetaData == null) {
			initialize();
		}					
		return actionsForMetadataProperties.get(index);
	}
	
	private void initialize() {
		final ArrayList<MetadataProperty> toRemove = new ArrayList<MetadataProperty>();
		for (MetadataProperty supportedProperty : supportedMetaData) {
			for (MetadataProperty currentMetadataProperty : currentMetaData) {
				if(supportedProperty.getName().equals(currentMetadataProperty.getName())) {
					if(supportedProperty.isSingle()) {
						toRemove.add(supportedProperty);
					}
				}
			}
		}
		supportedMetaData.removeAll(toRemove);
		actionsForMetadataProperties = ActionFactory.getAddMetadataActions(supportedMetaData, item);
	}
}
