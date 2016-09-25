package org.rr.jeborker.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.NumberUtil;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.metadata.mobi.EXTHRecord;

class MobiMetadataProperty extends MetadataProperty {
	
	private EXTHRecord exthRecord;
	
	private String encoding;
	
	MobiMetadataProperty(EXTHRecord exthRecord, String encoding) {
		super(getRecordName(exthRecord), exthRecord);
		this.exthRecord = exthRecord;
		this.encoding = encoding;
	}
	
	MobiMetadataProperty(int recordType, byte[] data, String encoding) {
		super(EXTHRecord.getDescriptionForType(recordType), new EXTHRecord(recordType, data));
		exthRecord = (EXTHRecord) super.getValues().get(0);
		this.encoding = encoding;
	}
	
	MobiMetadataProperty(int recordType, String data, String encoding) {
		this(recordType, data.getBytes(), encoding);
	}
	
	@Override
	public List<Object> getValues() {
		return Collections.<Object>singletonList(getValue());
	}
	
	private Object getValue() {
		if(EXTHRecord.isDateType(exthRecord.getRecordType())) {
			return DateConversionUtils.toDate(new String(exthRecord.getData()));
		} else if(isCreatorNumberType() || exthRecord.getRecordType() == 204) {
			return NumberUtil.getUnsignedInt(exthRecord.getData()) & 0xFF; // creator software
		} else if(exthRecord.getRecordType() == 300) { // font signature
			return NumberUtil.bytesToHex(exthRecord.getData());
		}
		
		return new String(exthRecord.getData());
	}

	protected boolean isCreatorNumberType() {
		return exthRecord.getRecordType() == 205 || exthRecord.getRecordType() == 206 || exthRecord.getRecordType() == 207;
	}

	private static String getRecordName(EXTHRecord exthRecord) {
		return exthRecord.getTypeDescription();
	}
	
	public Class<?> getPropertyClass() {
		if(EXTHRecord.isDateType(exthRecord.getRecordType())) {
			return Date.class;
		}
		return getValue().getClass();
	}

	@Override
	public boolean isEditable() {
		switch (exthRecord.getRecordType()) {
			case 100:
			case 101:
			case 102:
			case 103:
			case 104:
			case 105:
			case 106:
			case 107:
			case 108:
			case 109:
			case 204:
			case 205:
			case 206:
			case 207:
			case 112:
			case 113:
			case 501:
			case 524:
				return true;
		}
		return false;
	}

	/**
	 * Creates a new {@link MobiMetadataProperty} instance with the data of this {@link MobiMetadataProperty}.
	 */
	@Override
	public MetadataProperty clone() {
		MobiMetadataProperty newMetadataProperty = new MobiMetadataProperty(exthRecord, encoding);
		newMetadataProperty.hints = this.hints;
		newMetadataProperty.values = new ArrayList<>(this.values);
		return newMetadataProperty;
	}

	@Override
	public String getName() {
		return exthRecord.getTypeDescription() != null ? exthRecord.getTypeDescription() : "Unknown " + exthRecord.getRecordType();
	}
	
	public String getOriginCodeName() {
		return StringUtil.toString(exthRecord.getRecordType());
	}

	public EXTHRecord getExthRecord() {
		return exthRecord;
	}

	public void setValue(final Object value, final int idx) {
		if(EXTHRecord.isDateType(exthRecord.getRecordType())) {
			Date d = (value instanceof Date) ? (Date) value : DateConversionUtils.toDate(StringUtil.toString(value));
			String dateString = DateConversionUtils.DATE_FORMATS.W3C_SECOND.getString(d);
			exthRecord.setData(dateString, encoding);
		} else if(exthRecord.getRecordType() == 204 || isCreatorNumberType()) {
			Long l = (value instanceof Long) ? (Long) value : NumberUtils.toLong(StringUtil.toString(value));
			exthRecord.setData(NumberUtil.toByteArray(l));
		} else if(exthRecord.getRecordType() == 300) { 
		// font signature, changing is not supported
		} else {
			exthRecord.setData(StringUtil.toString(value), encoding);
		}
	}

	public void setValues(final List<Object> newValues) {
		setValue(newValues.get(0), 0);
	}

	@Override
	public String getAdditionalDescription() {
		String key = "MobiMetadataProperty." + exthRecord.getRecordType() + ".description";
		if(Bundle.getBundle().containsKey(key)) {
			String description = Bundle.getBundle().getString("MobiMetadataProperty." + exthRecord.getRecordType() + ".description");
			if (StringUtil.isNotBlank(description)) {
				return description;
			}
		}
		return super.getAdditionalDescription();
	}
	
	@Override
	public boolean isSingle() {
		switch(exthRecord.getRecordType()) {
		case 100: //author
		case 105: //subject, genre
			return false;
		}
		return super.isSingle();
	}


}
