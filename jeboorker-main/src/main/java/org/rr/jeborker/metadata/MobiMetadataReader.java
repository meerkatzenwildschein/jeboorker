package org.rr.jeborker.metadata;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.metadata.AEpubMetadataHandler.EPUB_METADATA_TYPES;
import org.rr.jeborker.metadata.mobi.EXTHRecord;
import org.rr.jeborker.metadata.mobi.MobiMeta;
import org.rr.jeborker.metadata.mobi.MobiMetaException;

public class MobiMetadataReader extends APDFCommonMetadataHandler implements IMetadataReader {

	private IResourceHandler ebookResource;

	public MobiMetadataReader(IResourceHandler ebookResource) {
		this.ebookResource = ebookResource;
	}

	@Override
	public List<IResourceHandler> getEbookResource() {
		return Collections.singletonList(this.ebookResource);
	}

	@Override
	public List<MetadataProperty> readMetadata() {
		try {
			final ArrayList<MetadataProperty> result = new ArrayList<>();

			MobiMeta mobiMeta = new MobiMeta(ebookResource.toFile());

			String fullName = mobiMeta.getFullName();
			if (StringUtil.isNotBlank(fullName)) {
				result.add(new MetadataProperty("title", fullName));
			}

			List<EXTHRecord> exthRecords = mobiMeta.getEXTHRecords();
			for (EXTHRecord exthRecord : exthRecords) {
				result.add(new MobiMetadataProperty(exthRecord));
			}
			return result;
		} catch (MobiMetaException e) {
			LoggerFactory.logWarning(getClass(), "Could not read metadata for pdf " + ebookResource, e);
		}
		return new ArrayList<MetadataProperty>(0);
	}

	@Override
	public List<MetadataProperty> getSupportedMetadata() {
		final ArrayList<MetadataProperty> result = new ArrayList<>();
		result.add(new MetadataProperty("title", EMPTY));
		result.add(new MobiMetadataProperty(100, EMPTY)); // author
		result.add(new MobiMetadataProperty(103, EMPTY)); // description
		result.add(new MobiMetadataProperty(104, EMPTY)); // isbn
		result.add(new MobiMetadataProperty(105, EMPTY)); // subject
		result.add(new MobiMetadataProperty(106, EMPTY)); // publishingdate
		result.add(new MobiMetadataProperty(107, EMPTY)); // review
		result.add(new MobiMetadataProperty(108, EMPTY)); // contributor
		result.add(new MobiMetadataProperty(109, EMPTY)); // rights
		result.add(new MobiMetadataProperty(112, EMPTY)); // source
		result.add(new MobiMetadataProperty(113, EMPTY)); // asin
		result.add(new MobiMetadataProperty(524, EMPTY)); // language
		result.add(new MobiMetadataProperty(501, EMPTY)); // cdetype
		return result;
	}

	@Override
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item) {
		item.clearMetadata();
		for (MetadataProperty metadataProperty : metadataProperties) {
			for (EPUB_METADATA_TYPES type : EPUB_METADATA_TYPES.values()) {
				if (type.getName().equals(metadataProperty.getName())) {
					type.fillItem(metadataProperty, item);
					break;
				}
			}
		}
	}

	@Override
	public String getPlainMetadata() {
		return null;
	}

	@Override
	public String getPlainMetadataMime() {
		return null;
	}

	@Override
	public List<MetadataProperty> getMetadataByType(boolean create, List<MetadataProperty> props, COMMON_METADATA_TYPES type) {
		List<MetadataProperty> result = getMetadataByType(props, type);
		if (create && result.isEmpty()) {
			switch (type) {
			case TITLE:
				result.add(new MetadataProperty("title", EMPTY));
				break;
			case AUTHOR:
				result.add(new MobiMetadataProperty(100, EMPTY)); // author
				break;
			case ISBN:
				result.add(new MobiMetadataProperty(104, EMPTY)); // isbn
				break;
			case LANGUAGE:
				result.add(new MobiMetadataProperty(524, EMPTY)); // language
				break;
			case DESCRIPTION:
				result.add(new MobiMetadataProperty(103, EMPTY)); // description
				break;
			}
		}
		return result;
	}

	private List<MetadataProperty> getMetadataByType(List<MetadataProperty> props, COMMON_METADATA_TYPES type) {
		final ArrayList<MetadataProperty> result = new ArrayList<>();
		for (MetadataProperty prop : props) {
			switch (type) {
			case TITLE:
				if (StringUtil.equalsIgnoreCase(prop.getName(), "title")) {
					result.add(prop);
				}
				break;
			case AUTHOR:
				if (StringUtil.equalsIgnoreCase(prop.getName(), "author")) {
					result.add(prop);
				}
				break;
			case ISBN:
				if (StringUtil.equalsIgnoreCase(prop.getName(), "ISBN")) {
					result.add(prop);
				}
				break;
			case LANGUAGE:
				if (StringUtil.equalsIgnoreCase(prop.getName(), "language")) {
					result.add(prop);
				}
				break;
			case DESCRIPTION:
				if (StringUtil.equalsIgnoreCase(prop.getName(), "description")) {
					result.add(prop);
				}
				break;
			}

		}
		return result;
	}

}
