/*
 * $Id: DefaultFileFilter.java,v 1.6 2008/01/28 21:28:37 edankert Exp $
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
package org.bounce;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.filechooser.FileFilter;

/**
 * The default file filter, allows for setting a description 
 * and a file-type extension.
 *
 * @version	$Revision: 1.6 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class DefaultFileFilter extends FileFilter {

    private String description = null;
    private List<String> extensions = null;

    /**
     * Creates a file filter that accepts the given file types.
	 * The extensions should be divided by any of the following 
	 * tokens " \t\n\r\f;,.:".
	 * 
	 * @param extensions the extensions for the file-type.
     * @param description the description of the file-type.
     */
    public DefaultFileFilter( String extensions, String description) {
		this.extensions = new ArrayList<String>();
		setExtensions( extensions);

	 	this.description = description;
    }

	/**
	 * Add an extension to the list of extensions.
	 * 
	 * @param extension the extension for the file-type without the '.'.
	 */
	public void addExtension( String extension) {
		extensions.add( extension);
	}

	/**
	 * Sets all possible extensions for this filter, 
	 * divided by any of the following tokens " \t\n\r\f;,.:".
	 * 
	 * @param extensions the extensions for the file-type.
	 */
	public void setExtensions( String extensions) {
		StringTokenizer tokenizer = new StringTokenizer( extensions, " \t\n\r\f;,.:");
		
		while ( tokenizer.hasMoreTokens()) {
			this.extensions.add( tokenizer.nextToken());
		}
	}

	/**
	 * @inheritDoc javax.swing.filechooser.FileFilter#accept();
	 */
    public boolean accept( File file) {
		boolean result = false;
		
		if ( file != null) {
			if( file.isFile()) {

		    	String ext = getExtension( file);
	    	
				if( ext != null && isSupportedExtension( ext)) {
					result = true;
	    		}
			} else if ( file.isDirectory()) { 
				result = true;
			} 
		}
		
		return result;
    }

	/**
	 * @inheritDoc javax.swing.filechooser.FileFilter#getDescription();
	 */
     public String getDescription() {
		StringBuffer result = new StringBuffer( description);
		
		if ( extensions.size() > 0) {
			result.append(" (");
			for ( int i = 0; i < extensions.size(); i++) {
				if ( i > 0) {
					result.append( ",");
				}

				result.append("*.");
				result.append(extensions.get(i));
			}
			result.append(")");
		}
		
		return result.toString();
    }

	private boolean isSupportedExtension( String extension) {
		for ( int i = 0; i < extensions.size(); i++) {
//			System.out.println( (String)extensions.elementAt(i)+" == "+extension);
			if ( extensions.get(i).equalsIgnoreCase( extension)) {
				return true;
			}
		}
		
		return false;
	}
	
	private String getExtension( File file) {
		if( file != null) {
			String filename = file.getName();

			int i = filename.lastIndexOf('.');

			if( i > 0 && i < (filename.length() - 1)) {
				return filename.substring( i+1).toLowerCase();
			}
		}

		return null;
	}
}
