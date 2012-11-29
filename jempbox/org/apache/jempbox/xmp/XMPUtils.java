package org.apache.jempbox.xmp;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.SAXException;


public class XMPUtils {
	
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
		
		return true;
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
	 * Simple test if <x:xmpmeta is contained by the given byte array. 
	 * @param xmpMetadataBytes The bytes to be tested.
	 * @return <code>true</code> if <x:xmpmeta is contained and <code>false</code> otherwise. 
	 */
	public static boolean containsXMPMetaTag(byte[] xmpMetadataBytes) {
		for (int i = 0; i < xmpMetadataBytes.length - 10; i++) {
			if(xmpMetadataBytes[i] == '<') {
				if(xmpMetadataBytes[i + 1] == 'x' &&
					xmpMetadataBytes[i + 2] == ':' &&
					xmpMetadataBytes[i + 3] == 'x' &&
					xmpMetadataBytes[i + 4] == 'm' &&
					xmpMetadataBytes[i + 5] == 'p' &&
					xmpMetadataBytes[i + 6] == 'm' &&
					xmpMetadataBytes[i + 7] == 'e' &&
					xmpMetadataBytes[i + 8] == 't' &&
					xmpMetadataBytes[i + 9] == 'a') {
					
					return true;
				}
			}			
		}		
		return false;
	}
	
}
