/*
 * $Id$
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

package org.bounce.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.StringTokenizer;

/**
 * Put comment...
 * 
 * @version $Revision$, $Date$
 * @author Edwin Dankert <edankert@gmail.com>
 */

public class URIUtils {

    /* 
     * Encode the URI into canonicalized version.
     */
	private static String encodeURI( String url) {
		StringBuffer uri = new StringBuffer( url.length());
        int length = url.length();
        for (int i = 0; i < length; i++) {
            char c = url.charAt(i);

            switch(c) {
                case '!': 
                case '#':  
                case '$':  
                case '%':  
                case '&':  
                case '\'':  
                case '(':  
                case ')':  
                case '*':  
                case '+':  
                case ',':  
                case '-':  
                case '.':  
                case '/':  
                case '0':  
                case '1':  
                case '2':  
                case '3':  
                case '4':  
                case '5':  
                case '6':  
                case '7':  
                case '8':  
                case '9':  
                case ':':  
                case ';':  
                case '=':  
                case '?':  
                case '@':  
                case 'A':  
                case 'B':  
                case 'C':  
                case 'D':  
                case 'E':  
                case 'F':  
                case 'G':  
                case 'H':  
                case 'I':  
                case 'J':  
                case 'K':  
                case 'L':  
                case 'M':  
                case 'N':  
                case 'O':  
                case 'P':  
                case 'Q':  
                case 'R':  
                case 'S':  
                case 'T':  
                case 'U':  
                case 'V':  
                case 'W':  
                case 'X':  
                case 'Y':  
                case 'Z':  
                case '[':  
                case ']':  
                case '_':  
                case 'a':  
                case 'b':  
                case 'c':  
                case 'd':  
                case 'e':  
                case 'f':  
                case 'g':  
                case 'h':  
                case 'i':  
                case 'j':  
                case 'k':  
                case 'l':  
                case 'm':  
                case 'n':  
                case 'o':  
                case 'p':  
                case 'q':  
                case 'r':  
                case 's':  
                case 't':  
                case 'u':  
                case 'v':  
                case 'w':  
                case 'x':  
                case 'y':  
                case 'z':  
                case '~':  
                    uri.append(c);
                    break;
                default: 
                    StringBuffer result = new StringBuffer(3);
	                String s = String.valueOf(c);
	            
	                try {
	                    byte[] data = s.getBytes("UTF8");
	                    for ( int j = 0; j < data.length; j++) {
	                        result.append('%');
	                        String hex = Integer.toHexString( data[j]);
	                        result.append( hex.substring( hex.length()-2));
	                    }
	                    uri.append( result.toString());
	                } catch ( UnsupportedEncodingException ex) {
	                    // should never happen
	                }
            }
        }    

        return uri.toString();
    }
	
    /**
     * Returns the name of the file that is pointed to by this URI.

     * @param uri the uri to get the name for.
     * 
     * @return the name of the file.
     */
	public static String getName(URI uri) {
        if ( uri != null) {
    		String location = uri.getSchemeSpecificPart();
    		
    		if ( location != null) {
    			String name = location;
    			int index = location.lastIndexOf( '/');
    			
    			if ( index != -1) {
    				name = name.substring( index + 1, name.length());
    			}
    			
    			return name;
    		}
        }
    		
    	return "";
	}

    /**
     * Returns the extension of the file that is pointed to by this URI.

     * @param uri the uri to get the extension for.
     * 
     * @return the extension of the file.
     */
	public static String getExtension(URI uri) {
        if ( uri != null) {
    		String location = uri.getSchemeSpecificPart();
    		
    		if ( location != null) {
    			String name = location;
    			int index = location.lastIndexOf( '.');
    			
    			if ( index != -1) {
    				name = name.substring( index + 1, name.length());
    			}
    			
    			return name;
    		}
        }
    		
    	return "";
	}

	/**
     * Returns the name of the directory that is pointed to by this URI.

     * @param uri the uri to get the directory name for.
     * 
     * @return the name of the directory.
     */
    public static String getDirectoryName(URI uri) {
        String location = uri.getSchemeSpecificPart();
        
        if ( location != null) {
            String name = location;
            int end = location.lastIndexOf( '/');
            
            if ( end != -1) {
                int start = location.lastIndexOf( '/', end-1);
                name = name.substring( start+1, end);
            }
            
            return name;
        }
        
        return null;
    }
    
    /**
     * Creates a URI from a path, the path can be relative or absolute, 
     * '\' and '/' are normalised.
     * 
     * @param path the path to create the URI for.
     * @return a new URI.
     */
    public static URI createURI(String path) {
        path = path.replace('\\', '/');
        
        return URI.create(encodeURI(path));
    }
    
    /**
     * Creates a String from a URI, the URI can be relative or absolute, 
     * the URI is decoded.
     * 
     * TODO Why can't I just return uri.toString()???
     * 
     * @param uri the URI to return the string representation for.
     * @return a string representation of the URI.
     */
    public static String toString(URI uri) {
        StringBuffer buffer = new StringBuffer();

        if (uri.getScheme() != null) {
            buffer.append(uri.getScheme());
            buffer.append(":");
        }

        buffer.append(uri.getSchemeSpecificPart());

        if (uri.getFragment() != null) {
            buffer.append("#");
            buffer.append(uri.getFragment());
        }

        return buffer.toString();
    }

