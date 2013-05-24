package org.rr.jeborker;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.rr.commons.collection.LruList;
import org.rr.jeborker.gui.MainController;

public class JeboorkerLogger extends Handler {
	
	/**
	 * List that is limited to n elements and overwrites the
	 * toString method so each entry is written into one line.
	 */
	public static final LruList<String> log = new LruList<String>(1000) {
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			for(String s : map.values()) {
				if(result.length() != 0) {
					result.append("\n");
				}
				result.append(s);
			}
			
			return result.toString();
		}
	};
	
	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord record) {
		toConsole(record);
		toAppLog(record);
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
	
	private static void toMonitor(LogRecord record) {
		if (record.getMessage() != null) {
			String thrownCause = "";
			if(record.getThrown() != null) {
				thrownCause = record.getThrown().getMessage();
				if(thrownCause != null && thrownCause.length() == 0) {
					thrownCause = null;
				}
			}
			try {
				MainController.getController().getProgressMonitor().setMessage(record.getMessage() + (thrownCause != null && !thrownCause.isEmpty() ? " (" + thrownCause+ ")" : ""));
			} catch(Exception e) {
				
			}
		}
	}
	
	private static void toAppLog(LogRecord record) {
		if (record.getMessage() != null) {
			log.add(SimpleDateFormat.getDateTimeInstance().format(new Date()) + " " + record.getLevel() + ": " + record.getMessage());
		}
		
		if(record.getThrown() != null) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			record.getThrown().printStackTrace(new PrintWriter(stringWriter));
			printWriter.flush();
			printWriter.close();
			log.add(stringWriter.getBuffer().toString());
		}
	}
}
