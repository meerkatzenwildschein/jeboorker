package org.rr.jeborker.metadata;


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

	void setType(T epubLibMetadataEntry) {
		this.epubLibMetadataEntry = epubLibMetadataEntry;
	}
	
	T getType() {
		return this.epubLibMetadataEntry;
	}
	
	@Override
	public boolean isEditable() {
		if(getName().equals("cover")) {
			return false;
		}
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
		return newMetadataProperty;
	}

}
