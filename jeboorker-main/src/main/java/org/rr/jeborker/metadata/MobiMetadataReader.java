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
import org.rr.mobi4java.EXTHRecord;
import org.rr.mobi4java.EXTHRecord.RECORD_TYPE;
import org.rr.mobi4java.EXTHRecordFactory;
import org.rr.mobi4java.MobiDocument;
import org.rr.mobi4java.MobiReader;
import org.rr.mobi4java.exth.ASINRecordDelegate;
import org.rr.mobi4java.exth.BinaryRecordDelegate;
import org.rr.mobi4java.exth.DateRecordDelegate;
import org.rr.mobi4java.exth.ISBNRecordDelegate;
import org.rr.mobi4java.exth.LanguageRecordDelegate;
import org.rr.mobi4java.exth.RecordDelegate;
import org.rr.mobi4java.exth.StringRecordDelegate;

public class MobiMetadataReader extends AMetadataHandler implements IMetadataReader {

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
			ArrayList<MetadataProperty> result = new ArrayList<>();
			MobiDocument mobiDoc = new MobiReader().read(ebookResource.getContentInputStream());
			characterEncoding = StringUtils.defaultIfBlank(mobiDoc.getCharacterEncoding(), StringUtil.UTF_8);

			String fullName = mobiDoc.getFullName();
			if (StringUtil.isNotBlank(fullName)) {
				result.add(new MetadataProperty("title", fullName));
			}

			result.addAll(transform(mobiDoc.getMetaData().getAuthorRecords(), COMMON_METADATA_TYPES.AUTHOR.getName()));
			result.addAll(transform(mobiDoc.getMetaData().getPublisherRecords(), "publisher"));
			result.addAll(transform(mobiDoc.getMetaData().getImprintRecords(), "imprint"));
			result.addAll(transform(mobiDoc.getMetaData().getDescriptionRecords(), COMMON_METADATA_TYPES.DESCRIPTION.getName()));
			result.addAll(transform(mobiDoc.getMetaData().getISBNRecords(), COMMON_METADATA_TYPES.ISBN.getName()));
			result.addAll(transform(mobiDoc.getMetaData().getSubjectRecords(), COMMON_METADATA_TYPES.GENRE.getName()));
			result.addAll(transform(mobiDoc.getMetaData().getPublishingDateRecords(), "publishingdate"));
			result.addAll(transform(mobiDoc.getMetaData().getReviewRecords(), "review"));
			result.addAll(transform(mobiDoc.getMetaData().getContributorRecords(), "contributor"));
			result.addAll(transform(mobiDoc.getMetaData().getRightsRecords(), "rights"));
			result.addAll(transform(mobiDoc.getMetaData().getSourceRecords(), "source"));
			result.addAll(transform(mobiDoc.getMetaData().getASINRecords(), "asin"));
			result.addAll(transform(Collections.singletonList(mobiDoc.getMetaData().getLanguageRecord()), COMMON_METADATA_TYPES.LANGUAGE.getName()));
	
			applyAllNonDelegateMetadata(mobiDoc, result);
			
			byte[] cover = mobiDoc.getCover();
			if(cover != null) {
				result.add(new MetadataProperty(COMMON_METADATA_TYPES.COVER.getName(), cover));
			} else {
				List<byte[]> images = mobiDoc.getImages();
				if(images != null && !images.isEmpty()) {
					result.add(new MetadataProperty(COMMON_METADATA_TYPES.COVER.getName(), images.get(0)));
				}
			}
			
