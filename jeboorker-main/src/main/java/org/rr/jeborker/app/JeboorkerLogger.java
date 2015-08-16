package org.rr.jeborker.app;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMonitor;

public class JeboorkerLogger extends Handler {

	private static final File LOG_FILE = new File(APreferenceStore.getConfigDirectory(), Jeboorker.APP + ".log");

	private static final String LINE_BREAK = System.getProperty("line.separator");

	private OutputStream logfileOutputStream;

	public JeboorkerLogger() {
		try {
			logfileOutputStream = new FileOutputStream(LOG_FILE);
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void close() throws SecurityException {
		flush();
		IOUtils.closeQuietly(logfileOutputStream);
	}

	@Override
	public void flush() {
		try {
			logfileOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void publish(LogRecord record) {
		toConsole(record);
		toLogFile(record);
		if(MainController.isInitialized()) {
			toMonitor(record);
		}
	}

	private static void toConsole(LogRecord record) {
		System.err.println(record.getMessage());
		Throwable thrown = record.getThrown();
		if(thrown!=null) {
			thrown.printStackTrace(System.err);
		}
	}

	/**
	 * Log the given record to the UI progress monitor. The record will be
	 * discarded if the UI is not initialized. This can happen with errors
	 * in the startup phase.

	 * @param record The record to be published to the UI monitor.
	 */
	private static void toMonitor(LogRecord record) {
		if (record.getMessage() != null) {
			String thrownCause = EMPTY;
			if(record.getThrown() != null) {
				thrownCause = record.getThrown().getMessage();
				if(thrownCause != null && thrownCause.length() == 0) {
					thrownCause = null;
				}
			}
			try {
				MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
				if(progressMonitor != null) {
					progressMonitor.setMessage(
							record.getMessage() + (thrownCause != null && !thrownCause.isEmpty() ? " (" + thrownCause+ ")" : EMPTY));
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void toLogFile(LogRecord record) {
		StringBuilder s = new StringBuilder();
		if (record.getMessage() != null) {
			s.append(SimpleDateFormat.getDateTimeInstance().format(new Date()) + " " + record.getLevel() + ": " + record.getMessage());
			s.append(LINE_BREAK);
		}

		if(record.getThrown() != null) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			record.getThrown().printStackTrace(new PrintWriter(stringWriter));
			printWriter.flush();
			printWriter.close();
			s.append(stringWriter.getBuffer().toString());
		}
		try {
			logfileOutputStream.write(s.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	public static String getLogFilePrint() {
		int bytesToRead = 500000; //500kb
		if(LOG_FILE.length() <= bytesToRead * 2) {
			try {
				return FileUtils.readFileToString(LOG_FILE);
			} catch (IOException e) {
				LoggerFactory.log(Level.WARNING, JeboorkerLogger.class, "Failed to read bytes from log file " + LOG_FILE, e);
			}
		} else {
			byte[] intro = new byte[bytesToRead];
			byte[] tail = new byte[bytesToRead];
			try(RandomAccessFile raf = new RandomAccessFile(LOG_FILE, "r")) {
				raf.read(intro);
				raf.seek(LOG_FILE.length() - bytesToRead);
				raf.read(tail, 0, bytesToRead);

				return new StringBuilder(new String(intro)).append("\n...\n").append(new String(tail)).toString();
			} catch (Exception e) {
				LoggerFactory.log(Level.WARNING, JeboorkerLogger.class, "Failed to read bytes from log file " + LOG_FILE, e);
			}
		}
		return null;
	}
}
