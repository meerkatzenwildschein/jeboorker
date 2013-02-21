package org.rr.jeborker.converter;

import java.io.InputStream;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.compression.rar.LazyRarEntryStream;
import org.rr.commons.utils.compression.rar.RarUtils;
import org.rr.jeborker.JeboorkerConstants;
import org.rr.jeborker.JeboorkerConstants.SUPPORTED_MIMES;

/**
 * A converter for comic cbr files to epub 
 */
class CbrToPdfConverter extends ACompressedImageToEpubConverter {

	public CbrToPdfConverter(IResourceHandler comicBookResource) {
		super(comicBookResource);
	}
	
	protected InputStream getCompressionEntryStream(IResourceHandler resourceHandler, String entry) {
		return new LazyRarEntryStream(resourceHandler, entry);
	}
	
	protected List<String> listEntries(IResourceHandler cbrResource) {
		final List<String> cbzEntries = RarUtils.list(this.comicBookResource);
		return cbzEntries;
	}

	@Override
	public SUPPORTED_MIMES getConversionSourceType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_CBR;
	}

	@Override
	public SUPPORTED_MIMES getConversionTargetType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_PDF;
	}	
}
