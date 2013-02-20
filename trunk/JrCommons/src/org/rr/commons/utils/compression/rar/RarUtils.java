package org.rr.commons.utils.compression.rar;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.ProcessExecutor;
import org.rr.commons.utils.ProcessExecutorHandler;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.compression.CompressedDataEntry;
import org.rr.commons.utils.compression.zip.ZipFileFilter;
import org.rr.jeborker.Jeboorker;

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
		final CommandLine cl = new CommandLine(getUnRarExecutable());
		
		cl.addArgument("vb");
		cl.addArgument(rarFileHandler.toFile().getPath());
		
		try {
			ProcessExecutor.runProcessAsScript(cl, new ProcessExecutorHandler() {
				
				@Override
				public void onStandardOutput(String msg) {
					if(msg.trim().isEmpty()) {
						return;
					} else if(msg.indexOf(cl.toString()) != -1) {
						return;
					} else if(rarFileFilter.accept(msg)) {
						msg = StringUtils.replace(msg, "\\", "/");
						result.add(msg);
					}
				}
				
				@Override
				public void onStandardError(String msg) {
				}
			}, 100000);
		} catch (Exception e) {
			LoggerFactory.getLogger().log(Level.SEVERE, "To list files in rar " + rarFileHandler, e);
		}		
		return result;
	}		
	
	/**
	 * Add / Replace the an entry with the given name and given data
	 */
	public static boolean add(IResourceHandler rarFileHandler, String name, InputStream data) {
		final CommandLine cl = new CommandLine(getRarExecutable());
		File in = null;
		try {
			cl.addArgument("a"); //add
			cl.addArgument("-o+"); //overwrite existing
			
			//rar archive path
			if(name.indexOf('/') != -1) {
				String path = name.substring(0, name.lastIndexOf('/'));
				cl.addArgument("-ap\"" + path + "\"", false); 
				name = name.substring(name.lastIndexOf('/') + 1);
			} else {
				cl.addArgument("-ap");
			}
			
			cl.addArgument("-ep"); //do not use the fs path
			
			String rarFilePath = rarFileHandler.toFile().getPath();
			cl.addArgument(rarFilePath); //rar archive
			
			//create a copy of the entry that should be added to the rar.
			in = new File(FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID().toString() + File.separator + name);
			FileUtils.copyInputStreamToFile(data, in);
			
			cl.addArgument(in.getPath()); //entry to add
			
			ProcessExecutor.runProcessAsScript(cl, new ProcessExecutorHandler() {
				
				@Override
				public void onStandardOutput(String msg) {
					System.out.println(msg);
				}
				
				@Override
				public void onStandardError(String msg) {
				}
			}, ExecuteWatchdog.INFINITE_TIMEOUT);
			
			return true;
		} catch (Exception e) {
			LoggerFactory.getLogger().log(Level.SEVERE, "Faild to add " + name + " to "+ rarFileHandler, e);
		} finally {
			if(in != null) {
				FileUtils.deleteQuietly(in.getParentFile());
			}
		}
		return false;
	}
	
	/**
	 * Get the rar executable.
	 */
	private static String getRarExecutable() {
		if(ReflectionUtils.getOS() == ReflectionUtils.OS_WINDOWS) {
			return Jeboorker.getAppFolder() + File.separator + "exec" + File.separator + "Rar.exe";
		} else if(ReflectionUtils.getOS() == ReflectionUtils.OS_LINUX) {
			return "/usr/bin/rar";
		}
		throw new RuntimeException("No rar executable!");
	}
	
	/**
	 * Get the rar executable.
	 */
	static String getUnRarExecutable() {
		if(ReflectionUtils.getOS() == ReflectionUtils.OS_WINDOWS) {
			return Jeboorker.getAppFolder() + File.separator + "exec" + File.separator + "UnRAR.exe";
		} else if(ReflectionUtils.getOS() == ReflectionUtils.OS_LINUX) {
			return "/usr/bin/unrar";
		}
		throw new RuntimeException("No rar executable!");
	}
}
