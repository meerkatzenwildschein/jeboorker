package org.rr.jeborker.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetadataUtils {

	/**
	 * Merges the given list with all the metadata entries from the first and second list. 
	 * The second list entries will win if any entry already exists in first. 
	 * @param first First list with entries to merge
	 * @param second Second list with entries to merge
	 * @return The merged list.
	 */
	public static List<MetadataProperty> mergeMetadata(List<MetadataProperty> first, List<MetadataProperty> second) {
		if(first == null || first.isEmpty()) {
			if(second!=null) {
				return second;
			} 
			return Collections.emptyList();
		} else if(second == null || second.isEmpty()) {
			if(first!=null) {
				return first;
			} 
		}		
		
		final ArrayList<MetadataProperty> merged = new ArrayList<MetadataProperty>(first.size() + second.size());
		merged.addAll(first);
		for (MetadataProperty secondMetadataProperty : second) {
			for (MetadataProperty firstMetadataProperty : first) {
				if(firstMetadataProperty.getName() == secondMetadataProperty.getName()) {
					merged.remove(firstMetadataProperty);
				}
			}
		}
		merged.addAll(second);
		return merged;
	}
}
