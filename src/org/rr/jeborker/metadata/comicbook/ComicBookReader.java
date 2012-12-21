package org.rr.jeborker.metadata.comicbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.zip.ZipUtils;
import org.rr.commons.utils.zip.ZipUtils.ZipDataEntry;
import org.rr.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static org.rr.jeborker.JeboorkerConstants.MIME_CBZ;

public class ComicBookReader {

	private IResourceHandler resource;
	
	private String name;
	
	private String mimeType;
	
	private ComicBookDocument doc;
	
	
	public ComicBookReader(IResourceHandler resource) {
		this.resource = resource;
		this.name = resource.getName();
		this.mimeType = resource.getMimeType();
	}
	
	public ComicBookDocument getDocument() throws IOException {
		if(doc == null) {
			doc = read();
		}
		return doc;
	}
	
	private ComicBookDocument read() throws IOException {
		final List<String> archiveEntries = new ArrayList<String>();
		byte[] comicInfoXmlContent = null;
		
		if(MIME_CBZ.equals(mimeType)) {
			final List<ZipDataEntry> comicInfoXml = ZipUtils.extract(resource.getContentInputStream(), new ZipUtils.ZipFileFilter() {
				
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
			
			if(!comicInfoXml.isEmpty()) {
				comicInfoXmlContent = comicInfoXml.get(0).data;
			}
		}
		
		//needs to be sorted because the entries will be shown in it's sorted order.
		Collections.sort(archiveEntries);
		ComicBookDocument doc = buildDocument(comicInfoXmlContent, archiveEntries);
		
		//no cover set, simply take the first image 
		if(doc.getCover() == null) {
			String archiveEntry = archiveEntries.get(0);
			doc.setCover(getArchiveEntry(archiveEntry));
		}
		return doc;
	}
	
	private byte[] getArchiveEntry(String archiveEntry) throws IOException {
		if(MIME_CBZ.equals(mimeType)) {
			ZipDataEntry extract = ZipUtils.extract(resource.getContentInputStream(), archiveEntry);
			return extract.data;
		}
		return null;
	}
	
	private ComicBookDocument buildDocument(byte[] comicInfoXmlContent, List<String> archiveEntries) throws IOException {
		final ComicBookDocument doc = new ComicBookDocument(comicInfoXmlContent);
		if(comicInfoXmlContent != null) {
			try {
				Document document = XMLUtils.getDocument(comicInfoXmlContent);
				Element comicInfoElement = document.getDocumentElement();
				NodeList childNodes = comicInfoElement.getChildNodes();
				for(int i = 0; i < childNodes.getLength(); i++) {
					Element item = (Element) childNodes.item(i);
					if(item.getNodeName().equalsIgnoreCase("Pages")) {
						setupPages(item, archiveEntries, doc);
					} else {
						setupComicItem(item, doc);
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
		String name = item.getNodeName();
		if(name.equals("BlackAndWhite") || name.equals("Manga")) {
			doc.getInfo().put(item.getNodeName(), YeyNoType.getInstance(item.getNodeValue()));	
		} else if(name.equals("Count") || name.equals("Volume") || name.equals("AlternateCount")
				|| name.equals("Year") || name.equals("Month") || name.equals("PageCount")) {
			Number number = CommonUtils.toNumber(item.getNodeValue());
			if(number != null) {
				doc.getInfo().put(item.getNodeName(), number.intValue());
			} else {
				LoggerFactory.getLogger().log(Level.INFO, name + " the value " + item.getNodeValue() + " is not a number.");
			}
		} else {
			doc.getInfo().put(item.getNodeName(), item.getNodeValue());
		}
	}
	
	private void setupPages(Element pagesElement, List<String> archiveEntries, ComicBookDocument doc) throws IOException {
		NodeList pageNodes = pagesElement.getChildNodes();
		for(int i = 0; i < pageNodes.getLength(); i++) {
			final Element pageElement = (Element) pageNodes.item(i);
			final ComicBookPageInfo page = new ComicBookPageInfo();
			
			String attributeValue;
			if((attributeValue = pageElement.getAttribute("Image")) != null) {
				Number number = CommonUtils.toNumber(attributeValue);
				if(number != null) {
					page.setImage(number.intValue());
				} else {
					LoggerFactory.getLogger().log(Level.INFO, name + " the Image attribute value " + attributeValue + " is not a number.");
				}				
			} else if((attributeValue = pageElement.getAttribute("Type")) != null) {
				ComicPageType type = ComicPageType.getInstance(attributeValue);
				page.setType(type);
			} else if((attributeValue = pageElement.getAttribute("DoublePage")) != null) {
				Boolean doublePage = CommonUtils.toBoolean(attributeValue);
				if(doublePage != null) {
					page.setDoublePage(doublePage);
				} else {
					LoggerFactory.getLogger().log(Level.INFO, name + " the DoublePage attribute value " + attributeValue + " is not a boolean.");
				}
			} else if((attributeValue = pageElement.getAttribute("ImageSize")) != null) {
				Number number = CommonUtils.toNumber(attributeValue);
				if(number != null) {
					page.setImageSize(number.intValue());
				} else {
					LoggerFactory.getLogger().log(Level.INFO, name + " the ImageSize attribute value " + attributeValue + " is not a number.");
				}				
			} else if((attributeValue = pageElement.getAttribute("ImageWidth")) != null) {
				Number number = CommonUtils.toNumber(attributeValue);
				if(number != null) {
					page.setImageWidth(number.intValue());
				} else {
					LoggerFactory.getLogger().log(Level.INFO, name + " the ImageWidth attribute value " + attributeValue + " is not a number.");
				}				
			} else if((attributeValue = pageElement.getAttribute("Key")) != null) {
				page.setKey(attributeValue);
			}
			doc.getPages().add(page);
		}
		setupCover(doc.getPages(), archiveEntries, doc);
	}
	
	private void setupCover(List<ComicBookPageInfo> pages, List<String> archiveEntries, ComicBookDocument doc) throws IOException {
		//search for the cover
		for(ComicBookPageInfo page : pages) {
			if(page.getType().equals(ComicPageType.TYPE_FRONTCOVER)) {
				int index = page.getImage();
				String archiveEntry = archiveEntries.get(index);
				doc.setCover(getArchiveEntry(archiveEntry));
			}
		}
	}
}
