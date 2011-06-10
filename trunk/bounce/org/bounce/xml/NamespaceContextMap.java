/*
 * $Id: NamespaceContextMap.java,v 1.1 2008/05/20 20:19:20 edankert Exp $
 *
 * Copyright (c) 2002 - 2008, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *	 this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 * 	 notice, this list of conditions and the following disclaimer in the 
 *	 documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *	 may  be used to endorse or promote products derived from this software 
 *	 without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.bounce.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Implementation of the a Namespace Context as a HashMap.
 * 
 * @version $Revision: 1.1 $, $Date: 2008/05/20 20:19:20 $
 * @author Edwin Dankert <edankert@gmail.com>
 */

public class NamespaceContextMap extends HashMap<String,String> implements NamespaceContext {

	private static final long serialVersionUID = 3257568403886650425L;
    
    public NamespaceContextMap() {
        put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    }

	/**
	 * Get Namespace URI bound to a prefix in the current scope.
	 * 
	 * @param prefix the namespace prefix.
	 * @return the URI found for the prefix. 
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
	 */
	public String getNamespaceURI(String prefix) {
		if (prefix == null) {
			throw new IllegalArgumentException("Prefix cannot be null!");
		}

		String uri = get(prefix);
        
        if (uri != null) {
            return uri;
        }
		
		return XMLConstants.NULL_NS_URI;
	}

	/**
	 * Return the prefix bound to the namespace uri, null if no prefix could be found.
	 * 
	 * @param namespaceURI the namespace URI.
	 * @return the prefix found for the URI.
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
	 */
	public String getPrefix(String namespaceURI) {
		if ( namespaceURI == null) {
			throw new IllegalArgumentException("Namespace URI cannot be null.");
		}

		for (String prefix : keySet()) {
			if (get(prefix).equals(namespaceURI)) {
				return prefix;
			}
		}
		
		return null;
	}

	/**
	 * Return the list of prefixes bound to the namespace uri, null if no prefix 
	 * could be found.
	 * 
	 * @param namespaceURI the namespace URI.
	 * @return the prefixs bound to the URI.
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
	 */
	public Iterator<String> getPrefixes(String namespaceURI) {
		if ( namespaceURI == null) {
			throw new IllegalArgumentException("Namespace URI cannot be null.");
		}

		List<String> prefixes = new ArrayList<String>();
		
		for ( String prefix : keySet()) {
			if (get(prefix).equals(namespaceURI)) {
				prefixes.add( prefix);
			}
		}
		
		return prefixes.iterator();
	}
}
