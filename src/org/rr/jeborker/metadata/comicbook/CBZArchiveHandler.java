package org.rr.jeborker.metadata.comicbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.truezip.TrueZipDataEntry;
import org.rr.commons.utils.truezip.TrueZipUtils;
import org.rr.commons.utils.zip.ZipFileFilter;

class CBZArchiveHandler implements IArchiveHandler {
	
	private IResourceHandler resource;
	
	private List<String> archiveEntries = new ArrayList<String>();
	
	private byte[] comicInfoXmlContent = null;
	
	private String comicInfoXmlFilePath = null;	
	
	CBZArchiveHandler(IResourceHandler resource) {
		this.resource = resource;
	}

	@Override
	public byte[] getArchiveEntry(String archiveEntry) throws IOException {
		TrueZipDataEntry extract = TrueZipUtils.extract(resource, archiveEntry);
		return extract.getBytes();
	}

	@Override
	public void readArchive() throws IOException {
		archiveEntries.clear();
		List<TrueZipDataEntry> comicInfoXml = TrueZipUtils.extract(resource, new ZipFileFilter() {
			
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
	public String getComicXmlFilename() {
		return this.comicInfoXmlFilePath != null ? comicInfoXmlFilePath : "ComicInfo.xml";
	}

	@Override
	public byte[] getComicXmlData() {
		return this.comicInfoXmlContent;
	}

	@Override
	public List<String> getArchiveEntries() {
		return archiveEntries;
	}

	@Override
	public IResourceHandler getUnderlyingResourceHandler() {
		return this.resource;
	}

	@Override
	public boolean replaceComicInfoXml(byte[] comicInfoXml, String comicInfoFilePath) throws IOException {
		boolean success = TrueZipUtils.add(resource, comicInfoFilePath, new ByteArrayInputStream(comicInfoXml));
		if(!success) {
			LoggerFactory.getLogger().log(Level.WARNING, "Writing CBZ " + resource + " has failed.");
		}	
		
		return true;
	}

}
