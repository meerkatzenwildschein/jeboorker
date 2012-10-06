package org.rr.jeborker;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.rr.jeborker.gui.MainController;

public class JeboorkerLogger extends Handler {

	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord record) {
		toConsole(record);
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
		if(record.getMessage()!=null) {
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
	

}
