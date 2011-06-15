package org.rr.commons.xml;

/*  Package Tigase XMPP/Jabber Server
 *  Copyright (C) 2001, 2002, 2003, 2004, 2005
 *  "Artur Hefczyc" <artur.hefczyc@gmail.com>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import org.rr.commons.utils.ArrayUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;

/**
 * This is temporary code used for testing purposes only. It is subject to change or remove at any time of server development. It has been created to format
 * <em>XML</em> files to make them easier to read and modify by a human. With current <code>XMLDB</code> implementation however it is not necessary to use this
 * formatter for configuration files and user repositories as they are saved in proper format.
 * 
 * <p>
 * Created: Thu Oct 21 14:49:41 2004
 * </p>
 * 
 * @author <a href="mailto:artur.hefczyc@gmail.com">Artur Hefczyc</a>
 * @version $Rev$
 */
class XMLFormatter implements SimpleHandler {

	private PrintStream output = null;
	private int indentSpaces = 0;
	private boolean cdataWritten = false;
	private boolean openedElement = false;
	private Object parserData = null;

	// config
	private int maxCDataLength = -1;
	private int indent = 2;

	/**
	 * Creates a new <code>XMLFormatter</code> instance.
	 * 
	 */
	public XMLFormatter(OutputStream out) {
		output = new PrintStream(out);
	}

	// Implementation of tigase.xml.SimpleHandler

	/**
	 * Describe <code>error</code> method here.
	 * 
	 */
	public void error() {
	}

	private void indent() {
		for (int idx = 0; idx < indentSpaces; idx++) {
			output.print(" ");
		}
	}

	/**
	 * Describe <code>startElement</code> method here.
	 * 
	 * @param name
	 *            a <code>StringBuilder</code> value
	 * @param att_names
	 *            a <code>StringBuilder[]</code> value
	 * @param att_values
	 *            a <code>StringBuilder[]</code> value
	 */
	public void startElement(final StringBuilder name, final StringBuilder[] att_names, final StringBuilder[] att_values) {
		if (openedElement) {
			output.println(">");
		} else {
			output.println("");
		}
		indent();
		output.print("<" + name);
		if (att_names != null) {
			for (int i = 0; i < att_names.length; i++) {
				if (att_names[i] != null) {
					output.print(" " + att_names[i] + "='" + att_values[i] + "'");
				}
			}
		}
		indentSpaces += 2;
		cdataWritten = false;
		openedElement = true;
	}

	/**
	 * Describe <code>elementCData</code> method here.
	 * 
	 * @param cdata
	 *            a <code>StringBuilder</code> value
	 */
	public void elementCData(final StringBuilder cdata) {
		output.print(">");
		openedElement = false;
		if(cdata.indexOf("\n")!=-1) {
			//do not format already formatted values
			List<String> splitted = ListUtils.split(cdata.toString(), "\n");
			for (String split : splitted) {
				output.print('\n');
				indent();
				output.print(split.trim());
			}
		} else if (getMaxCDataLength() > 0) {
			for (int srcBegin = 0; srcBegin < cdata.length(); srcBegin += maxCDataLength) {
				output.print('\n');
				indent();
				if (cdata.length() >= srcBegin + maxCDataLength) {
					CharSequence subSequence = cdata.subSequence(srcBegin, srcBegin + maxCDataLength);
					output.print(subSequence);
				} else {
					output.print(cdata.subSequence(srcBegin, cdata.length()));
				}
			}
		} else {
			output.print(cdata);
		}
		cdataWritten = true;
	}

	/**
	 * Describe <code>endElement</code> method here.
	 * 
	 * @param name
	 *            a <code>StringBuilder</code> value
	 */
	public void endElement(final StringBuilder name) {
		if (cdataWritten) {
			output.println("");
			for (int idx = 0; idx < (indentSpaces - 2); idx++) {
				output.print(" ");
			}
			output.print("</" + name + ">");
		} else {
			output.print("/>");
		}
		indentSpaces -= 2;
		cdataWritten = true;
		openedElement = false;
	}

	/**
	 * Describe <code>otherXML</code> method here.
	 * 
	 * @param other
	 *            a <code>StringBuilder</code> value
	 */
	public void otherXML(final StringBuilder other) {
		output.println("<" + other + ">");
	}

	/**
	 * Describe <code>saveParserState</code> method here.
	 * 
	 * @param object
	 *            an <code>Object</code> value
	 */
	public void saveParserState(final Object object) {
		parserData = object;
	}

	/**
	 * Describe <code>restoreParserState</code> method here.
	 * 
	 * @return an <code>Object</code> value
	 */
	public Object restoreParserState() {
		return parserData;
	}

	public void outputExtraData(String extra) {
		output.println(extra);
	}

	public int getMaxCDataLength() {
		return maxCDataLength;
	}

	public void setMaxCDataLength(int maxCDataLength) {
		this.maxCDataLength = maxCDataLength;
	}

	public int getIndent() {
		return indent;
	}

	public void setIndent(int indent) {
		this.indent = indent;
	}

} // XMLFormatter
