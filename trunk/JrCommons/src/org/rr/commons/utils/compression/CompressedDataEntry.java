package org.rr.commons.utils.compression;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.rr.commons.utils.ReflectionUtils;

/**
 * Just a data holder class.
 */
public class CompressedDataEntry implements Comparable<CompressedDataEntry> {

	public String path;

	public InputStream data;

	public CompressedDataEntry(org.rr.commons.utils.compression.zip.ZipEntry entry) {
		path = entry.getName();
	}

	public CompressedDataEntry(String path, InputStream data) {
		this.path = path;
		this.data = data;
	}

	public CompressedDataEntry(String path, byte[] data) {
		this.path = path;
		if(data != null) {
			this.data = new ByteArrayInputStream(data);
		} else {
			this.data = null;
		}
	}

	@Override
	public int compareTo(CompressedDataEntry o) {
		return o.path.compareTo(path);
	}

	public String toString() {
		return path;
	}

	public byte[] getBytes() {
		if (!ReflectionUtils.getFields(data.getClass(), ReflectionUtils.VISIBILITY_VISIBLE_ALL).isEmpty()) {
			try {
				return (byte[]) ReflectionUtils.getFieldValue(data, "buf", false);
			} catch (Exception e) {
			}
		}

		try {
			return IOUtils.toByteArray(this.data);
		} catch (Exception e) {
			return null;
		}
	}
	
	public InputStream getData() {
		return data;
	}

	public String getName() {
		return new File(path).getName();
	}
}
