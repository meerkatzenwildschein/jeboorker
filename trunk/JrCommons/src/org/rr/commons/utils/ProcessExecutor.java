package org.rr.commons.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;

/**
 * @author Nadav Azaria
 * @see http://www.javacodegeeks.com/2013/01/executing-a-command-line-executable-from-java.html?utm_source=feedburner&utm_medium=feed&utm_campaign=Feed%3A+JavaCodeGeeks+%28Java+Code+Geeks%29
 */
public class ProcessExecutor {

	public static final Long WATCHDOG_EXIT_VALUE = -999L;
	
	public static void runProcessAsScript(final CommandLine commandline, final ProcessExecutorHandler handler, final long watchdogTimeout) throws IOException, InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		ProcessCallable processCallable;
		CommandLine commandLineScript = null;
		try {
			if(commandline.getArguments().length > 0) {
				commandLineScript = createCommandLineScript(commandline);	
				processCallable = new ProcessCallable(watchdogTimeout, handler, commandLineScript);
			} else {
				processCallable = new ProcessCallable(watchdogTimeout, handler, commandline);
			}
			Future<Long> submit = executor.submit(processCallable);
			submit.get();
		} finally {
			if(commandLineScript != null) {
				IResourceHandler execResourceHandler = ResourceHandlerFactory.getResourceHandler(commandLineScript.getExecutable());
				execResourceHandler.delete();
			}
		}
	}	
	
	public static Future<Long> runProcess(final CommandLine commandline, final ProcessExecutorHandler handler, final long watchdogTimeout) throws IOException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		ProcessCallable processCallable;
		processCallable = new ProcessCallable(watchdogTimeout, handler, commandline);
		return executor.submit(processCallable);
	}
	
	private static CommandLine createCommandLineScript(final CommandLine commandline) {
		if(ReflectionUtils.getOS() == ReflectionUtils.OS_LINUX) {
			StringBuilder script = new StringBuilder();
			script.append("#!/bin/sh");
			script.append(System.getProperty("line.separator"));
			script.append(commandline.toString());

			OutputStream contentOutputStream = null;
			try {
				IResourceHandler temporaryScriptResource = ResourceHandlerFactory.getTemporaryResource("sh");
				contentOutputStream = temporaryScriptResource.getContentOutputStream(false);
				IOUtils.write(script, contentOutputStream);
				contentOutputStream.flush();
				
				//make executable
				ExecutorService executor = Executors.newSingleThreadExecutor();
				ProcessCallable processCallable = new ProcessCallable(10000, new LogProcessExecutorHandler(), 
						new CommandLine("chmod").addArgument("u+x").addArgument(temporaryScriptResource.toString(), true));
				Future<Long> submit = executor.submit(processCallable);
				submit.get(10, TimeUnit.SECONDS);
				
				//create command for exec script
				CommandLine commandLineScript = new CommandLine(temporaryScriptResource.toFile().toString());
				return commandLineScript;
			} catch(Exception e) {
				LoggerFactory.getLogger(ProcessExecutor.class).log(Level.SEVERE, "Could not create script.", e);
			} finally {
				if(contentOutputStream != null) {
					IOUtils.closeQuietly(contentOutputStream);
				}
			}
		} else if(ReflectionUtils.getOS() == ReflectionUtils.OS_WINDOWS) {
			StringBuilder script = new StringBuilder(commandline.toString());

			OutputStream contentOutputStream = null;
			try {
				IResourceHandler temporaryScriptResource = ResourceHandlerFactory.getTemporaryResource("bat");
				contentOutputStream = temporaryScriptResource.getContentOutputStream(false);
				IOUtils.write(script, contentOutputStream);
				contentOutputStream.flush();
				
				//create command for exec script
				CommandLine commandLineScript = new CommandLine(temporaryScriptResource.toFile().toString());
				return commandLineScript;
			} catch(Exception e) {
				LoggerFactory.getLogger(ProcessExecutor.class).log(Level.SEVERE, "Could not create script.", e);
			} finally {
				if(contentOutputStream != null) {
					IOUtils.closeQuietly(contentOutputStream);
				}
			}			
		}
			
		return commandline;
	}
	
	/**
	 * Replaces the whitespace char with one which did not cause parsing problems.
	 */
	public static String saveWhitespaces(String s) {
		return StringUtils.replace(s, " ", "\u00A0");		
	}

	private static class ProcessCallable implements Callable<Long> {

	    /**
	     * the timeout for the process in milliseconds. It must be
	     *  greater than 0 or {@link ExecuteWatchdog#INFINITE_TIMEOUT} 
	     */		
		private long watchdogTimeout;

		private ProcessExecutorHandler handler;

		private CommandLine commandline;
		
		private ProcessCallable(long watchdogTimeout, ProcessExecutorHandler handler, CommandLine commandline) {
			this.watchdogTimeout = watchdogTimeout;
			this.handler = handler;
			this.commandline = commandline;
		}

		@Override
		public Long call() throws Exception {
			Executor executor = new DefaultExecutor();
			executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
			ExecuteWatchdog watchDog = new ExecuteWatchdog(watchdogTimeout);
			executor.setWatchdog(watchDog);
			executor.setStreamHandler(new PumpStreamHandler(new StreamHandler(handler, StreamHandler.STD_OUT), new StreamHandler(handler, StreamHandler.STD_ERR)));
			Long exitValue;

			try {
				exitValue = new Long(executor.execute(commandline));
			} catch (ExecuteException e) {
				exitValue = new Long(e.getExitValue());
			}

			if (watchDog.killedProcess()) {
				exitValue = WATCHDOG_EXIT_VALUE;
			}

			return exitValue;
		}

	}

	private static class StreamHandler extends LogOutputStream {

		private static int STD_OUT = 0;
		private static int STD_ERR = 1;
		
		private ProcessExecutorHandler handler;

		private int type;

		private StreamHandler(ProcessExecutorHandler handler, int type) {
			this.handler = handler;
			this.type = type;
		}

		@Override
		protected void processLine(String line, int level) {
			if (type == STD_OUT) {
				handler.onStandardOutput(line);
			} else {
				handler.onStandardError(line);
			}
		}
	}

	public static class LogProcessExecutorHandler implements ProcessExecutorHandler {

		@Override
		public void onStandardOutput(String msg) {
			LoggerFactory.getLogger().log(Level.INFO, msg);
		}

		@Override
		public void onStandardError(String msg) {
			LoggerFactory.getLogger().log(Level.WARNING, msg);
		}

	}
	
	public static class EmptyProcessExecutorHandler implements ProcessExecutorHandler {

		@Override
		public void onStandardOutput(String msg) {
		}

		@Override
		public void onStandardError(String msg) {
		}

	}	

}
