package org.rr.commons.utils.compression.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.compression.CompressedDataEntry;

public class ZipUtils {

	public static List<String> list(byte[] zipData) {
		return list(new ByteArrayInputStream(zipData), null);
	}
	
	public static List<String> list(InputStream zipData) {
		return list(zipData, null);
	}
	
	public static List<String> list(InputStream zipData, ZipFileFilter filter) {
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
				if(filter == null || filter.accept(name)) {
					result.add(name);
				}
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
    	
	public static List<CompressedDataEntry> extract(byte[] zipData, Charset zipDataFileNameEncoding, ZipFileFilter filter, int maxEntries) {
		return extract(new ByteArrayInputStream(zipData), zipDataFileNameEncoding, filter, maxEntries);
	}
	
	public static List<CompressedDataEntry> extract(InputStream zipData, Charset zipDataFileNameEncoding, ZipFileFilter filter, int maxEntries) {
		if(zipData == null) {
			return null;
		}
		if(maxEntries < 0) {
			maxEntries = Integer.MAX_VALUE;
		}
		
		org.rr.commons.utils.compression.zip.ZipInputStream zipIn = null;
		try {
			zipIn = new org.rr.commons.utils.compression.zip.ZipInputStream(zipData, zipDataFileNameEncoding);
			final ArrayList<CompressedDataEntry> result = new ArrayList<CompressedDataEntry>();

			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			final byte[] readBuff = new byte[4096];

			org.rr.commons.utils.compression.zip.ZipEntry nextEntry;
			while ((nextEntry = zipIn.getNextEntry()) != null && maxEntries > result.size()) {
				if( filter == null || (!nextEntry.isDirectory() && filter.accept(nextEntry.getName())) ) {
					int len = 0;
					while ((len = zipIn.read(readBuff)) != -1) {
						bout.write(readBuff, 0, len);
					}
					if(!nextEntry.isDirectory()) {
						result.add(new CompressedDataEntry(nextEntry.getName(), bout.toByteArray()));
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
	
	public static List<CompressedDataEntry> extract(InputStream zipData, ZipFileFilter filter, int maxEntries) {
		return extract(zipData, Charset.forName("UTF-8"), filter, maxEntries);
	}
	
	public static List<CompressedDataEntry> extract(byte[] zipData, ZipFileFilter filter, int maxEntries) {
		return extract(zipData, Charset.forName("UTF-8"), filter, maxEntries);
	}
	
	public static CompressedDataEntry extract(byte[] zipData, String entry) {
		return extract(new ByteArrayInputStream(zipData), entry);
	}

	/**
	 * Extracts the entry specified with the entry parameter and returns it.
	 * @param zipData The zip data containing the file to be extracted.
	 * @param entry The entry to be extracted. for example 'META-INF/container.xml'
	 * @return The desired entry or <code>null</code> if the entry is not in the zip.
	 */
	public static CompressedDataEntry extract(InputStream zipData, final String entry) {
		List<CompressedDataEntry> extract = extract(zipData, new ZipFileFilter() {
			
			@Override
			public boolean accept(String e) {
				return entry.equals(e);
			}
		}, Integer.MAX_VALUE);
		if(!extract.isEmpty()) {
			return extract.get(0);
		}
		return null;
	}
	

	/**
	 * Adds or replaces the given entry to existing zip data. Note that the whole
	 * zip is copied.
	 * @param zipData The zip data where the given entry should be added.
	 * @param entry The entry to be added.
	 * @return the zip data with the added entry or <code>null</code> if something was wrong.
	 */	
	public static byte[] add(byte[] zipData, CompressedDataEntry entry) {
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
	public static boolean add(InputStream zipDateInputStream, OutputStream zipDateOutputStream, CompressedDataEntry entry) {
		return add(zipDateInputStream, zipDateOutputStream, entry, false);
	}
	
	/**
	 * Adds or replaces the given entry to existing zip data. Note that the whole zip is copied.
	 * The given InputStream and OutputStream is flushed and closed.
	 * 
	 * @param zipDateInputStream The zip data where the given entry should be added.
	 * @param OutputStream zipDateOutputStream The target for the zip data with the new or replaced entry.
	 * @param entry The entry to be added.
	 * @param storeOnly Did not compress the zip if <code>true</code> and does if <code>false</code>.
	 * @return the zip data with the added entry or <code>null</code> if something was wrong.
	 */
	public static boolean add(InputStream zipDateInputStream, OutputStream zipDataOutputStream, CompressedDataEntry entry, boolean storeOnly) {
		boolean success = false;
		if (entry == null || zipDateInputStream == null || zipDataOutputStream == null) {
			return success;
		}

		ZipOutputStream zipOutputStream = null;
		ZipInputStream zipInputStream = null;
		try {
		    zipInputStream = new ZipInputStream(zipDateInputStream);
		    zipOutputStream = new ZipOutputStream(zipDataOutputStream);
		    if(storeOnly) {
		    	zipOutputStream.setMethod(ZipEntry.STORED);
		    } else {
		    	zipOutputStream.setMethod(ZipEntry.DEFLATED);
		    }
	    	boolean replaceSuccess = false;
		    ZipEntry zipEntryIn;
		    while ((zipEntryIn = zipInputStream.getNextEntry()) != null) {
		    	ZipEntry out;
		    	InputStream read;
		    	if(zipEntryIn.getName().equals(entry.path)) {
		    		//replace
		    		out = new ZipEntry(zipEntryIn.getName());
		    		read = entry.data;
		    		replaceSuccess = true;
		    	} else {
		    		//add
		    		out = new ZipEntry(zipEntryIn.getName());
		    		read = zipInputStream;
		    	}
		    	byte[] readBytes = IOUtils.toByteArray(read);
		    	if(storeOnly) {
		    		//no need with compression
			    	out.setSize(readBytes.length);
			    	long crc = CommonUtils.calculateCrc(readBytes);
			    	out.setCrc(crc);
		    	}
	            zipOutputStream.putNextEntry(out);
	            IOUtils.copy(new ByteArrayInputStream(readBytes), zipOutputStream);
	            zipOutputStream.flush();
	            
	            
		        zipInputStream.closeEntry();
		        zipOutputStream.closeEntry();
		    }
		    
		    if(!replaceSuccess) {
		    	//new entry must be added
		    	ZipEntry out = new ZipEntry(entry.path);
		    	if(storeOnly) {
		    		//no need with compression
		    		byte[] bytes = entry.getBytes();
			    	out.setSize(bytes.length);
			    	long crc = CommonUtils.calculateCrc(bytes);
			    	out.setCrc(crc);
		    	}		    	
		    	InputStream read = entry.data;
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
			IOUtils.closeQuietly(zipInputStream);
			IOUtils.closeQuietly(zipOutputStream);
		}
		return success;
	}

}
