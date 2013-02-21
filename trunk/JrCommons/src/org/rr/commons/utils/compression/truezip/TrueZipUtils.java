package org.rr.commons.utils.compression.truezip;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.compression.CompressedDataEntry;
import org.rr.commons.utils.compression.CompressionUtils;
import org.rr.commons.utils.compression.EmptyFileEntryFilter;
import org.rr.commons.utils.compression.FileEntryFilter;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileOutputStream;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.FsOutputOption;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;

public class TrueZipUtils {
	
	static {
		TConfig.get().setArchiveDetector(
			    new TArchiveDetector(
			        "cbz|epub",
			        new JarDriver(IOPoolLocator.SINGLETON)));			
	}
	
	/**
	 * Extracts all entries from a zip file that are accepted by the given {@link ZipFileFilter}.
	 * @param zipFile The zip file in the file system.
	 * @param path qualified zip path of the entry to be extracted.
	 * @return The desired extracted entries.
	 */	
	public static List<CompressedDataEntry> extract(IResourceHandler zipFileHandler, FileEntryFilter filter) {
		final List<CompressedDataEntry> result = new ArrayList<CompressedDataEntry>();
		final List<String> entryList = list(zipFileHandler);
		for(String entry : entryList) {
			if(filter.accept(entry)) {
				result.add(extract(zipFileHandler, entry));
			}
		}
		return result;
	}	
	
	/**
	 * Extracts one entry from a zip file.
	 * @param zipFile The zip file in the file system.
	 * @param name qualified zip path of the entry to be extracted.
	 * @return The desired extracted entry. Never returns <code>null</code> but the 
	 * result {@link TrueZipDataEntry} would return no bytes if the zip entry did not exists.
	 */
	public static CompressedDataEntry extract(IResourceHandler zipFileHandler, String name) {
		return new LazyTrueZipDataEntry(zipFileHandler, name);
	}
	
	/**
	 * List all entries of the zip file.
	 */
	public static List<String> list(IResourceHandler zipFileHandler) {
		return list(zipFileHandler, new EmptyFileEntryFilter());
	}
	
	/**
	 * List all entries of the zip file allowed by the given {@link ZipFileFilter} instance.
	 */	
	public static List<String> list(IResourceHandler zipFileHandler, FileEntryFilter zipFileFilter) {
		File zipFile = zipFileHandler.toFile();
		TFile archive = new TFile(zipFile.toString());
		Collection<File> listFiles = FileUtils.listFiles(archive, null, true);
		ArrayList<String> result = new ArrayList<String>();
		for(File f : listFiles) {
			String enclEntryName = ((TFile)f).getEnclEntryName();
			if(zipFileFilter.accept(enclEntryName)) {
				result.add(enclEntryName);
			}
		}
		return result;
	}	

	/**
	 * Add / Replace the an entry with the given name and given data
	 */
	public static boolean add(IResourceHandler zipFileHandler, String name, InputStream data) {
		File zipFile = zipFileHandler.toFile();
		
		// First, push a new current configuration on the inheritable thread local
		// stack.
		TConfig config = TConfig.push();
		try {
		    // Set FsOutputOption.GROW for appending-to rather than reassembling an
		    // archive file.
			if(zipFile.length() > 5000000) { //Larger than five MB - don't reassemble the zip file.
				config.setOutputPreferences(config.getOutputPreferences().set(FsOutputOption.GROW));
			}
			if(CompressionUtils.isStoreOnlyFile(name)) {
				config.setOutputPreferences(config.getOutputPreferences().set(FsOutputOption.STORE));
			}

		    TFile file = new TFile(zipFile.toString() + "/" + name);
		    
		    // Now use the current configuration and append the entry to the archive
		    // file even if it's already present.
		    TFileOutputStream zipOut = new TFileOutputStream(file);
		    try {
		        // Do some output here.
		    	IOUtils.copy(data, zipOut);
		    } finally {
		    	zipOut.flush();
		    	IOUtils.closeQuietly(zipOut);
		    	
		    	TVFS.umount(); //commit changes
		    }
		} catch(Exception e) {
			return false;
		} finally {
		    // Pop the current configuration off the inheritable thread local stack.
		    config.close();
		}
		return true;
	}
	
	public static ZipOutputStream createZipOutputStream(OutputStream out) {
		ZipOutputStream zout = new ZipOutputStream(out);
		return zout;
	}
}
