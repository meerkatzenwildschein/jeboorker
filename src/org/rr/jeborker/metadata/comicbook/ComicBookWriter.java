package org.rr.jeborker.metadata.comicbook;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ComicBookWriter {
	
	
	private ComicBookDocument doc;

	private IArchiveHandler archiveHandler;


	public ComicBookWriter(ComicBookDocument doc, IResourceHandler resource) {
		this.doc = doc;
		this.archiveHandler = ArchiveHandlerFactory.getHandler(resource);
	}
	
	public void writeDocument() throws IOException {
		try {
			final byte[] comicInfoXml = createXML();
			writePlainXML(comicInfoXml);
		} catch(IOException e) {
			throw e;
		} catch(Exception e) {
			throw new IOException(e);
		}
	}
	
	public void writePlainXML(byte[] comicInfoXml) throws IOException {
		try {
			String comicInfoFilePath;
			if(doc != null) {
				comicInfoFilePath = doc.getComicInfoFilePath() != null ? doc.getComicInfoFilePath() : "ComicInfo.xml";
			} else {
				ComicBookReader comicBookReader = new ComicBookReader(archiveHandler.getUnderlyingResourceHandler());
				comicInfoFilePath = comicBookReader.getDocument().getComicInfoFilePath();
			}
			this.archiveHandler.replaceComicInfoXml(comicInfoXml, comicInfoFilePath);
		} catch(IOException e) {
			throw e;
		} catch(Exception e) {
			throw new IOException(e);
		}		
	}
	
	private byte[] createXML() throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException {
		Document xmlDoc = XMLUtils.createEmptyDocument("ComicInfo");
		Element comicInfoElement = xmlDoc.getDocumentElement();
		comicInfoElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
		comicInfoElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
	
		setupEntries(xmlDoc, comicInfoElement);		
		setupPages(xmlDoc, comicInfoElement);
		String xml = XMLUtils.formatDocument(xmlDoc);
		return xml.getBytes();
	}

	protected void setupEntries(Document xmlDoc, Element comicInfoElement) {
		HashMap<String, Object> docInfo = doc.getInfo();
		for (Entry<String, Object> e : docInfo.entrySet()){
		    String tagName = e.getKey();
		    Object value = e.getValue();
		    
		    if(value != null) {
			    Element element = xmlDoc.createElement(tagName);
			    element.setTextContent(String.valueOf(value));
			    comicInfoElement.appendChild(element);
		    }
		}
	}
	
	private void setupPages(Document xmlDoc, Element comicInfoElement) {
		List<ComicBookPageInfo> pages = doc.getPages();
		if(!pages.isEmpty()) {
			Element pagesElement = xmlDoc.createElement("Pages");
			for(ComicBookPageInfo page : pages) {
				HashMap<String, Object> pagesInfo = page.getInfo();
				Element pageElement = xmlDoc.createElement("Page");
				for (Entry<String, Object> pageEntry : pagesInfo.entrySet()){
					pageElement.setAttribute(pageEntry.getKey(), StringUtils.toString(pageEntry.getValue()));
				}
				pagesElement.appendChild(pageElement);
			}
			comicInfoElement.appendChild(pagesElement);
		}
	}
	
}
