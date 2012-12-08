package org.rr.jeborker.gui.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Action;

import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataProperty;

public class MetadataAddListModel extends AbstractListModel {
	
	private static final long serialVersionUID = 661585601885502869L;
	
	private List<MetadataProperty> supportedMetaData = null;
	
	private List<Action> actionsForMetadataProperties = null;
	
	private List<MetadataProperty> currentMetaData;
	
	private final IMetadataReader reader;
	
	private final EbookPropertyItem item;
	
	public MetadataAddListModel(final IMetadataReader reader, final List<MetadataProperty> currentMetaData, final EbookPropertyItem item) {
		this.reader = reader;
		this.item = item;
		this.currentMetaData = currentMetaData;
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
		if(this.currentMetaData == null) {
			this.currentMetaData = reader.readMetaData();
		}
		supportedMetaData = reader.getSupportedMetaData();
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
