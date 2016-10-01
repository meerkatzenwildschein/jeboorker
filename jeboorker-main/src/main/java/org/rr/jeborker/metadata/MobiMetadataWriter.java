package org.rr.jeborker.metadata;

import java.io.IOException;
import java.util.List;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.app.FileRefreshBackground;
import org.rr.jeborker.metadata.mobi.EXTHRecord;
import org.rr.jeborker.metadata.mobi.MobiMeta;
import org.rr.jeborker.metadata.mobi.MobiMetaException;

public class MobiMetadataWriter implements IMetadataWriter {
	
	private IResourceHandler ebookResource;

	public MobiMetadataWriter(IResourceHandler ebookResource) {
		this.ebookResource = ebookResource;
	}

	@Override
	public void writeMetadata(List<MetadataProperty> props) {
		try {
			FileRefreshBackground.setDisabled(true);
			
			MobiMeta mobiMeta = new MobiMeta().readMetaData(ebookResource.toFile());
			List<EXTHRecord> exthRecords = mobiMeta.getEXTHRecords();
			exthRecords.clear();
			
			for (MetadataProperty prop : props) {
				if (StringUtil.equalsIgnoreCase(prop.getName(), "title")) {
					mobiMeta.setFullName(prop.getValueAsString());
				} else if(prop instanceof MobiMetadataProperty) {
					exthRecords.add(((MobiMetadataProperty)prop).getExthRecord());
				} else {
					LoggerFactory.logWarning(this.getClass(), "Unkown property " + prop.getName() + " in " + ebookResource);
				}
			}
			mobiMeta.setEXTHRecords();
			
			writeBook(mobiMeta);
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "could not write pdf meta data for " + ebookResource, e);
		} finally {
			FileRefreshBackground.setDisabled(false);
		}
	}

	protected void writeBook(MobiMeta mobiMeta) throws MobiMetaException, IOException {
		IResourceHandler temporaryResourceLoader = ResourceHandlerFactory.getUniqueResourceHandler(ebookResource, "tmp");
		mobiMeta.saveToNewFile(temporaryResourceLoader.toFile());
		if(temporaryResourceLoader.size() > 0) {
			temporaryResourceLoader.moveTo(ebookResource, true);
		} else {
			temporaryResourceLoader.delete();
		}
	}

	@Override
	public void storePlainMetadata(byte[] plainMetadata) {
		throw new UnsupportedOperationException("Plain meta data is not supported with mobi.");
	}

}
