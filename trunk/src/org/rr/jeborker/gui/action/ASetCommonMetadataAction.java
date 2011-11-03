package org.rr.jeborker.gui.action;

import java.util.List;

import org.rr.commons.swing.dialogs.SimpleInputDialog;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.event.RefreshAbstractAction;
import org.rr.jeborker.metadata.IMetadataWriter;
import org.rr.jeborker.metadata.MetadataProperty;
import org.rr.jeborker.metadata.MetadataUtils;

public abstract class ASetCommonMetadataAction extends RefreshAbstractAction implements IDoOnlyOnceAction<SimpleInputDialog> {

	/**
	 * Transfers the given value to each entry in the given MetadataProperty list.
	 * @param value The value to be set.
	 * @param specificMetaData The metadata getting the value.
	 */
	protected void transferValueToMetadata(final String value, List<MetadataProperty> specificMetaData) {
		for (MetadataProperty authorMetadataProperty : specificMetaData) {
			List<String> authors = ListUtils.split(value, ',');
			for (int i = 0; i < authors.size(); i++) {
				authorMetadataProperty.setValue(authors.get(i), i);
			}
		}
	}
	

	/**
	 * Merge and write the metadata.
	 * @param writer  Writer to be used for writing the metadata
	 * @param allMetaData The complete metadata set for the file. 
	 * @param specificMetaData The metadata which should be added or overwrites this ones in the allMetaData parameter.
	 */
	protected void mergeAndWrite(final IMetadataWriter writer, List<MetadataProperty> allMetaData, List<MetadataProperty> specificMetaData) {
		List<MetadataProperty> mergeMetadata = MetadataUtils.mergeMetadata(allMetaData, specificMetaData);
		writer.writeMetadata(mergeMetadata.iterator());
	}	
}
