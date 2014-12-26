package org.rr.jeborker.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.math.NumberUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.MimeUtils;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.metadata.ComicBookMetadataReader.COMICBOOK_METADATA_TYPES;
import org.rr.jeborker.metadata.comicbook.ArchiveHandlerFactory;
import org.rr.jeborker.metadata.comicbook.IArchiveHandler;

public class MetadataUtils {

	/**
	 * searches for all metadata properties matching to the given one and returns them.
	 * @param ref The ref to be searched. This one is in the result list in any case.
	 * @param allMetadata The metadata list to be searched.
	 * @return The list with all metadata instances matching with the given one (including the reference one).
	 *     Never returns <code>null</code>.
	 */
	public static List<MetadataProperty> getSameProperties(MetadataProperty ref, List<MetadataProperty> allMetadata) {
		final ArrayList<MetadataProperty> result = new ArrayList<MetadataProperty>(3);
		for (MetadataProperty metadataProperty : allMetadata) {
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


	/**
	 * Copies the metadata from the source to target. This method only works with the same ebook types.
	 */
	public static void copyMetadata(IResourceHandler source, IResourceHandler target) {
		if(!source.getMimeType(true).equals(target.getMimeType(true))) {
			throw new IllegalArgumentException("Can't copy metadata from " + source + " to " + target);
		}
		IMetadataReader sourceReader = MetadataHandlerFactory.getReader(source);
		List<MetadataProperty> metadata = sourceReader.readMetadata();

		MetadataUtils.refreshPageNumberMetadata(sourceReader, metadata, target);

		IMetadataWriter writer = MetadataHandlerFactory.getWriter(target);
		writer.writeMetadata(metadata);
	}

	/**
	 * Some ebook metdata store the page number which could change after processing. This method rereads the
	 * ebook and corrects the metadata value if there is one present.
	 * @param sourceReader The reader for the ebook which page numbers should be refreshed.
	 * @param metadataProperties The metadata properties previously read from the given {@link IMetadataReader}.
	 * @param target The target ebook resource where the actual page count is read from.
	 */
	public static void refreshPageNumberMetadata(IMetadataReader sourceReader, List<MetadataProperty> metadataProperties, IResourceHandler target) {
		if (sourceReader instanceof ComicBookMetadataReader && (MimeUtils.isCbr(target, true) || MimeUtils.isCbz(target, true))) {
			IArchiveHandler archiveHandler = ArchiveHandlerFactory.getHandler(target);
			try {
				archiveHandler.readArchive();
			} catch (IOException e) {
				LoggerFactory.getLogger(MetadataUtils.class).log(Level.WARNING, "Failed to read archive " + target, e);
				return;
			}

			for (MetadataProperty metadataProperty : metadataProperties) {
				if (metadataProperty.getName().equals(COMICBOOK_METADATA_TYPES.PAGECOUNT.getName())
						|| metadataProperty.getName().equals(COMICBOOK_METADATA_TYPES.COUNT.getName())) {
					int actualPageCount = archiveHandler.getArchiveEntries().size();
					if (NumberUtils.isNumber(metadataProperty.getValueAsString())) {
						metadataProperty.setValue(String.valueOf(actualPageCount), 0);
					}
				}
			}
		}
	}
}
