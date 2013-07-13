package org.rr.jeborker.metadata;

import java.util.ArrayList;
import java.util.List;

import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.StringUtils;

public class MetadataUtils {
	
	/**
	 * searches for all metadata properties matching to the given one and returns them. 
	 * @param ref The ref to be searched. This one is in the result list in any case.
	 * @param allMetaData The metadata list to be searched.
	 * @return The list with all metadata instances matching with the given one (including the reference one). 
	 *     Never returns <code>null</code>.
	 */
	public static List<MetadataProperty> getSameProperties(MetadataProperty ref, List<MetadataProperty> allMetaData) {
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>(3);
		for (MetadataProperty metadataProperty : allMetaData) {
			if(comparePropertiesForMerge(metadataProperty, ref)) {
				result.add(metadataProperty);
			}
		}
		return result;
	}
	


	/**
	 * Compares the metadata properties if they could be merged to one property. Properties
	 * can only be merged if they have the same value and size.
	 * 
	 * @param metadataProperty1 The first property to be compared.
	 * @param metadataProperty2 The second property to be compared.
	 * @return <code>true</code> for merge and <code>false</code> otherwise.
	 */
	private static boolean comparePropertiesForMerge(final MetadataProperty metadataProperty1, MetadataProperty metadataProperty2) {
		if(metadataProperty1 == metadataProperty2) {
			return true;
		}
		
		// test for value
		boolean result = metadataProperty2.getValues().size() == 1 && metadataProperty1.getValues().size() == 1
			&& CommonUtils.compareTo(metadataProperty2.getValues().get(0), metadataProperty1.getValues().get(0)) == 0;
		
		final String metadataProperty1Name = StringUtils.toString(metadataProperty1.getName()).toLowerCase();
		final String metadataProperty2Name = StringUtils.toString(metadataProperty2.getName()).toLowerCase();		

		if (result && metadataProperty1Name.equals(metadataProperty2Name)) {
			// name is the same
			return true;
		} else if(StringUtils.compareTwice(metadataProperty1Name, metadataProperty2Name, "createdate", "creationdate")) {
			//merge createdate and creationdate because they have the same sense
			return true;
		}  else if(StringUtils.compareTwice(metadataProperty1Name, metadataProperty2Name, "modifydate", "moddate")) {
			//merge createdate and creationdate because they have the same sense
			return true;
		} else if(StringUtils.compareTwice(metadataProperty1Name, metadataProperty2Name, "calibrerating", "rating")) {
			return true;
		} else {
			return false;
		}
	}	
		
}
