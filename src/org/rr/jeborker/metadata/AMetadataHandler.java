package org.rr.jeborker.metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;



public class AMetadataHandler {

	/**
	 * Creates a document from the given xml bytes.
	 * @return The desired document
	 */
	protected Document getDocument(byte[] xml, IResourceHandler ebookResource) {
		try {
			if(xml != null) {
				final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				// factory.setNamespaceAware( true );
				// factory.setValidating( true );
				final DocumentBuilder builder = factory.newDocumentBuilder();
				final Document document = builder.parse(new ByteArrayInputStream(xml));
				return document;
			}
		} catch (Exception e) {
			LoggerFactory.logWarning(AMetadataHandler.class, "Could not read epub " + ebookResource, e);
		}
		return null;
	}
	
	/**
	 * Transform the given {@link Document} instance into bytes.
	 * @param document The document to be transformed into bytes.
	 * @return The xml data as bytes.
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws IOException 
	 */
	protected byte[] getDocumentBytes(Document document) throws TransformerFactoryConfigurationError, TransformerException, IOException {
		return formatDocument(document).getBytes();
	}
	
	protected String formatDocument(Document document) throws TransformerFactoryConfigurationError, TransformerException, IOException {
        OutputFormat format = new OutputFormat(document);
        format.setLineWidth(160);
        format.setIndenting(true);
        format.setIndent(2);
        format.setEncoding("UTF-8");
        Writer out = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(out, format);
        serializer.serialize(document);

        return out.toString();
	}
	
	/**
	 * gets the children Element from the given Element e.
	 * @param e The Element where the chidls should be fetched from.
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