    /**
     * Creates a File from a URI, the URI can be relative or absolute, 
     * this method returns only a file for the Scheme Specific Part.
     * 
     * @param uri the URI to create the file for.
     * @return a new file.
     */
    public static File toFile(URI uri) {
//        if (uri.getScheme() == null) {
//            try {
//                uri = new URI("file", uri.getSchemeSpecificPart(), null);
//            } catch (URISyntaxException e) {
//                // should never happen
//                Logger.getLogger(URIUtils.class).fatal(uri, e);
//            }
//        }

        return new File((File)null, uri.getSchemeSpecificPart());
    }

    /**
     * Return the uri for the uri relative to the base uri.
     * 
     * @param base the base uri.
     * @param uri the file location to get the relative path for.
     * 
     * @return the relative uri.
     */
    public static URI getRelativeURI(URI base, URI uri) {

        if (base != null && uri != null && (uri.getScheme() == null || uri.getScheme().equals(base.getScheme()))) {
            StringTokenizer baseParts = tokenizeBase(base);
            StringTokenizer uriParts = new StringTokenizer(uri.getSchemeSpecificPart(), "/");
            
            StringBuffer relativePath = new StringBuffer();
            String part = null;
            boolean first = true;
            
            if (!baseParts.hasMoreTokens()) {
                return uri;
            }
            
            while (baseParts.hasMoreTokens() && uriParts.hasMoreTokens()) {
                String baseDir = baseParts.nextToken();
                part = uriParts.nextToken(); 
                
                if ((baseDir.equals(part) && baseDir.contains(":")) && first) {
                    baseDir = baseParts.nextToken();
                    part = uriParts.nextToken(); 
                }

                if (!baseDir.equals(part)) { // || (baseDir.equals(part) && baseDir.contains(":"))) {
                    if (first) {
                        return uri;
                    }

                    relativePath.append("../");
                    break;
                }
                
                part = null;
                first = false;
            }
            
            while (baseParts.hasMoreTokens()) {
                relativePath.append("../");
                baseParts.nextToken();
            }
            
            if (part != null) {
                relativePath.append(part);

                if (uriParts.hasMoreTokens()) {
                    relativePath.append("/");
                }
            }

            while (uriParts.hasMoreTokens()) {
                relativePath.append(uriParts.nextToken());

                if (uriParts.hasMoreTokens()) {
                    relativePath.append("/");
                }
            }
            
            return createURI(relativePath.toString());
        }
        
        return uri;
    }

    /**
     * Return the path for the file relative to the base uri.
     * 
     * @param base the base url.
     * @param file the file location to get the relative path for.
     * 
     * @return the relative path.
     */
    public static String getRelativePath(URI base, File file) {
        return toString(getRelativeURI(base, file.toURI()));
    }

//    /**
//     * Return the path for the uri relative to the base uri.
//     * 
//     * @param base the base url.
//     * @param path the file location to get the relative path for,
//     *        '\' and '/' are normalised.
//     * 
//     * @return the relative path.
//     */
//    public static String getRelativePath(URI base, String path) {
//        return toString(getRelativeURI(base, createURI(path)));
//    }
//
    /**
     * Return the absolute URI, composed from a base URI and a 
     * relative path.
     * 
     * @param base the base uri.
     * @param relativePath the relative path.
     * 
     * @return the absolute URI.
     */
    public static URI composeURI(URI base, String relativePath) {
        if ( base != null) {
            return base.resolve(createURI(relativePath));
        }
        
        return createURI(relativePath);
    }

    /**
     * Return the absolute path, composed from a base URI and a 
     * relative path.
     * 
     * @param base the base uri.
     * @param relativePath the relative path.
     * 
     * @return the absolute path.
     */
    public static File composeFile(URI base, String relativePath) {
        URI uri = composeURI(base, relativePath);
        if (uri.isAbsolute()) {
            return new File(uri);
        } else if ( base != null) {
            return new File(new File(base), uri.toString());
        }
        
        return null;
    }

    /**
     * Return the absolute path, composed from a base URI and a 
     * relative path.
     * 
     * @param base the base uri.
     * @param relativePath the relative path.
     * 
     * @return the absolute path.
     */
    public static String composePath(URI base, String relativePath) {
        URI uri = composeURI( base, relativePath);
        if ( uri.isAbsolute()) {
            File file = new File(uri);
            return file.toString();
        } else if ( base != null) {
            File file = new File(new File(base), uri.toString());
            return file.toString();
        }

        return relativePath;
    }
    
    private static StringTokenizer tokenizeBase(URI uri) {
        String scheme = uri.getSchemeSpecificPart();
        int index = scheme.lastIndexOf("/");

        if (index != -1) {
            scheme = scheme.substring(0, index+1);
        }
        
        return new StringTokenizer(scheme, "/");
    }
}
