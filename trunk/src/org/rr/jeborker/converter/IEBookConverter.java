package org.rr.jeborker.converter;

import java.io.IOException;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.JeboorkerConstants.SUPPORTED_MIMES;

public interface IEBookConverter {

	public IResourceHandler convert() throws IOException;
	
	/**
	 * Get the name of the source file format 
	 */
	public SUPPORTED_MIMES getConversionSourceType();
	
	/**
	 * Get the name of the target file format
	 */
	public SUPPORTED_MIMES getConversionTargetType();
}
