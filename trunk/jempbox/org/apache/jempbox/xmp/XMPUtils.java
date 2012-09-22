package org.apache.jempbox.xmp;

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
	
}
