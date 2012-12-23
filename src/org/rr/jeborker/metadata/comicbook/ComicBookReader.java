package org.rr.jeborker.metadata.comicbook;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ComicBookReader {
	
	private ComicBookDocument doc;
	
	private IArchiveHandler archiveHandler;
	
	public ComicBookReader(IResourceHandler resource) {
		this.archiveHandler = ArchiveHandlerFactory.getHandler(resource);
	}
	
	public ComicBookDocument getDocument() throws IOException {
		if(doc == null) {
			doc = read();
		}
		return doc;
	}

	private ComicBookDocument read() throws IOException {
		archiveHandler.readArchive();
		
		//needs to be sorted because the entries will be shown in it's sorted order.
		ComicBookDocument doc = buildDocument(archiveHandler);
		
		return doc;
	}
	
	private ComicBookDocument buildDocument(IArchiveHandler archiveHandler) throws IOException {
		final ComicBookDocument doc = new ComicBookDocument(archiveHandler);
		byte[] comicXmlData = archiveHandler.getComicXmlData();
		if(comicXmlData != null && comicXmlData.length > 0) {
			try {
				Document document = XMLUtils.getDocument(comicXmlData);
				Element comicInfoElement = document.getDocumentElement();
				NodeList childNodes = comicInfoElement.getChildNodes();
				for(int i = 0; i < childNodes.getLength(); i++) { 
					if(childNodes.item(i) instanceof Element) {
						Element item = (Element) childNodes.item(i);
						if(item.getNodeName().equalsIgnoreCase("Pages")) {
							setupPages(item, doc);
						} else {
							setupComicItem(item, doc);
						}
					}
				}
			} catch (ParserConfigurationException e) {
				throw new IOException(e);
			} catch (SAXException e) {
				throw new IOException(e);
			}
		}
		return doc;
	}
	
	private void setupComicItem(Element item, ComicBookDocument doc) {
		doc.getInfo().put(item.getNodeName(), item.getTextContent());
	}
	
	private void setupPages(Element pagesElement, ComicBookDocument doc) throws IOException {
		NodeList pageNodes = pagesElement.getChildNodes();
		for(int i = 0; i < pageNodes.getLength(); i++) {
			if(pageNodes.item(i) instanceof Element) {
				final ComicBookPageInfo page = new ComicBookPageInfo();
				final Element pageElement = (Element) pageNodes.item(i);
				final NamedNodeMap attributes = pageElement.getAttributes();
				
				for(int j = 0; j < attributes.getLength(); j++) {
					Node item = attributes.item(j);
					String attributeName = item.getNodeName();
					String attributeValue = item.getNodeValue();
					
					page.getInfo().put(attributeName, attributeValue);
				}
				doc.getPages().add(page);
			}
		}
	}
}
