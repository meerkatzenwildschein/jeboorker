package org.rr.jeborker.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.UtilConstants;
import org.rr.commons.utils.ZipUtils;
import org.rr.commons.utils.ZipUtils.ZipDataEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AEpubMetadataHandler extends AMetadataHandler {
	
	private IResourceHandler ebookResourceHandler;

	private byte[] containerOpfData = null;
	
	protected byte[] zipContent = null;
	
	private String opfFileName = null;
	
	AEpubMetadataHandler(IResourceHandler ebookResourceHandler) {
		this.ebookResourceHandler = ebookResourceHandler;
	}	
	
	/**
	 * Gets the {@link IResourceHandler} instance for the ebook which is processed
	 * by this {@link AMetadataHandler} instance.
	 * @return The desired {@link IResourceHandler} instance.
	 */
	public IResourceHandler getEbookResource() {
		return this.ebookResourceHandler;
	}
	
	/**
	 * Gets the Opf file where the metadata is stored. The information where the opf
	 * file could be found is stored in the META-INF/container.xml file. This information
	 * will be extracted from there. 
	 * 
	 * @param zipData The zip data bytes.
	 * @return The desired file name or <code>null</code> if no one could be found.
	 */
	protected String getOpfFile(byte[] zipData) {
		if(this.opfFileName == null) {
			final String fullPathString = "full-path=";
			final ZipDataEntry containerXml = ZipUtils.extract(zipData, "META-INF/container.xml");
			if(containerXml!=null) {
				final String containerXmlData = new String(containerXml.data);
				final int fullPathIndex = containerXmlData.indexOf(fullPathString);
				if(fullPathIndex!=-1) {
					final int startIdx = fullPathIndex + fullPathString.length() + 1;
					final int endIdx = containerXmlData.indexOf('"', startIdx);
					final String fullPathValue = containerXmlData.substring(startIdx, endIdx);
					this.opfFileName = fullPathValue;
				}
			}
		}
		return this.opfFileName;
	}	
	
	/**
	 * Gets the path of the opf file. For example if the opf file is located at
	 * "OEPS/container.opf" the vlaue "OEPS/" is returned.  
	 * @param zipData The zip data containing the opf file.
	 * @return The desired path. Never returns <code>null</code>.
	 */
	protected String getOpfFilePath(byte[] zipData) {
		String opfFile = getOpfFile(zipData);
		if(opfFile.indexOf('/')!=-1) {
			return StringUtils.substringBefore(opfFile, "/", false, UtilConstants.COMPARE_BINARY) + "/";
		}
		return "";
	}
	

	/**
	 * gets the container opf file content bytes containing the metdadata informations.
	 */
	protected byte[] getContainerOPF(final IResourceHandler ebookResource) throws IOException {
		if (this.containerOpfData == null) {
			final byte[] zipData = this.getContent(ebookResource);
			return getContainerOPF(zipData);
		}
		return this.containerOpfData;
	}	
	
	/**
	 * gets the container opf file content bytes containing the metdadata informations.
	 */
	protected byte[] getContainerOPF(final byte[] zipData) throws IOException {
		if (this.containerOpfData == null) {
			final String opfFile = this.getOpfFile(zipData);
			if (opfFile != null) {
				final ZipDataEntry containerXml = ZipUtils.extract(zipData, opfFile);
				if (containerXml != null) {
					this.containerOpfData = containerXml.data;
				} else {
					LoggerFactory.logWarning(this, "Could not get file" + opfFile, new RuntimeException("dumpstack"));
				}
			}
		}
		return this.containerOpfData;
	}
	

	/**
	 * Get the zip data content bytes from the epub+zip file. The content
	 * is cached so it's performance safe so invoke this method frequently.
	 */
	protected byte[] getContent(final IResourceHandler ebookResource) throws IOException {
		if (this.zipContent == null) {
			this.zipContent = ebookResource.getContent();
		}
		return this.zipContent;
	}	
	
	/**
	 * Gets the metadata node. The metadata node contains all the informations
	 * that should be provided by the reader.
	 * @param xmlData
	 * @return The desired metadata node.
	 */
	protected Element getMetadataElement(Document document) {
		try {
			NodeList metadataElement = document.getElementsByTagName("metadata");
			if(metadataElement!=null && metadataElement.getLength()>0) {
				return (Element) metadataElement.item(0);
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "parsing xml has failed", e);
		}
		return null;
	}	
	
	/**
	 * Gets the manifest node. The metadata node contains all the informations
	 * that should be provided by the reader.
	 * @param xmlData
	 * @return The desired metadata node.
	 */
	protected Element getManifestElement(Document document) {
		try {
			NodeList metadataElement = document.getElementsByTagName("manifest");
			if(metadataElement!=null && metadataElement.getLength()>0) {
				return (Element) metadataElement.item(0);
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "parsing xml has failed", e);
		}
		return null;
	}	
	
	/**
	 * Gets the child node with the given tag name from the given parent. If the attribute name and
	 * value is also given, the tag must have these defined.
	 * @param parent The parent which contains the node with the searched name.
	 * @param name The name of the node to be searched.
	 * @param attributeName The name of the attribute of the given tag. Can be <code>null</code>
	 * @param attributeValue The value of the attribute. Can be <code>null</code>
	 * @return The searched Node.
	 */	
	protected Element getChildByTagAndAttribute(Node parent, String name, String attributeName, String attributeValue) {
		final List<Element> resultList = getChildsByTagAndAttribute(parent, name, attributeName, attributeValue);
		if(resultList==null || resultList.isEmpty()) {
			return null;
		}
		return resultList.get(0);
	}	
	
	/**
	 * Gets the child nodes with the given tag name from the given parent. If the attribute name and
	 * value is also given, the tag must have these defined.
	 * @param parent The parent which contains the node with the searched name.
	 * @param name The name of the node to be searched.
	 * @param attributeName The name of the attribute of the given tag. Can be <code>null</code>
	 * @param attributeValue The value of the attribute. Can be <code>null</code>
	 * @return The searched Node.
	 */		
	protected List<Element> getChildsByTagAndAttribute(Node parent, String name, String attributeName, String attributeValue) {
		ArrayList<Element> result = new ArrayList<Element>();
		NodeList childNodes = parent.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if(item.getNodeName().equals(name)) {
				if(attributeName!=null) {
					NamedNodeMap attributes = item.getAttributes();
					if(attributes.getNamedItem(attributeName)!=null && attributeValue!=null && attributes.getNamedItem(attributeName).getTextContent().equalsIgnoreCase(attributeValue)) {
						result.add((Element) item);
					} else if((attributeValue==null || attributeValue.length()==0) && attributes.getNamedItem(attributeName)!=null) {
						result.add((Element) item);
					}
				} else {
					result.add((Element) item);
				}
			}
		}
		return result;		
	}	
	
	/**
	 * Gets the element text from the given {@link Element}. If the
	 * element did not have text content, the content attribute is
	 * used for the result.
	 * @param element The element where the text should be fetched from.
	 * @return The text. <code>null</code> if no text could be found.
	 */
	protected String getText(Element element) {
		if(element==null) {
			return null;
		}
		
		String textContent = element.getTextContent();
		if(textContent!=null && textContent.trim().length()>0) {
			return textContent.trim();
		}
		
		String value = element.getAttribute("content");
		if(value!=null && value.length()>0) {
			return value;
		}
		return null;
	}
	
	public void dispose() {
		this.containerOpfData = null;
		this.zipContent = null;
		this.ebookResourceHandler = null;
		this.opfFileName = null;
	}

	/**
	 * Gets the cover name from the metadata node.
	 * @param metadataNode The metadata node.
	 * @return The cover name or <code>null</code> if no cover entry is specified.
	 */
	protected String findMetadataCoverNameReference(final Node metadataNode) {
		Node coverNode = getChildByTagAndAttribute(metadataNode, "meta", "name", "cover");
		String cover = null;
		if (coverNode != null) {
			NamedNodeMap attributes = coverNode.getAttributes();
			if (attributes != null) {
				Node contentNode = coverNode.getAttributes().getNamedItem("content");
				if (contentNode != null) {
					cover = contentNode.getTextContent();
				}
			}
		}
		return cover;
	}
	
	/**
	 * Gets the cover name from the manifest node.
	 * @param manifestElement The manifest node.
	 * @return The cover name or <code>null</code> if no cover entry is specified.
	 */
	protected String findManifestCoverName(final Element manifestElement, final String coverId) {
		if(coverId==null) {
			return null;
		}
		List<Element> manifestChildren = getChildren(manifestElement);
		for (Element child : manifestChildren) {
			if(child.getTagName().equals("item") && child.getAttribute("id")!=null && child.getAttribute("id").equals(coverId)) {
				String href = child.getAttribute("href");
				return href;
			}
		}
		
		return null;
	}	

	/**
	 * fetch the cover from the given zip data.
	 * @param zipData The zip data containing the cover.
	 * @param coverFile The path/name of the cover file. 
	 * @return The {@link ZipDataEntry} with the cover data or <code>null</code> if no cover could be extracted.
	 */
	protected ZipDataEntry extractCoverFromZip(final byte[] zipData, final String coverFile) {
		final String guessCoverFileName = getName(coverFile);
		final String exactCoverFileName = coverFile != null ? getOpfFilePath(zipData) + StringUtils.decodeURL(coverFile) : "";
		final String[] certainEntryName = new String[1];
		final List<ZipDataEntry> extract = ZipUtils.extract(zipData, new ZipUtils.ZipFileFilter() {
			
			@Override
			public boolean accept(ZipEntry entry) {
				if(certainEntryName[0] == null) {
					final String entryName = getName(entry.getName());
					
					if(coverFile!=null && entry.getName().equals(exactCoverFileName)) {
						//exact match with path and file name.
						certainEntryName[0] = entry.getName();
						return true;
					} else if (entryName.startsWith(guessCoverFileName) && (entryName.endsWith(".jpg") || entryName.endsWith(".jpeg"))) {
						if(coverFile!=null && entry.getName().equals(exactCoverFileName)) {
							certainEntryName[0] = entry.getName();
						}
						return true;
					} else if (entryName.startsWith("cover") && (entryName.endsWith(".jpg") || entryName.endsWith(".jpeg"))) {
						return true;
					}
				}
				return false;
			}
		}, -1);
		
		if(certainEntryName[0]!=null && !extract.isEmpty()) {
			for (ZipDataEntry zipDataEntry : extract) {
				if(zipDataEntry.path.equals(certainEntryName[0])) {
					return zipDataEntry;
				}
			}
			return extract.iterator().next();
		} else if(!extract.isEmpty()) {
			return extract.iterator().next();
		}
		return null; 
	}
	
	/**
	 * Gets the file name from the given file with path.
	 * @param filePath A full qualified file path.
	 * @return The desired name of the file or an empty string if the given path is <code>null</code>.
	 */
	private String getName(String filePath) {
		if(filePath==null) {
			return "";
		}
		
		String result;
		if (filePath.indexOf('/') != -1) {
			result = filePath.substring(filePath.lastIndexOf('/') + 1);
		} else {
			result = filePath;
		}
		
		if(result.indexOf('%')!=-1) {
			result = StringUtils.decodeURL(result);
		}
		return result;
	}	
	
}
