package org.rr.jeborker.metadata;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.app.FileRefreshBackground;
import org.rr.jeborker.metadata.IMetadataReader.COMMON_METADATA_TYPES;
import org.rr.mobi4java.MobiDocument;
import org.rr.mobi4java.MobiMetaData;
import org.rr.mobi4java.MobiReader;
import org.rr.mobi4java.MobiWriter;

public class MobiMetadataWriter implements IMetadataWriter {
	
	private IResourceHandler ebookResource;

	public MobiMetadataWriter(IResourceHandler ebookResource) {
		this.ebookResource = ebookResource;
	}

	@Override
	public void writeMetadata(List<MetadataProperty> props) {
		try {
			FileRefreshBackground.setDisabled(true);
			MobiDocument mobiDoc = new MobiReader().read(ebookResource.getContentInputStream());
			MobiMetaData metaData = mobiDoc.getMetaData();
			metaData.removeAllEXTHRecords();
			
			applyChanges(props, mobiDoc, metaData);
			
			writeBook(mobiDoc);
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "could not write pdf meta data for " + ebookResource, e);
		} finally {
			FileRefreshBackground.setDisabled(false);
		}
	}

	private void applyChanges(List<MetadataProperty> props, MobiDocument mobiDoc, MobiMetaData metaData) throws UnsupportedEncodingException {
		for (MetadataProperty prop : props) {
			if (StringUtil.equalsIgnoreCase(prop.getName(), "title")) {
				mobiDoc.setFullName(prop.getValueAsString());
			} else if (StringUtil.equals(prop.getName(), COMMON_METADATA_TYPES.COVER.getName())) {
				mobiDoc.setCover((byte[]) prop.getValues().get(0));
			} else if(prop instanceof MobiMetadataProperty) {
				metaData.addEXTHRecord(((MobiMetadataProperty)prop).getExthRecord());
			} else {
				LoggerFactory.logWarning(this.getClass(), "Unkown property " + prop.getName() + " in " + ebookResource);
			}
		}
	}

	protected void writeBook(MobiDocument mobiDoc) throws IOException {
		IResourceHandler temporaryResourceLoader = ResourceHandlerFactory.getUniqueResourceHandler(ebookResource, "tmp");
		try (OutputStream out = temporaryResourceLoader.getContentOutputStream(false);) {
			new MobiWriter().write(mobiDoc, out);
			if (temporaryResourceLoader.size() > 0) {
				temporaryResourceLoader.moveTo(ebookResource, true);
			} else {
				temporaryResourceLoader.delete();
			}
		}
	}

	@Override
	public void storePlainMetadata(byte[] plainMetadata) {
		throw new UnsupportedOperationException("Plain meta data is not supported with mobi.");
	}

}
