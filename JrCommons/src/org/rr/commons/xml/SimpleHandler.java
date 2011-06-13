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
 * $Author$
 * $Date$
 */

/**
 * <code>SimpleHandler</code> - parser handler interface for event driven
 *  parser. It is very simplified version of
 *  <code>org.xml.sax.ContentHandler</code> interface created for
 *  <code>SimpleParser</code> needs. It allows to receive events like start
 *  element (with element attributes), end element, element cdata, other XML
 *  content and error event if XML error found.
 *
 * <p>
 * Created: Sat Oct  2 00:00:08 2004
 * </p>
 * @author <a href="mailto:artur.hefczyc@gmail.com">Artur Hefczyc</a>
 * @version $Rev$
 * @see SimpleParser
 */

interface SimpleHandler {

  void error();

  void startElement(StringBuilder name,
    StringBuilder[] attr_names, StringBuilder[] attr_values);

  void elementCData(StringBuilder cdata);

  void endElement(StringBuilder name);

  void otherXML(StringBuilder other);

  void saveParserState(Object state);

  Object restoreParserState();
  

}// SimpleHandler
