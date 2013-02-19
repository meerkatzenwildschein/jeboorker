package org.rr.commons.utils.zip;

public class EmptyZipFileFilter implements ZipFileFilter {
	@Override
	public boolean accept(String entry) {
		return true;
	}
}
