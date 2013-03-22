package org.rr.jeborker.converter;

import java.io.IOException;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.JeboorkerConstants.SUPPORTED_MIMES;
import org.rr.jeborker.gui.ConverterPreferenceController;

public interface IEBookConverter {

	public IResourceHandler convert() throws IOException;
	
	/**
	 * Creates the {@link ConverterPreferenceController} instance that was used/set for this {@link IEBookConverter}
	 * instance.
	 */
	public ConverterPreferenceController createConverterPreferenceController();
	
	/**
	 * Sets the {@link ConverterPreferenceController} that should be used for this {@link IEBookConverter}
	 * instance.
	 */
	public void setConverterPreferenceController(ConverterPreferenceController controller);
	
	/**
	 * Get the name of the source file format 
	 */
	public SUPPORTED_MIMES getConversionSourceType();
	
	/**
	 * Get the name of the target file format
	 */
	public SUPPORTED_MIMES getConversionTargetType();
}
