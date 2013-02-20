package org.rr.commons.utils.compression.rar;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.ProcessExecutor;
import org.rr.commons.utils.ProcessExecutorHandler;
import org.rr.commons.utils.compression.CompressedDataEntry;
import org.rr.commons.utils.compression.zip.ZipFileFilter;

public class RarUtils {
	
	/**
	 * Extracts all entries from a rar file that are accepted by the given {@link RarFileFilter}.
	 * @param rarFileHandler The rar file in the file system.
	 * @param path qualified rar path of the entry to be extracted.
	 * @return The desired extracted entries.
	 */	
	public static List<CompressedDataEntry> extract(IResourceHandler rarFileHandler, RarFileFilter rarFileFilter) {
		ArrayList<CompressedDataEntry> result = new ArrayList<CompressedDataEntry>();
		List<String> list = list(rarFileHandler, rarFileFilter);
		for(String entry : list) {
			result.add(extract(rarFileHandler, entry));
		}
		return result;		
	}
	
	/**
	 * Extracts one entry from a rar file.
	 * @param rarFileHandler The rar file in the file system.
	 * @param name qualified rar path of the entry to be extracted.
	 * @return The desired extracted entry. Never returns <code>null</code> but the 
	 * result {@link TrueZipDataEntry} would return no bytes if the rar entry did not exists.
	 */
	public static CompressedDataEntry extract(IResourceHandler rarFileHandler, String name) {
		return new LazyRarDataEntry(rarFileHandler, name);
	}	
	
	/**
	 * List all entries of the rar file.
	 */
	public static List<String> list(IResourceHandler rarFileHandler) {
		return list(rarFileHandler, new RarFileFilter() {
			
			@Override
			public boolean accept(String entry) {
				return true;
			}
		});
	}
	
	/**
	 * List all entries of the rar file allowed by the given {@link ZipFileFilter} instance.
	 */	
	public static List<String> list(final IResourceHandler rarFileHandler, final RarFileFilter rarFileFilter) {
		final ArrayList<String> result = new ArrayList<String>();
		final String commandLineString = "/usr/bin/unrar lb";
		final CommandLine cl = CommandLine.parse(commandLineString);
		
		cl.addArgument(rarFileHandler.toFile().getName());
		
		try {
			Future<Long> p = ProcessExecutor.runProcess(cl, new ProcessExecutorHandler() {
				
				@Override
				public void onStandardOutput(String msg) {
					if(rarFileFilter.accept(msg)) {
						result.add(msg);
					}
				}
				
				@Override
				public void onStandardError(String msg) {
				}
			}, ExecuteWatchdog.INFINITE_TIMEOUT);
			p.get(); //wait
		} catch (Exception e) {
			LoggerFactory.getLogger().log(Level.SEVERE, "To list files in rar " + rarFileHandler, e);
		}		
		return result;
	}		
	
	/**
	 * Add / Replace the an entry with the given name and given data
	 */
	public static boolean add(IResourceHandler rarFileHandler, String name, InputStream data) {
		final String commandLineString = "/usr/bin/rar a";
		final CommandLine cl = CommandLine.parse(commandLineString);
		
		try {
			cl.addArgument("-o+"); //overwrite existing
			
			//rar archive path
			if(name.indexOf('/') != -1) {
				String path = ProcessExecutor.saveWhitespaces(name.substring(0, name.lastIndexOf('/')));
				cl.addArgument("-ap" + path); 
				name = name.substring(name.lastIndexOf('/') + 1);
			} else {
				cl.addArgument("-ap");
			}
			ProcessExecutor.saveWhitespaces(name);
			
			cl.addArgument("-ep"); //do not use the fs path
			
			String rarFilePath = rarFileHandler.toFile().getPath();
			rarFilePath = ProcessExecutor.saveWhitespaces(rarFilePath);
			cl.addArgument(rarFilePath); //rar archive
			
			File in = new File(FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID().toString() + File.separator + name);
			FileUtils.copyInputStreamToFile(data, in);
			
			cl.addArgument(in.getPath()); //entry to add
			
			Process exec = Runtime.getRuntime().exec(cl.toString());
			exec.waitFor();
			
			Future<Long> p = ProcessExecutor.runProcess(cl, data, new ProcessExecutorHandler() {
				
				@Override
				public void onStandardOutput(String msg) {
					System.out.println(msg);
				}
				
				@Override
				public void onStandardError(String msg) {
				}
			}, ExecuteWatchdog.INFINITE_TIMEOUT);
			
			p.get(); //wait
			
			if(in.toString().startsWith(FileUtils.getTempDirectoryPath())) {
				FileUtils.deleteQuietly(in.getParentFile());
			}
			return true;
		} catch (Exception e) {
			LoggerFactory.getLogger().log(Level.SEVERE, "Faild to add " + name + " to "+ rarFileHandler, e);
		}				
		return false;
	}
}
