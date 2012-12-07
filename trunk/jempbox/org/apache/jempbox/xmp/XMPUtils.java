package org.apache.jempbox.xmp;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.SAXException;


public class XMPUtils {
	
	private static final char[] XMPMETA = "<x:xmpmeta".toCharArray();
	
	private static final char[] XMPPACKET = "<?xpacket".toCharArray();
	
	/**
	 * Fast check if this xmp contains any usable data.
	 * @param xmpMetadataBytes The xmp data to be tested.
	 * @return <code>true</code> if the xmp looks well and <code>false</code> otherwise.
	 */
	public static boolean isValidXMP(byte[] xmpMetadataBytes) {
		if(xmpMetadataBytes != null) {
			int openTags = 0;
			int closeTags = 0;
			
			for (int i = 0; i < xmpMetadataBytes.length; i++) {
				if(Character.isWhitespace(xmpMetadataBytes[i])) {
					//whitespace
				} else if(xmpMetadataBytes[i] == '<') {
					openTags++;
				} else if(xmpMetadataBytes[i] == '>') {
					closeTags++;
				}
			}
			
			if(openTags + closeTags < 8) {
				//Looks like one of these pdf which have only a <?xpacket begin="﻿.. and a  <?xpacket end="w"?> specified.
				return false;
			} else if(openTags - closeTags != 0) {
				return false;
			}
		} else {
			//is null
			return false;
		}
		
		if(contains(XMPPACKET, xmpMetadataBytes)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Test if a <x:xmpmeta xmlns:x="adobe:ns:meta/"> is present. If not it will be added. Otherwise
	 * the given bytes will be returned.
	 * 
	 * @param xmpMetadataBytes XMP metadata to be tested and repaired
	 * @return The repaired metadata bytes.
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 * @throws TransformerFactoryConfigurationError 
	 */
	public static byte[] handleMissingXMPRootTag(byte[] xmpMetadataBytes) throws ParserConfigurationException, IOException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		if(xmpMetadataBytes == null) { 
			return xmpMetadataBytes; //null given, null returned.
		}
		
		boolean containsXMPMetaTag = containsXMPMetaTag(xmpMetadataBytes);
		if(!containsXMPMetaTag && isValidXMP(xmpMetadataBytes)) {
			//need to insert root
			StringBuilder xml = new StringBuilder(new String(xmpMetadataBytes, "UTF-8"));
			xml.insert(xml.indexOf("?>") + 2, "\n<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">");
			
			int xpacketIdx = xml.lastIndexOf("<?xpacket") - 1;
			for(int i = xpacketIdx; i > 0; i--) {
				if(!Character.isWhitespace(xml.charAt(i))) {
					xml.insert(i + 1, "\n</x:xmpmeta>");
					break;
				}
			}
			
			return xml.toString().getBytes("UTF-8");
		}
		
		return xmpMetadataBytes;
	}
	
	/**
	 * Simple test if <?xpacket is contained by the given byte array. 
	 * @param xmpMetadataBytes The bytes to be tested.
	 * @return <code>true</code> if <?xpacket is contained and <code>false</code> otherwise. 
	 */
	public static boolean containsXMPPacketTag(byte[] xmpMetadataBytes) {
		return contains(XMPPACKET, xmpMetadataBytes);
	}
	
	/**
	 * Simple test if <x:xmpmeta is contained by the given byte array. 
	 * @param xmpMetadataBytes The bytes to be tested.
	 * @return <code>true</code> if <x:xmpmeta is contained and <code>false</code> otherwise. 
	 */
	public static boolean containsXMPMetaTag(byte[] xmpMetadataBytes) {
		return contains(XMPMETA, xmpMetadataBytes);
	}	
	
	private static boolean contains(final char[] values, final byte[] xmpMetadataBytes) {
		for (int i = 0; i < xmpMetadataBytes.length - 10; i++) {
			boolean found = false;
			int len = values.length;
			for(int j = 0; j < len; j++) {
				byte c = xmpMetadataBytes[i+j];
				if(c == values[j]) {
					found = true;
				} else {
					found = false;
					break;
				}
			}
			
			if(found) {
				return true;
			}
		}		
		return false;
	}
	
}
