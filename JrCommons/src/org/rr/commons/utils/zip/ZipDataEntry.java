package org.rr.commons.utils.zip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.rr.commons.utils.ReflectionUtils;

/**
 * Just a data holder class.
 */
public class ZipDataEntry implements Comparable<ZipDataEntry> {

	public String path;

	public InputStream data;

	public ZipDataEntry(org.rr.commons.utils.zip.ZipEntry entry) {
		path = entry.getName();
	}

	public ZipDataEntry(String path, InputStream data) {
		this.path = path;
		this.data = data;
	}

	public ZipDataEntry(String path, byte[] data) {
		this.path = path;
		this.data = new ByteArrayInputStream(data);
	}

	@Override
	public int compareTo(ZipDataEntry o) {
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

	public String getName() {
		return new File(path).getName();
	}
}
