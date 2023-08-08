package org.rr.commons.utils.compression.rar;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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
import org.rr.commons.utils.StringUtil;
import org.rr.commons.utils.compression.CompressedDataEntry;
import org.rr.commons.utils.compression.CompressionUtils;
import org.rr.commons.utils.compression.EmptyFileEntryFilter;
import org.rr.commons.utils.compression.FileEntryFilter;

public class RarUtils {
	
	private static String rarExecFolder;
	
	/**
	 * Set the folder where the rar executables could be found.
	 */
	public static void setRarExecFolder(String rarExecF) {
		rarExecFolder = rarExecF;
	}
	
	/**
	 * Extracts all entries from a rar file that are accepted by the given {@link FileEntryFilter}.
	 * @param rarFileHandler The rar file in the file system.
	 * @param path qualified rar path of the entry to be extracted.
	 * @return The desired extracted entries.
	 */
	public static List<CompressedDataEntry> extract(IResourceHandler rarFileHandler, FileEntryFilter rarFileFilter) {
		ArrayList<CompressedDataEntry> result = new ArrayList<>();
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
		List<String> result = list(rarFileHandler, new EmptyFileEntryFilter());
		return result;
	}
	
	private static void removeDirectoryEntries(List<String> files) {
		//need to sort previous because the dir path is always directly before
		//the file entry.
		Collections.sort(files);
		ArrayList<String> toRemove = new ArrayList<>();
		for(int i = 0; i < files.size(); i++) {
			String current = files.get(i);
			if(files.size() > i + 1) {
				String next = files.get(i + 1);
				if(next.startsWith(current + "/")) {
					toRemove.add(current);
				}
			}
		}
		files.removeAll(toRemove);
	}
	
	private static List<String> processFileEntryFilter(List<String> files, FileEntryFilter filter) {
		ArrayList<String> result = new ArrayList<>(files.size());
		for(String file: files) {
			if(filter == null || filter.accept(file, file.getBytes())) {
				result.add(file);
			}
		}
		return result;
	}
	
	/**
	 * List all entries of the rar file allowed by the given {@link ZipFileFilter} instance.
	 */
	public static List<String> list(final IResourceHandler rarFileHandler, final FileEntryFilter rarFileFilter) {
		final List<String> result = new ArrayList<>();
		final CommandLine cl = new CommandLine(getUnRarExecutable());
		
		cl.addArgument("vb");
		cl.addArgument("\"" + rarFileHandler.toFile().getPath() + "\"", false);
		
		try {
			ProcessExecutor.runProcessAsScript(cl, new ProcessExecutorHandler() {
				
				@Override
				public void onStandardOutput(String msg) {
					if(msg.trim().isEmpty()) {
						return;
					} else if(msg.indexOf(cl.toString()) != -1) {
						return;
					} else {
						msg = StringUtil.replace(msg, "\\", "/");
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
		
		//remove dirs
		removeDirectoryEntries(result);
		
		//apply filter
		return processFileEntryFilter(result, rarFileFilter);
	}
	
	public static boolean add(IResourceHandler rarFileHandler, String name, File in) {
		final CommandLine cl = new CommandLine(getRarExecutable());
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
			
			//store only
			if(CompressionUtils.isStoreOnlyFile(name)) {
				cl.addArgument("-m0");
			}
			
			cl.addArgument("-ep"); //do not use the fs path
			
			String rarFilePath = rarFileHandler.toFile().getAbsolutePath();
			cl.addArgument(StringUtil.quote(rarFilePath, '"'), false); //rar archive
			cl.addArgument(StringUtil.quote(in.getAbsolutePath(), '"'), false); //entry to add
			
			ProcessExecutor.runProcessAsScript(cl, new ProcessExecutorHandler() {
				
				@Override
				public void onStandardOutput(String msg) {
					LoggerFactory.getLogger(this).log(Level.INFO, msg);
				}
				
				@Override
				public void onStandardError(String msg) {
					LoggerFactory.getLogger(this).log(Level.WARNING, msg);
				}
			}, ExecuteWatchdog.INFINITE_TIMEOUT);
			
			return true;
		} catch (Exception e) {
			LoggerFactory.getLogger().log(Level.SEVERE, "Faild to add " + name + " to "+ rarFileHandler, e);
		}
		return false;
	}
	
	/**
	 * Add / Replace the an entry with the given name and given data
	 */
	public static boolean add(IResourceHandler rarFileHandler, String name, InputStream data) {
		File file = null;
		try {
			//create a copy of the entry that should be added to the rar.
			file = new File(FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID() + File.separator + name);
			FileUtils.copyInputStreamToFile(data, file);
			add(rarFileHandler, name, file);
		} catch (Exception e) {
			LoggerFactory.getLogger().log(Level.SEVERE, "Faild to add " + name + " to "+ rarFileHandler, e);
		} finally {
			if(file != null) {
				FileUtils.deleteQuietly(file.getParentFile());
			}
		}
		return false;
	}
	
	/**
	 * Get the rar executable.
	 */
	private static String getRarExecutable() {
		if(ReflectionUtils.getOS() == ReflectionUtils.OS_WINDOWS) {
			return rarExecFolder + File.separator + "Rar.exe";
		} else if(ReflectionUtils.getOS() == ReflectionUtils.OS_LINUX) {
			return rarExecFolder + File.separator + "rar";
		}
		throw new RuntimeException("No rar executable!");
	}
	
	/**
	 * Get the rar executable.
	 */
	static String getUnRarExecutable() {
		if(ReflectionUtils.getOS() == ReflectionUtils.OS_WINDOWS) {
			return rarExecFolder + File.separator + "UnRAR.exe";
		} else if(ReflectionUtils.getOS() == ReflectionUtils.OS_LINUX) {
			return rarExecFolder + File.separator + "unrar";
		}
		throw new RuntimeException("No rar executable!");
	}
}
