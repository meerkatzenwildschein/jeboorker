package org.rr.jeborker.converter;

import java.io.File;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.compression.CompressedDataEntry;
import org.rr.commons.utils.compression.FileEntryFilter;
import org.rr.commons.utils.compression.rar.RarUtils;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES;

public class CbrToCbrConverter extends AArchiveToArchiveConverter {

	public CbrToCbrConverter(IResourceHandler cbrResource) {
		super(cbrResource);
	}
	
	@Override
	protected String getTargetArchiveExtension() {
		return SUPPORTED_MIMES.MIME_CBR.getName();
	}

	@Override
	protected List<CompressedDataEntry> extractArchive(IResourceHandler cbrResource) {
		return RarUtils.extract(cbrResource, (FileEntryFilter) null);
	}

	@Override
	public SUPPORTED_MIMES getConversionSourceType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_CBR;
	}

	@Override
	public SUPPORTED_MIMES getConversionTargetType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_CBR;
	}

	@Override
	protected void addToArchive(IResourceHandler targetCbzResource, String archiveFile, File imageBytes) {
		RarUtils.add(targetCbzResource, archiveFile, imageBytes);
	}

}
