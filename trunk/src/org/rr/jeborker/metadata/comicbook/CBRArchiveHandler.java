package org.rr.jeborker.metadata.comicbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.compression.CompressedDataEntry;
import org.rr.commons.utils.compression.FileEntryFilter;
import org.rr.commons.utils.compression.rar.RarUtils;

public class CBRArchiveHandler extends AArchiveHandler {

	CBRArchiveHandler(IResourceHandler resource) {
		super(resource);
	}

	@Override
	public boolean replaceComicInfoXml(byte[] comicInfoXml, String comicInfoFilePath) throws IOException {
		boolean success = RarUtils.add(resource, comicInfoFilePath, new ByteArrayInputStream(comicInfoXml));
		if(!success) {
			LoggerFactory.getLogger().log(Level.WARNING, "Writing CBR " + resource + " has failed.");
		}	
		
		return success;
	}
	
	@Override
	public void readArchive() throws IOException {
		archiveEntries.clear();
		List<CompressedDataEntry> comicInfoXml = RarUtils.extract(resource, new FileEntryFilter() {
			
			@Override
			public boolean accept(String entry) {
				if(entry.toLowerCase().endsWith("comicinfo.xml")) {
					return true;
				} else {
					archiveEntries.add(entry);
				}
				return false;
			}
		});
		
		Collections.sort(archiveEntries);
		
		if(!comicInfoXml.isEmpty()) {
			comicInfoXmlContent = comicInfoXml.get(0).getBytes();
			comicInfoXmlFilePath = comicInfoXml.get(0).path;
		}
	}
	
	@Override
	public byte[] getArchiveEntry(String archiveEntry) throws IOException {
		CompressedDataEntry extract = RarUtils.extract(resource, archiveEntry);
		return extract.getBytes();
	}	
}
