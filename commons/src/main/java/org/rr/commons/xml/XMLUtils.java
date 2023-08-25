package org.rr.commons.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XMLUtils
{

    public static String formatXML(String xml)
    {
        return formatXML(xml, 4, -1);
    }

    /**
     * Formats the given xml data.
     *
     * @param xml            The xml data to be formatted.
     * @param indent         Number of whitespaces to indent.
     * @param maxCDataLength Max line length for cdata.
     * @return The formatted xml.
     */
    public static String formatXML(String xml, int indent, int maxCDataLength)
    {
        return formatXML(xml.getBytes(), indent, maxCDataLength);
    }

    /**
     * Formats the given xml data.
     *
     * @param xml            The xml data to be formatted.
     * @param indent         Number of whitespaces to indent.
     * @param maxCDataLength Max line length for cdata.
     * @return The formatted xml.
     */
    public static String formatXML(byte[] xml, int indent, int maxCDataLength)
    {
        SimpleParser parser = new SimpleParser();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLFormatter formatter = new XMLFormatter(out);
        formatter.setMaxCDataLength(maxCDataLength);
        formatter.setIndent(0);
        if (indent >= 0)
        {
            formatter.setIndent(indent);
        }

        char[] charArray = new String(xml).toCharArray();
        parser.parse(formatter, charArray, 0, charArray.length);

        return out.toString();
    }

    public static String formatDocument(Document document) throws TransformerException
    {
        DOMSource domSource = new DOMSource(document);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamResult streamResult = new StreamResult(out);
        TransformerFactory tf = TransformerFactory.newInstance();

        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.transform(domSource, streamResult);

        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Creates a document from the given xml bytes.
     *
     * @return The desired document. Never returns null but throws some Exception.
     * @throws ParserConfigurationException, IOException, SAXException
     */
    public static Document getDocument(byte[] xml) throws ParserConfigurationException, IOException, SAXException
    {
        if (xml != null)
        {
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
     * Create new XML document.
     *
     * @param rootElementName name of the root element to add, or <code>null</code> if the
     *                        document should not have any root just yet
     * @throws ParserConfigurationException
     */
    public static Document createEmptyDocument(String rootElementName) throws ParserConfigurationException
    {
        return createEmptyDocument(rootElementName, null);
    }

    /**
     * Create new XML document.
     *
     * @param rootElementName name of the root element to add, or <code>null</code> if the
     *                        document should not have any root just yet
     * @throws ParserConfigurationException
     */
    public static Document createEmptyDocument(String rootElementName, String namespace) throws ParserConfigurationException
    {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        Document result = builder.newDocument();

        if (rootElementName != null)
        {
            Element rootElement;
            if (namespace != null && !namespace.isEmpty())
            {
                rootElement = result.createElementNS(rootElementName, namespace);
            }
            else
            {
                rootElement = result.createElement(rootElementName);
            }
            result.appendChild(rootElement);
        }
        return result;
    }

    /**
     * Tests if the given xml data could be parsed into a document.
     *
     * @param xml The xml data to be parsed
     * @return <code>true</code> if the given data could be parsed and <code>false</code> otherwise.
     */
    public static boolean isValidXML(byte[] xml)
    {
        try
        {
            getDocument(xml);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }
}
