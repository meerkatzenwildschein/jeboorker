package org.rr.jeborker.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



abstract class AMetadataHandler {

	/**
	 * Creates a document from the given xml bytes.
	 * @return The desired document
	 */
	protected Document getDocument(byte[] xml, IResourceHandler ebookResource) throws IOException {
		try {
			if(xml != null) {
				return XMLUtils.getDocument(xml);
			}
		} catch (Exception e) {
			throw new IOException("Could not read metadata document " + ebookResource, e);
		}
		return null;
	}
	
	/**
	 * gets the children Element from the given Element e.
	 * @param e The Element where the childs should be fetched from.
	 * @return The desired children. Never returns <code>null</code>
	 */
	protected List<Element> getChildren(Element e) {
		final NodeList allElements = e.getElementsByTagName("*");
		final ArrayList<Element> result = new ArrayList<Element>(allElements.getLength());
		int length = allElements.getLength();
		for (int i = 0; i < length; i++) {
			Element item = (Element) allElements.item(i);
			if(item.getParentNode() == e) {
				//only the direct childs of e should be added.
				result.add(item);
			}
		}
		return result;
	}
}
