package org.rr.jeborker.metadata;

class EpubLibMetadataProperty<T> extends MetadataProperty {
	
	private T epubLibMetadataEntry;

	EpubLibMetadataProperty(String name, Object value, T epubLibMetadataEntry) {
		super(name, value);
		this.epubLibMetadataEntry = epubLibMetadataEntry;
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
		} else if(getName().startsWith("identifier")) {
			//epub identifier is mandatory
			return false;
		}
		return true;
	}	

}
