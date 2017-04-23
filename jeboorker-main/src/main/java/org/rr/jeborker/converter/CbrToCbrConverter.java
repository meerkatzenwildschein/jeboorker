package org.rr.jeborker.converter;

import java.io.ByteArrayInputStream;
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
	protected void addToArchive(IResourceHandler targetCbzResource, CompressedDataEntry sourceFile, byte[] imageBytes) {
		RarUtils.add(targetCbzResource, sourceFile.getName(), new ByteArrayInputStream(imageBytes));
	}

	@Override
	protected void addToArchive(IResourceHandler targetCbzResource, CompressedDataEntry sourceFile, int i, byte[] imageBytes) {
		RarUtils.add(targetCbzResource, injectCounterToFileName(sourceFile.getName(), i), new ByteArrayInputStream(imageBytes));
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
	protected void addToArchive(IResourceHandler targetCbzResource, String archiveFile, byte[] imageBytes) {
		RarUtils.add(targetCbzResource, archiveFile, new ByteArrayInputStream(imageBytes));
	}

}
