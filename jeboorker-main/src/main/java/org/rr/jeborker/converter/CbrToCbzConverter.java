package org.rr.jeborker.converter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.compression.CompressedDataEntry;
import org.rr.commons.utils.compression.FileEntryFilter;
import org.rr.commons.utils.compression.rar.RarUtils;
import org.rr.commons.utils.compression.truezip.TrueZipUtils;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES;

public class CbrToCbzConverter extends AArchiveToArchiveConverter {

	public CbrToCbzConverter(IResourceHandler cbrResource) {
		super(cbrResource);
	}
	
	@Override
	protected String getTargetArchiveExtension() {
		return SUPPORTED_MIMES.MIME_CBZ.getName();
	}

	@Override
	protected List<CompressedDataEntry> extractArchive(IResourceHandler cbzResource) {
		return RarUtils.extract(cbzResource, (FileEntryFilter) null);
	}

	@Override
	public SUPPORTED_MIMES getConversionSourceType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_CBR;
	}

	@Override
	public SUPPORTED_MIMES getConversionTargetType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_CBZ;
	}

	@Override
	protected void addToArchive(IResourceHandler targetCbzResource, String archiveFile, File imageBytes) {
		try {
			TrueZipUtils.add(targetCbzResource, archiveFile, new ByteArrayInputStream(FileUtils.readFileToByteArray(imageBytes)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
