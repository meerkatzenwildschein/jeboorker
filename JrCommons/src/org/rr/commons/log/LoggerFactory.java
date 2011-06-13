package org.rr.commons.log;

import java.util.HashSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


public class LoggerFactory {
	
	private static final boolean DEBUG = true;
	
	private static final HashSet<Handler> loggers = new HashSet<Handler>();

	/**
	 * Gets a logger to be used for logging
	 * @param loggerName The name of the logger to be used.
	 * @return The desired {@link Logger} instance.
	 */
	public static Logger getLogger(String loggerName) {
		Logger logger = getLogger();
		return logger;
	}
	
	/**
	 * Gets a logger to be used for logging
	 * @param instance The object instance needs tio use the logger.
	 * @return The desired {@link Logger} instance.
	 */	
	public static Logger getLogger(Class<?> clazz) {
		String loggerName = clazz.getName();
		return getLogger(loggerName);
	}
	
	public static void logDebug(Object instance, String message, Throwable throwable) {
		if(DEBUG) {
			System.err.println("DEBUG: " + message);
		}
	}
	
	public static void logWarning(Object instance, String message, Throwable throwable) {
		log(Level.WARNING, instance, message, throwable);
	}
	
	public static void logInfo(Object instance, String message, Throwable throwable) {
		log(Level.INFO, instance, message, throwable);
	}	
	
	/**
	 * creates a log entry for the given object instance. 
	 * @param level The level for logging. use {@link Level#WARNING} etc. 
	 * @param instance The object instance which would write the log.
	 * @param message The message to be logged
	 * @param throwable The Exception which has been thrown.
	 */
	public static void log(Level level, Object instance, String message, Throwable throwable) {
		if(instance instanceof Class<?>) {
			getLogger((Class<?>)instance).log(level, message, throwable);
		} else {
			getLogger(instance).log(level, message, throwable);
		}
	}
	
	/**
	 * creates a log entry for the given object instance. 
	 * @param level The level for logging. use {@link Level#WARNING} etc. 
	 * @param instance The object instance which would write the log.
	 * @param message The message to be logged
	 */
	public static void log(Level level, Object instance, String message) {
		if(instance instanceof Class<?>) {
			getLogger((Class<?>)instance).log(level, message);
		} else {
			getLogger(instance).log(level, message);
		}
	}	
	
	/**
	 * Sets the default logger handler to be used for logging for each logger.
	 * @param handler The handler to be used for each logger.
	 */
	public static void addHandler(Handler handler) {
		loggers.add(handler);
	}
	
	private static void publish(LogRecord record) {
		if(loggers.isEmpty()) {
			Logger.getAnonymousLogger().log(record);
		} else {
			for (Handler logger : loggers) {
				logger.publish(record);
			}
		}
	}

	public static Logger getLogger(Object instance) {
		return getLogger();
	}
	
	public static Logger getLogger() {
		return new Logger(null, null) {
		    /**
		     * Log a LogRecord.
		     * <p>
		     * All the other logging methods in this class call through
		     * this method to actually perform any logging.  Subclasses can
		     * override this single method to capture all log activity.
		     *
		     * @param record the LogRecord to be published
		     */
		    public void log(LogRecord record) {
		    	LoggerFactory.publish(record);
		    }
		};
	}

	public static void logWarning(Class<?> class1, String message) {
		log(Level.WARNING, class1, message);
	}

}
