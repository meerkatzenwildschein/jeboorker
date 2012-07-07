package org.rr.commons.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XMLUtils {
	
	public static String formatXML(String xml) {
		return formatXML(xml, 4, -1);
	}

	/**
	 * Formats the given xml data.
	 * @param xml The xml data to be formatted.
	 * @param indent Number of whitespaces to indent. 
	 * @param maxCDataLength Max line length for cdata.
	 * @return The formatted xml.
	 */
	public static String formatXML(String xml, int indent, int maxCDataLength) {
		SimpleParser parser = new SimpleParser();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLFormatter formatter = new XMLFormatter(out);
		formatter.setMaxCDataLength(maxCDataLength);
		
		if(indent >= 0) {
			formatter.setIndent(indent);
		}
		
		char[] charArray = new String(xml.getBytes()).toCharArray();
		parser.parse(formatter, charArray, 0, charArray.length);

		return out.toString();
	}
	
	/**
	 * Creates a document from the given xml bytes.
	 * @return The desired document. Never returns null but throws some Exception.
	 * @throws ParserConfigurationException, IOException, SAXException  
	 */
	public static Document getDocument(byte[] xml) throws ParserConfigurationException, IOException, SAXException {
		if(xml != null) {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// factory.setNamespaceAware( true );
			// factory.setValidating( true );
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(new ByteArrayInputStream(xml));
			return document;
		}
		throw new IOException("No xml data");
	}	
	
	/**
	 * Tests if the given xml data could be parsed into a document.
	 * @param xml The xml data to be parsed
	 * @return <code>true</code> if the given data could be parsed and <code>false</code> otherwise.
	 */
	public static boolean isValidXML(byte[] xml) {
		try {
			getDocument(xml);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
