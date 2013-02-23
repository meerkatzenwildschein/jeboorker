package org.rr.commons.utils.compression.rar;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ProcessExecutor;
import org.rr.commons.utils.ProcessExecutorHandler;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.compression.CompressedDataEntry;

class LazyRarDataEntry extends CompressedDataEntry {

	private IResourceHandler rarFileHandler;
	
	LazyRarDataEntry(IResourceHandler rarFileHandler, String name) {
		super(name, (byte[]) null);
		this.rarFileHandler = rarFileHandler;
	}

	@Override
	public byte[] getBytes() {
		final CommandLine cl = new CommandLine(RarUtils.getUnRarExecutable());
		cl.addArgument("e");
		
		cl.addArgument("-n\"" + StringUtils.replace(path, "/", File.separator) + "\"", false);
		cl.addArgument("-o+"); //overwrite existing
		cl.addArgument("\"" + rarFileHandler.toFile().getPath() + "\"", false);
		
		File out;
		try {
			out = new File(FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID().toString() + File.separator);
			out.mkdir();
			cl.addArgument(out.getPath());
			
			final StringBuilder file = new StringBuilder();
			ProcessExecutor.runProcessAsScript(cl, new ProcessExecutorHandler() {
				
				@Override
				public void onStandardOutput(String msg) {
					if(msg.startsWith("Extracting") && msg.indexOf(' ') != -1) {
						//Extracting  C:\Users\guru\AppData\Local\Temp\923f52d2-14c9-4690-958f-e5d3dd6444f4\file 1.test     \b\b\b\b 14%\b\b\b\b\b  OK 
						String tmpFileName = msg.substring(msg.indexOf(' ')).trim();
						if(tmpFileName.endsWith("OK")) {
							tmpFileName = tmpFileName.substring(0, tmpFileName.length() - 2);
							if(tmpFileName.indexOf('\b') != -1) {
								tmpFileName = tmpFileName.substring(0, tmpFileName.indexOf('\b'));
							} else if(tmpFileName.indexOf('%') != -1) {
								tmpFileName = tmpFileName.substring(0, tmpFileName.lastIndexOf('%'));
							}
							file.append(tmpFileName.trim());
						}
					} else if(msg.startsWith("No files to extract")) {
						//something goes wrong
						LoggerFactory.getLogger().log(Level.SEVERE, "Failed to extract file " + path + " from rar "+ rarFileHandler);
					}
				}
				
				@Override
				public void onStandardError(String msg) {
				}
			}, ExecuteWatchdog.INFINITE_TIMEOUT);
			
			if(file.length() > 0) {
				File extractedFiled = new File(file.toString());
				if(extractedFiled.exists()) {
					IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(extractedFiled);
					byte[] content = resourceHandler.getContent();
					FileUtils.deleteQuietly(new File(file.toString()).getParentFile());
					return content;
				}
			} else {
				FileUtils.deleteQuietly(out);
			}
		} catch (Exception e) {
			LoggerFactory.getLogger().log(Level.SEVERE, "To list files in rar " + rarFileHandler, e);
		}		
		return null;
	}
	
	
}
