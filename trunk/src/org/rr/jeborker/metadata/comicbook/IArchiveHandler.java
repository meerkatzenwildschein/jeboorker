package org.rr.jeborker.metadata.comicbook;

import java.io.IOException;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;

public interface IArchiveHandler {

	public byte[] getArchiveEntry(String archiveEntry) throws IOException;
	
	/**
	 * Reads the comic book archive and collects some informations which can be
	 * fetched with the {@link #getComicXmlFilename()} or {@link #getComicXmlData()}
	 * methods.
	 * @throws IOException
	 */
	public void readArchive() throws IOException;
	
	/**
	 * Gets the file name for the ComicInfo.xml file stored in the zip archive. in most
	 * cases it will be named ComicInfo.xml. Please note that {@link #readArchive()} 
	 * must be invoked before this getter returns a valuable result. 
	 */
	public String getComicXmlFilename();

	/**
	 * Gets the xml data for the ComicInfo.xml file stored in the zip archive.
	 * Please note that {@link #readArchive()}  must be invoked before this getter returns a valuable result. 
	 */	
	public byte[] getComicXmlData();
	
	/**
	 * get the entries from the archive in the right, alphabetical sorted order.
	 */
	public List<String> getArchiveEntries();
	
	/**
	 * Gets the {@link IResourceHandler} instance for this {@link IArchiveHandler} instance
	 * which contains the comic book archive data.
	 */
	public IResourceHandler getUnderlyingResourceHandler();
	
	/**
	 * Add or replace the ComicInfo.xml file
	 */
	public boolean replaceComicInfoXml(byte[] comicInfoXml, String comicInfoFilePath) throws IOException;
	
}
