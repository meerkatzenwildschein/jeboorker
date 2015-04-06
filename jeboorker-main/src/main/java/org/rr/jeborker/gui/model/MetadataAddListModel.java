package org.rr.jeborker.gui.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Action;

import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.metadata.MetadataProperty;

public class MetadataAddListModel extends AbstractListModel<Action> {

	private static final long serialVersionUID = 661585601885502869L;

	private List<MetadataProperty> supportedMetadata;

	private List<Action> actionsForMetadataProperties;

	private List<MetadataProperty> currentMetadata;

	private final EbookPropertyItem item;

	public MetadataAddListModel(final List<MetadataProperty> supportedMetadata, final List<MetadataProperty> currentMetadata, final EbookPropertyItem item) {
		this.item = item;
		this.currentMetadata = currentMetadata;
		this.supportedMetadata = supportedMetadata;
	}

	@Override
	public int getSize() {
		if(supportedMetadata == null || actionsForMetadataProperties == null) {
			initialize();
		}
		return supportedMetadata.size();
	}

	@Override
	public Action getElementAt(int index) {
		if(supportedMetadata == null || actionsForMetadataProperties == null) {
			initialize();
		}
		return actionsForMetadataProperties.get(index);
	}

	private void initialize() {
		//remove those metadata from the button list which are already in use and only be single ones.
		final ArrayList<MetadataProperty> toRemove = new ArrayList<>();
		for (MetadataProperty supportedProperty : supportedMetadata) {
			for (MetadataProperty currentMetadataProperty : currentMetadata) {
				if(supportedProperty.getName().equals(currentMetadataProperty.getName())) {
					if(supportedProperty.isSingle()) {
						toRemove.add(supportedProperty);
					}
				}
			}
		}
		supportedMetadata.removeAll(toRemove);
		actionsForMetadataProperties = ActionFactory.getAddMetadataActions(supportedMetadata, item, null);
	}
}
