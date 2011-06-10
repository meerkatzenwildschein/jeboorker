package org.rr.jeborker;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.rr.jeborker.gui.JEBorkerMainController;

public class JEBorkerConsoleLogger extends Handler {

	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord record) {
		toConsole(record);
		toMonitor(record);
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
			JEBorkerMainController.getController().getProgressMonitor().setMessage(record.getMessage());
		}
	}
	

}
