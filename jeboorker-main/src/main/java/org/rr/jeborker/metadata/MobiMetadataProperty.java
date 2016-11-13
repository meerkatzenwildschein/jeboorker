package org.rr.jeborker.metadata;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.mobi4java.EXTHRecord;
import org.rr.mobi4java.EXTHRecord.RECORD_TYPE;
import org.rr.mobi4java.exth.BinaryRecordDelegate;
import org.rr.mobi4java.exth.DateRecordDelegate;
import org.rr.mobi4java.exth.RecordDelegate;
import org.rr.mobi4java.exth.StringRecordDelegate;

class MobiMetadataProperty extends MetadataProperty {
	
	private RecordDelegate exthRecord;
	
	private String encoding;
	
	MobiMetadataProperty(RecordDelegate record, String name, String encoding) {
		super(name, record);
		this.exthRecord = record;
	}
	
	MobiMetadataProperty(EXTHRecord record, String name, String encoding) {
		super(name, record);
	}
	
	@Override
	public List<Object> getValues() {
		return Collections.<Object>singletonList(getValue());
	}
	
	private Object getValue() {
		if(exthRecord instanceof DateRecordDelegate) {
			try {
				return ((DateRecordDelegate) exthRecord).getAsDate();
			} catch (ParseException e) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to read date value.", e);
				return StringUtil.EMPTY;
			}
		} else if(exthRecord instanceof BinaryRecordDelegate) {
			return ((BinaryRecordDelegate) exthRecord).getAsString();
		}
		
		return new String(exthRecord.getRecord().getData());
	}
	
	public Class<?> getPropertyClass() {
		if(exthRecord instanceof DateRecordDelegate) {
			return Date.class;
		}
		return getValue().getClass();
	}
	
	@Override
	public boolean isVisible() {
		return super.isVisible() && !(exthRecord instanceof BinaryRecordDelegate);
	}

	/**
	 * Creates a new {@link MobiMetadataProperty} instance with the data of this {@link MobiMetadataProperty}.
	 */
	@Override
	public MetadataProperty clone() {
		MobiMetadataProperty newMetadataProperty = new MobiMetadataProperty(exthRecord, getName(), encoding);
		newMetadataProperty.hints = this.hints;
		newMetadataProperty.values = new ArrayList<>(this.values);
		return newMetadataProperty;
	}
	
	public String getOriginCodeName() {
		return StringUtil.toString(exthRecord.getRecord().getRecordType().getType());
	}

	public EXTHRecord getExthRecord() {
		return exthRecord.getRecord();
	}

	public void setValue(final Object value, final int idx) {
		if(exthRecord instanceof DateRecordDelegate) {
			Date d = (value instanceof Date) ? (Date) value : DateConversionUtils.toDate(StringUtil.toString(value));
			((DateRecordDelegate)exthRecord).setDateData(d);
		} else if(exthRecord instanceof StringRecordDelegate) {
			try {
				((StringRecordDelegate)exthRecord).setStringData(StringUtil.toString(value), encoding);
			} catch (UnsupportedEncodingException e) {
				LoggerFactory.getLogger().log(Level.SEVERE, "Failed to set value " + StringUtil.toString(value), e);
			}
		} else {
			exthRecord.getRecord().setData(StringUtil.toString(value).getBytes());
		}
	}

	public void setValues(final List<Object> newValues) {
		setValue(newValues.get(0), 0);
	}
	
	@Override
	public boolean isSingle() {
		if(exthRecord.getRecord().getRecordType() == RECORD_TYPE.AUTHOR) {
			return false;
		} else if(exthRecord.getRecord().getRecordType() == RECORD_TYPE.SUBJECT) {
			return false;
		}
		return super.isSingle();
	}


}
