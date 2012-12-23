package org.rr.commons.utils.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;

public class ZipUtils {

	public static List<String> list(byte[] zipData) {
		return list(new ByteArrayInputStream(zipData));
	}
	
	public static List<String> list(InputStream zipData) {
		if (zipData == null) {
			return null;
		}

		ZipInputStream jar = null;
		try {
			jar = new ZipInputStream(zipData);
			final ArrayList<String> result = new ArrayList<String>();
			
			ZipEntry nextEntry;
			while ((nextEntry = jar.getNextEntry()) != null) {
				String name = nextEntry.getName();
				result.add(name);
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if(jar != null) {
				try {jar.close();} catch (IOException e) {}
			}
		}
	}
    	
	public static List<ZipDataEntry> extract(byte[] zipData, Charset zipDataFileNameEncoding, ZipFileFilter filter, int maxEntries) {
		return extract(new ByteArrayInputStream(zipData), zipDataFileNameEncoding, filter, maxEntries);
	}
	
	public static List<ZipDataEntry> extract(InputStream zipData, Charset zipDataFileNameEncoding, ZipFileFilter filter, int maxEntries) {
		if(zipData==null) {
			return null;
		}
		if(maxEntries < 0) {
			maxEntries = Integer.MAX_VALUE;
		}
		
		org.rr.commons.utils.zip.ZipInputStream zipIn = null;
		try {
			zipIn = new org.rr.commons.utils.zip.ZipInputStream(zipData, zipDataFileNameEncoding);
			final ArrayList<ZipDataEntry> result = new ArrayList<ZipDataEntry>();

			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			final byte[] readBuff = new byte[4096];

			org.rr.commons.utils.zip.ZipEntry nextEntry;
			while ((nextEntry=zipIn.getNextEntry()) != null && maxEntries > result.size()) {
				if(filter==null || filter.accept(nextEntry.getName())) {
					int len = 0;
					while ((len = zipIn.read(readBuff)) != -1) {
						bout.write(readBuff, 0, len);
					}
					if(!nextEntry.isDirectory()) {
						result.add(new ZipDataEntry(nextEntry.getName(), bout.toByteArray()));
					}
					bout.reset();
				}
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (zipIn != null) {
				try {zipIn.close();} catch (IOException e) {}
			}
		}
	}
	
	public static List<ZipDataEntry> extract(InputStream zipData, ZipFileFilter filter, int maxEntries) {
		return extract(zipData, Charset.forName("UTF-8"), filter, maxEntries);
	}
	
	public static List<ZipDataEntry> extract(byte[] zipData, ZipFileFilter filter, int maxEntries) {
		return extract(zipData, Charset.forName("UTF-8"), filter, maxEntries);
	}
	
	public static ZipDataEntry extract(byte[] zipData, String entry) {
		return extract(new ByteArrayInputStream(zipData), entry);
	}

	/**
	 * Extracts the entry specified with the entry parameter and returns it.
	 * @param zipData The zip data containing the file to be extracted.
	 * @param entry The entry to be extracted. for example 'META-INF/container.xml'
	 * @return The desired entry or <code>null</code> if the entry is not in the zip.
	 */
	public static ZipDataEntry extract(InputStream zipData, String entry) {
		if(entry==null) {
			return null;
		}
		if(zipData==null) {
			return null;
		}
		
		org.rr.commons.utils.zip.ZipInputStream jar = null;
		try {
			jar = new org.rr.commons.utils.zip.ZipInputStream(zipData);

			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			final byte[] readBuff = new byte[4096];

			org.rr.commons.utils.zip.ZipEntry nextEntry;
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
			final ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipData));
			final ArrayList<ZipDataEntry> result = new ArrayList<ZipDataEntry>();

			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			final byte[] readBuff = new byte[4096];

			ZipEntry nextEntry;
			while ((nextEntry=zip.getNextEntry()) != null) {
				int len = 0;
				while ((len = zip.read(readBuff)) != -1) {
					bout.write(readBuff, 0, len);
				}

				result.add(new ZipDataEntry(nextEntry.getName(), bout.toByteArray()));
				bout.reset();
			}
			zip.close();
			
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
		ByteArrayInputStream in = new ByteArrayInputStream(zipData);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		add(in, out, entry);
		return out.toByteArray();
	}
	
	/**
	 * Adds or replaces the given entry to existing zip data. Note that the whole zip is copied.
	 * The given InputStream and OutputStream is flushed and closed.
	 * 
	 * @param zipDateInputStream The zip data where the given entry should be added.
	 * @param OutputStream zipDateOutputStream The target for the zip data with the new or replaced entry.
	 * @param entry The entry to be added.
	 * @return the zip data with the added entry or <code>null</code> if something was wrong.
	 */
	public static boolean add(InputStream zipDateInputStream, OutputStream zipDateOutputStream, ZipDataEntry entry) {
		boolean success = false;
		if (entry == null || zipDateInputStream == null || zipDateOutputStream == null) {
			return success;
		}

		try {
		    ZipInputStream zipInputStream = new ZipInputStream(zipDateInputStream);
		    ZipOutputStream zipOutputStream = new ZipOutputStream(zipDateOutputStream);
		    
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
		    success = true;
		} catch(Throwable e) {
			LoggerFactory.logWarning(ZipUtils.class, "could not add data to zip", e);
			success = false;
		} finally {
			IOUtils.closeQuietly(zipDateInputStream);
			IOUtils.closeQuietly(zipDateOutputStream);
		}
		return success;
	}
	
	
	/**
	 * Just a data holder class.
	 */
	public static class ZipDataEntry implements Comparable<ZipDataEntry> {
		
		public String path;
		
		public byte[] data;
		
		public ZipDataEntry(org.rr.commons.utils.zip.ZipEntry entry) {
			path = entry.getName();
		}
		
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
		public boolean accept(String entry);
	}
	
	public static class EmptyZipFileFilter implements ZipFileFilter {

		@Override
		public boolean accept(String entry) {
			return true;
		}
		
	}
}
