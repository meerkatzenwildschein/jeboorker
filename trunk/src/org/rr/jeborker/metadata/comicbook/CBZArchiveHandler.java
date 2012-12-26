package org.rr.jeborker.metadata.comicbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.zip.ZipUtils;
import org.rr.commons.utils.zip.ZipUtils.ZipDataEntry;

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
		ZipDataEntry extract = ZipUtils.extract(resource.getContentInputStream(), archiveEntry);
		return extract.getBytes();
	}

	@Override
	public void readArchive() throws IOException {
		archiveEntries.clear();
		List<ZipDataEntry> comicInfoXml = ZipUtils.extract(resource.getContentInputStream(), new ZipUtils.ZipFileFilter() {
			
			@Override
			public boolean accept(String entry) {
				if(entry.toLowerCase().endsWith("comicinfo.xml")) {
					return true;
				} else {
					archiveEntries.add(entry);
				}
				return false;
			}
		}, -1);
		
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
		final ZipUtils.ZipDataEntry zipDataEntry = new ZipUtils.ZipDataEntry(comicInfoFilePath, comicInfoXml);
		
		IResourceHandler tmpEbookResource = resource.getTemporaryResource();
		OutputStream out = tmpEbookResource.getContentOutputStream(false);
		InputStream in = resource.getContentInputStream();
		
		boolean success = ZipUtils.add(in, out, zipDataEntry, true);
		if(success && tmpEbookResource.size() > 0) {
			//new temp pdf looks good. Move the new temp one over the old one. 
			tmpEbookResource.moveTo(resource, true);
		} else {
			LoggerFactory.getLogger().log(Level.WARNING, "Writing CBZ " + resource + " has failed.");
			tmpEbookResource.delete();
		}		
		
		return true;
	}

}
