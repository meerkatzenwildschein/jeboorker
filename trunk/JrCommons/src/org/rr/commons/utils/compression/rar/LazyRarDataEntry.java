package org.rr.commons.utils.compression.rar;

import java.util.concurrent.Future;
import java.util.logging.Level;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ProcessExecutor;
import org.rr.commons.utils.ProcessExecutorHandler;
import org.rr.commons.utils.compression.CompressedDataEntry;

class LazyRarDataEntry extends CompressedDataEntry {

	private IResourceHandler rarFileHandler;
	
	LazyRarDataEntry(IResourceHandler rarFileHandler, String name) {
		super(name, (byte[]) null);
		this.rarFileHandler = rarFileHandler;
	}

	@Override
	public byte[] getBytes() {
		final String commandLineString = "/usr/bin/unrar e";
		final CommandLine cl = CommandLine.parse(commandLineString);
		
		cl.addArgument("-n\"" + path + "\"");
		cl.addArgument("-o+"); //overwrite existing
		cl.addArgument(rarFileHandler.toFile().getName());
		cl.addArgument(FileUtils.getTempDirectoryPath());
		
		try {
			final StringBuilder file = new StringBuilder();
			Future<Long> p = ProcessExecutor.runProcess(cl, new ProcessExecutorHandler() {
				
				@Override
				public void onStandardOutput(String msg) {
					if(msg.startsWith("Extracting") && msg.indexOf(' ') != -1) {
						String tmpFileName = msg.substring(msg.indexOf(' ')).trim();
						if(tmpFileName.endsWith("OK")) {
							file.append(tmpFileName.substring(0, tmpFileName.length() - 2));
						}
					}
				}
				
				@Override
				public void onStandardError(String msg) {
				}
			}, ExecuteWatchdog.INFINITE_TIMEOUT);
			p.get(); //wait
			
			if(file.length() > 0) {
				IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(file.toString());
				byte[] content = resourceHandler.getContent();
				resourceHandler.delete();
				return content;
			}
		} catch (Exception e) {
			LoggerFactory.getLogger().log(Level.SEVERE, "To list files in rar " + rarFileHandler, e);
		}		
		return null;
	}
	
	
}
