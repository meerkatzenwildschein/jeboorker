package org.rr.jeborker.metadata;

import java.util.ArrayList;
import java.util.Date;

import org.rr.commons.utils.DateConversionUtils;


class EpubLibMetadataProperty<T> extends MetadataProperty {
	
	private T epubLibMetadataEntry;
	
	private Object value;
	
	EpubLibMetadataProperty(String name, Object value) {
		this(name, value, null);
	}

	EpubLibMetadataProperty(String name, Object value, T epubLibMetadataEntry) {
		super(name, value);
		this.epubLibMetadataEntry = epubLibMetadataEntry;
		this.value = value;
	}
	
	T getType() {
		return this.epubLibMetadataEntry;
	}
	
	Date getValueAsDate() {
		Date date;
		if(!this.getValues().isEmpty() && this.getValues().get(0) instanceof Date) {
			date = (Date) this.getValues().get(0);
		} else {
			date = DateConversionUtils.toDate(this.getValueAsString());
		}
		return date;
	}	
	
	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isDeletable() {
		if(getName().equals("cover")) {
			return false;
		} else if(getName().equals(AEpubMetadataHandler.EPUB_METADATA_TYPES.IDENTIFIER.getName())
				|| getName().equals(AEpubMetadataHandler.EPUB_METADATA_TYPES.UUID.getName())) {
			//epub identifier is mandatory
			return false;
		}
		return true;
	}

	@Override
	public boolean isSingle() {
		if(getName().equals("cover")) {
			return true;
		} else if(getName().equals(AEpubMetadataHandler.EPUB_METADATA_TYPES.IDENTIFIER.getName())
				|| getName().equals(AEpubMetadataHandler.EPUB_METADATA_TYPES.UUID.getName())) {
			return true;
		} 
		return false;
	}	
	
	/**
	 * Creates a new {@link EpubLibMetadataProperty} instance with the data of this {@link EpubLibMetadataProperty}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public MetadataProperty clone() {
		EpubLibMetadataProperty<?> newMetadataProperty = new EpubLibMetadataProperty(this.name, this.value, this.epubLibMetadataEntry);
		newMetadataProperty.hints = this.hints;
		newMetadataProperty.values = new ArrayList<>(this.values);
		return newMetadataProperty;
	}

}
