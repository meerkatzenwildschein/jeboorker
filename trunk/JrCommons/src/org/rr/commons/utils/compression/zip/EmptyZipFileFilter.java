package org.rr.commons.utils.compression.zip;

public class EmptyZipFileFilter implements ZipFileFilter {
	@Override
	public boolean accept(String entry) {
		return true;
	}
}
