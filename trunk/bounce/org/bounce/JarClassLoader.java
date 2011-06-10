/*
 * $Id: JarClassLoader.java,v 1.5 2008/01/28 21:28:37 edankert Exp $
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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A class loader for loading resources and classes out of a jar file.
 * Overrides the findClass and the findResource methods.
 *
 * @version	$Revision: 1.5 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class JarClassLoader extends SecureClassLoader {
	private boolean DEBUG = false;

	private static final String JAR_SCHEME 		= "jar";
	private static final String JAR_SEPARATOR 	= "!/";
	private static final String CLASS_EXTENSION	= ".class";
	private static final char WINDOWS_SLASH 	= '\\';
	private static final char UNIX_SLASH 		= '/';
	private static final char DOT 				= '.';
	
    private HashMap<String, byte[]> contents;
    private URL url;
  
    /**
     * Creates a new JarClassLoader that will allow the loading
     * of classes stored in the jar file supplied. It uses the 
	 * system class loader as the parent classloader.
     *
     * @param url the URL of the jar file
     *
     * @exception IOException an error happened while reading
     *            the contents of the jar file.
     */
    public JarClassLoader(URL url) throws IOException {
	    this(getSystemClassLoader(), url);
    }

    /**
     * Creates a new JarClassLoader that will allow the loading
     * of classes stored in the jar file supplied.
     *
	 * @param parent the parent classloader.
	 * @param url the URL of the jar file
	 *
     * @exception IOException an error happened while reading
     *            the contents of the jar file.
     */
    public JarClassLoader(ClassLoader parent, URL url) throws IOException {
		super(parent);

		if (DEBUG) System.out.println( "["+this+"] JarClassLoader( "+parent+", "+url+")");

		/*
		 * Get the sizes of the files in the jar and place them 
		 * in a temporary table.
		 */
		HashMap<String, Integer> sizes = new HashMap<String, Integer>();
		this.url = url;
		
		ZipFile zf = new ZipFile( url.getFile());

		Enumeration entries = zf.entries();

		while( entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry)entries.nextElement();
			int size	= (int)entry.getSize();
			String name	= entry.getName();
			
			sizes.put( getEntryKey( name), new Integer( size));

//			if (DEBUG) System.out.println( "["+this+"] JarClassLoader [ZipEntry: name = "+name+" size = "+size+"]");
		}

		zf.close();

		/*
		 * Get the contents of the files in the jar and place them 
		 * in the hash table, so a class does not have to be loaded again.
		 */
		contents = new HashMap<String, byte[]>();

		// Create a Jar Input Stream
		FileInputStream fis		= new FileInputStream( url.getFile());
		BufferedInputStream bis = new BufferedInputStream( fis);
		JarInputStream jar		= new JarInputStream( bis);

		JarEntry entry = jar.getNextJarEntry();
		
		while ( entry != null) {
			// Get the entry name and size
			String name = getEntryKey( entry.getName());
			int size = (int)entry.getSize();

//			if (DEBUG) System.out.println( "["+this+"] JarClassLoader [JarEntry: name = "+name+" size = "+size+"]");
			
			if ( size < 0) {
				size = sizes.get(name).intValue();
			}

			// Create a buffer that can hold the file
			byte[] buffer = new byte[size];
			int totalRead = 0;

			while ( totalRead != size) {
				// Read the bytes into the buffer
				int read = jar.read( buffer, totalRead, size - totalRead);

				if ( read < 0) {
					break;
				}

				totalRead += read;
			}

			if ( totalRead != size) {
				throw new IOException("Cannot read entry: "+name);
			}

			// Place the file in the contents table.
			contents.put( name, buffer);

			entry = jar.getNextJarEntry();
		}

		jar.close();
	}
  
    /**
     * Checks the contents table for an occurence of the class.
     *
     * @param name the name of the class.
	 *
     * @return the class found for the name.
	 *
     * @exception ClassNotFoundException the jar file did not contain
     *            the class.
     */
    @SuppressWarnings("unchecked")
    public synchronized Class findClass(String name) throws ClassNotFoundException {
		if (DEBUG) System.out.println("["+this+"] JarClassLoader.findClass( "+name+")");

		Class result = super.findLoadedClass( name);
		
		if ( result == null) {
			// Change the name to the key used to store the class in the table.
			String key = name.replace(DOT, UNIX_SLASH) + CLASS_EXTENSION;

			byte[] bytes = contents.get(key);

			if ( bytes == null) {
				throw new ClassNotFoundException();
			}

			if (DEBUG) System.out.println("["+this+"] defineClass( "+name+", "+bytes+", 0, "+bytes.length+")");

			result = defineClass( name, bytes, 0, bytes.length,(java.security.ProtectionDomain)null);
		}

		return result;
	}

    /**
     * Checks the contents table for an occurence of the resource file.
     *
     * @param name the name of the resource.
	 *
     * @return The url of the resource or null if not found.
     */
    public URL findResource( String name) {
		if (DEBUG) System.out.println("["+this+"] JarClassLoader.findResource( "+name+")");
		URL resource = null;
		
		if ( contents.containsKey( name)) {
			try  {
				/**
				 * Create a URL that looks like:
				 * 'jar:file:<jar path>!/<resource path>'
				 */
				StringBuffer buffer = new StringBuffer( url.toExternalForm());
				buffer.append( JAR_SEPARATOR);
				buffer.append( name);

				resource = new URL( JAR_SCHEME, null, buffer.toString());
			} catch (Exception e)  {
				e.printStackTrace();
			}
		}

		if (DEBUG) System.out.println("["+this+"] JarClassLoader.findResource( "+name+") ["+resource+"]");
		
		return resource;
	}
	
	/**
	 * Returns the key for an entry name.
	 */
	private String getEntryKey(String name) {
	 	return name.replace(WINDOWS_SLASH, UNIX_SLASH);
	}
	
}
