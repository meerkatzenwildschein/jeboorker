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

import java.util.Arrays;

/**
 * <code>SimpleParser</code> - implementation of <em>SAX</em> parser.
 *  This is very basic implementation of <em>XML</em> parser designed especially
 *  to be light and parse <em>XML</em> streams like jabber <em>XML</em> stream.
 *  It is very efficient, capable of parsing parts of <em>XML</em> document
 *  received from the network connection as well as handling a few <em>XML</em>
 *  documents in one buffer. This is especially useful when parsing data
 *  received from the network. Packets received from the network can contain
 *  non-comlete
 *  <em>XML</em> document as well as a few complete <em>XML</em> documents. It
 *  doesn't support <em>XML</em> comments, processing instructions, document
 *  inclussions. Actually it supports only:
 *  <ul>
 *   <li>Start element event (with all attributes found).</li>
 *   <li>End element even.</li>
 *   <li>Character data event.</li>
 *   <li>'OtherXML' data event - everything between '&#60;' and '&#62;' if after
 *   &#60; is '?' or '!'. So it can 'catch' doctype declaration, processing
 *   instructions but it can't process correctly commented blocks.</li>
 *  </ul> Although very simple this imlementation is sufficient for Jabber
 *  protocol needs and is even used by some other packages of this server like
 *  implementation of <code>UserRepository</code> based on <em>XML</em> file or
 *  server configuration.
 *  <p>It is worth to note also that this class is fully thread safe. It means that
 *   one instance of this class can be simultanously used by many threads. This
 *   is to improve resources usage when processing many client connections at
 *   the same time.</p>
 * <p>
 * Created: Fri Oct  1 23:02:15 2004
 * </p>
 * @author <a href="mailto:artur.hefczyc@gmail.com">Artur Hefczyc</a>
 * @version $Rev$
 */

class SimpleParser {

  /**
   * Variable constant <code>MAX_ATTRIBS_NUMBER</code> keeps value of
   * maximum possible attributes number. Real XML parser shouldn't have
   * such limit but in most cases XML elements don't have too many attributes.
   * For efficiency it is better to use fixed number of attributes and
   * operate on arrays than on lists.
   */
  public static int MAX_ATTRIBS_NUMBER = 6;

  private static enum State
  {
    START, OPEN_BRACKET, ELEMENT_NAME, END_ELEMENT_NAME, ATTRIB_NAME,
    END_OF_ATTR_NAME, ATTRIB_VALUE, ELEMENT_CDATA, OTHER_XML, ERROR,
    CLOSE_ELEMENT
  };

  private static final char OPEN_BRACKET = '<';
  private static final char CLOSE_BRACKET = '>';
  private static final char QUESTION_MARK = '?';
  private static final char EXCLAMATION_MARK = '!';
  private static final char SLASH = '/';
  private static final char SPACE = ' ';
  private static final char TAB = '\t';
  private static final char LF = '\n';
  private static final char CR = '\r';
  private static final char EQUALS = '=';
  private static final char SINGLE_QUOTE = '\'';
  private static final char DOUBLE_QUOTE = '"';
  private static final char[] WHITE_CHARS = { SPACE, TAB, LF, CR };
  private static final char[] ERR_NAME_CHARS = { OPEN_BRACKET, QUESTION_MARK };

  static {
    Arrays.sort(WHITE_CHARS);
  }

  private StringBuilder[] initArray(int size) {
    StringBuilder[] array = new StringBuilder[size];
    Arrays.fill(array, null);
    return array;
  }

  private StringBuilder[] resizeArray(StringBuilder[] src, int size) {
    StringBuilder[] array = new StringBuilder[size];
    System.arraycopy(src, 0, array, 0, src.length);
    Arrays.fill(array, src.length, array.length, null);
    return array;
  }

