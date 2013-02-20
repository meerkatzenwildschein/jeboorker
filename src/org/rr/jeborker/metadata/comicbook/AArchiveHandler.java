package org.rr.jeborker.metadata.comicbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;

abstract class AArchiveHandler implements IArchiveHandler {
	
	protected IResourceHandler resource;
	
	protected List<String> archiveEntries = new ArrayList<String>();
	
	protected byte[] comicInfoXmlContent = null;
	
	protected String comicInfoXmlFilePath = null;	
	
	AArchiveHandler(IResourceHandler resource) {
		this.resource = resource;
	}

	@Override
	public abstract byte[] getArchiveEntry(String archiveEntry) throws IOException;
	
	@Override
	public abstract void readArchive() throws IOException;

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
	public abstract boolean replaceComicInfoXml(byte[] comicInfoXml, String comicInfoFilePath) throws IOException;

}
