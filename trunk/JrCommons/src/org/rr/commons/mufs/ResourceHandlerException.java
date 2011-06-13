package org.rr.commons.mufs;

public class ResourceHandlerException extends Exception {

	private static final long serialVersionUID = -246521802131237110L;
	
	public ResourceHandlerException(String reason) {
		super(reason);
	}
	
	public ResourceHandlerException(Exception e) {
		super(e);
	}
	
	public ResourceHandlerException(String message, Throwable cause) {
		super(message, cause);
	}

}
