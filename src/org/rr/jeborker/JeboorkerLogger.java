package org.rr.jeborker;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.rr.jeborker.gui.MainController;

public class JeboorkerLogger extends Handler {
	
	public static final StringBuilder log = new StringBuilder();
	
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
			MainController.getController().getProgressMonitor().setMessage(record.getMessage() + (thrownCause != null && !thrownCause.isEmpty() ? " (" + thrownCause+ ")" : ""));
		}
	}
	
	private static void toAppLog(LogRecord record) {
		if (record.getMessage() != null) {
			log.append(record.getMessage());
			log.append("\n");
		}
		
		if(record.getThrown() != null) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			record.getThrown().printStackTrace(new PrintWriter(stringWriter));
			printWriter.flush();
			printWriter.close();
			log.append(stringWriter.getBuffer());
		}
	}
}