  public final void parse(SimpleHandler handler, char[] data,
    int off, int len) {

    ParserState parser_state = (ParserState)handler.restoreParserState();
    if (parser_state == null) {
      parser_state = new ParserState();
    } // end of if (parser_state == null)

    for (int index = off; index < len; index++) {
      char chr = data[index];
//System.out.print(chr);
      switch (parser_state.state) {
      case START:
        if (chr == OPEN_BRACKET) {
          parser_state.state = State.OPEN_BRACKET;
          parser_state.slash_found = false;
        } // end of if (chr == OPEN_BRACKET)
        // Skip everything up to open bracket
        break;

      case OPEN_BRACKET:
        switch (chr) {
        case QUESTION_MARK:
        case EXCLAMATION_MARK:
          parser_state.state = State.OTHER_XML;
          parser_state.element_cdata = new StringBuilder();
          parser_state.element_cdata.append(chr);
          break;
        case SLASH:
          parser_state.state = State.CLOSE_ELEMENT;
          parser_state.element_name = new StringBuilder();
          parser_state.slash_found = true;
          break;
        default:
          if (Arrays.binarySearch(WHITE_CHARS, chr) < 0) {
            parser_state.state = State.ELEMENT_NAME;
            parser_state.element_name = new StringBuilder();
            parser_state.element_name.append(chr);
          } // end of if ()
          break;
        } // end of switch (chr)
        break;

      case ELEMENT_NAME:

        if (Arrays.binarySearch(WHITE_CHARS, chr) >= 0) {
          parser_state.state = State.END_ELEMENT_NAME;
          break;
        } // end of if ()

        if (chr == SLASH) {
          parser_state.slash_found = true;
          break;
        } // end of if (chr == SLASH)

        if (chr == CLOSE_BRACKET) {
          parser_state.state = State.ELEMENT_CDATA;
          handler.startElement(parser_state.element_name, null, null);
          if (parser_state.slash_found) {
            handler.endElement(parser_state.element_name);
            parser_state.state = State.START;
          } // end of if (slash_found)
          parser_state.element_name = null;
          break;
        } // end of if ()

        if (chr == ERR_NAME_CHARS[0] || chr == ERR_NAME_CHARS[1]) {
          parser_state.state = State.ERROR;
          break;
        } // end of if ()

        parser_state.element_name.append(chr);
        break;

      case CLOSE_ELEMENT:

        if (Arrays.binarySearch(WHITE_CHARS, chr) >= 0) {
          break;
        } // end of if ()

        if (chr == SLASH) {
          parser_state.state = State.ERROR;
          break;
        } // end of if (chr == SLASH)

        if (chr == CLOSE_BRACKET) {
          parser_state.state = State.START;
          handler.endElement(parser_state.element_name);
          parser_state.element_name = null;
          break;
        } // end of if ()

        if (chr == ERR_NAME_CHARS[0] || chr == ERR_NAME_CHARS[1]) {
          parser_state.state = State.ERROR;
          break;
        } // end of if ()

        parser_state.element_name.append(chr);
        break;

      case END_ELEMENT_NAME:
        if (chr == SLASH) {
          parser_state.slash_found = true;
          break;
        } // end of if (chr == SLASH)

        if (chr == CLOSE_BRACKET) {
          parser_state.state = State.ELEMENT_CDATA;
          handler.startElement(parser_state.element_name,
            parser_state.attrib_names, parser_state.attrib_values);
          if (parser_state.slash_found) {
            handler.endElement(parser_state.element_name);
            parser_state.state = State.START;
          } // end of if (slash_found)
          parser_state.element_name = null;
          parser_state.attrib_names = null;
          parser_state.attrib_values = null;
          parser_state.current_attr = -1;
          break;
        } // end of if ()

        if (Arrays.binarySearch(WHITE_CHARS, chr) < 0) {
          parser_state.state = State.ATTRIB_NAME;
          if (parser_state.attrib_names == null) {
            parser_state.attrib_names = initArray(MAX_ATTRIBS_NUMBER);
            parser_state.attrib_values = initArray(MAX_ATTRIBS_NUMBER);
          } else {
            if (parser_state.current_attr ==
              parser_state.attrib_names.length - 1) {
              int new_size =
                parser_state.attrib_names.length + MAX_ATTRIBS_NUMBER;
              parser_state.attrib_names =
                resizeArray(parser_state.attrib_names, new_size);
              parser_state.attrib_values =
                resizeArray(parser_state.attrib_values, new_size);
            }
          } // end of else
          parser_state.attrib_names[++parser_state.current_attr] =
            new StringBuilder();
          parser_state.attrib_names[parser_state.current_attr].append(chr);
          break;
        } // end of if ()

        // do nothing, skip white chars
        break;

      case ATTRIB_NAME:
        if (Arrays.binarySearch(WHITE_CHARS, chr) >= 0 ||
            chr == EQUALS) {
          parser_state.state = State.END_OF_ATTR_NAME;
          break;
        } // end of if ()
        parser_state.attrib_names[parser_state.current_attr].append(chr);
        break;

      case END_OF_ATTR_NAME:
        if (chr == SINGLE_QUOTE || chr == DOUBLE_QUOTE) {
          parser_state.state = State.ATTRIB_VALUE;
          parser_state.attrib_values[parser_state.current_attr] = new StringBuilder();
        } // end of if (chr == SINGLE_QUOTE || chr == DOUBLE_QUOTE)
        // Skip white characters and actually everything except quotes
        break;

      case ATTRIB_VALUE:
        if (chr == SINGLE_QUOTE || chr == DOUBLE_QUOTE) {
          parser_state.state = State.END_ELEMENT_NAME;
          break;
        } // end of if (chr == SINGLE_QUOTE || chr == DOUBLE_QUOTE)
        parser_state.attrib_values[parser_state.current_attr].append(chr);
        break;

      case ELEMENT_CDATA:
        if (chr == OPEN_BRACKET) {
          parser_state.state = State.OPEN_BRACKET;
          parser_state.slash_found = false;
          if (parser_state.element_cdata != null) {
        	rtrim(parser_state.element_cdata);
            handler.elementCData(parser_state.element_cdata);
            parser_state.element_cdata = null;
          } // end of if (parser_state.element_cdata != null)
          break;
        } // end of if (chr == OPEN_BRACKET)
        if (parser_state.element_cdata == null) {
          // Skip leading white characters
          if (Arrays.binarySearch(WHITE_CHARS, chr) < 0) {
            parser_state.element_cdata = new StringBuilder();
            parser_state.element_cdata.append(chr);
          } // end of if (Arrays.binarySearch(WHITE_CHARS, chr) < 0)
        } // end of if (parser_state.element_cdata == null)
        else {
          parser_state.element_cdata.append(chr);
        } // end of if (parser_state.element_cdata == null) else
        break;

      case OTHER_XML:
        if (chr == CLOSE_BRACKET) {
          parser_state.state = State.START;
          handler.otherXML(parser_state.element_cdata);
          parser_state.element_cdata = null;
          break;
        } // end of if (chr == CLOSE_BRACKET)
        parser_state.element_cdata.append(chr);
        break;

      case ERROR:
        parser_state = null;
        handler.error();
        return;

      default:
        assert false : "Unknown SimpleParser state: "+parser_state.state;
        break;
      } // end of switch (state)

    } // end of for ()

    handler.saveParserState(parser_state);
  }
  
  private static void rtrim(StringBuilder value) {
	  int newSize = value.length();
	  for (int i = value.length(); i > 0; i--) {
		  char chr = value.charAt(i-1);
		  if (Arrays.binarySearch(WHITE_CHARS, chr) >= 0) {
			  newSize--;
		  } else {
			  break;
		  }
	  }
	  
	  if(newSize < value.length()) {
		  value.delete(newSize, value.length());
	  }
  }

  private class ParserState {
    StringBuilder element_name = null;
    StringBuilder[] attrib_names = null;
    StringBuilder[] attrib_values = null;
    StringBuilder element_cdata = null;
    int current_attr = -1;
    boolean slash_found = false;
    State state = State.START;
  }

}// SimpleParser
