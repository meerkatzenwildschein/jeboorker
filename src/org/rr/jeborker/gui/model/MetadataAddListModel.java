package org.rr.jeborker.gui.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Action;

import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataProperty;

public class MetadataAddListModel extends AbstractListModel {
	
	private static final long serialVersionUID = 661585601885502869L;
	
	private List<MetadataProperty> supportedMetaData = null;
	
	private List<Action> actionsForMetadataProperties = null;
	
	private final IMetadataReader reader;
	
	public MetadataAddListModel(final IMetadataReader reader) {
		this.reader = reader;
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
		final List<MetadataProperty> currentMetaData = reader.readMetaData();
		final ArrayList<MetadataProperty> toRemove = new ArrayList<MetadataProperty>();
		supportedMetaData = reader.getSupportedMetaData();
		for (MetadataProperty supportedProperty : supportedMetaData) {
			for (MetadataProperty metadataProperty : currentMetaData) {
				if(supportedProperty.getName().equals(metadataProperty.getName())) {
					toRemove.add(supportedProperty);
				}
			}
		}
		supportedMetaData.removeAll(toRemove);
		actionsForMetadataProperties = ActionFactory.getAddMetadataActions(supportedMetaData);
	}
}
