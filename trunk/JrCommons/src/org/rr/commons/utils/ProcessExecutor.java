package org.rr.commons.utils;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.rr.commons.log.LoggerFactory;
/**
 * @author Nadav Azaria
 * @see http://www.javacodegeeks.com/2013/01/executing-a-command-line-executable-from-java.html?utm_source=feedburner&utm_medium=feed&utm_campaign=Feed%3A+JavaCodeGeeks+%28Java+Code+Geeks%29
 */
public class ProcessExecutor {

	public static final Long WATCHDOG_EXIST_VALUE = -999L;

	public static Future<Long> runProcess(final CommandLine commandline, final ProcessExecutorHandler handler, final long watchdogTimeout) throws IOException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		return executor.submit(new ProcessCallable(watchdogTimeout, handler, commandline));
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
			executor.setStreamHandler(new PumpStreamHandler(new MyLogOutputStream(handler, true), new MyLogOutputStream(handler, false)));
			Long exitValue;

			try {
				exitValue = new Long(executor.execute(commandline));
			} catch (ExecuteException e) {
				exitValue = new Long(e.getExitValue());
			}

			if (watchDog.killedProcess()) {
				exitValue = WATCHDOG_EXIST_VALUE;
			}

			return exitValue;
		}

	}

	private static class MyLogOutputStream extends LogOutputStream {

		private ProcessExecutorHandler handler;

		private boolean forewordToStandardOutput;

		private MyLogOutputStream(ProcessExecutorHandler handler, boolean forewordToStandardOutput) {
			this.handler = handler;
			this.forewordToStandardOutput = forewordToStandardOutput;
		}

		@Override
		protected void processLine(String line, int level) {

			if (forewordToStandardOutput) {
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

}
