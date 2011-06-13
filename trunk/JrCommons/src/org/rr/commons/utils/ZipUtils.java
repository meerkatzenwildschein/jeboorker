package org.rr.commons.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;

public class ZipUtils {
	
	public static List<ZipDataEntry> extract(byte[] zipData, ZipFileFilter filter, int maxEntries) {
		if(zipData==null) {
			return null;
		}
		if(maxEntries<0) {
			maxEntries = Integer.MAX_VALUE;
		}
		
		JarInputStream jar = null;
		try {
			jar = new JarInputStream(new ByteArrayInputStream(zipData));
			final ArrayList<ZipDataEntry> result = new ArrayList<ZipDataEntry>();

			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			final byte[] readBuff = new byte[4096];

			ZipEntry nextEntry;
			while ((nextEntry=jar.getNextEntry()) != null && maxEntries > result.size()) {
				if(filter==null || filter.accept(nextEntry)) {
					int len = 0;
					while ((len = jar.read(readBuff)) != -1) {
						bout.write(readBuff, 0, len);
					}
					result.add(new ZipDataEntry(nextEntry.getName(), bout.toByteArray()));
					bout.reset();
				}
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if(jar!=null) {
				try {jar.close();} catch (IOException e) {}
			}
		}
	}

	/**
	 * Extracts the entry specified with the entry parameter and returns it.
	 * @param zipData The zip data containing the file to be extracted.
	 * @param entry The entry to be extracted. for example 'META-INF/container.xml'
	 * @return The desired entry or <code>null</code> if the entry is not in the zip.
	 */
	public static ZipDataEntry extract(byte[] zipData, String entry) {
		if(entry==null) {
			return null;
		}
		if(zipData==null) {
			return null;
		}
		
		JarInputStream jar = null;
		try {
			jar = new JarInputStream(new ByteArrayInputStream(zipData));

			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			final byte[] readBuff = new byte[4096];

			ZipEntry nextEntry;
			while ((nextEntry=jar.getNextEntry()) != null) {
				if(nextEntry.getName().equals(entry)) {
					int len = 0;
					while ((len = jar.read(readBuff)) != -1) {
						bout.write(readBuff, 0, len);
					}
					ZipDataEntry zipDataEntry = new ZipDataEntry(nextEntry.getName(), bout.toByteArray());
					bout.reset();
					return zipDataEntry;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if(jar!=null) {
				try {jar.close();} catch (IOException e) {}
			}
		}
		return null;
	}
	
	/**
	 * Extracts the given zip data.
	 * @param zipData The data to be extracted.
	 * @return A list with the ziped content.
	 */
	public static List<ZipDataEntry> extractAll(byte[] zipData) {
		if(zipData==null) {
			return null;
		}
		
		try {
			final JarInputStream jar = new JarInputStream(new ByteArrayInputStream(zipData));
			final ArrayList<ZipDataEntry> result = new ArrayList<ZipDataEntry>();

			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			final byte[] readBuff = new byte[4096];

			ZipEntry nextEntry;
			while ((nextEntry=jar.getNextEntry()) != null) {
				int len = 0;
				while ((len = jar.read(readBuff)) != -1) {
					bout.write(readBuff, 0, len);
				}

				result.add(new ZipDataEntry(nextEntry.getName(), bout.toByteArray()));
				bout.reset();
			}
			jar.close();
			
			Collections.sort(result);
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}	
	
	/**
	 * Adds or replaces the given entry to existing zip data. Note that the whole
	 * zip is copied.
	 * @param zipData The zip data where the given entry should be added.
	 * @param entry The entry to be added.
	 * @return the zip data with the added entry or <code>null</code> if something was wrong.
	 */
	public static byte[] add(byte[] zipData, ZipDataEntry entry) {
		if(entry==null) {
			return null;
		}
		if(zipData==null) {
			return null;
		}
		
		ByteArrayInputStream input = new ByteArrayInputStream(zipData);
	    ByteArrayOutputStream output = new ByteArrayOutputStream();
	    ZipInputStream zipInputStream = new ZipInputStream(input);
	    ZipOutputStream zipOutputStream = new ZipOutputStream(output);
	    
	    try {
	    	boolean replaceSuccess = false;
		    ZipEntry zipEntryIn;
		    while ((zipEntryIn = zipInputStream.getNextEntry()) != null) {
		    	ZipEntry out;
		    	InputStream read;
		    	if(zipEntryIn.getName().equals(entry.path)) {
		    		//replace
		    		out = new ZipEntry(zipEntryIn.getName());
		    		read = new ByteArrayInputStream(entry.data);
		    		replaceSuccess = true;
		    	} else {
		    		//add
		    		out = new ZipEntry(zipEntryIn.getName());
		    		read = zipInputStream;
		    	}
	            zipOutputStream.putNextEntry(out);
	            IOUtils.copy(read, zipOutputStream);
	            zipOutputStream.flush();
	            
	            
		        zipInputStream.closeEntry();
		        zipOutputStream.closeEntry();
		    }
		    
		    if(!replaceSuccess) {
		    	//new entry must be added
		    	ZipEntry out = new ZipEntry(entry.path);
		    	InputStream read = new ByteArrayInputStream(entry.data);
		    	zipOutputStream.putNextEntry(out);
		    	IOUtils.copy(read, zipOutputStream);
		    	zipOutputStream.flush();
		        zipInputStream.closeEntry();
		        zipOutputStream.closeEntry();
		    }
		} catch(Exception e) {
			LoggerFactory.logWarning(ZipUtils.class, "could not add data to zip", e);
			return null;
		} finally {
			IOUtils.closeQuietly(zipInputStream);
			IOUtils.closeQuietly(zipOutputStream);
		}
		byte[] result = output.toByteArray();
		if(result.length>0) {
			return result;
		}
		return null;
	}	
	
	/**
	 * Just a data holder class.
	 */
	public static class ZipDataEntry implements Comparable<ZipDataEntry> {
		
		public String path;
		
		public byte[] data;
		
		public ZipDataEntry(String path, byte[] data) {
			this.path = path;
			this.data = data;
		}

		@Override
		public int compareTo(ZipDataEntry o) {
			return o.path.compareTo(path);
		}
		
		public String toString() {
			return path;
		}
	}
	
	public static interface ZipFileFilter {
		public boolean accept(ZipEntry entry);
	}
}
