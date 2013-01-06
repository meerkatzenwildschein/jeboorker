package org.rr.commons.utils;

public interface ProcessExecutorHandler {

	void onStandardOutput(String msg);

	void onStandardError(String msg);

}
