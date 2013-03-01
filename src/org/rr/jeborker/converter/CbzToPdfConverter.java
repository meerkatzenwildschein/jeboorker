package org.rr.jeborker.converter;

import java.io.InputStream;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.compression.truezip.LazyTrueZipEntryStream;
import org.rr.commons.utils.compression.truezip.TrueZipUtils;
import org.rr.jeborker.JeboorkerConstants;
import org.rr.jeborker.JeboorkerConstants.SUPPORTED_MIMES;

/**
 * A converter for comic cbz files to epub 
 */
class CbzToPdfConverter extends ACompressedImageToPdfConverter {

	public CbzToPdfConverter(IResourceHandler cbzResource) {
		super(cbzResource);
	}
	
	protected InputStream getCompressionEntryStream(IResourceHandler resourceHandler, String entry) {
		return new LazyTrueZipEntryStream(resourceHandler, entry);
	}
	
	protected List<String> listEntries(IResourceHandler cbzResource) {
		final List<String> cbzEntries = TrueZipUtils.list(this.comicBookResource);
		return cbzEntries;
	}
	
	@Override
	public SUPPORTED_MIMES getConversionSourceType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_CBZ;
	}

	@Override
	public SUPPORTED_MIMES getConversionTargetType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_PDF;
	}	
}