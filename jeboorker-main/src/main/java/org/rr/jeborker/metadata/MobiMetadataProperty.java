package org.rr.jeborker.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.NumberUtil;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.metadata.mobi.EXTHRecord;

class MobiMetadataProperty extends MetadataProperty {
	
	private EXTHRecord exthRecord;
	
	MobiMetadataProperty(EXTHRecord exthRecord) {
		super(getRecordName(exthRecord), exthRecord);
		this.exthRecord = exthRecord;
	}
	
	MobiMetadataProperty(int recordType, byte[] data) {
		super(EXTHRecord.getDescriptionForType(recordType), new EXTHRecord(recordType, data));
		exthRecord = (EXTHRecord) super.getValues().get(0);
	}
	
	MobiMetadataProperty(int recordType, String data) {
		this(recordType, data.getBytes());
	}
	
	@Override
	public List<Object> getValues() {
		return Collections.<Object>singletonList(getValue());
	}
	
	private Object getValue() {
		if(EXTHRecord.isDateType(exthRecord.getRecordType())) {
			return DateConversionUtils.toDate(new String(exthRecord.getData()));
		} else if(exthRecord.getRecordType() == 204) {
			// creator software
			return NumberUtil.getUnsignedInt(exthRecord.getData()) & 0xFF;
		} else if(isCreatorNumberType()) {
			// creator number
			return NumberUtil.getUnsignedInt(exthRecord.getData()) & 0xFF;
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
		MobiMetadataProperty newMetadataProperty = new MobiMetadataProperty(this.exthRecord);
		newMetadataProperty.hints = this.hints;
		newMetadataProperty.values = new ArrayList<>(this.values);
		return newMetadataProperty;
	}

	@Override
	public String getName() {
		return exthRecord.getTypeDescription() != null ? exthRecord.getTypeDescription() : "Unknown " + exthRecord.getRecordType();
	}

	public EXTHRecord getExthRecord() {
		return exthRecord;
	}
	
	/**
	 * Sets the value to the desired index.
	 * @param idx The index of the value
	 */
	public void setValue(final Object value, final int idx) {
		if(EXTHRecord.isDateType(exthRecord.getRecordType())) {
			Date d = (value instanceof Date) ? (Date) value : DateConversionUtils.toDate(StringUtil.toString(value));
			String dateString = DateConversionUtils.DATE_FORMATS.W3C_SECOND.getString(d);
			exthRecord.setData(dateString, StringUtil.UTF_8);
		} else if(exthRecord.getRecordType() == 204) {
			// creator software, changing is not supported
		} else if(isCreatorNumberType()) {
			// creator number, changing is not supported
		} else if(exthRecord.getRecordType() == 300) { 
		// font signature, changing is not supported
		} else {
			exthRecord.setData(StringUtil.toString(value), StringUtil.UTF_8);
		}
	}

	/**
	 * Drop all existing values and set this ones from the given List.
	 * @param newValues The new values for this {@link MetadataProperty} instance.
	 */
	public void setValues(final List<Object> newValues) {
		setValue(newValues.get(0), 0);
	}

}