			return result;
		} catch (Exception e) {
			LoggerFactory.logWarning(getClass(), "Could not read metadata for pdf " + ebookResource, e);
		}
		return new ArrayList<MetadataProperty>(0);
	}
	
	private void applyAllNonDelegateMetadata(MobiDocument mobiDoc, ArrayList<MetadataProperty> metadataProperties) {
		List<EXTHRecord> exthRecords = mobiDoc.getMetaData().getEXTHRecords();
		for (EXTHRecord exthRecord : exthRecords) {
			boolean found = false;
			for (MetadataProperty metadataProperty : metadataProperties) {
				if(metadataProperty instanceof MobiMetadataProperty && ((MobiMetadataProperty) metadataProperty).getExthRecord() == exthRecord) {
					found = true;
					break;
				}
			}
			if(!found) {
				BinaryRecordDelegate record = new BinaryRecordDelegate(exthRecord);
				metadataProperties.add(new MobiMetadataProperty(record, record.getRecord().getRecordType().name(), characterEncoding));
			}
		}
	}
	
	private List<MobiMetadataProperty> transform(List<? extends RecordDelegate> records, String name) {
		List<MobiMetadataProperty> result = new ArrayList<>();
		for (RecordDelegate record : records) {
			if(record != null) {
				result.add(new MobiMetadataProperty(record, name, characterEncoding));
			}
		}
		return result;
	}
	
	@Override
	public List<MetadataProperty> getSupportedMetadata() {
		final ArrayList<MetadataProperty> result = new ArrayList<>();
		result.add(new MetadataProperty("title", EMPTY));
		
		result.add(new MobiMetadataProperty(new StringRecordDelegate(EXTHRecordFactory.createEXTHRecord(RECORD_TYPE.AUTHOR)), COMMON_METADATA_TYPES.AUTHOR.getName(), characterEncoding));
		result.add(new MobiMetadataProperty(new StringRecordDelegate(EXTHRecordFactory.createEXTHRecord(RECORD_TYPE.PUBLISHER)), "publisher", characterEncoding));
		result.add(new MobiMetadataProperty(new StringRecordDelegate(EXTHRecordFactory.createEXTHRecord(RECORD_TYPE.DESCRIPTION)), COMMON_METADATA_TYPES.DESCRIPTION.getName(), characterEncoding));
		result.add(new MobiMetadataProperty(new ISBNRecordDelegate(EXTHRecordFactory.createEXTHRecord(RECORD_TYPE.ISBN)), COMMON_METADATA_TYPES.ISBN.getName(), characterEncoding));
		result.add(new MobiMetadataProperty(new StringRecordDelegate(EXTHRecordFactory.createEXTHRecord(RECORD_TYPE.SUBJECT)), COMMON_METADATA_TYPES.GENRE.getName(), characterEncoding));
		result.add(new MobiMetadataProperty(new DateRecordDelegate(EXTHRecordFactory.createEXTHRecord(RECORD_TYPE.PUBLISHING_DATE)), "publishingdate", characterEncoding));
		result.add(new MobiMetadataProperty(new StringRecordDelegate(EXTHRecordFactory.createEXTHRecord(RECORD_TYPE.REVIEW)), "review", characterEncoding));
		result.add(new MobiMetadataProperty(new StringRecordDelegate(EXTHRecordFactory.createEXTHRecord(RECORD_TYPE.CONTRIBUTOR)), "contributor", characterEncoding));
		result.add(new MobiMetadataProperty(new StringRecordDelegate(EXTHRecordFactory.createEXTHRecord(RECORD_TYPE.RIGHTS)), "rights", characterEncoding));
		result.add(new MobiMetadataProperty(new StringRecordDelegate(EXTHRecordFactory.createEXTHRecord(RECORD_TYPE.SOURCE)), "source", characterEncoding));
		result.add(new MobiMetadataProperty(new ASINRecordDelegate(EXTHRecordFactory.createEXTHRecord(RECORD_TYPE.ASIN)), "asin", characterEncoding));
		result.add(new MobiMetadataProperty(new LanguageRecordDelegate(EXTHRecordFactory.createEXTHRecord(RECORD_TYPE.LANGUAGE)), COMMON_METADATA_TYPES.LANGUAGE.getName(), characterEncoding));
		return result;
	}

	@Override
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item) {
		item.clearMetadata();
		for (MetadataProperty metadataProperty : metadataProperties) {
			if(metadataProperty instanceof MobiMetadataProperty) {
				if(((MobiMetadataProperty) metadataProperty).getExthRecord().getRecordType() == RECORD_TYPE.AUTHOR) {
					COMMON_METADATA_TYPES.AUTHOR.fillItem(metadataProperty, item);
				} else if(((MobiMetadataProperty) metadataProperty).getExthRecord().getRecordType() == RECORD_TYPE.PUBLISHER) {
					item.setPublisher(metadataProperty.getValueAsString());
				} else if(((MobiMetadataProperty) metadataProperty).getExthRecord().getRecordType() == RECORD_TYPE.PUBLISHING_DATE) {
					item.setPublishingDate(DateConversionUtils.toDate(metadataProperty.getValueAsString()));
				} else if(((MobiMetadataProperty) metadataProperty).getExthRecord().getRecordType() == RECORD_TYPE.SUBJECT) {
					item.setGenre(StringUtil.join(" ", item.getGenre(), metadataProperty.getValueAsString()));
				} else if(((MobiMetadataProperty) metadataProperty).getExthRecord().getRecordType() == RECORD_TYPE.RIGHTS) {
					item.setRights(metadataProperty.getValueAsString());
				} else if(((MobiMetadataProperty) metadataProperty).getExthRecord().getRecordType() == RECORD_TYPE.ISBN) {
					item.setIsbn(metadataProperty.getValueAsString());
				} else if(((MobiMetadataProperty) metadataProperty).getExthRecord().getRecordType() == RECORD_TYPE.LANGUAGE) {
					item.setLanguage(metadataProperty.getValueAsString());
				} else if(((MobiMetadataProperty) metadataProperty).getExthRecord().getRecordType() == RECORD_TYPE.DESCRIPTION) {
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
				result.add(getSupportedMetadataByType(RECORD_TYPE.AUTHOR));
				break;
			case ISBN:
				result.add(getSupportedMetadataByType(RECORD_TYPE.ISBN));
				break;
			case LANGUAGE:
				result.add(getSupportedMetadataByType(RECORD_TYPE.LANGUAGE));
				break;
			case DESCRIPTION:
				result.add(getSupportedMetadataByType(RECORD_TYPE.DESCRIPTION));
				break;
			case GENRE:
				result.add(getSupportedMetadataByType(RECORD_TYPE.SUBJECT));
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
	
	private MetadataProperty getSupportedMetadataByType(RECORD_TYPE type) {
		List<MetadataProperty> supportedMetadata = getSupportedMetadata();
		for (MetadataProperty metadataProperty : supportedMetadata) {
			if(metadataProperty instanceof MobiMetadataProperty && ((MobiMetadataProperty)metadataProperty).getExthRecord().getRecordType() == type) {
				return metadataProperty;
			}
		}
		return null;
	}

	private List<MetadataProperty> getMetadataByType(List<MetadataProperty> metadataProperties, COMMON_METADATA_TYPES type) {
		final ArrayList<MetadataProperty> result = new ArrayList<>();
		for (MetadataProperty metadataProperty : metadataProperties) {
			switch (type) {
			case TITLE:
				if (StringUtil.equalsIgnoreCase(metadataProperty.getName(), "title")) {
					result.add(metadataProperty);
				}
				break;
			case AUTHOR:
				if (metadataProperty instanceof MobiMetadataProperty && ((MobiMetadataProperty) metadataProperty).getExthRecord().getRecordType() == RECORD_TYPE.AUTHOR) {
					result.add(metadataProperty);
				}
				break;
			case ISBN:
				if (metadataProperty instanceof MobiMetadataProperty && ((MobiMetadataProperty) metadataProperty).getExthRecord().getRecordType() == RECORD_TYPE.ISBN) {
					result.add(metadataProperty);
				}
				break;
			case LANGUAGE:
				if (metadataProperty instanceof MobiMetadataProperty && ((MobiMetadataProperty) metadataProperty).getExthRecord().getRecordType() == RECORD_TYPE.LANGUAGE) {
					result.add(metadataProperty);
				}
				break;
			case DESCRIPTION:
				if (metadataProperty instanceof MobiMetadataProperty && ((MobiMetadataProperty) metadataProperty).getExthRecord().getRecordType() == RECORD_TYPE.DESCRIPTION) {
					result.add(metadataProperty);
				}
				break;
			case GENRE:
				if (metadataProperty instanceof MobiMetadataProperty && ((MobiMetadataProperty) metadataProperty).getExthRecord().getRecordType() == RECORD_TYPE.SUBJECT) {
					result.add(metadataProperty);
				}
				break;
			case COVER:
				if(StringUtil.equalsIgnoreCase(metadataProperty.getName(), COMMON_METADATA_TYPES.COVER.getName())) {
					result.add(metadataProperty);
				}
				break;
			default:
				break;
			}

		}
		return result;
	}

}
