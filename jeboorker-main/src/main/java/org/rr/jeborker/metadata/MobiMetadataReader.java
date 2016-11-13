package org.rr.jeborker.metadata;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.metadata.mobi.EXTHRecord;
import org.rr.jeborker.metadata.mobi.MobiMeta;
import org.rr.mobi4java.MobiDocument;

public class MobiMetadataReader extends APDFCommonMetadataHandler implements IMetadataReader {

	private IResourceHandler ebookResource;
	
	private String characterEncoding;

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

			MobiMeta mobiMeta = new MobiMeta().readMetaData(ebookResource.toFile());
			characterEncoding = StringUtils.defaultIfBlank(mobiMeta.getCharacterEncoding(), StringUtil.UTF_8);

			String fullName = mobiMeta.getFullName();
			if (StringUtil.isNotBlank(fullName)) {
				result.add(new MetadataProperty("title", fullName));
			}

			List<EXTHRecord> exthRecords = mobiMeta.getEXTHRecords();
			for (EXTHRecord exthRecord : exthRecords) {
				result.add(new MobiMetadataProperty(exthRecord, characterEncoding));
			}
			
			byte[] cover = mobiMeta.getCoverOrThumb();
			if(cover != null) {
				result.add(new MetadataProperty(IMetadataReader.COMMON_METADATA_TYPES.COVER.getName(), cover));
			}
			
			return result;
		} catch (Exception e) {
			LoggerFactory.logWarning(getClass(), "Could not read metadata for pdf " + ebookResource, e);
		}
		return new ArrayList<MetadataProperty>(0);
	}

	@Override
	public List<MetadataProperty> getSupportedMetadata() {
		final ArrayList<MetadataProperty> result = new ArrayList<>();
		result.add(new MetadataProperty("title", EMPTY));
		result.add(new MobiMetadataProperty(100, EMPTY, characterEncoding)); // author
		result.add(new MobiMetadataProperty(101, EMPTY, characterEncoding)); // publisher
		result.add(new MobiMetadataProperty(103, EMPTY, characterEncoding)); // description
		result.add(new MobiMetadataProperty(104, EMPTY, characterEncoding)); // isbn
		result.add(new MobiMetadataProperty(105, EMPTY, characterEncoding)); // subject
		result.add(new MobiMetadataProperty(106, EMPTY, characterEncoding)); // publishingdate
		result.add(new MobiMetadataProperty(107, EMPTY, characterEncoding)); // review
		result.add(new MobiMetadataProperty(108, EMPTY, characterEncoding)); // contributor
		result.add(new MobiMetadataProperty(109, EMPTY, characterEncoding)); // rights
		result.add(new MobiMetadataProperty(112, EMPTY, characterEncoding)); // source
		result.add(new MobiMetadataProperty(113, EMPTY, characterEncoding)); // asin
		result.add(new MobiMetadataProperty(524, EMPTY, characterEncoding)); // language
		result.add(new MobiMetadataProperty(501, EMPTY, characterEncoding)); // cdetype
		return result;
	}

	@Override
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item) {
		item.clearMetadata();
		for (MetadataProperty metadataProperty : metadataProperties) {
			if(metadataProperty instanceof MobiMetadataProperty) {
				if(StringUtil.equalsIgnoreCase(metadataProperty.getName(), "author")) {
					COMMON_METADATA_TYPES.AUTHOR.fillItem(metadataProperty, item);
				} else if(StringUtil.equalsIgnoreCase(metadataProperty.getName(), "publisher")) {
					item.setPublisher(metadataProperty.getValueAsString());
				} else if(StringUtil.equalsIgnoreCase(metadataProperty.getName(), "publishing date")) {
					item.setPublishingDate(DateConversionUtils.toDate(metadataProperty.getValueAsString()));
				} else if(StringUtil.equalsIgnoreCase(metadataProperty.getName(), "subject")) {
					item.setGenre(StringUtil.join(" ", item.getGenre(), metadataProperty.getValueAsString()));
				} else if(StringUtil.equalsIgnoreCase(metadataProperty.getName(), "rights")) {
					item.setRights(metadataProperty.getValueAsString());
				} else if(StringUtil.equalsIgnoreCase(metadataProperty.getName(), "ISBN")) {
					item.setIsbn(metadataProperty.getValueAsString());
				} else if(StringUtil.equalsIgnoreCase(metadataProperty.getName(), "language")) {
					item.setLanguage(metadataProperty.getValueAsString());
				} else if(StringUtil.equalsIgnoreCase(metadataProperty.getName(), "description")) {
					item.setDescription(metadataProperty.getValueAsString());
				}
			} else {
				if(StringUtil.equalsIgnoreCase(metadataProperty.getName(), "title")) {
					item.setTitle(metadataProperty.getValueAsString());
				} else if(StringUtil.equalsIgnoreCase(metadataProperty.getName(), IMetadataReader.COMMON_METADATA_TYPES.COVER.getName())) {
					COMMON_METADATA_TYPES.COVER.fillItem(metadataProperty, item);
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
				result.add(new MobiMetadataProperty(100, EMPTY, characterEncoding)); // author
				break;
			case ISBN:
				result.add(new MobiMetadataProperty(104, EMPTY, characterEncoding)); // isbn
				break;
			case LANGUAGE:
				result.add(new MobiMetadataProperty(524, EMPTY, characterEncoding)); // language
				break;
			case DESCRIPTION:
				result.add(new MobiMetadataProperty(103, EMPTY, characterEncoding)); // description
				break;
			case GENRE:
				result.add(new MobiMetadataProperty(105, EMPTY, characterEncoding)); // subject
				break;
			case COVER:
				result.add(new MetadataProperty(COMMON_METADATA_TYPES.COVER.getName(), null)); // cover
				break;
			default:
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
			case GENRE:
				if (StringUtil.equalsIgnoreCase(prop.getName(), "subject")) {
					result.add(prop);
				}
				break;
			case COVER:
				if(StringUtil.equalsIgnoreCase(prop.getName(), COMMON_METADATA_TYPES.COVER.getName())) {
					result.add(prop);
				}
				break;
			default:
				break;
			}

		}
		return result;
	}

}
