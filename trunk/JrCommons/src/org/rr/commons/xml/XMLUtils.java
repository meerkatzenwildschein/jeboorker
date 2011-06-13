package org.rr.commons.xml;

import java.io.ByteArrayOutputStream;

public class XMLUtils {
	
	public static String formatXML(String xml) {
		return formatXML(xml, 4, -1);
	}

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
		formatter.outputExtraData("");

		return out.toString();
	}
}
